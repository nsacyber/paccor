using Google.Protobuf;
using HardwareManifestProto;
using OidsProto;
using PlatformCertificateProto;

namespace HardwareManifestPlugin {
    public class HardwareManifestConverter {
        public static ManifestV3 FromManifestV2(ManifestV2 v2, string traitDescription, string traitDescriptionUri) {
            // Wrap V2 Manifest with Trait details and return a V3 Manifest
            ManifestV3 v3 = new();

            // Convert Platform Fields
            if (v2.PLATFORM != null) {
                v3.PlatformIdentifier = new PlatformIdentifierOtherName {
                    TypeId = OidsUtils.Find(TCG_COMMON_NODE.TcgAtPlatformidentifier),
                    Value = new PlatformIdentifier()
                };
                if (!string.IsNullOrEmpty(v2.PLATFORM.PLATFORMMANUFACTURERSTR)) {
                    v3.PlatformIdentifier.Value.PlatformManufacturer = new Manufacturer {
                        Utf8 = new UTF8StringTrait {
                            TraitId = OidsUtils.Find(TCG_TR_ID_NODE.TcgTrIdUtf8String),
                            TraitCategory = OidsUtils.Find(TCG_TR_CAT_NODE.TcgTrCatPlatformmanufacturer),
                            TraitRegistry = OidsUtils.Find(TCG_TR_REG_NODE.TcgTrRegNone),
                            Description = new UTF8String {
                                String = traitDescription
                            },
                            DescriptionURI = new IA5String {
                                String = traitDescriptionUri
                            },
                            TraitValue = new UTF8String {
                                String = v2.PLATFORM.PLATFORMMANUFACTURERSTR
                            }
                        }
                    };
                }

                if (!string.IsNullOrEmpty(v2.PLATFORM.PLATFORMMANUFACTURERID)) {
                    v3.PlatformIdentifier.Value.PlatformManufacturerIdentifier = new PENTrait {
                        TraitId = OidsUtils.Find(TCG_TR_ID_NODE.TcgTrIdPen),
                        TraitCategory = OidsUtils.Find(TCG_TR_CAT_NODE.TcgTrCatPlatformmanufactureridentifier),
                        TraitRegistry = OidsUtils.Find(TCG_TR_REG_NODE.TcgTrRegNone),
                        Description = new UTF8String {
                            String = traitDescription
                        },
                        DescriptionURI = new IA5String {
                            String = traitDescriptionUri
                        },
                        TraitValue = new ObjectIdentifier {
                            Oid = v2.PLATFORM.PLATFORMMANUFACTURERID
                        }
                    };
                }

                if (!string.IsNullOrEmpty(v2.PLATFORM.PLATFORMMODEL)) {
                    v3.PlatformIdentifier.Value.PlatformModel = new Model {
                        Utf8 = new UTF8StringTrait {
                            TraitId = OidsUtils.Find(TCG_TR_ID_NODE.TcgTrIdUtf8String),
                            TraitCategory = OidsUtils.Find(TCG_TR_CAT_NODE.TcgTrCatPlatformmodel),
                            TraitRegistry = OidsUtils.Find(TCG_TR_REG_NODE.TcgTrRegNone),
                            Description = new UTF8String {
                                String = traitDescription
                            },
                            DescriptionURI = new IA5String {
                                String = traitDescriptionUri
                            },
                            TraitValue = new UTF8String {
                                String = v2.PLATFORM.PLATFORMMODEL
                            }
                        }
                    };
                }

                if (!string.IsNullOrEmpty(v2.PLATFORM.PLATFORMSERIAL)) {
                    v3.PlatformIdentifier.Value.PlatformSerial = new Serial {
                        Utf8 = new UTF8StringTrait {
                            TraitId = OidsUtils.Find(TCG_TR_ID_NODE.TcgTrIdUtf8String),
                            TraitCategory = OidsUtils.Find(TCG_TR_CAT_NODE.TcgTrCatPlatformserial),
                            TraitRegistry = OidsUtils.Find(TCG_TR_REG_NODE.TcgTrRegNone),
                            Description = new UTF8String {
                                String = traitDescription
                            },
                            DescriptionURI = new IA5String {
                                String = traitDescriptionUri
                            },
                            TraitValue = new UTF8String {
                                String = v2.PLATFORM.PLATFORMSERIAL
                            }
                        }
                    };
                }

                if (!string.IsNullOrEmpty(v2.PLATFORM.PLATFORMVERSION)) {
                    v3.PlatformIdentifier.Value.PlatformVersion = new Revision {
                        Utf8 = new UTF8StringTrait {
                            TraitId = OidsUtils.Find(TCG_TR_ID_NODE.TcgTrIdUtf8String),
                            TraitCategory = OidsUtils.Find(TCG_TR_CAT_NODE.TcgTrCatPlatformversion),
                            TraitRegistry = OidsUtils.Find(TCG_TR_REG_NODE.TcgTrRegNone),
                            Description = new UTF8String {
                                String = traitDescription
                            },
                            DescriptionURI = new IA5String {
                                String = traitDescriptionUri
                            },
                            TraitValue = new UTF8String {
                                String = v2.PLATFORM.PLATFORMVERSION
                            }
                        }
                    };
                }
            }

            // Convert Components
            v3.PlatformConfiguration = new PlatformConfiguration();
            foreach (HardwareManifestProto.ComponentIdentifier component in v2.COMPONENTS) {
                ComponentIdentifierTrait trait = new() {
                    ComponentIdentifierV11 = new ComponentIdentifierV11Trait {
                        TraitId = OidsUtils.Find(TCG_TR_ID_NODE.TcgTrIdComponentidentifierv11),
                        TraitCategory = OidsUtils.Find(TCG_TR_CAT_NODE.TcgTrCatComponentidentifierv11),
                        TraitRegistry = OidsUtils.Find(TCG_TR_REG_NODE.TcgTrRegNone),
                        Description = new UTF8String {
                            String = traitDescription
                        },
                        DescriptionURI = new IA5String {
                            String = traitDescriptionUri
                        },
                        TraitValue = new ComponentIdentifierV11()
                    }
                };

                // Copy component class
                if (!string.IsNullOrEmpty(component.COMPONENTCLASS.COMPONENTCLASSVALUE)) {
                    byte[] componentClassValue = System.Convert.FromHexString(component.COMPONENTCLASS.COMPONENTCLASSVALUE);
                    trait.ComponentIdentifierV11.TraitValue.ComponentClass = new PlatformCertificateProto.ComponentClass {
                        ComponentClassRegistry = new ObjectIdentifier {
                            Oid = component.COMPONENTCLASS.COMPONENTCLASSREGISTRY
                        },
                        ComponentClassValue = new OctetString {
                            Base64 = ByteString.CopyFrom(componentClassValue)
                        }
                    };
                }

                // Copy main strings
                if (!string.IsNullOrEmpty(component.MANUFACTURER)) {
                    trait.ComponentIdentifierV11.TraitValue.ComponentManufacturer = new UTF8String {
                        String = component.MANUFACTURER
                    };
                }

                if (!string.IsNullOrEmpty(component.MODEL)) {
                    trait.ComponentIdentifierV11.TraitValue.ComponentModel = new UTF8String {
                        String = component.MODEL
                    };
                }

                if (!string.IsNullOrEmpty(component.SERIAL)) {
                    trait.ComponentIdentifierV11.TraitValue.ComponentSerial = new UTF8String {
                        String = component.SERIAL
                    };
                }

                if (!string.IsNullOrEmpty(component.REVISION)) {
                    trait.ComponentIdentifierV11.TraitValue.ComponentRevision = new UTF8String {
                        String = component.REVISION
                    };
                }

                if (!string.IsNullOrEmpty(component.MANUFACTURERID)) {
                    trait.ComponentIdentifierV11.TraitValue.ComponentManufacturerId = new ObjectIdentifier {
                        Oid = component.MANUFACTURERID
                    };
                }

                if (!string.IsNullOrEmpty(component.FIELDREPLACEABLE)) {
                    trait.ComponentIdentifierV11.TraitValue.FieldReplaceable = new Boolean {
                        Bool = bool.Parse(component.FIELDREPLACEABLE)
                    };
                }

                // Copy component addresses
                foreach (Address address in component.ADDRESSES) {
                    ComponentAddress newAddress = new();
                    switch (address.ADDRESSESOneofCase) {
                        case Address.ADDRESSESOneofOneofCase.BLUETOOTHMAC:
                            newAddress.AddressType = OidsUtils.Find(TCG_ADDRESS_NODE.TcgAddressBluetoothmac);
                            newAddress.AddressValue = new UTF8String {
                                String = address.BLUETOOTHMAC
                            };
                            break;
                        case Address.ADDRESSESOneofOneofCase.ETHERNETMAC:
                            newAddress.AddressType = OidsUtils.Find(TCG_ADDRESS_NODE.TcgAddressEthernetmac);
                            newAddress.AddressValue = new UTF8String {
                                String = address.ETHERNETMAC
                            };
                            break;
                        case Address.ADDRESSESOneofOneofCase.WLANMAC:
                            newAddress.AddressType = OidsUtils.Find(TCG_ADDRESS_NODE.TcgAddressWlanmac);
                            newAddress.AddressValue = new UTF8String {
                                String = address.WLANMAC
                            };
                            break;
                        case Address.ADDRESSESOneofOneofCase.None:
                        default:
                            // Don't propagate unknown network address types
                            continue;
                    }
                    trait.ComponentIdentifierV11.TraitValue.ComponentAddresses.Add(newAddress);
                }

                // Copy component hashed or attribute cert identifier, saving only the last entry
                //   Handle case where the ATTRIBUTECERTIDENTIFIER key is used within the PLATFORMCERT key
                if (component.PLATFORMCERT != null) {
                    if (component.PLATFORMCERT.ATTRIBUTECERTIDENTIFIER != null) {
                        byte[] hvBytes = System.Text.Encoding.UTF8.GetBytes(component.PLATFORMCERT.ATTRIBUTECERTIDENTIFIER.HASHVALUE);
                        if (hvBytes.Length > 0) {
                            string hvBase64 = System.Convert.ToBase64String(hvBytes);

                            trait.ComponentIdentifierV11.TraitValue.ComponentPlatformCert = new PlatformCertificateProto.CertificateIdentifier {
                                HashedCertIdentifier = new PlatformCertificateProto.HashedCertificateIdentifier {
                                    HashAlgorithm = new AlgorithmIdentifier {
                                        Algorithm = new ObjectIdentifier {
                                            Oid = component.PLATFORMCERT.ATTRIBUTECERTIDENTIFIER.HASHALG
                                        }
                                    },
                                    HashOverSignatureValue = new OctetString {
                                        Base64 = ByteString.FromBase64(hvBase64)
                                    }
                                }
                            };
                        }
                    }

                    //   Handle case where the HASHEDCERTIDENTIFIER key is used within the PLATFORMCERT key
                    if (component.PLATFORMCERT.HASHEDCERTIDENTIFIER != null) {
                        byte[] hvBytes = System.Text.Encoding.UTF8.GetBytes(component.PLATFORMCERT.HASHEDCERTIDENTIFIER.HASHVALUE);
                        if (hvBytes.Length > 0) {
                            string hvBase64 = System.Convert.ToBase64String(hvBytes);

                            trait.ComponentIdentifierV11.TraitValue.ComponentPlatformCert = new PlatformCertificateProto.CertificateIdentifier {
                                HashedCertIdentifier = new PlatformCertificateProto.HashedCertificateIdentifier {
                                    HashAlgorithm = new AlgorithmIdentifier {
                                        Algorithm = new ObjectIdentifier {
                                            Oid = component.PLATFORMCERT.HASHEDCERTIDENTIFIER.HASHALG
                                        }
                                    },
                                    HashOverSignatureValue = new OctetString {
                                        Base64 = ByteString.FromBase64(hvBase64)
                                    }
                                }
                            };
                        }
                    }
                }

                //   Handle case where the ATTRIBUTECERTIDENTIFIER key is used within the CERTIFICATEIDENTIFIER key
                if (component.CERTIFICATEIDENTIFIER != null) {
                    if (component.CERTIFICATEIDENTIFIER.ATTRIBUTECERTIDENTIFIER != null) {
                        byte[] hvBytes = System.Text.Encoding.UTF8.GetBytes(component.CERTIFICATEIDENTIFIER.ATTRIBUTECERTIDENTIFIER.HASHVALUE);
                        if (hvBytes.Length > 0) {
                            string hvBase64 = System.Convert.ToBase64String(hvBytes);

                            trait.ComponentIdentifierV11.TraitValue.ComponentPlatformCert = new PlatformCertificateProto.CertificateIdentifier {
                                HashedCertIdentifier = new PlatformCertificateProto.HashedCertificateIdentifier {
                                    HashAlgorithm = new AlgorithmIdentifier {
                                        Algorithm = new ObjectIdentifier {
                                            Oid = component.CERTIFICATEIDENTIFIER.ATTRIBUTECERTIDENTIFIER.HASHALG
                                        }
                                    },
                                    HashOverSignatureValue = new OctetString {
                                        Base64 = ByteString.FromBase64(hvBase64)
                                    }
                                }
                            };
                        }
                    }

                    //   Handle case where the HASHEDCERTIDENTIFIER key is used within the CERTIFICATEIDENTIFIER key
                    if (component.CERTIFICATEIDENTIFIER.HASHEDCERTIDENTIFIER != null) {
                        byte[] hvBytes = System.Text.Encoding.UTF8.GetBytes(component.CERTIFICATEIDENTIFIER.HASHEDCERTIDENTIFIER.HASHVALUE);
                        if (hvBytes.Length > 0) {
                            string hvBase64 = System.Convert.ToBase64String(hvBytes);

                            trait.ComponentIdentifierV11.TraitValue.ComponentPlatformCert = new PlatformCertificateProto.CertificateIdentifier {
                                HashedCertIdentifier = new PlatformCertificateProto.HashedCertificateIdentifier {
                                    HashAlgorithm = new AlgorithmIdentifier {
                                        Algorithm = new ObjectIdentifier {
                                            Oid = component.CERTIFICATEIDENTIFIER.HASHEDCERTIDENTIFIER.HASHALG
                                        }
                                    },
                                    HashOverSignatureValue = new OctetString {
                                        Base64 = ByteString.FromBase64(hvBase64)
                                    }
                                }
                            };
                        }
                    }
                }

                // Copy component attribute status
                if (component.STATUS != null) {
                    switch (component.STATUS) {
                        case "ADDED":
                        case "added":
                            trait.ComponentIdentifierV11.TraitValue.Status = AttributeStatus.Added;
                            break;
                        case "MODIFIED":
                        case "modified":
                            trait.ComponentIdentifierV11.TraitValue.Status = AttributeStatus.Modified;
                            break;
                        case "REMOVED":
                        case "removed":
                            trait.ComponentIdentifierV11.TraitValue.Status = AttributeStatus.Removed;
                            break;
                        default:
                            break;
                    }
                }

                // Copy component generic cert identifier 
                if (component.CERTIFICATEIDENTIFIER != null) {
                    if (component.CERTIFICATEIDENTIFIER.GENERICCERTIDENTIFIER != null) {
                        trait.ComponentIdentifierV11.TraitValue.ComponentPlatformCert.GenericCertIdentifier = new IssuerSerial {
                            Issuer = new IssuerSerialDN {
                                RdnShorthand = component.CERTIFICATEIDENTIFIER.GENERICCERTIDENTIFIER.ISSUER
                            },
                            Serial = new CertificateSerialNumber {
                                SerialNumber = new Integer {
                                    Int = long.Parse(component.CERTIFICATEIDENTIFIER.GENERICCERTIDENTIFIER.SERIAL)
                                }
                            }
                        };
                    }
                }

                // Copy component platform cert uri
                if (component.COMPONENTPLATFORMCERTURI != null) {
                    byte[] hvBytes = System.Text.Encoding.UTF8.GetBytes(component.COMPONENTPLATFORMCERTURI.HASHVALUE);
                    if (hvBytes.Length > 0) {
                        string hvBase64 = System.Convert.ToBase64String(hvBytes);

                        trait.ComponentIdentifierV11.TraitValue.ComponentPlatformCertUri = new URIReference {
                            HashAlgorithm = new AlgorithmIdentifier {
                                Algorithm = new ObjectIdentifier {
                                    Oid = component.COMPONENTPLATFORMCERTURI.HASHALG
                                }
                            },
                            HashValue = new BitString {
                                Base64 = ByteString.FromBase64(hvBase64)
                            }
                        };
                    }

                    trait.ComponentIdentifierV11.TraitValue.ComponentPlatformCertUri.UniformResourceIdentifier.String = component.COMPONENTPLATFORMCERTURI.UNIFORMRESOURCEIDENTIFIER;
                }

                // Save wrapped component identifier v11
                v3.PlatformConfiguration.PlatformComponents.Add(trait);
            }

            // Convert Properties
            foreach (HardwareManifestProto.Property property in v2.PROPERTIES) {
                PlatformCertificateProto.Property newProperty = new() {
                    PropertyName = new UTF8String {
                        String = property.PROPERTYNAME
                    },
                    PropertyValue = new UTF8String {
                        String = property.PROPERTYVALUE
                    }
                };
                switch (property.STATUS) {
                    case "ADDED":
                    case "added":
                        newProperty.Status = AttributeStatus.Added;
                        break;
                    case "MODIFIED":
                    case "modified":
                        newProperty.Status = AttributeStatus.Modified;
                        break;
                    case "REMOVED":
                    case "removed":
                        newProperty.Status = AttributeStatus.Removed;
                        break;
                }
                v3.PlatformConfiguration.PlatformProperties.Add(newProperty);
            }

            return v3;
        }
    }
}
