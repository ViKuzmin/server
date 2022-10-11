package com.example.server.ws;


import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

@Component
public class WebSocketHandler extends AbstractWebSocketHandler {

    private final JsonReader reader = new JsonReader();
    private final Array<StandardWebSocketSession> sessions = new Array<>();
    private ConnectListener connectListener;
    private DisconnectListener disconnectListener;
    private MessageListener messageListener;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        StandardWebSocketSession webSession = (StandardWebSocketSession) session;
        synchronized (sessions) {
            sessions.add(webSession);
            connectListener.handle(webSession);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        StandardWebSocketSession webSession = (StandardWebSocketSession) session;
        String payload = message.getPayload();
        JsonValue jsonValue = reader.parse(payload);

        messageListener.handle(webSession, jsonValue);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        StandardWebSocketSession webSession = (StandardWebSocketSession) session;
        synchronized (sessions) {
            sessions.removeValue(webSession, true);
            disconnectListener.handle(webSession);
        }
    }

    public Array<StandardWebSocketSession> getSessions() {
        return sessions;
    }

    public void setConnectListener(ConnectListener connectListener) {
        this.connectListener = connectListener;
    }

    public void setDisconnectListener(DisconnectListener disconnectListener) {
        this.disconnectListener = disconnectListener;
    }

    public void setMessageListener(MessageListener messageListener) {
        this.messageListener = messageListener;
    }
}
