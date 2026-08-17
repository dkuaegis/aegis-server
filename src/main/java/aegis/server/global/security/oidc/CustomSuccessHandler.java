package aegis.server.global.security.oidc;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import static aegis.server.global.security.oidc.RefererFilter.REDIRECT_URI_SESSION_ATTRIBUTE;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final AllowedClientUrlValidator allowedClientUrlValidator;

    @Value("${oauth2-login.default-redirect-uri}")
    private String defaultRedirectUri;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        HttpSession httpSession = request.getSession(false);
        String redirectUri = null;
        if (httpSession != null) {
            Object savedRedirectUri = httpSession.getAttribute(REDIRECT_URI_SESSION_ATTRIBUTE);
            if (savedRedirectUri instanceof String value) {
                redirectUri = value;
            }
            httpSession.removeAttribute(REDIRECT_URI_SESSION_ATTRIBUTE);
        }

        String targetUrl = allowedClientUrlValidator.isAllowed(redirectUri) ? redirectUri : defaultRedirectUri;
        if (!allowedClientUrlValidator.isAllowed(targetUrl)) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        clearAuthenticationAttributes(request);
        response.sendRedirect(targetUrl);
    }
}
