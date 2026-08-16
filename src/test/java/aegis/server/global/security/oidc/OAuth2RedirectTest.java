package aegis.server.global.security.oidc;

import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class OAuth2RedirectTest {

    private final AllowedClientUrlValidator validator = new AllowedClientUrlValidator();

    @Test
    void 허용된_클라이언트의_경로를_포함한_URL을_허용한다() {
        assertThat(validator.isAllowed("https://study.dkuaegis.org/studies/1?tab=notice#content"))
                .isTrue();
    }

    @Test
    void 호스트가_같아도_스킴이나_포트가_다르면_거부한다() {
        assertThat(validator.isAllowed("http://study.dkuaegis.org/studies/1")).isFalse();
        assertThat(validator.isAllowed("https://study.dkuaegis.org:444/studies/1"))
                .isFalse();
        assertThat(validator.isAllowed("https://attacker.example@study.dkuaegis.org/studies/1"))
                .isFalse();
    }

    @Test
    void 로그인_요청의_허용된_Referer를_세션에_저장한다() throws Exception {
        RefererFilter filter = new RefererFilter(validator);
        MockHttpServletRequest request = authorizationRequest();
        request.addHeader("Referer", "https://mypage.dkuaegis.org/profile");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(request.getSession().getAttribute(RefererFilter.REDIRECT_URI_SESSION_ATTRIBUTE))
                .isEqualTo("https://mypage.dkuaegis.org/profile");
        assertThat(filterChain.getRequest()).isSameAs(request);
    }

    @Test
    void Referer가_없는_새_로그인_요청은_이전_리다이렉트_주소를_제거한다() throws Exception {
        RefererFilter filter = new RefererFilter(validator);
        MockHttpServletRequest request = authorizationRequest();
        request.getSession()
                .setAttribute(RefererFilter.REDIRECT_URI_SESSION_ATTRIBUTE, "https://study.dkuaegis.org/old");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(request.getSession().getAttribute(RefererFilter.REDIRECT_URI_SESSION_ATTRIBUTE))
                .isNull();
    }

    @Test
    void 허용되지_않은_Referer의_로그인_요청을_거부한다() throws Exception {
        RefererFilter filter = new RefererFilter(validator);
        MockHttpServletRequest request = authorizationRequest();
        request.addHeader("Referer", "https://attacker.example/login");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        filter.doFilter(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(400);
        assertThat(filterChain.getRequest()).isNull();
    }

    @Test
    void 저장된_리다이렉트_주소가_없으면_기본_클라이언트로_이동한다() throws Exception {
        CustomSuccessHandler successHandler = createSuccessHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(request, response, mock(Authentication.class));

        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173");
    }

    @Test
    void 저장된_리다이렉트_주소가_허용되면_해당_경로로_이동한다() throws Exception {
        CustomSuccessHandler successHandler = createSuccessHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession()
                .setAttribute(RefererFilter.REDIRECT_URI_SESSION_ATTRIBUTE, "https://study.dkuaegis.org/studies/1");
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(request, response, mock(Authentication.class));

        assertThat(response.getRedirectedUrl()).isEqualTo("https://study.dkuaegis.org/studies/1");
    }

    @Test
    void 세션의_리다이렉트_주소도_다시_검증한다() throws Exception {
        CustomSuccessHandler successHandler = createSuccessHandler();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession()
                .setAttribute(RefererFilter.REDIRECT_URI_SESSION_ATTRIBUTE, "https://attacker.example/login");
        MockHttpServletResponse response = new MockHttpServletResponse();

        successHandler.onAuthenticationSuccess(request, response, mock(Authentication.class));

        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173");
        assertThat(request.getSession().getAttribute(RefererFilter.REDIRECT_URI_SESSION_ATTRIBUTE))
                .isNull();
    }

    private MockHttpServletRequest authorizationRequest() {
        return new MockHttpServletRequest("GET", "/oauth2/authorization/google");
    }

    private CustomSuccessHandler createSuccessHandler() {
        CustomSuccessHandler successHandler = new CustomSuccessHandler(validator);
        ReflectionTestUtils.setField(successHandler, "defaultRedirectUri", "http://localhost:5173");
        return successHandler;
    }
}
