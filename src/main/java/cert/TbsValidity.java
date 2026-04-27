package cert;

import java.math.BigInteger;
import java.util.Date;
import lombok.Builder;

@Builder
record TbsValidity(BigInteger serial, Date notBefore, Date notAfter) {
}
