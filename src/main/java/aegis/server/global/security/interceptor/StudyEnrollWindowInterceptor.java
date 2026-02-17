package aegis.server.global.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.featureflag.service.FeaturePolicyService;

@Component
@RequiredArgsConstructor
public class StudyEnrollWindowInterceptor implements HandlerInterceptor {

    private final FeaturePolicyService featurePolicyService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (featurePolicyService.isStudyEnrollmentAllowed()) {
            return true;
        }

        response.setStatus(HttpStatus.FORBIDDEN.value());
        return false;
    }
}
