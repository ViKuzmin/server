package com.example.server.ws;


import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

public interface DisconnectListener {

    void handle(StandardWebSocketSession session);
}
