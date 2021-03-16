package uk.gov.hmcts.reform.divorce.caseapi.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.hmcts.reform.divorce.caseapi.config.interceptors.RequestInterceptor;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RequestInterceptor requestInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(requestInterceptor)
            .excludePathPatterns(
                "/health",
                "/webjars/springfox-swagger-ui/**",
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/v2/**",
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
