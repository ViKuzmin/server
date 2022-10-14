package com.example.server.actors;


import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

public class Emitter implements Json.Serializable {

    private String id;

    private float angle;
    private float x;
    private float y;
    private int speed = 300;
    private float damge = 10;

    @Override
    public void write(Json json) {

    }

    @Override
    public void read(Json json, JsonValue jsonValue) {

    }


}
