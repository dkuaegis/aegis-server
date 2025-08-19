package aegis.server.global.security.oidc;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final HttpSession httpSession;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        CustomOidcUser customOidcUser = (CustomOidcUser) authentication.getPrincipal();
        UserDetails userDetails = customOidcUser.getUserDetails();

        httpSession.setAttribute("userDetails", userDetails);
        httpSession.setAttribute(
                FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME,
                userDetails.getMemberId().toString());

        String redirectUri = (String) httpSession.getAttribute("redirectUri");
        httpSession.removeAttribute("redirectUri");

        response.sendRedirect(redirectUri);
    }
}
