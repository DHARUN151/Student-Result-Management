package servlet;

import dao.UserDAO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import model.User;
import saml.SamlConfiguration;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMValidateContext;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Base64;

@WebServlet("/saml/acs")
public class SamlAcsServlet
        extends HttpServlet {

    private static final long serialVersionUID = 1L;

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

    private UserDAO userDAO;

    @Override
    public void init()
            throws ServletException {

        userDAO =
                new UserDAO();
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, java.io.IOException {

        HttpSession session =
                request.getSession(false);

        try {

            if (!isValidPendingSession(session)) {

                fail(
                        request,
                        response,
                        "SAML authentication session is invalid"
                );

                return;
            }

            String encodedResponse =
                    request.getParameter(
                            "SAMLResponse"
                    );

            String relayState =
                    request.getParameter(
                            "RelayState"
                    );

            if (encodedResponse == null
                    || encodedResponse.trim().isEmpty()) {

                fail(
                        request,
                        response,
                        "SAMLResponse is missing"
                );

                return;
            }

            String expectedRelayState =
                    (String) session.getAttribute(
                            "SAML_RELAY_STATE"
                    );

            if (expectedRelayState != null
                    && !expectedRelayState.equals(
                            relayState
                    )) {

                fail(
                        request,
                        response,
                        "Invalid RelayState"
                );

                return;
            }

            byte[] responseBytes =
                    Base64.getDecoder().decode(
                            encodedResponse
                    );

            Document document =
                    parseXml(
                            responseBytes
                    );

            Element root =
                    document.getDocumentElement();

            validateResponseElement(
                    root
            );

            String requestId =
                    (String) session.getAttribute(
                            "SAML_REQUEST_ID"
                    );

            validateResponseAttributes(
                    root,
                    requestId
            );

            Element assertion =
                    getSingleAssertion(
                            document
                    );

            validateAssertionSignature(
                    assertion
            );

            validateAssertion(
                    assertion,
                    requestId
            );

            String username =
                    extractNameId(
                            assertion
                    );

            if (username == null
                    || username.trim().isEmpty()) {

                throw new SecurityException(
                        "SAML NameID is missing"
                );
            }

            /*
             * Never trust the role from the SAML
             * assertion.
             *
             * Read the current role from DB.
             */
            User user =
                    userDAO.findUserByUsername(
                            username
                    );

            if (user == null) {

                throw new SecurityException(
                        "SAML user does not exist"
                );
            }

            if (!"ACTIVE".equals(
                    user.getAccountStatus())) {

                throw new SecurityException(
                        "SAML user account is not active"
                );
            }

            if (!isPrivilegedRole(
                    user.getRole())) {

                throw new SecurityException(
                        "SAML role is not permitted"
                );
            }

            Integer pendingUserId =
                    (Integer) session.getAttribute(
                            "SAML_PENDING_USER_ID"
                    );

            if (pendingUserId == null
                    || pendingUserId != user.getUserId()) {

                throw new SecurityException(
                        "SAML user mismatch"
                );
            }

            /*
             * Session fixation protection.
             */
            request.changeSessionId();

            /*
             * Now authentication is complete.
             *
             * Create the normal application session.
             */
            session.setAttribute(
                    "userId",
                    user.getUserId()
            );

            session.setAttribute(
                    "username",
                    user.getUsername()
            );

            session.setAttribute(
                    "role",
                    user.getRole()
            );

            session.setAttribute(
                    "studentId",
                    user.getStudentId()
            );

            session.setAttribute(
                    "SAML_AUTHENTICATED",
                    true
            );

            /*
             * Remove temporary SAML transaction data.
             */
            clearSamlAttributes(
                    session
            );

            /*
             * Send the user to the existing dashboard.
             */
            redirectToDashboard(
                    request,
                    response,
                    user.getRole()
            );

        } catch (Exception e) {

            e.printStackTrace();

            if (session != null) {
                session.invalidate();
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/login.jsp?error=saml"
            );
        }
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, java.io.IOException {

        response.sendError(
                HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "SAML ACS requires HTTP POST"
        );
    }

    private boolean isValidPendingSession(
            HttpSession session) {

        return session != null
                && Boolean.TRUE.equals(
                        session.getAttribute(
                                "SAML_FLOW_ACTIVE"
                        )
                )
                && session.getAttribute(
                        "SAML_REQUEST_ID"
                ) != null
                && session.getAttribute(
                        "SAML_PENDING_USER_ID"
                ) != null
                && session.getAttribute(
                        "SAML_PENDING_USERNAME"
                ) != null;
    }

    private Document parseXml(
            byte[] data)
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

        try (InputStream inputStream =
                     new ByteArrayInputStream(data)) {

            return factory
                    .newDocumentBuilder()
                    .parse(inputStream);
        }
    }

    private void validateResponseElement(
            Element root) {

        if (root == null) {
            throw new SecurityException(
                    "SAML Response is empty"
            );
        }

        if (!"Response".equals(
                root.getLocalName())) {

            throw new SecurityException(
                    "Invalid SAML Response element"
            );
        }

        if (!SAML_PROTOCOL_NAMESPACE.equals(
                root.getNamespaceURI())) {

            throw new SecurityException(
                    "Invalid SAML protocol namespace"
            );
        }

        if (!"2.0".equals(
                root.getAttribute("Version"))) {

            throw new SecurityException(
                    "Unsupported SAML version"
            );
        }
    }

    private void validateResponseAttributes(
            Element response,
            String expectedRequestId) {

        String issuer =
                getIssuer(
                        response
                );

        if (!SamlConfiguration.IDP_ENTITY_ID.equals(
                issuer)) {

            throw new SecurityException(
                    "Invalid SAML issuer"
            );
        }

        String destination =
                response.getAttribute(
                        "Destination"
                );

        if (!SamlConfiguration.SP_ACS_URL.equals(
                destination)) {

            throw new SecurityException(
                    "Invalid SAML destination"
            );
        }

        String inResponseTo =
                response.getAttribute(
                        "InResponseTo"
                );

        if (expectedRequestId == null
                || !expectedRequestId.equals(
                        inResponseTo
                )) {

            throw new SecurityException(
                    "Invalid SAML InResponseTo"
            );
        }

        String issueInstant =
                response.getAttribute(
                        "IssueInstant"
                );

        validateInstant(
                issueInstant
        );
    }

    private Element getSingleAssertion(
            Document document) {

        NodeList assertions =
                document.getElementsByTagNameNS(
                        SAML_ASSERTION_NAMESPACE,
                        "Assertion"
                );

        if (assertions.getLength() != 1) {

            throw new SecurityException(
                    "SAML Response must contain exactly one Assertion"
            );
        }

        Node node =
                assertions.item(0);

        if (!(node instanceof Element)) {

            throw new SecurityException(
                    "Invalid SAML Assertion"
            );
        }

        return (Element) node;
    }

    private void validateAssertionSignature(
            Element assertion)
            throws Exception {

        /*
         * Find XML Signature.
         */
        NodeList signatures =
                assertion.getElementsByTagNameNS(
                        XMLDSIG_NAMESPACE,
                        "Signature"
                );

        if (signatures.getLength() != 1) {

            throw new SecurityException(
                    "SAML Assertion must contain exactly one signature"
            );
        }

        /*
         * Read Assertion ID.
         */
        String assertionId =
                assertion.getAttribute(
                        "ID"
                );

        if (assertionId == null
                || assertionId.trim().isEmpty()) {

            throw new SecurityException(
                    "SAML Assertion ID is missing"
            );
        }

        /*
         * Make the Assertion namespace explicit.
         *
         * This must match the namespace used when
         * the Assertion was signed.
         */
        assertion.setAttributeNS(
                "http://www.w3.org/2000/xmlns/",
                "xmlns:saml",
                "urn:oasis:names:tc:SAML:2.0:assertion"
        );

        /*
         * Register Assertion ID as DOM ID.
         */
        assertion.setIdAttributeNS(
                null,
                "ID",
                true
        );

        /*
         * Get Signature element.
         */
        Element signatureElement =
                (Element) signatures.item(0);

        /*
         * Load trusted IdP certificate.
         */
        X509Certificate certificate =
                loadCertificate();

        PublicKey publicKey =
                certificate.getPublicKey();

        /*
         * Create XML signature validation context.
         */
        DOMValidateContext validateContext =
                new DOMValidateContext(
                        publicKey,
                        signatureElement
                );

        /*
         * Register Assertion ID.
         */
        validateContext.setIdAttributeNS(
                assertion,
                null,
                "ID"
        );

        /*
         * Enable secure XML signature validation.
         */
        validateContext.setProperty(
                "org.jcp.xml.dsig.secureValidation",
                Boolean.TRUE
        );

        /*
         * Create XML Signature factory.
         */
        XMLSignatureFactory factory =
                XMLSignatureFactory.getInstance(
                        "DOM"
                );

        /*
         * Unmarshal the signature.
         */
        XMLSignature signature =
                factory.unmarshalXMLSignature(
                        validateContext
                );

        /*
         * Exactly one reference is expected.
         */
        if (signature.getSignedInfo()
                .getReferences()
                .size() != 1) {

            throw new SecurityException(
                    "Unexpected number of SAML signature references"
            );
        }

        /*
         * Get reference.
         */
        javax.xml.crypto.dsig.Reference reference =
                (javax.xml.crypto.dsig.Reference)
                        signature.getSignedInfo()
                                .getReferences()
                                .get(0);

        /*
         * The reference MUST point to this Assertion.
         */
        String expectedUri =
                "#" + assertionId;

        if (!expectedUri.equals(
                reference.getURI()
        )) {

            throw new SecurityException(
                    "SAML signature does not reference the Assertion"
            );
        }

        /*
         * Check signature value.
         */
        boolean signatureValueValid =
                signature.getSignatureValue()
                        .validate(
                                validateContext
                        );

        /*
         * Check reference digest.
         */
        boolean referenceValid =
                reference.validate(
                        validateContext
                );

        /*
         * Both checks must pass.
         */
        if (!signatureValueValid
                || !referenceValid) {

            throw new SecurityException(
                    "Invalid SAML XML signature. "
                    + "signatureValueValid="
                    + signatureValueValid
                    + ", referenceValid="
                    + referenceValid
            );
        }
    }
    private void validateAssertion(
            Element assertion,
            String requestId) {

        String issuer =
                getIssuer(
                        assertion
                );

        if (!SamlConfiguration.IDP_ENTITY_ID.equals(
                issuer)) {

            throw new SecurityException(
                    "Invalid Assertion issuer"
            );
        }

        String assertionId =
                assertion.getAttribute(
                        "ID"
                );

        if (assertionId == null
                || assertionId.trim().isEmpty()) {

            throw new SecurityException(
                    "Assertion ID is missing"
            );
        }

        validateAssertionConditions(
                assertion
        );

        validateSubjectConfirmation(
                assertion,
                requestId
        );

        validateAudience(
                assertion
        );
    }

    private void validateAssertionConditions(
            Element assertion) {

        NodeList conditionsList =
                assertion.getElementsByTagNameNS(
                        SAML_ASSERTION_NAMESPACE,
                        "Conditions"
                );

        if (conditionsList.getLength() != 1) {

            throw new SecurityException(
                    "Invalid SAML Conditions"
            );
        }

        Element conditions =
                (Element) conditionsList.item(0);

        String notBefore =
                conditions.getAttribute(
                        "NotBefore"
                );

        String notOnOrAfter =
                conditions.getAttribute(
                        "NotOnOrAfter"
                );

        Instant now =
                Instant.now();

        if (notBefore == null
                || notBefore.trim().isEmpty()
                || notOnOrAfter == null
                || notOnOrAfter.trim().isEmpty()) {

            throw new SecurityException(
                    "SAML Conditions time values are missing"
            );
        }

        Instant validFrom =
                Instant.parse(
                        notBefore
                );

        Instant validUntil =
                Instant.parse(
                        notOnOrAfter
                );

        if (now.isBefore(
                validFrom.minusSeconds(30)
        )) {

            throw new SecurityException(
                    "SAML assertion is not yet valid"
            );
        }

        if (!now.isBefore(
                validUntil
        )) {

            throw new SecurityException(
                    "SAML assertion has expired"
            );
        }
    }

    private void validateSubjectConfirmation(
            Element assertion,
            String requestId) {

        NodeList confirmationDataList =
                assertion.getElementsByTagNameNS(
                        SAML_ASSERTION_NAMESPACE,
                        "SubjectConfirmationData"
                );

        if (confirmationDataList.getLength() != 1) {

            throw new SecurityException(
                    "Invalid SAML SubjectConfirmationData"
            );
        }

        Element confirmationData =
                (Element) confirmationDataList.item(0);

        String inResponseTo =
                confirmationData.getAttribute(
                        "InResponseTo"
                );

        if (!requestId.equals(
                inResponseTo
        )) {

            throw new SecurityException(
                    "Invalid SubjectConfirmation InResponseTo"
            );
        }

        String recipient =
                confirmationData.getAttribute(
                        "Recipient"
                );

        if (!SamlConfiguration.SP_ACS_URL.equals(
                recipient
        )) {

            throw new SecurityException(
                    "Invalid SAML recipient"
            );
        }

        String notOnOrAfter =
                confirmationData.getAttribute(
                        "NotOnOrAfter"
                );

        if (notOnOrAfter == null
                || notOnOrAfter.trim().isEmpty()) {

            throw new SecurityException(
                    "SubjectConfirmation expiry is missing"
            );
        }

        Instant expiry =
                Instant.parse(
                        notOnOrAfter
                );

        if (!Instant.now().isBefore(
                expiry
        )) {

            throw new SecurityException(
                    "SAML SubjectConfirmation has expired"
            );
        }
    }

    private void validateAudience(
            Element assertion) {

        NodeList audiences =
                assertion.getElementsByTagNameNS(
                        SAML_ASSERTION_NAMESPACE,
                        "Audience"
                );

        if (audiences.getLength() != 1) {

            throw new SecurityException(
                    "Invalid SAML Audience"
            );
        }

        String audience =
                audiences
                        .item(0)
                        .getTextContent()
                        .trim();

        if (!SamlConfiguration.SP_ENTITY_ID.equals(
                audience
        )) {

            throw new SecurityException(
                    "Invalid SAML audience"
            );
        }
    }

    private String extractNameId(
            Element assertion) {

        NodeList nameIds =
                assertion.getElementsByTagNameNS(
                        SAML_ASSERTION_NAMESPACE,
                        "NameID"
                );

        if (nameIds.getLength() != 1) {
            return null;
        }

        return nameIds
                .item(0)
                .getTextContent()
                .trim();
    }

    private String getIssuer(
            Element parent) {

        NodeList issuers =
                parent.getElementsByTagNameNS(
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

    private void validateInstant(
            String value) {

        if (value == null
                || value.trim().isEmpty()) {

            throw new SecurityException(
                    "SAML IssueInstant is missing"
            );
        }

        Instant instant;

        try {

            instant =
                    Instant.parse(
                            value
                    );

        } catch (Exception e) {

            throw new SecurityException(
                    "Invalid SAML IssueInstant",
                    e
            );
        }

        Instant now =
                Instant.now();

        if (instant.isAfter(
                now.plusSeconds(60)
        )) {

            throw new SecurityException(
                    "SAML IssueInstant is in the future"
            );
        }

        if (instant.isBefore(
                now.minusSeconds(
                        SamlConfiguration
                                .ASSERTION_VALIDITY_SECONDS
                )
        )) {

            throw new SecurityException(
                    "SAML IssueInstant has expired"
            );
        }
    }

    private X509Certificate loadCertificate()
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
                    "SAML keystore password is not configured"
            );
        }

        try (InputStream inputStream =
                     getServletContext()
                             .getResourceAsStream(
                                     KEYSTORE_PATH
                             )) {

            if (inputStream == null) {

                throw new IllegalStateException(
                        "SAML keystore not found"
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

            X509Certificate certificate =
                    (X509Certificate)
                            keyStore.getCertificate(
                                    KEY_ALIAS
                            );

            if (certificate == null) {

                throw new IllegalStateException(
                        "SAML certificate not found"
                );
            }

            return certificate;
        }
    }

    private boolean isPrivilegedRole(
            String role) {

        return "ADMIN".equals(role)
                || "SUPER_ADMIN".equals(role)
                || "FACULTY".equals(role)
                || "HOD".equals(role)
                || "EXAM_CELL".equals(role);
    }

    private void redirectToDashboard(
            HttpServletRequest request,
            HttpServletResponse response,
            String role)
            throws java.io.IOException {

        String contextPath =
                request.getContextPath();

        if ("FACULTY".equals(role)) {

            response.sendRedirect(
                    contextPath
                    + "/FacultyDashboardServlet"
            );

        } else if ("HOD".equals(role)) {

            response.sendRedirect(
                    contextPath
                    + "/hodDashboard.jsp"
            );

        } else if ("EXAM_CELL".equals(role)) {

            response.sendRedirect(
                    contextPath
                    + "/ExamCellDashboardServlet"
            );

        } else if ("ADMIN".equals(role)
                || "SUPER_ADMIN".equals(role)) {

            response.sendRedirect(
                    contextPath
                    + "/adminDashboard.jsp"
            );

        } else {

            response.sendRedirect(
                    contextPath
                    + "/login.jsp?error=role"
            );
        }
    }

    private void clearSamlAttributes(
            HttpSession session) {

        session.removeAttribute(
                "SAML_FLOW_ACTIVE"
        );

        session.removeAttribute(
                "SAML_REQUEST_ID"
        );

        session.removeAttribute(
                "SAML_RELAY_STATE"
        );

        session.removeAttribute(
                "SAML_AUTHN_REQUEST_TIME"
        );

        session.removeAttribute(
                "SAML_SP_ENTITY_ID"
        );

        session.removeAttribute(
                "SAML_ACS_URL"
        );

        session.removeAttribute(
                "SAML_REQUEST_ISSUE_INSTANT"
        );

        session.removeAttribute(
                "SAML_PENDING_USER_ID"
        );

        session.removeAttribute(
                "SAML_PENDING_USERNAME"
        );

        session.removeAttribute(
                "SAML_PENDING_ROLE"
        );
    }

    private void fail(
            HttpServletRequest request,
            HttpServletResponse response,
            String message)
            throws java.io.IOException {

        HttpSession session =
                request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(
                request.getContextPath()
                + "/login.jsp?error=saml"
        );
    }

}