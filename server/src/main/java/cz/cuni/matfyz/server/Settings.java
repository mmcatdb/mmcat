package cz.cuni.matfyz.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 
 * @author jachym.bartik
 */
@Configuration
class Settings
{
    @Bean
    public WebMvcConfigurer corsConfigurer()
    {
        return new WebMvcConfigurer()
        {
            @Override
            public void addCorsMappings(CorsRegistry registry)
            {
                // TODO konfigurace portu pro clienta
                registry.addMapping("/**").allowedOrigins("http://localhost:3000");
            }
        };
    }
}