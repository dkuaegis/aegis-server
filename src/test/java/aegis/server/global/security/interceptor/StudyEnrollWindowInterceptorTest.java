package aegis.server.global.security.interceptor;

import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import org.junit.jupiter.api.Test;

import aegis.server.domain.featureflag.service.FeaturePolicyService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StudyEnrollWindowInterceptorTest {

    private final FeaturePolicyService featurePolicyService = mock(FeaturePolicyService.class);
    private final StudyEnrollWindowInterceptor interceptor = new StudyEnrollWindowInterceptor(featurePolicyService);

    @Test
    void post_요청이_기간_외면_차단한다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/studies/1/enrollment");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(featurePolicyService.isStudyEnrollmentAllowed()).thenReturn(false);

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertFalse(result);
        assertEquals(403, response.getStatus());
    }

    @Test
    void post_요청이_기간_내면_허용한다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/studies/1/enrollment");
        MockHttpServletResponse response = new MockHttpServletResponse();
        when(featurePolicyService.isStudyEnrollmentAllowed()).thenReturn(true);

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertTrue(result);
    }

    @Test
    void post_외_요청은_검사하지_않는다() {
        // given
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/studies/1/enrollment");
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        boolean result = interceptor.preHandle(request, response, new Object());

        // then
        assertTrue(result);
        verifyNoInteractions(featurePolicyService);
    }
}
