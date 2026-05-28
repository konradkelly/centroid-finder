package io.github.konradkelly.centroidfinder;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class StaticFileConfig implements WebMvcConfigurer {
    private final ServerPathsProperties paths;

    public StaticFileConfig(ServerPathsProperties paths) {
        this.paths = paths;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/videos/**")
            .addResourceLocations(paths.videosDir().toUri().toString());

        registry.addResourceHandler("/results/**")
            .addResourceLocations(paths.resultsDir().toUri().toString());
    }
}