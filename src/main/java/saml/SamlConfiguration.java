package saml;

public final class SamlConfiguration {

    private SamlConfiguration() {
    }

    public static final String IDP_ENTITY_ID =
            "http://localhost:8080/StudentResultManagement/saml/idp";

    public static final String IDP_SSO_URL =
            "http://localhost:8080/StudentResultManagement/saml/idp/sso";

    public static final String IDP_METADATA_URL =
            "http://localhost:8080/StudentResultManagement/saml/idp/metadata";

    public static final String SP_ENTITY_ID =
            "http://localhost:8080/StudentResultManagement/saml/sp";

    public static final String SP_ACS_URL =
            "http://localhost:8080/StudentResultManagement/saml/acs";

    public static final String SP_METADATA_URL =
            "http://localhost:8080/StudentResultManagement/saml/sp/metadata";

    public static final String SAML_VERSION = "2.0";

    public static final String NAME_ID_FORMAT =
            "urn:oasis:names:tc:SAML:2.0:nameid-format:unspecified";

    public static final String AUTHN_CONTEXT =
            "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport";

    public static final int ASSERTION_VALIDITY_SECONDS = 300;

    public static final int REQUEST_VALIDITY_SECONDS = 300;
}