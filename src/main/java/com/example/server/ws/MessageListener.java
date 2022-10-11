package com.example.server.ws;


import com.badlogic.gdx.utils.JsonValue;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

public interface MessageListener {

    void handle(StandardWebSocketSession session, JsonValue message);
}
