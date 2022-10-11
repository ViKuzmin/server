package com.example.server.config;

import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.example.server.GameLoop;
import com.example.server.actors.Soldier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public HeadlessApplication getApplication(GameLoop gameloop) {

        return new HeadlessApplication(gameloop);
    }

    @Bean
    public Json getJson() {

        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        json.addClassTag("Soldier", Soldier.class);
        return json;
    }

}
