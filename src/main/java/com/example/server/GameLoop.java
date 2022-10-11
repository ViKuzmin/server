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

    private final static float frameRate = 1 / 2f;
    private final WebSocketHandler webSocketHandler;
    private final Array<String> event = new Array<>();
    private final Json json;
    private final ObjectMap<String, Soldier> soldiers = new ObjectMap<>();
    private final ForkJoinPool pool = ForkJoinPool.commonPool();
    private float lastRender = 0;

    public GameLoop(WebSocketHandler webSocketHandler, Json json) {
        this.webSocketHandler = webSocketHandler;
        this.json = json;
    }

    @Override
    public void create() {

        webSocketHandler.setConnectListener(session -> {
            event.add(session.getId() + " just joined");
            Soldier soldier = new Soldier();
            soldier.setId(session.getId());
            soldiers.put(session.getId(), soldier);
            try {
                session.getNativeSession().getBasicRemote().sendText(session.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        webSocketHandler.setDisconnectListener(session -> {
            event.add(session.getId() + " just disconnected");
            soldiers.remove(session.getId());
        });

        webSocketHandler.setMessageListener((session, message) -> {
            pool.execute(() -> {
                String type = message.getString("type");

                switch (type) {
                    case "state":
                        Soldier soldier = soldiers.get(session.getId());
                        soldier.setLeftPressed(message.getBoolean("leftPressed"));
                        soldier.setRightPressed(message.getBoolean("rightPressed"));
                        soldier.setUpPressed(message.getBoolean("upPressed"));
                        soldier.setDownPressed(message.getBoolean("downPressed"));
                        soldier.setAngle(message.getFloat("angle"));

                        break;
                    default:
                        throw new RuntimeException("Unknown WS object type: " + type);
                }

            });
            event.add(session.getId() + " said " + message);
        });
    }

    @Override
    public void render() {
        lastRender += Gdx.graphics.getDeltaTime();
        if (lastRender >= frameRate) {

            for (ObjectMap.Entry<String, Soldier> soldier : soldiers) {
                Soldier soldat = soldier.value;
                soldat.act(lastRender);
            }
            lastRender = 0;

            pool.execute(() -> {
                String stateJson = json.toJson(soldiers);

                for (StandardWebSocketSession session : webSocketHandler.getSessions()) {
                    try {
                        session.getNativeSession().getBasicRemote().sendText(stateJson);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
