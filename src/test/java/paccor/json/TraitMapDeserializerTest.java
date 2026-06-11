package paccor.json;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import paccor.tcg.credential.ASN1ObjectTrait;
import paccor.tcg.credential.AttributeStatus;
import paccor.tcg.credential.BooleanTrait;
import paccor.tcg.credential.CertificateIdentifierTrait;
import paccor.tcg.credential.CommonCriteriaTrait;
import paccor.tcg.credential.ComponentClassTrait;
import paccor.tcg.credential.ComponentIdentifierV11Trait;
import paccor.tcg.credential.CountryOfOriginTrait;
import paccor.tcg.credential.EntityGeoLocationTrait;
import paccor.tcg.credential.EnumWithIntegerValue;
import paccor.tcg.credential.FIPSLevelTrait;
import paccor.tcg.credential.IA5StringTrait;
import paccor.tcg.credential.ISO9000Trait;
import paccor.tcg.credential.NetworkMACTrait;
import paccor.tcg.credential.OIDTrait;
import paccor.tcg.credential.PEMCertStringTrait;
import paccor.tcg.credential.PENTrait;
import paccor.tcg.credential.PlatformFirmwareCapabilitiesTrait;
import paccor.tcg.credential.PlatformFirmwareSignatureVerificationTrait;
import paccor.tcg.credential.PlatformFirmwareUpdateComplianceTrait;
import paccor.tcg.credential.PlatformHardwareCapabilitiesTrait;
import paccor.tcg.credential.PublicKeyTrait;
import paccor.tcg.credential.RTMTrait;
import paccor.tcg.credential.RTMTypes;
import paccor.tcg.credential.StatusTrait;
import paccor.tcg.credential.TraitId;
import paccor.tcg.credential.TraitMap;
import paccor.tcg.credential.URITrait;
import paccor.tcg.credential.UTF8StringTrait;
import tools.jackson.databind.ObjectMapper;

public class TraitMapDeserializerTest {
    public static final String asn1Json_1 = """
                {
                  "traitId": "1.2.3.4",
                  "traitCategory": "1.2.7.8",
                  "traitRegistry": "1.2.11.12",
                  "description": "",
                  "descriptionURI": "",
                  "asn1": "AgEB"
                }
    """;
    public static final String asn1Json_2 = """
                {
                  "traitId": "1.2.15.16",
                  "traitCategory": "1.2.19.20",
                  "traitRegistry": "1.2.23.24",
                  "description": "",
                  "descriptionURI": "",
                  "asn1": "0x020101"
                }
    """;
    public static final String asn1JsonCsv = String.join(",", asn1Json_1, asn1Json_2);
    public static final String asn1Json = "[" + asn1JsonCsv + "]";
    public static final String boolJson_1 = """
                {
                  "traitId": "",
                  "traitCategory": "",
                  "traitRegistry": "",
                  "description": "",
                  "descriptionURI": "",
                  "booleanValue": false
                }
    """;
    public static final String boolJson_2 = """
                {
                  "bool": false
                }
    """;
    public static final String boolJsonCsv = String.join(",", boolJson_1, boolJson_2);
    public static final String boolJson = "[" + boolJsonCsv + "]";

    public static final String certificateIdentifierJson_1 = """
            {
                  "traitCategory": "2.23.133.19.2.15",
                  "description": "Certificate Identifier 1",
                  "certificateIdentifier": {
                    "hashedCertIdentifier": {
                      "hashAlgorithm": {
                        "algorithm": "2.16.840.1.101.3.4.2.1"
                      },
                      "hashOverSignatureValue": "6S5k++IQMc/GcbCpFKTeaQCmFOO+61AZMUgqPUchDFs="
                    },
                    "genericCertIdentifier": {
                      "issuer": "CN=Issuer A",
                      "serial": 41465
                    }
                  }
                }
            """;
    public static final String certificateIdentifierJsonCsv = String.join(",", certificateIdentifierJson_1);
    public static final String certificateIdentifierJson = "[" + certificateIdentifierJsonCsv + "]";

    public static final String commonCriteriaJson_1 = """
                {
                  "commonCriteria": {
                    "commonCriteriaMeasures": {
                      "version": "2",
                      "assuranceLevel": "EVALUATIONASSURANCELEVEL_LEVEL3",
                      "evaluationStatus": "EVALUATIONSTATUS_DESIGNEDTOMEET",
                      "plus": false,
                      "strengthOfFunction": "STRENGTHOFFUNCTION_BASIC"
                    },
                    "cCCertificateNumber": "text1",
                    "cCCertificateAuthority": "text2",
                    "evaluationScheme": "text3",
                    "cCCertificateIssuanceDate": "20200716",
                    "cCCertificateExpiryDate": "20880101"
                  }
                }
            """;
    public static final String commonCriteriaJsonCsv = String.join(",", commonCriteriaJson_1);
    public static final String commonCriteriaJson = "[" + commonCriteriaJsonCsv + "]";

    public static final String componentClassJson_1 = """
                {
                  "componentClassValue": [0,0,0,1]
                }
            """;
    public static final String componentClassJson_2 = """
                {
                  "traitId": "2.23.133.19.1.4",
                  "traitValue": "AAAAAQ=="
                }
            """;
    public static final String componentClassJson_3 = """
                {
                  "traitCategory": "2.23.133.19.2.7",
                  "traitValue": "AAABAA=="
                }
            """;
    public static final String componentClassJsonCsv = String.join(",", componentClassJson_1, componentClassJson_2, componentClassJson_3);
    public static final String componentClassJson = "[" + componentClassJsonCsv + "]";

    public static final String componentIdentifierV11Json_1 = """
                {
                  "componentIdentifierV11": {
                     "COMPONENTCLASS": {
                         "COMPONENTCLASSREGISTRY": "2.23.133.18.3.1",
                         "COMPONENTCLASSVALUE": "00020002"
                     },
                     "MANUFACTURER": "Sample Chassis Manufacturer",
                     "MODEL": "Sample Chassis Model",
                     "SERIAL": "Sample Chassis Serial Number",
                     "REVISION": "Sample Chassis Revision",
                     "MANUFACTURERID": "1.3.6.1.4.1.32473",
                     "FIELDREPLACEABLE": "true",
                     "PLATFORMCERT": {
                         "ATTRIBUTECERTIDENTIFIER": {
                             "HASHALGORITHM": "1.3.6.1.4.1.22554.1.2.1",
                             "HASH": "NjAwM0EzMzQzMkZEOTE0QjYwMDNBMzM0MzJGRDkxNEI2MDAzQTMzNDMyRkQ5MTRCNjAwM0EzMzQzMkZEOTE0Qg=="
                         },
                         "GENERICCERTIDENTIFIER": {
                             "ISSUER": "C=US, ST=FL, L=Sample City 1, O=Sample Corporation 1, OU=Platform Certificate Issuer, CN=www.example.com",
                             "SERIAL": "00001"
                         }
                     },
                     "PLATFORMCERTURI": {
                         "UNIFORMRESOURCEIDENTIFIER": "https://www.example.com/certs/00000.cer"
                     }
                  }
                }
            """;
    public static final String componentIdentifierV11JsonCsv = String.join(",", componentIdentifierV11Json_1);
    public static final String componentIdentifierV11Json = "[" + componentIdentifierV11JsonCsv + "]";

    public static final String fipsJson_1 = """
                {
                  "fipsLevel": {
                    "version": "2",
                    "level": "LEVEL3"
                  }
                }
            """;
    public static final String fipsJsonCsv = String.join(",", fipsJson_1);
    public static final String fipsJson = "[" + fipsJsonCsv + "]";

    public static final String iso9000Json_1 = """
                {
                  "iso9000": {
                    "iso9000Certified": false,
                    "iso9000Uri": "quick string"
                  }
                }
            """;
    public static final String iso9000JsonCsv = String.join(",", iso9000Json_1);
    public static final String iso9000Json = "[" + iso9000JsonCsv + "]";

    public static final String networkMacJson_1 = """
                {
                  "networkMAC": {
                    "addressType": "2.23.133.17.2",
                    "addressValue": "123456789056"
                  }
                }
            """;

    public static final String networkMacJson_2 = """
                {
                  "networkMAC": {
                    "blueToothMac": "88:33:22:11:44:55"
                  }
                }
            """;

    public static final String networkMacJson_3 = """
                {
                  "networkMAC": {
                    "Ethernetmac": "AB-CD-EF-12-23-45"
                  }
                }
            """;
    public static final String networkMacJsonCsv = String.join(",", networkMacJson_1, networkMacJson_2, networkMacJson_3);
    public static final String networkMacJson = "[" + networkMacJsonCsv + "]";

    public static final String oidJson_1 = """
                {
                  "oid": "1.2.5.9.4564"
                }
            """;
    public static final String oidJsonCsv = String.join(",", oidJson_1);
    public static final String oidJson = "[" + oidJsonCsv + "]";

    public static final String penJson_1 = """
                {
                  "traitId": "2.23.133.19.1.10",
                  "traitValue": "1.2.8.346.84846"
                }
            """;
    public static final String penJsonCsv = String.join(",", penJson_1);
    public static final String penJson = "[" + penJsonCsv + "]";

    public static final String platformFirmwareCapabilitiesJson_1 = """
            {
                  "platformFirmwareCapabilities": {
                    "values": [
                      "PLATFORMFIRMWARECAPABILITIES_FWSETUPAUTHLOCAL"
                    ]
                  }
                }
            """;
    public static final String platformFirmwareCapabilitiesJsonCsv = String.join(",", platformFirmwareCapabilitiesJson_1);
    public static final String platformFirmwareCapabilitiesJson = "[" + platformFirmwareCapabilitiesJsonCsv + "]";

    public static final String platformFirmwareSignatureVerificationJson_1 = """
                {
                  "platformFirmwareSignatureVerification": {
                    "values": [
                      "PLATFORMFIRMWARESIGNATUREVERIFICATION_HARDWARESRTM"
                    ]
                  }
                }
            """;
    public static final String platformFirmwareSignatureVerificationJsonCsv = String.join(",", platformFirmwareSignatureVerificationJson_1);
    public static final String platformFirmwareSignatureVerificationJson = "[" + platformFirmwareSignatureVerificationJsonCsv + "]";

    public static final String platformFirmwareUpdateComplianceJson_1 = """
                {
                  "platformFirmwareUpdateCompliance": {
                    "values": [
                      "PLATFORMFIRMWAREUPDATECOMPLIANCE_SP800_147"
                    ]
                  }
                }
            """;
    public static final String platformFirmwareUpdateComplianceJsonCsv = String.join(",", platformFirmwareUpdateComplianceJson_1);
    public static final String platformFirmwareUpdateComplianceJson = "[" + platformFirmwareUpdateComplianceJsonCsv + "]";

    public static final String platformHardwareCapabilitiesJson_1 = """
                {
                  "platformHardwareCapabilities": {
                    "values": [
                      "PLATFORMHARDWARECAPABILITIES_IOMMUSUPPORT",
                      "physicalTamperProtection",
                      "physicalTamperDetection",
                      "externalDMASupport"
                    ]
                  }
                }
            """;
    public static final String platformHardwareCapabilitiesJsonCsv = String.join(",", platformHardwareCapabilitiesJson_1);
    public static final String platformHardwareCapabilitiesJson = "[" + platformHardwareCapabilitiesJsonCsv + "]";

    public static final String rtmJson_1 = """
                {
                  "rTMTypes": {
                    "values": [
                      3
                    ]
                  }
                }
            """;

    public static final String rtmJson_2 = """
                {
                  "rTMTypes": {
                    "values": [
                      "RTMTYPES_STATIC",
                      3
                    ]
                  }
                }
            """;
    public static final String rtmJsonCsv = String.join(",", rtmJson_1, rtmJson_2);
    public static final String rtmJson = "[" + rtmJsonCsv + "]";

    public static final String statusJson_1 = """
                {
                  "status": "ATTRIBUTESTATUS_REMOVED"
                }
            """;
    public static final String statusJsonCsv = String.join(",", statusJson_1);
    public static final String statusJson = "[" + statusJsonCsv + "]";

    public static final String uriJson_1 = """
                {
                  "uri": {
                    "uniformResourceIdentifier": "https://no.invalid"
                  }
                }
            """;
    public static final String uriJsonCsv = String.join(",", uriJson_1);
    public static final String uriJson = "[" + uriJsonCsv + "]";

    public static final String utf8Json_1 = """
                {
                  "utf8": "I got 8 problems but a Trait is all of them."
                }
            """;
    public static final String utf8JsonCsv = String.join(",", utf8Json_1);
    public static final String utf8Json = "[" + utf8JsonCsv + "]";

    public static final String ia5Json_1 = """
                {
                  "ia5": "5 times table"
                }
            """;
    public static final String ia5JsonCsv = String.join(",", ia5Json_1);
    public static final String ia5Json = "[" + ia5JsonCsv + "]";

    public static final String pemJson_1 = """
                {
                  "pem": "This is not PEM."
                }
            """;
    public static final String pemJsonCsv = String.join(",", pemJson_1);
    public static final String pemJson = "[" + pemJsonCsv + "]";

    public static final String publicKeyJson_1 = """
                {
                  "publicKey": {
                    "algorithm": {
                      "algorithm": "2.16.840.1.101.3.4.2.1",
                      "parameters": null
                    },
                    "subjectPublicKey": "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEA0Mp0ZLoxH2f7Vwnq+jFWH0DAchozHE/u++DVP45X+RZCbrWKwUnIzqUd3DZE7Ey7hAK0n0wm9IqBtAD3MZRsYOo1dRulISyzP7NCPfdQ3i0uBJg7rSSKTG0W9yOj/FoV174v0V+nuhhMh1IV63aVgwio5s7rLilMN+Yz2qalq4gYXBb4MEb/9uLLEPDekL6MYo9TKbgglyfEzckkXAOzQhgEtj2t4bov/sUGZc7SSDv/uYH5/pRPBM5s1NcyomyG9BhnXkvQuWRWKBa5TjsATZRKac4LWc4YX8Mr5Z1dC6gSx8oWNtM5psN+3aMtJb5D4jCih2KCPb3/qRN88HdzMwIDAQAB"
                  }
                }
            """;
    public static final String publicKeyJsonCsv = String.join(",", publicKeyJson_1);
    public static final String publicKeyJson = "[" + publicKeyJsonCsv + "]";

    public static final String entGeoLocationJson_1 = """
                {
                  "entGeoLocation": {
                    "countryCode": "US",
                    "stateOrProvince": "US-MD"
                  }
                }
            """;
    public static final String entGeoLocationJsonCsv = String.join(",", entGeoLocationJson_1);
    public static final String entGeoLocationJson = "[" + entGeoLocationJsonCsv + "]";

    public static final String countryOfOriginJson_1 = """
                {
                  "countryOfOrigin": {
                    "location": {
                      "countryCode": "US",
                      "stateOrProvince": "US-CA"
                    },
                    "hasComponents": false
                  }
                }
            """;
    public static final String countryOfOriginJsonCsv = String.join(",", countryOfOriginJson_1);
    public static final String countryOfOriginJson = "[" + countryOfOriginJsonCsv + "]";
    public static final String TEST_TRAIT_MAP_1_CSV = String.join(",",
            asn1JsonCsv, boolJsonCsv, certificateIdentifierJsonCsv, commonCriteriaJsonCsv, componentClassJsonCsv,
            componentIdentifierV11JsonCsv, fipsJson, iso9000JsonCsv, networkMacJsonCsv, oidJsonCsv, penJsonCsv,
            platformFirmwareCapabilitiesJsonCsv, platformFirmwareSignatureVerificationJsonCsv,
            platformFirmwareUpdateComplianceJsonCsv, platformHardwareCapabilitiesJsonCsv, rtmJsonCsv,
            statusJsonCsv, uriJsonCsv, utf8JsonCsv, ia5JsonCsv, pemJsonCsv, publicKeyJsonCsv,
            entGeoLocationJsonCsv, countryOfOriginJsonCsv);
    public static final String TEST_TRAIT_MAP_1_JSON = "[" + TEST_TRAIT_MAP_1_CSV + "]";

    public static final String simpleJson2 = """
            [
                {
                    "certificateIdentifier": {
                        "hashedCertIdentifier": {
                            "hashAlgorithm": {
                                "algorithm": "2.16.840.1.101.3.4.2.1"
                            },
                            "hashOverSignatureValue": "dGVzdGhhc2g="
                        }
                    }
                },
                {
                    "status": "ATTRIBUTESTATUS_ADDED"
                },
                {
                    "utf8": "Test String Value"
                },
                {
                    "traitId": "2.23.133.19.1.16",
                    "status": "ATTRIBUTESTATUS_REMOVED"
                }
            ]
            """;

    @Test
    public void testDeserializeSimple() throws Exception {
        // Simple test with just status field (like protobuf with empty standard fields)
        String simpleJson = """
            [
                {
                  "traitId": "",
                  "traitCategory": "",
                  "traitRegistry": "",
                  "description": "",
                  "descriptionURI": "",
                  "status": "modified"
                }
            ]
            """;

        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(simpleJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        // Verify we have StatusTrait
        List<StatusTrait> statusTraits = traitMap.get(StatusTrait.class);
        Assertions.assertNotNull(statusTraits, "Should have StatusTrait");
        Assertions.assertEquals(1, statusTraits.size());

        StatusTrait statusTrait = (StatusTrait) statusTraits.getFirst();
        Assertions.assertEquals(AttributeStatus.Enumerated.modified, statusTrait.getTraitValue().getEnum());
    }

    @Test
    public void testDeserializeSimple2() throws Exception {
        // Simple test with just status field (like protobuf with empty standard fields)
        String simpleJson = """
            [
                {
                    "certificateIdentifier": {
                        "hashedCertIdentifier": {
                            "hashAlgorithm": {
                                "algorithm": "2.16.840.1.101.3.4.2.1"
                            },
                            "hashOverSignatureValue": "dGVzdGhhc2g="
                        }
                    }
                },
                {
                    "status": "ATTRIBUTESTATUS_ADDED"
                },
                {
                    "utf8": "Test String Value"
                },
                {
                    "traitId": "2.23.133.19.1.16",
                    "status": "ATTRIBUTESTATUS_REMOVED"
                }
            ]
            """;

        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(simpleJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        Assertions.assertEquals(4, traitMap.size()); // verify types of traits deserialized

        // Verify we have StatusTrait
        List<StatusTrait> statusTraits = traitMap.get(StatusTrait.class);
        Assertions.assertNotNull(statusTraits, "Should have StatusTrait");
        Assertions.assertEquals(2, statusTraits.size());
    }

    @Test
    public void testDeserializeAsn1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(asn1Json, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<ASN1ObjectTrait> traits = traitMap.get(ASN1ObjectTrait.class);
        Assertions.assertNotNull(traits, "Should have traits");
        Assertions.assertEquals(2, traits.size());
    }

    @Test
    public void testDeserializeBool1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(boolJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<BooleanTrait> traits = traitMap.get(BooleanTrait.class);
        Assertions.assertNotNull(traits, "Should have traits");
        Assertions.assertEquals(2, traits.size());
    }

    @Test
    public void testDeserializeCertificateIdentifier1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(certificateIdentifierJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<CertificateIdentifierTrait> traits = traitMap.get(CertificateIdentifierTrait.class);
        Assertions.assertNotNull(traits, "Should have traits");
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializeCommonCriteria1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(commonCriteriaJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<CommonCriteriaTrait> traits = traitMap.get(CommonCriteriaTrait.class);
        Assertions.assertNotNull(traits, "Should have traits");
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializeComponentClass1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(componentClassJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<ComponentClassTrait> traits = traitMap.get(ComponentClassTrait.class);
        Assertions.assertNotNull(traits, "Should have traits");
        Assertions.assertEquals(3, traits.size());
    }

    @Test
    public void testDeserializeComponentIdentifierV11_1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(componentIdentifierV11Json, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<ComponentIdentifierV11Trait> traits = traitMap.get(ComponentIdentifierV11Trait.class);
        Assertions.assertNotNull(traits, "Should have traits");
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializeFips1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(fipsJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<FIPSLevelTrait> traits = traitMap.get(FIPSLevelTrait.class);
        Assertions.assertNotNull(traits, "Should have traits");
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializeIso9000_1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(iso9000Json, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<ISO9000Trait> traits = traitMap.get(ISO9000Trait.class);
        Assertions.assertNotNull(traits, "Should have traits");
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializeNetworkMac1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(networkMacJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<NetworkMACTrait> traits = traitMap.get(NetworkMACTrait.class);
        Assertions.assertNotNull(traits, "Should have traits");
        Assertions.assertEquals(3, traits.size());
    }

    @Test
    public void testDeserializeOid1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(oidJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<OIDTrait> traits = traitMap.get(OIDTrait.class);
        Assertions.assertNotNull(traits, "Should have traits");
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializePen1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(penJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<PENTrait> traits = traitMap.get(PENTrait.class);
        Assertions.assertNotNull(traits, "Should have traits");
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializePlatformFirmwareCapabilities1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(platformFirmwareCapabilitiesJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<PlatformFirmwareCapabilitiesTrait> traits = traitMap.get(PlatformFirmwareCapabilitiesTrait.class);
        Assertions.assertNotNull(traits, "Should have traits");
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializePlatformFirmwareSignatureVerification1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(platformFirmwareSignatureVerificationJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<PlatformFirmwareSignatureVerificationTrait> traits = traitMap.get(PlatformFirmwareSignatureVerificationTrait.class);
        Assertions.assertNotNull(traits, "Should have traits");
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializePlatformFirmwareUpdateCompliance1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(platformFirmwareUpdateComplianceJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<PlatformFirmwareUpdateComplianceTrait> traits = traitMap.get(PlatformFirmwareUpdateComplianceTrait.class);
        Assertions.assertNotNull(traits, "Should have traits");
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializePlatformHardwareCapabilities1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(platformHardwareCapabilitiesJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<PlatformHardwareCapabilitiesTrait> traits = traitMap.get(PlatformHardwareCapabilitiesTrait.class);
        Assertions.assertNotNull(traits, "Should have traits");
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializeRtm1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(rtmJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<RTMTrait> traits = traitMap.get(RTMTrait.class);
        Assertions.assertEquals(2, traits.size());

        RTMTrait rtmTrait1 = (RTMTrait) traits.getFirst();
        Assertions.assertEquals(List.of(RTMTypes.Enumerated.virtual), EnumWithIntegerValue.decodeMask(rtmTrait1.getTraitValue().getValue(), RTMTypes.Enumerated.class));

        RTMTrait rtmTrait2 = (RTMTrait) traits.get(1);
        Assertions.assertArrayEquals(List.of(RTMTypes.Enumerated.Static, RTMTypes.Enumerated.virtual).toArray(), EnumWithIntegerValue.decodeMask(rtmTrait2.getTraitValue().getValue(), RTMTypes.Enumerated.class).toArray());
    }

    @Test
    public void testDeserializeStatus1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(statusJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<StatusTrait> traits = traitMap.get(StatusTrait.class);
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializeUri1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(uriJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<URITrait> traits = traitMap.get(URITrait.class);
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializeUtf8_1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(utf8Json, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<UTF8StringTrait> traits = traitMap.get(UTF8StringTrait.class);
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializeIa5_1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(ia5Json, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<IA5StringTrait> traits = traitMap.get(IA5StringTrait.class);
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializePem_1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(pemJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<PEMCertStringTrait> traits = traitMap.get(PEMCertStringTrait.class);
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializePublicKey1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(publicKeyJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<PublicKeyTrait> traits = traitMap.get(PublicKeyTrait.class);
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializeEntGeoLocation1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(entGeoLocationJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<EntityGeoLocationTrait> traits = traitMap.get(EntityGeoLocationTrait.class);
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserializeCountryOfOrigin1() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(countryOfOriginJson, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");

        List<CountryOfOriginTrait> traits = traitMap.get(CountryOfOriginTrait.class);
        Assertions.assertEquals(1, traits.size());
    }

    @Test
    public void testDeserialize() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TraitMap traitMap = mapper.readValue(TEST_TRAIT_MAP_1_JSON, TraitMap.class);

        Assertions.assertNotNull(traitMap);
        Assertions.assertFalse(traitMap.isEmpty(), "TraitMap should not be empty");
        Assertions.assertEquals(TraitId.getRegisteredIds().size(), traitMap.typeCount(), "TraitMap should have all registered traits");
    }
}
