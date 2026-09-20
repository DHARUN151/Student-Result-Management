package servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import saml.SamlMetadataService;

import java.io.IOException;

@WebServlet(urlPatterns = {
        "/saml/idp/metadata",
        "/saml/sp/metadata"
})
public class SamlMetadataServlet extends HttpServlet {

    private SamlMetadataService metadataService;

    @Override
    public void init() throws ServletException {
        metadataService =
                new SamlMetadataService(
                        getServletContext()
                );
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        String path = request.getRequestURI()
                .substring(request.getContextPath().length());

        String metadata;

        if ("/saml/idp/metadata".equals(path)) {

            metadata = metadataService.getIdpMetadata();

        } else if ("/saml/sp/metadata".equals(path)) {

            metadata = metadataService.getSpMetadata();

        } else {

            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "SAML metadata endpoint not found"
            );

            return;
        }

        response.setContentType(
                "application/samlmetadata+xml"
        );

        response.setCharacterEncoding("UTF-8");

        response.setHeader(
                "Cache-Control",
                "no-cache"
        );

        response.getWriter().write(metadata);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.sendError(
                HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                "GET is required for SAML metadata"
        );
    }
}