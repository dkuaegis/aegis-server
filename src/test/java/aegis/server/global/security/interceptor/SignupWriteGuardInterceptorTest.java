package aegis.server.global.security.interceptor;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.junit.jupiter.api.Test;

import aegis.server.domain.featureflag.service.FeaturePolicyService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SignupWriteGuardInterceptorTest {

    private final FeaturePolicyService featurePolicyService = mock(FeaturePolicyService.class);
    private final SignupWriteGuardInterceptor interceptor = new SignupWriteGuardInterceptor(featurePolicyService);

    @Test
    void 가입_쓰기_차단_플래그가_false면_요청을_차단한다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/members");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(featurePolicyService.isSignupWriteAllowed()).thenReturn(false);

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    void 가입_쓰기_차단_플래그가_true면_요청을_허용한다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/payments");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(featurePolicyService.isSignupWriteAllowed()).thenReturn(true);

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertTrue(result);
    }

    @Test
    void get_요청은_검사하지_않는다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/payments");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertTrue(result);
        verifyNoInteractions(featurePolicyService);
    }
}
