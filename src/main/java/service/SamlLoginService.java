package service;

import saml.SamlConfiguration;

import java.time.Instant;
import java.util.UUID;

public class SamlLoginService {

    public LoginContext createLoginContext(
            String username) {

        String requestId =
                "_" + UUID.randomUUID();

        String issueInstant =
                Instant.now().toString();

        return new LoginContext(
                requestId,
                username,
                SamlConfiguration.SP_ENTITY_ID,
                SamlConfiguration.SP_ACS_URL,
                issueInstant
        );
    }

    public static class LoginContext {

        private final String requestId;
        private final String username;
        private final String spEntityId;
        private final String acsUrl;
        private final String issueInstant;

        public LoginContext(
                String requestId,
                String username,
                String spEntityId,
                String acsUrl,
                String issueInstant) {

            this.requestId = requestId;
            this.username = username;
            this.spEntityId = spEntityId;
            this.acsUrl = acsUrl;
            this.issueInstant = issueInstant;
        }

        public String getRequestId() {
            return requestId;
        }

        public String getUsername() {
            return username;
        }

        public String getSpEntityId() {
            return spEntityId;
        }

        public String getAcsUrl() {
            return acsUrl;
        }

        public String getIssueInstant() {
            return issueInstant;
        }
    }
}