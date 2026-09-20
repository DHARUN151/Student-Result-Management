package saml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;
import java.util.zip.Deflater;

public class SamlAuthnRequestService {

    private static final String SAML_PROTOCOL_NAMESPACE =
            "urn:oasis:names:tc:SAML:2.0:protocol";

    private static final String SAML_ASSERTION_NAMESPACE =
            "urn:oasis:names:tc:SAML:2.0:assertion";

    public String generateRequestId() {
        return "_" + UUID.randomUUID();
    }

    public String generateRelayState() {
        return UUID.randomUUID().toString();
    }

    public String createAuthnRequest(String requestId) throws Exception {

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        factory.setNamespaceAware(true);

        Document document =
                factory.newDocumentBuilder().newDocument();

        Element authnRequest =
                document.createElementNS(
                        SAML_PROTOCOL_NAMESPACE,
                        "samlp:AuthnRequest"
                );

        authnRequest.setAttribute(
                "ID",
                requestId
        );

        authnRequest.setAttribute(
                "Version",
                SamlConfiguration.SAML_VERSION
        );

        authnRequest.setAttribute(
                "IssueInstant",
                Instant.now().toString()
        );

        authnRequest.setAttribute(
                "Destination",
                SamlConfiguration.IDP_SSO_URL
        );

        authnRequest.setAttribute(
                "ProtocolBinding",
                "urn:oasis:names:tc:SAML:2.0:bindings:HTTP-POST"
        );

        authnRequest.setAttribute(
                "AssertionConsumerServiceURL",
                SamlConfiguration.SP_ACS_URL
        );

        document.appendChild(authnRequest);

        Element issuer =
                document.createElementNS(
                        SAML_ASSERTION_NAMESPACE,
                        "saml:Issuer"
                );

        issuer.setTextContent(
                SamlConfiguration.SP_ENTITY_ID
        );

        authnRequest.appendChild(issuer);

        Element nameIdPolicy =
                document.createElementNS(
                        SAML_PROTOCOL_NAMESPACE,
                        "samlp:NameIDPolicy"
                );

        nameIdPolicy.setAttribute(
                "Format",
                SamlConfiguration.NAME_ID_FORMAT
        );

        nameIdPolicy.setAttribute(
                "AllowCreate",
                "true"
        );

        authnRequest.appendChild(nameIdPolicy);

        return documentToString(document);
    }

    public String deflateAndEncode(String xml)
            throws Exception {

        byte[] input =
                xml.getBytes(StandardCharsets.UTF_8);

        Deflater deflater =
                new Deflater(
                        Deflater.DEFAULT_COMPRESSION,
                        true
                );

        deflater.setInput(input);
        deflater.finish();

        byte[] buffer =
                new byte[input.length + 512];

        int length =
                deflater.deflate(buffer);

        deflater.end();

        byte[] compressed =
                new byte[length];

        System.arraycopy(
                buffer,
                0,
                compressed,
                0,
                length
        );

        return Base64.getEncoder()
                .encodeToString(compressed);
    }

    public String urlEncode(String value) {

        return java.net.URLEncoder
                .encode(
                        value,
                        StandardCharsets.UTF_8
                );
    }

    private String documentToString(
            Document document)
            throws Exception {

        TransformerFactory transformerFactory =
                TransformerFactory.newInstance();

        Transformer transformer =
                transformerFactory.newTransformer();

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
}