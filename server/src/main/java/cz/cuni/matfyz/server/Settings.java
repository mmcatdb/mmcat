package cz.cuni.matfyz.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author jachym.bartik
 */
@Configuration
class Settings {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                    // 3000 is the default port of the vite development server.
                    .allowedOrigins("http://localhost:3000", "http://localhost")
                    .allowCredentials(true);
            }
        };
    }
}