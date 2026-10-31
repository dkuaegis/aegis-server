package aegis.server.global.security;

import tools.jackson.databind.json.JsonMapper;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityErrorResponseHandlerTest {

    private final SecurityErrorResponseHandler handler =
            new SecurityErrorResponseHandler(JsonMapper.builder().build());

    @Test
    void 인증되지_않은_요청에_UNAUTHORIZED_본문으로_응답한다() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/mypage");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        handler.commence(request, response, new InsufficientAuthenticationException("로그인 필요"));

        // then
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).isEqualTo("{\"name\":\"UNAUTHORIZED\"}");
    }

    @Test
    void 권한이_부족한_요청에_FORBIDDEN_본문으로_응답한다() throws Exception {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/studies");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        handler.handle(request, response, new AccessDeniedException("정회원 아님"));

        // then
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).startsWith("application/json");
        assertThat(response.getContentAsString()).isEqualTo("{\"name\":\"FORBIDDEN\"}");
    }
}
