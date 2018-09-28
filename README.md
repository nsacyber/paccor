# Platform Credential Creator (paccor)
This program can create platform credentials according to the Platform Certificate Profile v1.0. A platform certificate (PC) is a X.509v2 Attribute Certificate which encapsulates details about components on a host and the security standards met by the platform manufacturer.

This program can assist in gathering all of the data that can go into a PC and produce a signed attribute certificate.

The device component information gathering aspect of the program is platform-dependent.  That aspect has been written and tested for CentOS 7.  Support for additional platforms is coming soon.  This platform dependency is limited to bash scripts.  An advanced user may customize the scripts to their own purpose.  The User Guide contains details regarding which scripts are platform dependent, and the data format expected as their output.

This source code contains our attempts at capturing ASN.1 definitions from TCG Credential specification documents in Java. The idea is to use the BouncyCastle style for parsing/building ASN.1 elements. The coding style is our own twist on what we found in the source code of BouncyCastle's provider library, especially those classes in org.bouncycastle.asn1.x509.

## See the User Guide for more information
[Platform Credential Creator User Guide (PDF)](docs/platformCredentialCreator.pdf)

## Getting started:
gradle clean build buildRpm

## References:
### Platform Attribute Certificate Profile
https://trustedcomputinggroup.org/resource/tcg-platform-attribute-credential-profile/

### The private enterprise numbers list came from:
https://www.ietf.org/assignments/enterprise-numbers/enterprise-numbers



