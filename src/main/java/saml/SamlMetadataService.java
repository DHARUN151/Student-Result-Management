package saml;

import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class SamlMetadataService {

    private static final String IDP_METADATA =
            "/WEB-INF/saml/idp-metadata.xml";

    private static final String SP_METADATA =
            "/WEB-INF/saml/sp-metadata.xml";

    private final ServletContext servletContext;

    public SamlMetadataService(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public String getIdpMetadata() throws IOException {
        return readMetadata(IDP_METADATA);
    }

    public String getSpMetadata() throws IOException {
        return readMetadata(SP_METADATA);
    }

    private String readMetadata(String resourcePath) throws IOException {

        InputStream inputStream =
                servletContext.getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new IOException(
                    "SAML metadata file not found: " + resourcePath
            );
        }

        try (InputStream stream = inputStream) {
            return new String(
                    stream.readAllBytes(),
                    StandardCharsets.UTF_8
            );
        }
    }
}