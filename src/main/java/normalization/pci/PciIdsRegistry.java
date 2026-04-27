package normalization.pci;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import normalization.HexNormalizer;

/**
 * Minimal parser for the pci.ids database. Supports vendor, device, and subsystem name lookups.
 * Parsing is intentionally lightweight and only builds maps needed for vendor/device/subsystem canonicalization.
 */
public final class PciIdsRegistry {
    private static volatile PciIdsRegistry INSTANCE;

    private final Map<String,String> vendorIdByNameKey = new HashMap<>();
    private final Map<String,String> vendorNameById = new HashMap<>();
    // deviceIdByVendorIdAndNameKey: key = vendorId + "\u0000" + nameKey
    private final Map<String,String> deviceIdByVendorAndNameKey = new HashMap<>();
    // deviceNameById: key = vendorId + "\u0000" + deviceId
    private final Map<String,String> deviceNameById = new HashMap<>();
    // subsystemNameById: key = vendorId + "\u0000" + deviceId + "\u0000" + subsysVendorId + "\u0000" + subsysDeviceId
    private final Map<String,String> subsystemNameById = new HashMap<>();

    private PciIdsRegistry() {}

    private static final class ParseState {
        private String currentVendorId;
        private String currentDeviceId;
        private boolean inClasses;
    }

    public static PciIdsRegistry get() {
        PciIdsRegistry inst = INSTANCE;
        if (inst == null) {
            synchronized (PciIdsRegistry.class) {
                inst = INSTANCE;
                if (inst == null) INSTANCE = inst = loadDefault();
            }
        }
        return inst;
    }

    public static void installForTests(PciIdsRegistry custom) {
        INSTANCE = custom;
    }

    private static PciIdsRegistry loadDefault() {
        String path = System.getProperty("PCI_IDS_PATH");
        try {
            if (path != null) {
                try (InputStream in = new FileInputStream(path)) {
                    return parse(in);
                }
            }
            // Try classpath resource
            InputStream in = PciIdsRegistry.class.getClassLoader().getResourceAsStream("pci/pci.ids");
            if (in != null) {
                try (in) { return parse(in); }
            }
        } catch (IOException ignored) {}
        // Fallback empty registry
        return new PciIdsRegistry();
    }

    public static PciIdsRegistry parse(InputStream in) throws IOException {
        PciIdsRegistry db = new PciIdsRegistry();
        ParseState state = new ParseState();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (shouldSkip(line, state)) {
                    continue;
                }
                parseEntry(db, state, line);
            }
        }
        return db;
    }

    private static boolean shouldSkip(String line, ParseState state) {
        if (line.isEmpty() || line.startsWith("#")) {
            return true;
        }
        if (!line.startsWith("\t") && line.startsWith("C ")) {
            state.inClasses = true;
            return true;
        }
        return state.inClasses;
    }

    private static void parseEntry(PciIdsRegistry db, ParseState state, String line) {
        if (line.charAt(0) != '\t') {
            parseVendorLine(db, state, line);
            return;
        }
        if (line.startsWith("\t\t")) {
            parseSubsystemLine(db, state, line);
            return;
        }
        parseDeviceLine(db, state, line);
    }

    private static void parseVendorLine(PciIdsRegistry db, ParseState state, String line) {
        String id = takeHexPrefix(line.trim());
        if (id == null || id.length() != 4) {
            state.currentVendorId = null;
            state.currentDeviceId = null;
            return;
        }

        state.currentVendorId = id.toLowerCase(Locale.ROOT);
        state.currentDeviceId = null;
        String name = nameAfterPrefix(line, id);
        String key = normalizeNameKey(name);
        if (!key.isEmpty()) {
            db.vendorIdByNameKey.putIfAbsent(key, state.currentVendorId);
        }
        db.vendorNameById.put(state.currentVendorId, name);
    }

    private static void parseSubsystemLine(PciIdsRegistry db, ParseState state, String line) {
        if (state.currentVendorId == null || state.currentDeviceId == null) {
            return;
        }
        String[] parts = line.trim().split("\\s+", 3);
        if (parts.length < 2) {
            return;
        }

        String subsysVendorId = parts[0].toLowerCase(Locale.ROOT);
        String subsysDeviceId = parts[1].toLowerCase(Locale.ROOT);
        String subsysName = parts.length > 2 ? parts[2] : "";
        String subsysKey = subsystemKey(
                state.currentVendorId,
                state.currentDeviceId,
                subsysVendorId,
                subsysDeviceId);
        db.subsystemNameById.put(subsysKey, subsysName);
    }

    private static void parseDeviceLine(PciIdsRegistry db, ParseState state, String line) {
        if (state.currentVendorId == null) {
            return;
        }
        String trimmed = line.trim();
        String devId = takeHexPrefix(trimmed);
        if (devId == null || devId.length() != 4) {
            state.currentDeviceId = null;
            return;
        }

        state.currentDeviceId = devId.toLowerCase(Locale.ROOT);
        String name = nameAfterPrefix(trimmed, devId);
        String key = key(state.currentVendorId, normalizeNameKey(name));
        db.deviceIdByVendorAndNameKey.putIfAbsent(key, state.currentDeviceId);
        db.deviceNameById.put(key(state.currentVendorId, state.currentDeviceId), name);
    }

    private static String nameAfterPrefix(String line, String prefix) {
        return line.substring(line.indexOf(prefix) + prefix.length()).trim();
    }

    private static String takeHexPrefix(String s) {
        int i = 0;
        while (i < s.length() && isHexChar(s.charAt(i))) i++;
        if (i == 0) return null;
        return s.substring(0, i);
    }

    private static boolean isHexChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
    }

    public static String normalizeNameKey(String s) {
        String t = s == null ? "" : s.toLowerCase(Locale.ROOT).trim();
        t = t.replaceAll("[^a-z0-9]+", "");
        return t;
    }

    private static String key(String vendorId, String nameKey) { return vendorId + "\u0000" + nameKey; }

    private static String subsystemKey(String vendorId, String deviceId, String subsysVendorId, String subsysDeviceId) {
        return vendorId + "\u0000" + deviceId + "\u0000" + subsysVendorId + "\u0000" + subsysDeviceId;
    }

    public Optional<String> vendorIdFromName(String name) {
        String key = normalizeNameKey(name);
        String id = vendorIdByNameKey.get(key);
        return Optional.ofNullable(id);
    }

    public Optional<String> canonicalVendorId(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        if (HexNormalizer.isHexString(token)) {
            return Optional.of(HexNormalizer.normalize(token, 2));
        }
        return vendorIdFromName(token).map(id -> HexNormalizer.normalize(id, 2));
    }

    public Optional<String> deviceIdFromVendorAndName(String vendorId, String name) {
        if (vendorId == null) return Optional.empty();
        String id = deviceIdByVendorAndNameKey.get(key(vendorId.toLowerCase(Locale.ROOT), normalizeNameKey(name)));
        return Optional.ofNullable(id);
    }

    public Optional<String> vendorName(String id) {
        return Optional.ofNullable(vendorNameById.get(id == null ? null : id.toLowerCase(Locale.ROOT)));
    }

    public Optional<String> deviceName(String vendorId, String deviceId) {
        if (vendorId == null || deviceId == null) return Optional.empty();
        String deviceKey = key(vendorId.toLowerCase(Locale.ROOT), deviceId.toLowerCase(Locale.ROOT));
        return Optional.ofNullable(deviceNameById.get(deviceKey));
    }

    public Optional<String> subsystemName(String vendorId, String deviceId, String subsysVendorId, String subsysDeviceId) {
        if (vendorId == null || deviceId == null || subsysVendorId == null || subsysDeviceId == null) {
            return Optional.empty();
        }
        String subsysKey = subsystemKey(
            vendorId.toLowerCase(Locale.ROOT),
            deviceId.toLowerCase(Locale.ROOT),
            subsysVendorId.toLowerCase(Locale.ROOT),
            subsysDeviceId.toLowerCase(Locale.ROOT)
        );
        return Optional.ofNullable(subsystemNameById.get(subsysKey));
    }
}
