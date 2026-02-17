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
public class SignupWriteGuardInterceptor implements HandlerInterceptor {

    private final FeaturePolicyService featurePolicyService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!isSignupWriteMethod(request.getMethod())) {
            return true;
        }

        if (featurePolicyService.isSignupWriteAllowed()) {
            return true;
        }

        response.setStatus(HttpStatus.FORBIDDEN.value());
        return false;
    }

    private boolean isSignupWriteMethod(String method) {
        return "POST".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method);
    }
}
