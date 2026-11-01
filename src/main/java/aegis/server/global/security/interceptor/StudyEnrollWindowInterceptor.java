package aegis.server.global.security.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.RequiredArgsConstructor;

import aegis.server.domain.featureflag.service.FeaturePolicyService;
import aegis.server.global.exception.CustomException;
import aegis.server.global.exception.ErrorCode;

@Component
@RequiredArgsConstructor
public class StudyEnrollWindowInterceptor implements HandlerInterceptor {

    private final FeaturePolicyService featurePolicyService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 신청(POST)과 지원서 수정(PUT)만 기간을 검사하고, 조회는 기간과 무관하게 허용합니다.
        String method = request.getMethod();
        if (!"POST".equalsIgnoreCase(method) && !"PUT".equalsIgnoreCase(method)) {
            return true;
        }

        if (featurePolicyService.isStudyEnrollmentAllowed()) {
            return true;
        }

        throw new CustomException(ErrorCode.STUDY_ENROLLMENT_CLOSED);
    }
}
