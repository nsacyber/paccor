namespace HardwareManifestPlugin {
    /// <summary>
    /// Evidence that a component in the plugin's manifest possesses a key, produced in response to a verifier's nonce.
    /// The verifier checks the signature in the evidence, the nonce it contains, and the certificate chain back to the component manufacturer.
    /// </summary>
    public sealed class ComponentEvidence {
        /// <summary>
        /// Index into the plugin's ManifestV2.COMPONENTS identifying the component this evidence is for.
        /// </summary>
        public int ComponentIndex {
            get;
            init;
        }

        /// <summary>
        /// Name of the evidence format, e.g. "HP_EPSC_BOOTLOG", "DICE_TCG_EVIDENCE", "SPDM_MEASUREMENT", "COSE_SIGN1".
        /// </summary>
        public string EvidenceFormat {
            get;
            init;
        } = "";

        /// <summary>
        /// Version of the evidence format, when the format defines one.
        /// </summary>
        public string EvidenceVersion {
            get;
            init;
        } = "";

        /// <summary>
        /// The raw evidence as produced by the component. It contains the nonce and a signature the verifier checks.
        /// </summary>
        public byte[] Evidence {
            get;
            init;
        } = [];

        /// <summary>
        /// DER encoded certificates for the evidence signing key, leaf first.
        /// </summary>
        public IReadOnlyList<byte[]> CertificateChain {
            get;
            init;
        } = [];
    }
}
