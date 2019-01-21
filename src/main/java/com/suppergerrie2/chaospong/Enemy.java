package com.suppergerrie2.chaospong;

public class Enemy extends Paddle {

    public Enemy(Pong instance) {
        super(instance.width - 20, instance.height / 2,  instance);
        this.speed = 5;
    }

    @Override
    public void update() {
        if(instance.ball.getY()<this.getY()) {
            this.move(Pong.Direction.UP);
        } else if (instance.ball.getY()>this.getY()) {
            this.move(Pong.Direction.DOWN);
        }

        super.update();
    }

}
