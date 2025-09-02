package tcg.credential;

import java.util.function.Predicate;
import lombok.NonNull;
import org.bouncycastle.asn1.ASN1String;

/**
 * Static definitions from the Platform Certificate specification(s).
 */
public class Definitions {
	/**
	 * URIMAX is a constant used to provide an upper bound on the length of a URI included in the platform certificate.
	 */
	public static final int URIMAX = 1024;
	/**
	 * STRMAX is a constant defining the upper bound on the length of a string type
	 */
	public static final int STRMAX = 256;
	/**
	 * CONFIGMAX was an upper bound on the platform configuration lists in v1.0.
	 */
	public static final int CONFIGMAX = 32;
	/**
	 * REFMAX was a constant used to provide an upper bound on URI lists.
	 */
	public static final int REFMAX = 32;
	/**
	 * CERTSTRMAX is a constant defining the upper bound on the length of a string containing a PEM-encoded certificate.
	 */
	public static final int CERTSTRMAX = 102400;
	/**
	 * MAX indicates that the upper bound is unspecified.
	 */
	public static final int MAX = 0;

	/**
	 * Returns a predicate that checks if the given ASN1String meets URIMAX length limits.
	 * @return Predicate&lt;ASN1String&gt;
	 */
	public static final Predicate<ASN1String> checkURIMAX() {
		return (o -> testStringLength(o, URIMAX));
	}

	/**
	 * Returns a predicate that checks if the given ASN1String meets STRMAX length limits.
	 * @return Predicate&lt;ASN1String&gt;
	 */
	public static final Predicate<ASN1String> checkSTRMAX() {
		return (o -> testStringLength(o, STRMAX));
	}

	/**
	 * Returns a predicate that checks if the given ASN1String meets CONFIGMAX length limits.
	 * @return Predicate&lt;ASN1String&gt;
	 */
	public static final Predicate<ASN1String> checkCONFIGMAX() {
		return (o -> testStringLength(o, CONFIGMAX));
	}

	/**
	 * Returns a predicate that checks if the given ASN1String meets REFMAX length limits.
	 * @return Predicate&lt;ASN1String&gt;
	 */
	public static final Predicate<ASN1String> checkREFMAX() {
		return (o -> testStringLength(o, REFMAX));
	}

	/**
	 * Returns a predicate that checks if the given ASN1String meets CERTSTRMAX length limits.
	 * @return Predicate&lt;ASN1String&gt;
	 */
	public static final Predicate<ASN1String> checkCERTSTRMAX() {
		return (o -> testStringLength(o, CERTSTRMAX));
	}

	/**
	 * Validates whether the length of the provided {@code ASN1String} is greater than 1
	 * and less than or equal to the specified maximum length.
	 *
	 * @param str the {@code ASN1String} to validate; must not be null
	 * @param max the maximum allowed length for the {@code ASN1String}
	 * @return {@code true} if the length of the string is greater than 1 and less than
	 *         or equal to the specified maximum length; {@code false} otherwise
	 */
	public static final boolean testStringLength(@NonNull ASN1String str, int max) {
		return (str.getString().length() > 1 && str.getString().length() <= max);
	}
}
