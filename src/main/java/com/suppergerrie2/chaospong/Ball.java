package com.suppergerrie2.chaospong;

import java.util.Random;

public class Ball {


    private final Pong instance;

    public float x;
    public float y;
    private float xVell = 5;
    private float yVell = 5;
    private final Random random = new Random();

    boolean collisionLastTick = false;

    public Ball(Pong instance) {
        this.instance = instance;
        reset();
    }

    public void update() {
        if(this.y < 0) {
            this.y = 0;
            this.yVell *= -1;
        } else if(this.y > instance.height - Pong.BALL_SIZE) {
            this.y = instance.height - Pong.BALL_SIZE;
            this.yVell *= -1;
        }

        if(this.x < 0) {
            instance.reset(false);
            return;
        } else if(this.x > instance.width- Pong.BALL_SIZE) {
            instance.reset(true);
            return;
        }

        boolean collided = false;

        if(instance.player.ballCollides(this)) {
            this.xVell = Math.abs(this.xVell);
            this.yVell = 5 + instance.player.yVell/5f;
            if(instance.player.yVell!=0) this.yVell *= Math.signum(instance.player.yVell);

            this.xVell+=0.5;
            this.yVell+=0.5;

            collided = true;

            if(!collisionLastTick) {
                instance.organism.increaseScore(Main.session.getTrainingRoom().getScoreEffect("PLAYER_HIT_BALL"));
            }
        } else if (instance.enemy.ballCollides(this)) {
            this.xVell = -Math.abs(this.xVell);
            this.yVell = 5 + instance.enemy.yVell/5f;
            if(instance.enemy.yVell!=0)  this.yVell *= Math.signum(instance.enemy.yVell);

            collided = true;
        }

        collisionLastTick = collided;

        this.x += xVell;
        this.y += yVell;
    }

    float getX() {
        return x;
    }
    float getY() {
        return y;
    }

    public void reset() {
        this.x = instance.width / 2f;
        this.y = random.nextInt(instance.height);

        double angle = 40 + random.nextDouble()*20;

        if(random.nextBoolean()) {
            angle*=-1;
        }

        angle+=90;

        angle = Math.toRadians(angle);
        this.xVell = (float) (Math.sin(angle) * 5);
        this.yVell = (float) (Math.cos(angle) * 5);
    }

    public float getXVell() {
        return this.xVell;
    }

    public float getYVell() {
        return this.yVell;
    }

}
