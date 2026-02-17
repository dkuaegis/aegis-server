package aegis.server.global.config;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.RequiredArgsConstructor;

import aegis.server.global.security.annotation.LoginUserArgumentResolver;
import aegis.server.global.security.interceptor.SignupGuardInterceptor;
import aegis.server.global.security.interceptor.StudyCreationGuardInterceptor;
import aegis.server.global.security.interceptor.StudyEnrollWindowInterceptor;
import aegis.server.global.security.interceptor.TransactionTrackInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoginUserArgumentResolver loginUserArgumentResolver;
    private final TransactionTrackInterceptor transactionTrackInterceptor;
    private final StudyEnrollWindowInterceptor studyEnrollWindowInterceptor;
    private final SignupGuardInterceptor signupGuardInterceptor;
    private final StudyCreationGuardInterceptor studyCreationGuardInterceptor;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(loginUserArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(transactionTrackInterceptor).addPathPatterns("/internal/transaction");
        registry.addInterceptor(studyEnrollWindowInterceptor).addPathPatterns("/studies/*/enrollment");
        registry.addInterceptor(studyCreationGuardInterceptor).addPathPatterns("/studies");
        registry.addInterceptor(signupGuardInterceptor).addPathPatterns("/members/**", "/survey/**", "/payments/**");
    }
}
