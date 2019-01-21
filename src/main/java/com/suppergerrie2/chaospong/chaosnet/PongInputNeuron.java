package com.suppergerrie2.chaospong.chaosnet;

import com.google.gson.JsonObject;
import com.suppergerrie2.ChaosNetClient.components.nnet.neurons.AbstractNeuron;
import com.suppergerrie2.ChaosNetClient.components.nnet.neurons.InputNeuron;
import com.suppergerrie2.chaospong.Pong;

public class PongInputNeuron extends InputNeuron {

    public enum Type {BALL_X, BALL_Y, BALL_VEL_X, BALL_VEL_Y, PLAYER_Y}

    Type type;

    public PongInputNeuron(Type type) {
        this.type = type;
    }

    @Override
    public AbstractNeuron parseFromJson(JsonObject object) {
        return ((PongInputNeuron) super.parseFromJson(object)).setType(type);
    }

    PongInputNeuron setType(Type type) {
        this.type = type;
        return this;
    }

    @Override
    public double getValue() {
        PongOrganism organism = ((PongOrganism) getOwner());

        Pong pongInstance = organism.pongInstance;

        switch (type) {
            case BALL_X:
                return pongInstance.ball.x / (double) pongInstance.width;
            case BALL_Y:
                return pongInstance.ball.y / (double) pongInstance.height;
            case BALL_VEL_X:
                return pongInstance.ball.getXVell() / 6.0;
            case BALL_VEL_Y:
                return pongInstance.ball.getYVell() / 6.0;
            case PLAYER_Y:
                return pongInstance.player.y / (double) pongInstance.height;
        }

        return 0;
    }
}
