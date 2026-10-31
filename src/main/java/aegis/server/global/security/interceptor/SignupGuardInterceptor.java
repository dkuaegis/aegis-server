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
public class SignupGuardInterceptor implements HandlerInterceptor {

    private final FeaturePolicyService featurePolicyService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!isSignupMutationMethod(request.getMethod())) {
            return true;
        }

        if (featurePolicyService.isSignupAllowed()) {
            return true;
        }

        throw new CustomException(ErrorCode.MEMBER_SIGNUP_CLOSED);
    }

    private boolean isSignupMutationMethod(String method) {
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method);
    }
}
