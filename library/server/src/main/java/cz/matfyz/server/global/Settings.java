package cz.matfyz.server.global;

import cz.matfyz.server.global.Configuration.ServerProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
class Settings implements WebMvcConfigurer {

    @Autowired
    private ServerProperties server;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override public void addCorsMappings(CorsRegistry registry) {
                registry
                    .addMapping("/**")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedOrigins(server.origin(), server.originOld())
                    .allowCredentials(true);
            }
        };
    }

    @Autowired
    private SessionRequestInterceptor sessionRequestInterceptor;

    @Override public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionRequestInterceptor);
    }

}
