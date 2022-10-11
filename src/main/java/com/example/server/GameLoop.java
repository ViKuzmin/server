package com.example.server;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.utils.Array;
import com.example.server.ws.WebSocketHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;

@Component
public class GameLoop extends ApplicationAdapter {

    private final WebSocketHandler webSocketHandler;
    private final Array<String> event = new Array<>();

    public GameLoop(WebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void create() {

        webSocketHandler.setConnectListener(session -> {
            event.add(session.getId() + " just joined");
        });
        webSocketHandler.setDisconnectListener(session -> {
            event.add(session.getId() + " just disconnected");
        });
        webSocketHandler.setMessageListener((session, message) -> {
            event.add(session.getId() + " said " + message);
        });
    }

    @Override
    public void render() {

        for (WebSocketSession session : webSocketHandler.getSessions()) {
            try {
                for (String message : event) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }
        event.clear();
    }
}
