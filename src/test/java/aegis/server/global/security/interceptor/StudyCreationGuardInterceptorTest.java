package aegis.server.global.security.interceptor;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.junit.jupiter.api.Test;

import aegis.server.domain.featureflag.service.FeaturePolicyService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudyCreationGuardInterceptorTest {

    private final FeaturePolicyService featurePolicyService = mock(FeaturePolicyService.class);
    private final StudyCreationGuardInterceptor interceptor = new StudyCreationGuardInterceptor(featurePolicyService);

    @Test
    void 스터디_개설_플래그가_false면_요청을_차단한다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/studies");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(featurePolicyService.isStudyCreationAllowed()).thenReturn(false);

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    void 스터디_개설_플래그가_true면_요청을_허용한다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/studies");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(featurePolicyService.isStudyCreationAllowed()).thenReturn(true);

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertTrue(result);
    }

    @Test
    void post_외_요청은_검사하지_않는다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/studies");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertTrue(result);
        verifyNoInteractions(featurePolicyService);
    }
}
