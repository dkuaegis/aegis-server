package aegis.server.global.security.interceptor;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.junit.jupiter.api.Test;

import aegis.server.domain.featureflag.service.FeaturePolicyService;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SignupGuardInterceptorTest {

    private final FeaturePolicyService featurePolicyService = mock(FeaturePolicyService.class);
    private final SignupGuardInterceptor interceptor = new SignupGuardInterceptor(featurePolicyService);

    @Test
    void 회원가입_허용_플래그가_false면_요청을_차단한다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/members");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(featurePolicyService.isSignupAllowed()).thenReturn(false);

        // when & then
        CustomException exception =
                assertThrows(CustomException.class, () -> interceptor.preHandle(request, response, new Object()));
        assertEquals(ErrorCode.MEMBER_SIGNUP_CLOSED, exception.getErrorCode());
    }

    @Test
    void 회원가입_허용_플래그가_true면_요청을_허용한다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/payments");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(featurePolicyService.isSignupAllowed()).thenReturn(true);

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
