package tcg.credential;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1BitString;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Enumerated;
import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1IA5String;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Object;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1PrintableString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1String;
import org.bouncycastle.asn1.ASN1TaggedObject;
import org.bouncycastle.asn1.ASN1UTF8String;
import org.bouncycastle.asn1.DERIA5String;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERPrintableString;
import org.bouncycastle.asn1.DERUTF8String;

/**
 * Common methods to transform ASN1 objects.
 */
public class ASN1Utils {
    /**
     * Given a list of ASN1Objects, put all elements into an ASN1 vector.
     * @param list List of ASN1Encodable objects, usually ASN1Objects
     * @return ASN1EncodableVector containing all elements from the list. Empty list if the input list was null.
     * @param <T> Anything that extends or implements ASN1Encodable
     * @param <U> List of T
     */
    public static final <T extends ASN1Object, U extends Collection<T>> ASN1EncodableVector toASN1EncodableVector(U list) {
        ASN1EncodableVector vec = new ASN1EncodableVector();

        if (list == null) {
            return vec;
        }

        list.stream()
                .map(obj -> (ASN1Object)obj)
                .forEach(vec::add);
        return vec;
    }

    /**
     * Pull out all ASN1TaggedObjects from the sequence and put them into a map where
     * the keys are tag numbers of ASN1TaggedObjects
     * and the values are the ASN1Object they reference.
     * @param seq ASN1Sequence of any size
     * @return Map of the tagged objects indexed by tag number. Empty map if no tagged objects are found.
     */
    public static final Map<Integer, ASN1TaggedObject> parseTaggedElements(ASN1Sequence seq) {
        Map<Integer, ASN1TaggedObject> map = new HashMap<>();

        if (seq == null) {
            return map;
        }

        Arrays.stream(seq.toArray())
                .map(obj -> (ASN1Object)obj)
                .map(opt -> safeGetDefaultElement(opt, null, ASN1TaggedObject::getInstance))
                .filter(Objects::nonNull)
                .sorted(Comparator.comparingInt(ASN1TaggedObject::getTagNo))
                .forEach(taggedElement -> map.put(taggedElement.getTagNo(), taggedElement));

        map.entrySet().removeIf(entry -> Objects.isNull(entry.getValue()));

        return map;
    }

    /**
     * List all untagged elements from the sequence in order. Null elements are not included in the list.
     * @param seq ASN1Sequence
     * @return List of ASN1Objects that are not ASN1TaggedObjects in order
     */
    public static final List<ASN1Object> listUntaggedElements(ASN1Sequence seq) {
        List<ASN1Object> list = new ArrayList<>();

        if (seq == null) {
            return list;
        }

        Arrays.stream(seq.toArray())
                .filter(obj -> obj instanceof ASN1Object)
                .map(obj -> (ASN1Object)obj)
                .map(obj -> safeGetDefaultElement(obj, obj, ASN1TaggedObject::getInstance))
                .filter(obj -> !(obj instanceof ASN1TaggedObject))
                .forEach(list::add);

        return list;
    }

    /**
     * Central method to apply the appropriate getInstance method if the object is a tagged object.
     * If the object is not a tagged object, the standard method will be used.
     *
     * @param <T>     The return type
     * @param obj     The object to transform
     * @param decoder The method to transform the object found at the index within the sequence
     */
    private static <T> T tagAware(Object obj,
                                  T defaultAnswer,
                                  @NonNull Function<Object, T> decoder) {
        if (obj == null) {
            return defaultAnswer;
        }
        try {
            return decoder.apply(obj);
        } catch (Exception ignored) {}

        return defaultAnswer;
    }

    /**
     * Attempt to transform the ASN1Object at the requested position within the ASN1Sequence into T using the method supplied.
     *
     * @param <T>            The return type
     * @param seq            If null, the default answer will be returned
     * @param zeroBasedIndex If this is out of bounds of the seq, the default answer will be returned
     * @param defaultAnswer  Can be null if the default answer is null
     * @param decoder        The method to transform the object found at the index within the sequence
     * @return The transformed object
     */
    public static final <T> T safeGetDefaultElementFromSequence(ASN1Sequence seq, int zeroBasedIndex, T defaultAnswer, @NonNull Function<Object, T> decoder) {
        T result = defaultAnswer;
        if (seq != null && seq.size() > zeroBasedIndex && 0 < seq.size() && seq.getObjectAt(zeroBasedIndex) instanceof ASN1Object obj) {
            result = tagAware(obj, defaultAnswer, decoder);
        }
        return result;
    }

    /**
     * Attempt to transform the ASN1Object into T using the method supplied.
     *
     * @param <T>           The return type
     * @param obj           ASN1Object
     * @param defaultAnswer Can be null if the default answer is null
     * @param decoder       The method to transform the object found at the index within the sequence
     * @return The transformed object
     */
    public static final <T> T safeGetDefaultElement(ASN1Object obj, T defaultAnswer, @NonNull Function<Object, T> decoder) {
        return tagAware(obj, defaultAnswer, decoder);
    }

    /**
     * Look for the first element, within the supplied range of the ASN1Sequence, that can be transformed into T using the method supplied.
     * If no qualified elements are found, the default answer will be returned.
     *
     * @param <T>                 The return type
     * @param seq                 If null, the default answer will be returned
     * @param zeroBasedStartIndex If this is out of bounds of the seq, the default answer will be returned
     * @param zeroBasedMaxIndex   If this is out of bounds of the seq, the default answer will be returned
     * @param defaultAnswer       Can be null if the default answer is null
     * @param decoder             The method to transform the object found at the index within the sequence
     * @return The transformed object
     */
    public static final <T> T safeGetFirstInstanceFromSequenceGivenRange(ASN1Sequence seq, int zeroBasedStartIndex, int zeroBasedMaxIndex, T defaultAnswer, @NonNull Function<Object, T> decoder) {
        T result = defaultAnswer;
        if (seq == null) return result;


        for (int i = zeroBasedStartIndex; i <= zeroBasedMaxIndex && i < seq.size(); i++) {
            T temp = safeGetDefaultElementFromSequence(seq, i, null, decoder);

            if (temp != null) {
                result = temp;
                break;
            }
        }

        return result;
    }

    /**
     * Resize the given ASN1OctetString to the given sequenceSize.
     * If the value already meets the requested sequenceSize, or if the value is null, the value will pass through.
     * If sequenceSize is smaller than value size, octets will be truncated from MSB.
     * If sequenceSize is larger than value size, zero bytes will be prepended.
     * @param sequenceSize New size
     * @param value object to resize
     * @return ASN1OctetString of requested size.
     */
    public static ASN1OctetString resizeOctets(int sequenceSize, ASN1OctetString value) {
        return new DEROctetString(resizeOctets(sequenceSize, value == null ? null : value.getOctets()));
    }

    /**
     * Resize the given byte array to the given sequenceSize.
     * If the value already meets the requested sequenceSize, or if the value is null, the value will pass through.
     * If sequenceSize is smaller than value size, octets will be truncated from MSB.
     * If sequenceSize is larger than value size, zero bytes will be prepended.
     * @param sequenceSize New size
     * @param value object to resize
     * @return byte array of the requested size.
     */
    public static byte[] resizeOctets(int sequenceSize, byte[] value) {
        // Example: Component class value is 4 bytes long
        byte[] newValue = new byte[sequenceSize];
        Arrays.fill(newValue, (byte)0);

        if (value != null && value.length > 0) {
            // If value is <= 4 octets, prepend with zeroes
            // If value is > 4 octets, save last 4 octets
            System.arraycopy(value, Math.max(0, value.length - sequenceSize), newValue, Math.max(sequenceSize - value.length, 0), Math.min(value.length, sequenceSize));
        }
        return newValue;
    }

    /**
     * Convert the given hex string into a byte array, then resize it to the given sequenceSize.
     * If the value already meets the requested sequenceSize, the value will pass through.
     * If sequenceSize is smaller than value size, octets will be truncated from MSB.
     * If sequenceSize is larger than value size, zero bytes will be prepended.
     * @param sequenceSize New size
     * @param valueHex hex data to read
     * @return ASN1OctetString of requested size.
     */
    public static ASN1OctetString resizeOctets(int sequenceSize, @NonNull String valueHex) {
        BigInteger value = new BigInteger(valueHex, 16);
        return new DEROctetString(resizeOctets(sequenceSize, value.toByteArray()));
    }

    /**
     * Get an ASN1Sequence from the given object.
     * @param o Object to convert
     * @return ASN1Sequence instance
     * @throws IllegalArgumentException if the object cannot be converted
     */
    public static ASN1Sequence getSequence(Object o) {
        return (o instanceof ASN1TaggedObject t)
                ? ASN1Sequence.getInstance(t, t.isExplicit())
                : ASN1Sequence.getInstance(o);
    }

    /**
     * Test if the object is an ASN1Sequence
     * @param o Object to convert
     * @return True if the object is an ASN1Sequence. Otherwise, false.
     */
    public static boolean isSequence(Object o) {
        try {
            getSequence(o);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Get a string or default if null.
     * @param str ASN1String to check
     * @param def Default string
     * @return String value or default
     */
    public static final String getStringOrDefault(ASN1String str, String def) {
        return str != null ? str.getString() : def;
    }

    /**
     * Get a string from the given object.
     * @param o Object to convert
     * @return instance
     * @throws IllegalArgumentException if the object cannot be converted
     */
    public static ASN1UTF8String getUTF8String(Object o) {
        return (o instanceof ASN1TaggedObject t)
                ? ASN1UTF8String.getInstance(t, t.isExplicit())
                : (o instanceof String s)
                    ? new DERUTF8String(s)
                    : ASN1UTF8String.getInstance(o);
    }

    /**
     * Get a string from the given object.
     * @param o Object to convert
     * @return instance
     * @throws IllegalArgumentException if the object cannot be converted
     */
    public static ASN1IA5String getIA5String(Object o) {
        return (o instanceof ASN1TaggedObject t)
                ? ASN1IA5String.getInstance(t, t.isExplicit())
                : (o instanceof String s)
                    ? new DERIA5String(s)
                    : ASN1IA5String.getInstance(o);
    }

    /**
     * Get a string from the given object.
     * @param o Object to convert
     * @return instance
     * @throws IllegalArgumentException if the object cannot be converted
     */
    public static ASN1PrintableString getPrintableString(Object o) {
        return (o instanceof ASN1TaggedObject t)
                ? ASN1PrintableString.getInstance(t, t.isExplicit())
                : (o instanceof String s)
                    ? new DERPrintableString(s)
                    : ASN1PrintableString.getInstance(o);
    }

    /**
     * Get a date object from the given object.
     * @param o Object to convert
     * @return instance
     * @throws IllegalArgumentException if the object cannot be converted
     */
    public static ASN1GeneralizedTime getGeneralizedTime(Object o) {
        return (o instanceof ASN1TaggedObject t)
                ? ASN1GeneralizedTime.getInstance(t, t.isExplicit())
                : ASN1GeneralizedTime.getInstance(o);
    }

    /**
     * Get an OID from the given object.
     * @param o Object to convert
     * @return instance
     * @throws IllegalArgumentException if the object cannot be converted
     */
    public static ASN1ObjectIdentifier getOID(Object o) {
        return (o instanceof ASN1TaggedObject t)
                ? ASN1ObjectIdentifier.getInstance(t, t.isExplicit())
                : (o instanceof String s)
                    ? new ASN1ObjectIdentifier(s)
                    : ASN1ObjectIdentifier.getInstance(o);
    }

    /**
     * Get an ASN1Boolean from the primitive. Complements getBoolean(Object).
     * @param b primitive boolean
     * @return instance
     */
    public static ASN1Boolean getBoolean(boolean b) {
        return ASN1Boolean.getInstance(b);
    }

    /**
     * Get an ASN1Boolean from the given object.
     * @param o Object to convert
     * @return instance
     * @throws IllegalArgumentException if the object cannot be converted
     */
    public static ASN1Boolean getBoolean(Object o) {
        return (o instanceof ASN1TaggedObject t)
                ? ASN1Boolean.getInstance(t, t.isExplicit())
                : ASN1Boolean.getInstance(o);
    }

    /**
     * Get an ASN1BitString from the given object.
     * @param o Object to convert
     * @return instance
     * @throws IllegalArgumentException if the object cannot be converted
     */
    public static ASN1BitString getBitString(Object o) {
        return (o instanceof ASN1TaggedObject t)
                ? ASN1BitString.getInstance(t, t.isExplicit())
                : ASN1BitString.getInstance(o);
    }

    /**
     * Get an ASN1Enumerated from the given object.
     * @param o Object to convert
     * @return instance
     * @throws IllegalArgumentException if the object cannot be converted
     */
    public static ASN1Enumerated getEnumerated(Object o) {
        return (o instanceof ASN1TaggedObject t)
                ? ASN1Enumerated.getInstance(t, t.isExplicit())
                : ASN1Enumerated.getInstance(o);
    }

    /**
     * Get an ASN1Integer from the given object.
     * @param o Object to convert
     * @return instance
     * @throws IllegalArgumentException if the object cannot be converted
     */
    public static ASN1Integer getInteger(Object o) {
        return (o instanceof ASN1TaggedObject t)
                ? ASN1Integer.getInstance(t, t.isExplicit())
                : (o instanceof Integer i)
                  ? new ASN1Integer(i)
                  : ASN1Integer.getInstance(o);
    }

    /**
     * Get an octet string from the given object.
     * @param o Object to convert
     * @return instance
     * @throws IllegalArgumentException if the object cannot be converted
     */
    public static ASN1OctetString getOctetString(Object o) {
        return (o instanceof ASN1TaggedObject t)
                ? ASN1OctetString.getInstance(t, t.isExplicit())
                : ASN1OctetString.getInstance(o);
    }
}
