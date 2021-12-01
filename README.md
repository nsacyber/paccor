# Platform Attribute Certificate Creator (paccor)
This program can create platform credentials according to the Platform Certificate Profile v1.0. A platform certificate (PC) is a X.509v2 Attribute Certificate which encapsulates details about components on a host and the security standards met by the platform manufacturer.

This program can assist in gathering all of the data that can go into a PC and produce a signed attribute certificate.

The device component information gathering aspect of the program is platform-dependent.  That aspect has been written and tested for CentOS 7.  Support for additional platforms is coming soon.  This platform dependency is limited to bash scripts.  An advanced user may customize the scripts to their own purpose.  The User Guide contains details regarding which scripts are platform dependent, and the data format expected as their output.

This source code contains our attempts at capturing ASN.1 definitions from TCG Credential specification documents in Java. The idea is to use the BouncyCastle style for parsing/building ASN.1 elements. The coding style is our own twist on what we found in the source code of BouncyCastle's provider library, especially those classes in org.bouncycastle.asn1.x509.

## Getting started:
Head to the [Releases](https://github.com/nsacyber/paccor/releases) page and download the package relevant to your OS to begin using paccor.

To build the project yourself, paccor uses Gradle to manage build tasks. paccor has been tested with ```Gradle 4.5.1```. Later versions of Gradle may also work.
* If you already have gradle installed on your system, you can run:<br/> 
```gradle clean build```
  * The following arguments can be added to build the relevant package:<br/>
  ```gradle clean build buildRpm buildDeb distZip```
* If you don't have gradle installed, use the included gradle wrapper.<br/>
```gradlew clean build buildRpm buildDeb distZip```
  * On Windows use:<br/>
  ```gradlew.bat clean build buildRpm buildDeb distZip```
  * Validation of the gradle wrapper jar file is performed by a GitHub Action maintained by Gradle:<br/>
  https://github.com/marketplace/actions/gradle-wrapper-validation<br/>
  Instructions to verify the Gradle Wrapper JAR locally are available [here](https://docs.gradle.org/current/userguide/gradle_wrapper.html#wrapper_checksum_verification).

## Minimum software requirements:
* Gradle 4.5.1
* Java 1.8.0

## See the User Guide for more information
[Platform Credential Creator User Guide (PDF)](docs/platformCertificateCreator.pdf)

## References:
### Platform Attribute Certificate Profile
https://trustedcomputinggroup.org/resource/tcg-platform-attribute-credential-profile/

### The private enterprise numbers list came from:
http://www.iana.org/assignments/enterprise-numbers



