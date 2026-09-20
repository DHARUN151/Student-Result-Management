package service;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import model.User;
import saml.SamlConfiguration;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.CanonicalizationMethod;
import javax.xml.crypto.dsig.DigestMethod;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignatureMethod;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;
import java.util.Collections;
import java.util.UUID;

public class SamlResponseService {

    private static final String KEYSTORE_PATH =
            "/WEB-INF/saml/idp-keystore.p12";

    private static final String KEYSTORE_TYPE =
            "PKCS12";

    private static final String KEY_ALIAS =
            "saml-idp";

    private static final String KEYSTORE_PASSWORD_PROPERTY =
            "SAML_KEYSTORE_PASSWORD";

    private static final String SAML_PROTOCOL_NAMESPACE =
            "urn:oasis:names:tc:SAML:2.0:protocol";

    private static final String SAML_ASSERTION_NAMESPACE =
            "urn:oasis:names:tc:SAML:2.0:assertion";

    private static final String XMLDSIG_NAMESPACE =
            "http://www.w3.org/2000/09/xmldsig#";

    private final ServletContext servletContext;

    public SamlResponseService(
            ServletContext servletContext) {

        this.servletContext = servletContext;
    }

    public String createSignedResponse(
            User user,
            HttpSession session)
            throws Exception {

        if (user == null) {
            throw new IllegalArgumentException(
                    "SAML user is missing"
            );
        }

        if (session == null) {
            throw new IllegalArgumentException(
                    "SAML session is missing"
            );
        }

        String requestId =
                (String) session.getAttribute(
                        "SAML_REQUEST_ID"
                );

        String acsUrl =
                (String) session.getAttribute(
                        "SAML_ACS_URL"
                );

        String spEntityId =
                (String) session.getAttribute(
                        "SAML_SP_ENTITY_ID"
                );

        if (requestId == null
                || requestId.trim().isEmpty()) {

            throw new IllegalStateException(
                    "SAML request ID is missing"
            );
        }

        if (!SamlConfiguration.SP_ACS_URL.equals(
                acsUrl)) {

            throw new IllegalStateException(
                    "Invalid SAML ACS URL"
            );
        }

        if (!SamlConfiguration.SP_ENTITY_ID.equals(
                spEntityId)) {

            throw new IllegalStateException(
                    "Invalid SAML SP entity ID"
            );
        }

        KeyMaterial keyMaterial =
                loadKeyMaterial();

        Document document =
                createDocument();

        Instant now =
                Instant.now();

        Instant notOnOrAfter =
                now.plusSeconds(
                        SamlConfiguration
                                .ASSERTION_VALIDITY_SECONDS
                );

        String responseId =
                "_" + UUID.randomUUID();

        String assertionId =
                "_" + UUID.randomUUID();

        Element response =
                document.createElementNS(
                        SAML_PROTOCOL_NAMESPACE,
                        "samlp:Response"
                );

        response.setAttribute(
                "ID",
                responseId
        );

        response.setAttribute(
                "Version",
                SamlConfiguration.SAML_VERSION
        );

        response.setAttribute(
                "IssueInstant",
                now.toString()
        );

        response.setAttribute(
                "Destination",
                SamlConfiguration.SP_ACS_URL
        );

        response.setAttribute(
                "InResponseTo",
                requestId
        );

        document.appendChild(response);

        /*
         * Response Issuer
         */
        Element responseIssuer =
                createElement(
                        document,
                        SAML_ASSERTION_NAMESPACE,
                        "saml:Issuer",
                        SamlConfiguration.IDP_ENTITY_ID
                );

        response.appendChild(
                responseIssuer
        );

        /*
         * Response Status
         */
        Element status =
                document.createElementNS(
                        SAML_PROTOCOL_NAMESPACE,
                        "samlp:Status"
                );

        Element statusCode =
                document.createElementNS(
                        SAML_PROTOCOL_NAMESPACE,
                        "samlp:StatusCode"
                );

        statusCode.setAttribute(
                "Value",
                "urn:oasis:names:tc:SAML:2.0:status:Success"
        );

        status.appendChild(
                statusCode
        );

        response.appendChild(
                status
        );

        /*
         * Assertion
         */
        Element assertion =
                document.createElementNS(
                        SAML_ASSERTION_NAMESPACE,
                        "saml:Assertion"
                );

        assertion.setAttribute(
                "ID",
                assertionId
        );

        assertion.setAttribute(
                "Version",
                SamlConfiguration.SAML_VERSION
        );

        assertion.setAttribute(
                "IssueInstant",
                now.toString()
        );

        response.appendChild(
                assertion
        );

        /*
         * Assertion Issuer
         */
        Element assertionIssuer =
                createElement(
                        document,
                        SAML_ASSERTION_NAMESPACE,
                        "saml:Issuer",
                        SamlConfiguration.IDP_ENTITY_ID
                );

        assertion.appendChild(
                assertionIssuer
        );

        /*
         * Subject
         */
        Element subject =
                document.createElementNS(
                        SAML_ASSERTION_NAMESPACE,
                        "saml:Subject"
                );

        Element nameId =
                document.createElementNS(
                        SAML_ASSERTION_NAMESPACE,
                        "saml:NameID"
                );

        nameId.setAttribute(
                "Format",
                SamlConfiguration.NAME_ID_FORMAT
        );

        nameId.setTextContent(
                user.getUsername()
        );

        subject.appendChild(
                nameId
        );

        Element confirmation =
                document.createElementNS(
                        SAML_ASSERTION_NAMESPACE,
                        "saml:SubjectConfirmation"
                );

        confirmation.setAttribute(
                "Method",
                "urn:oasis:names:tc:SAML:2.0:cm:bearer"
        );

        Element confirmationData =
                document.createElementNS(
                        SAML_ASSERTION_NAMESPACE,
                        "saml:SubjectConfirmationData"
                );

        confirmationData.setAttribute(
                "InResponseTo",
                requestId
        );

        confirmationData.setAttribute(
                "Recipient",
                SamlConfiguration.SP_ACS_URL
        );

        confirmationData.setAttribute(
                "NotOnOrAfter",
                notOnOrAfter.toString()
        );

        confirmation.appendChild(
                confirmationData
        );

        subject.appendChild(
                confirmation
        );

        assertion.appendChild(
                subject
        );

        /*
         * Conditions
         */
        Element conditions =
                document.createElementNS(
                        SAML_ASSERTION_NAMESPACE,
                        "saml:Conditions"
                );

        conditions.setAttribute(
                "NotBefore",
                now.minusSeconds(30).toString()
        );

        conditions.setAttribute(
                "NotOnOrAfter",
                notOnOrAfter.toString()
        );

        Element audienceRestriction =
                document.createElementNS(
                        SAML_ASSERTION_NAMESPACE,
                        "saml:AudienceRestriction"
                );

        Element audience =
                createElement(
                        document,
                        SAML_ASSERTION_NAMESPACE,
                        "saml:Audience",
                        SamlConfiguration.SP_ENTITY_ID
                );

        audienceRestriction.appendChild(
                audience
        );

        conditions.appendChild(
                audienceRestriction
        );

        assertion.appendChild(
                conditions
        );

        /*
         * Authentication statement
         */
        Element authnStatement =
                document.createElementNS(
                        SAML_ASSERTION_NAMESPACE,
                        "saml:AuthnStatement"
                );

        authnStatement.setAttribute(
                "AuthnInstant",
                now.toString()
        );

        authnStatement.setAttribute(
                "SessionIndex",
                "_" + UUID.randomUUID()
        );

        Element authnContext =
                document.createElementNS(
                        SAML_ASSERTION_NAMESPACE,
                        "saml:AuthnContext"
                );

        Element authnContextClassRef =
                createElement(
                        document,
                        SAML_ASSERTION_NAMESPACE,
                        "saml:AuthnContextClassRef",
                        SamlConfiguration.AUTHN_CONTEXT
                );

        authnContext.appendChild(
                authnContextClassRef
        );

        authnStatement.appendChild(
                authnContext
        );

        assertion.appendChild(
                authnStatement
        );

        /*
         * Attributes
         */
        Element attributeStatement =
                document.createElementNS(
                        SAML_ASSERTION_NAMESPACE,
                        "saml:AttributeStatement"
                );

        appendAttribute(
                document,
                attributeStatement,
                "username",
                user.getUsername()
        );

        appendAttribute(
                document,
                attributeStatement,
                "role",
                user.getRole()
        );

        appendAttribute(
                document,
                attributeStatement,
                "userId",
                String.valueOf(
                        user.getUserId()
                )
        );

        assertion.appendChild(
                attributeStatement
        );

        /*
         * Sign the Assertion.
         */
        signAssertion(
                document,
                assertion,
                keyMaterial
        );

        return documentToString(
                document
        );
    }

    public void sendResponseToAcs(
            HttpServletResponse response,
            String samlResponse,
            String relayState)
            throws Exception {

        if (samlResponse == null
                || samlResponse.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "SAML response is empty"
            );
        }

        String encodedResponse =
                Base64.getEncoder().encodeToString(
                        samlResponse.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

        response.setContentType(
                "text/html"
        );

        response.setCharacterEncoding(
                "UTF-8"
        );

        response.setHeader(
                "Cache-Control",
                "no-store"
        );

        StringBuilder html =
                new StringBuilder();

        html.append("<!DOCTYPE html>");
        html.append("<html>");
        html.append("<head>");
        html.append("<meta charset=\"UTF-8\">");
        html.append(
                "<title>SAML Authentication</title>"
        );
        html.append("</head>");
        html.append("<body>");
        html.append(
                "<p>Completing enterprise authentication...</p>"
        );

        html.append(
                "<form id=\"samlForm\" method=\"post\" action=\""
        );

        html.append(
                escapeHtml(
                        SamlConfiguration.SP_ACS_URL
                )
        );

        html.append("\">");

        html.append(
                "<input type=\"hidden\" name=\"SAMLResponse\" value=\""
        );

        html.append(
                escapeHtml(encodedResponse)
        );

        html.append("\">");

        if (relayState != null
                && !relayState.trim().isEmpty()) {

            html.append(
                    "<input type=\"hidden\" name=\"RelayState\" value=\""
            );

            html.append(
                    escapeHtml(relayState)
            );

            html.append("\">");
        }

        html.append("</form>");

        html.append(
                "<script>document.getElementById('samlForm').submit();</script>"
        );

        html.append("</body>");
        html.append("</html>");

        response.getWriter().write(
                html.toString()
        );
    }

    private void signAssertion(
            Document document,
            Element assertion,
            KeyMaterial keyMaterial)
            throws Exception {

        XMLSignatureFactory factory =
                XMLSignatureFactory.getInstance(
                        "DOM"
                );

        /*
         * Make the SAML Assertion namespace explicit.
         *
         * This prevents namespace-context changes when the
         * signed XML is serialized and parsed again by ACS.
         */
        assertion.setAttributeNS(
                "http://www.w3.org/2000/xmlns/",
                "xmlns:saml",
                SAML_ASSERTION_NAMESPACE
        );

        /*
         * Register Assertion ID as a real DOM ID.
         */
        assertion.setIdAttributeNS(
                null,
                "ID",
                true
        );

        /*
         * The signature references the Assertion:
         *
         * #<Assertion ID>
         *
         * Only the enveloped transform is required here.
         * It removes the Signature element before calculating
         * the SHA-256 digest.
         */
        Reference reference =
                factory.newReference(
                        "#" + assertion.getAttribute("ID"),
                        factory.newDigestMethod(
                                DigestMethod.SHA256,
                                null
                        ),
                        Collections.singletonList(
                                factory.newTransform(
                                        Transform.ENVELOPED,
                                        (javax.xml.crypto.dsig.spec.TransformParameterSpec) null
                                )
                        ),
                        null,
                        null
                );

        /*
         * SignedInfo.
         */
        SignedInfo signedInfo =
                factory.newSignedInfo(
                        factory.newCanonicalizationMethod(
                                CanonicalizationMethod.EXCLUSIVE,
                                (javax.xml.crypto.dsig.spec.C14NMethodParameterSpec) null
                        ),
                        factory.newSignatureMethod(
                                SignatureMethod.RSA_SHA256,
                                null
                        ),
                        Collections.singletonList(
                                reference
                        )
                );

        /*
         * Add the IdP X.509 certificate to KeyInfo.
         */
        KeyInfoFactory keyInfoFactory =
                factory.getKeyInfoFactory();

        KeyInfo keyInfo =
                keyInfoFactory.newKeyInfo(
                        Collections.singletonList(
                                keyInfoFactory.newX509Data(
                                        Collections.singletonList(
                                                keyMaterial.certificate
                                        )
                                )
                        )
                );

        /*
         * Create XML Signature.
         */
        XMLSignature signature =
                factory.newXMLSignature(
                        signedInfo,
                        keyInfo
                );

        /*
         * Find Subject.
         *
         * Signature is inserted after Issuer and
         * before Subject.
         */
        Element subject =
                (Element) assertion.getElementsByTagNameNS(
                        SAML_ASSERTION_NAMESPACE,
                        "Subject"
                ).item(0);

        if (subject == null) {

            throw new IllegalStateException(
                    "SAML Subject element is missing"
            );
        }

        /*
         * Create signing context.
         */
        DOMSignContext signContext =
                new DOMSignContext(
                        keyMaterial.privateKey,
                        assertion,
                        subject
                );

        /*
         * Use ds prefix.
         */
        signContext.setDefaultNamespacePrefix(
                "ds"
        );

        /*
         * Register Assertion ID in signing context.
         */
        signContext.setIdAttributeNS(
                assertion,
                null,
                "ID"
        );

        /*
         * Generate RSA-SHA256 XML signature.
         */
        signature.sign(
                signContext
        );
    }
    private KeyMaterial loadKeyMaterial()
            throws Exception {

        String password =
                System.getProperty(
                        KEYSTORE_PASSWORD_PROPERTY
                );

        if (password == null
                || password.trim().isEmpty()) {

            password =
                    System.getenv(
                            KEYSTORE_PASSWORD_PROPERTY
                    );
        }

        if (password == null
                || password.isEmpty()) {

            throw new IllegalStateException(
                    "SAML keystore password is not configured. "
                    + "Set system property or environment variable "
                    + KEYSTORE_PASSWORD_PROPERTY
            );
        }

        try (InputStream inputStream =
                     servletContext.getResourceAsStream(
                             KEYSTORE_PATH
                     )) {

            if (inputStream == null) {

                throw new IllegalStateException(
                        "SAML keystore not found: "
                        + KEYSTORE_PATH
                );
            }

            KeyStore keyStore =
                    KeyStore.getInstance(
                            KEYSTORE_TYPE
                    );

            keyStore.load(
                    inputStream,
                    password.toCharArray()
            );

            PrivateKey privateKey =
                    (PrivateKey) keyStore.getKey(
                            KEY_ALIAS,
                            password.toCharArray()
                    );

            if (privateKey == null) {

                throw new IllegalStateException(
                        "SAML private key not found for alias: "
                        + KEY_ALIAS
                );
            }

            X509Certificate certificate =
                    (X509Certificate) keyStore.getCertificate(
                            KEY_ALIAS
                    );

            if (certificate == null) {

                throw new IllegalStateException(
                        "SAML certificate not found for alias: "
                        + KEY_ALIAS
                );
            }

            return new KeyMaterial(
                    privateKey,
                    certificate
            );
        }
    }

    private Document createDocument()
            throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(
                true
        );

        factory.setFeature(
                XMLConstants.FEATURE_SECURE_PROCESSING,
                true
        );

        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false
        );

        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false
        );

        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false
        );

        factory.setXIncludeAware(
                false
        );

        factory.setExpandEntityReferences(
                false
        );

        return factory
                .newDocumentBuilder()
                .newDocument();
    }

    private Element createElement(
            Document document,
            String namespace,
            String qualifiedName,
            String value) {

        Element element =
                document.createElementNS(
                        namespace,
                        qualifiedName
                );

        element.setTextContent(
                value
        );

        return element;
    }

    private void appendAttribute(
            Document document,
            Element attributeStatement,
            String name,
            String value) {

        Element attribute =
                document.createElementNS(
                        SAML_ASSERTION_NAMESPACE,
                        "saml:Attribute"
                );

        attribute.setAttribute(
                "Name",
                name
        );

        Element attributeValue =
                document.createElementNS(
                        SAML_ASSERTION_NAMESPACE,
                        "saml:AttributeValue"
                );

        attributeValue.setTextContent(
                value
        );

        attribute.appendChild(
                attributeValue
        );

        attributeStatement.appendChild(
                attribute
        );
    }

    private String documentToString(
            Document document)
            throws Exception {

        TransformerFactory factory =
                TransformerFactory.newInstance();

        Transformer transformer =
                factory.newTransformer();

        transformer.setOutputProperty(
                OutputKeys.OMIT_XML_DECLARATION,
                "yes"
        );

        transformer.setOutputProperty(
                OutputKeys.ENCODING,
                "UTF-8"
        );

        transformer.setOutputProperty(
                OutputKeys.INDENT,
                "no"
        );

        StringWriter writer =
                new StringWriter();

        transformer.transform(
                new DOMSource(document),
                new StreamResult(writer)
        );

        return writer.toString();
    }

    private String escapeHtml(
            String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static class KeyMaterial {

        private final PrivateKey privateKey;

        private final X509Certificate certificate;

        private KeyMaterial(
                PrivateKey privateKey,
                X509Certificate certificate) {

            this.privateKey = privateKey;
            this.certificate = certificate;
        }
    }
}