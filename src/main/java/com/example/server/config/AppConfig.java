package com.example.server.config;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.example.server.GameLoop;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public HeadlessApplication getApplication(GameLoop gameloop) {

        return new HeadlessApplication(gameloop);
    }
}
