package uk.gov.hmcts.divorce.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.divorce.common.config.interceptors.RequestInterceptor;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final RequestInterceptor requestInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor)
            .excludePathPatterns(
                "/health",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/v3/**",
                "/favicon.ico",
                "/health",
                "/mappings",
                "/info",
                "/metrics",
                "/metrics/**",
                "/"
            );
    }
}
