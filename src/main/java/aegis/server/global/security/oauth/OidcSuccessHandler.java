package aegis.server.global.security.oauth;

import aegis.server.global.security.dto.SessionUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OidcSuccessHandler implements AuthenticationSuccessHandler {

    private final HttpSession httpSession;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        CustomOidcUser oidcUser = (CustomOidcUser) authentication.getPrincipal();
        SessionUser sessionUser = SessionUser.from(oidcUser.getUserAuthInfo());
        httpSession.setAttribute("user", sessionUser);
        response.sendRedirect("/user-info");
    }
}
