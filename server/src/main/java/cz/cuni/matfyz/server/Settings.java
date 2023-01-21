package cz.cuni.matfyz.server;

import cz.cuni.matfyz.server.configuration.ServerProperties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author jachym.bartik
 */
@Configuration
class Settings implements WebMvcConfigurer {

    @Autowired
    private ServerProperties server;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry
                    .addMapping("/**")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedOrigins(server.origin())
                    .allowCredentials(true);
            }
        };
    }
    
}