package com.example.server;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap;
import com.example.server.actors.Soldier;
import com.example.server.ws.WebSocketHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.adapter.standard.StandardWebSocketSession;

import java.io.IOException;
import java.util.concurrent.ForkJoinPool;

@Component
public class GameLoop extends ApplicationAdapter {

    private final static float frameRate = 1 / 60f;
    private final WebSocketHandler webSocketHandler;
    private final Json json;

    private final ForkJoinPool pool = ForkJoinPool.commonPool();
    private final ObjectMap<String, Soldier> soldiers = new ObjectMap<>();
    private final Array<Soldier> stateToSend = new Array<>();

    private float lastRender = 0;

    public GameLoop(WebSocketHandler webSocketHandler, Json json) {
        this.webSocketHandler = webSocketHandler;
        this.json = json;
    }

    @Override
    public void create() {

        webSocketHandler.setConnectListener(session -> {
            Soldier soldier = new Soldier();
            soldier.setId(session.getId());
            soldiers.put(session.getId(), soldier);
            try {
                session
                        .getNativeSession()
                        .getBasicRemote()
                        .sendText(
                                String.format("{\"class\":\"sessionKey\",\"id\":\"%s\"}", session.getId())
                        );
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        webSocketHandler.setDisconnectListener(session -> {
            sendToEverybody(
                    String.format("{\"class\":\"evict\",\"id\":\"%s\"}", session.getId())
            );
            soldiers.remove(session.getId());
        });

        webSocketHandler.setMessageListener((session, message) -> {
            pool.execute(() -> {
                String type = message.get("type").asText();

                switch (type) {
                    case "state":
                        Soldier soldier = soldiers.get(session.getId());
                        soldier.setLeftPressed(message.get("leftPressed").asBoolean());
                        soldier.setRightPressed(message.get("rightPressed").asBoolean());
                        soldier.setUpPressed(message.get("upPressed").asBoolean());
                        soldier.setDownPressed(message.get("downPressed").asBoolean());
                        soldier.setAngle((float) message.get("angle").asDouble());

                        break;
                    default:
                        throw new RuntimeException("Unknown WS object type: " + type);
                }

            });
        });
    }

    @Override
    public void render() {
        lastRender += Gdx.graphics.getDeltaTime();
        if (lastRender >= frameRate) {

            stateToSend.clear();
            for (ObjectMap.Entry<String, Soldier> soldier : soldiers) {
                Soldier soldat = soldier.value;
                soldat.act(lastRender);
                stateToSend.add(soldat);
            }
            lastRender = 0;
            String stateJson = json.toJson(stateToSend);

            sendToEverybody(stateJson);
        }
    }

    private void sendToEverybody(String json) {
        pool.execute(() -> {
            for (StandardWebSocketSession session : webSocketHandler.getSessions()) {
                try {
                    if (session.isOpen()) {
                        session.getNativeSession().getBasicRemote().sendText(json);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
