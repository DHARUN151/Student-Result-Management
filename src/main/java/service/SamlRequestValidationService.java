package service;

import saml.SamlConfiguration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class SamlRequestValidationService {

    private static final String SAML_PROTOCOL_NAMESPACE =
            "urn:oasis:names:tc:SAML:2.0:protocol";

    private static final String SAML_ASSERTION_NAMESPACE =
            "urn:oasis:names:tc:SAML:2.0:assertion";

    public SamlRequestData validateRedirectRequest(
            String encodedRequest)
            throws Exception {

        if (encodedRequest == null
                || encodedRequest.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Missing SAMLRequest"
            );
        }

        String xml =
                decodeRedirectRequest(encodedRequest);

        Document document =
                parseXml(xml);

        Element root =
                document.getDocumentElement();

        if (root == null) {
            throw new IllegalArgumentException(
                    "Empty SAML request"
            );
        }

        if (!"AuthnRequest".equals(root.getLocalName())
                || !SAML_PROTOCOL_NAMESPACE.equals(
                        root.getNamespaceURI())) {

            throw new IllegalArgumentException(
                    "Invalid SAML AuthnRequest"
            );
        }

        String requestId =
                root.getAttribute("ID");

        String version =
                root.getAttribute("Version");

        String issueInstant =
                root.getAttribute("IssueInstant");

        String destination =
                root.getAttribute("Destination");

        String acsUrl =
                root.getAttribute(
                        "AssertionConsumerServiceURL"
                );

        if (requestId == null
                || requestId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "SAML AuthnRequest ID is missing"
            );
        }

        if (!"2.0".equals(version)) {

            throw new IllegalArgumentException(
                    "Unsupported SAML version"
            );
        }

        validateIssueInstant(issueInstant);

        if (!SamlConfiguration.IDP_SSO_URL.equals(
                destination)) {

            throw new IllegalArgumentException(
                    "Invalid SAML destination"
            );
        }

        if (!SamlConfiguration.SP_ACS_URL.equals(
                acsUrl)) {

            throw new IllegalArgumentException(
                    "Invalid Assertion Consumer Service URL"
            );
        }

        String issuer =
                getIssuer(document);

        if (!SamlConfiguration.SP_ENTITY_ID.equals(
                issuer)) {

            throw new IllegalArgumentException(
                    "Unknown SAML Service Provider"
            );
        }

        return new SamlRequestData(
                requestId,
                issuer,
                acsUrl,
                issueInstant
        );
    }

    private String decodeRedirectRequest(
            String encodedRequest)
            throws Exception {

        byte[] compressed =
                Base64.getDecoder().decode(
                        encodedRequest
                );

        Inflater inflater =
                new Inflater(true);

        try (
                ByteArrayInputStream input =
                        new ByteArrayInputStream(
                                compressed
                        );

                InflaterInputStream inflaterInput =
                        new InflaterInputStream(
                                input,
                                inflater
                        );

                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            byte[] buffer =
                    new byte[1024];

            int count;

            while ((count =
                    inflaterInput.read(buffer)) != -1) {

                output.write(
                        buffer,
                        0,
                        count
                );
            }

            return output.toString(
                    StandardCharsets.UTF_8
            );
        }
    }

    private Document parseXml(String xml)
            throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);

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

        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        return factory
                .newDocumentBuilder()
                .parse(
                        new ByteArrayInputStream(
                                xml.getBytes(
                                        StandardCharsets.UTF_8
                                )
                        )
                );
    }

    private String getIssuer(
            Document document) {

        NodeList issuers =
                document.getElementsByTagNameNS(
                        SAML_ASSERTION_NAMESPACE,
                        "Issuer"
                );

        if (issuers.getLength() == 0) {
            return null;
        }

        return issuers
                .item(0)
                .getTextContent()
                .trim();
    }

    private void validateIssueInstant(
            String issueInstant) {

        if (issueInstant == null
                || issueInstant.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "SAML IssueInstant is missing"
            );
        }

        Instant issuedAt;

        try {
            issuedAt =
                    Instant.parse(
                            issueInstant
                    );
        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Invalid SAML IssueInstant"
            );
        }

        Instant now =
                Instant.now();

        Duration age =
                Duration.between(
                        issuedAt,
                        now
                );

        if (age.getSeconds()
                > SamlConfiguration.REQUEST_VALIDITY_SECONDS) {

            throw new IllegalArgumentException(
                    "SAML request has expired"
            );
        }

        if (issuedAt.isAfter(
                now.plusSeconds(60))) {

            throw new IllegalArgumentException(
                    "SAML request IssueInstant is in the future"
            );
        }
    }

    public static class SamlRequestData {

        private final String requestId;
        private final String issuer;
        private final String acsUrl;
        private final String issueInstant;

        public SamlRequestData(
                String requestId,
                String issuer,
                String acsUrl,
                String issueInstant) {

            this.requestId = requestId;
            this.issuer = issuer;
            this.acsUrl = acsUrl;
            this.issueInstant = issueInstant;
        }

        public String getRequestId() {
            return requestId;
        }

        public String getIssuer() {
            return issuer;
        }

        public String getAcsUrl() {
            return acsUrl;
        }

        public String getIssueInstant() {
            return issueInstant;
        }
    }
}