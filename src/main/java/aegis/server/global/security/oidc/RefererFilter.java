package aegis.server.global.security.oidc;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RefererFilter extends OncePerRequestFilter {

    static final String REDIRECT_URI_SESSION_ATTRIBUTE = "redirectUri";

    private final RequestMatcher authorizationRequestMatcher =
            PathPatternRequestMatcher.withDefaults().matcher("/oauth2/authorization/**");
    private final AllowedClientUrlValidator allowedClientUrlValidator;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (authorizationRequestMatcher.matches(request)) {
            HttpSession currentSession = request.getSession(false);
            if (currentSession != null) {
                currentSession.removeAttribute(REDIRECT_URI_SESSION_ATTRIBUTE);
            }

            String referer = request.getHeader(HttpHeaders.REFERER);
            if (referer != null && !referer.isBlank()) {
                if (!allowedClientUrlValidator.isAllowed(referer)) {
                    response.setStatus(HttpStatus.BAD_REQUEST.value());
                    return;
                }
                request.getSession().setAttribute(REDIRECT_URI_SESSION_ATTRIBUTE, referer);
            }
        }
        filterChain.doFilter(request, response);
    }
}
