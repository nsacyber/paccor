package paccor.json.schema;

import java.util.List;
import lombok.Getter;

public final class TargetingInformationSchema {
    public static final String TARGETING_INFORMATION = "targetingInformation";
    public static final String TARGET_INFORMATION = "targetInformation";
    public static final String FILE = "file";

    private TargetingInformationSchema() {}

    @Getter
    public enum RootField implements JsonSchemaField {
        TARGETING_INFORMATION_FIELD(TargetingInformationSchema.TARGETING_INFORMATION,
                List.of(TargetingInformationSchema.TARGET_INFORMATION));

        private final String jsonName;
        private final List<String> aliases;

        RootField(String jsonName, List<String> aliases) {
            this.jsonName = jsonName;
            this.aliases = aliases;
        }
    }

    @Getter
    public enum Field implements JsonSchemaField {
        FILE_FIELD(TargetingInformationSchema.FILE);

        private final String jsonName;

        Field(String jsonName) {
            this.jsonName = jsonName;
        }
    }
}
