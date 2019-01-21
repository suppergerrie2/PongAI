package com.suppergerrie2.chaospong;

public class Paddle {

    final Pong instance;

    @SuppressWarnings("CanBeFinal")
    public int x;
    public int y;

    int yVell = 0;

    public int points = 0;

    int speed = 6;

    public Paddle(int x, int y, Pong instance) {
        this.instance = instance;
        this.x = x;
        this.y = y;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

    public void update() {
        this.y += this.yVell;

        if(this.y < Pong.PADDLE_HEIGHT/2) {
            this.y = Pong.PADDLE_HEIGHT/2;
        } else if(this.y > instance.height - Pong.PADDLE_HEIGHT/2) {
            this.y = instance.height - Pong.PADDLE_HEIGHT/2;
        }
    }

    public void move(Pong.Direction playerMove) {
        switch(playerMove) {
            case UP:
                this.yVell = -speed;
                break;
            case DOWN:
                this.yVell = speed;
                break;
            default:
                this.yVell = 0;
                break;
        }
    }

    public boolean ballCollides(Ball ball) {

        //Distance between ball center and bat center on x axis and y axis
        float xDistance = Math.abs(ball.getX() - this.x);
        float yDistance = Math.abs(ball.getY() - this.y);

        //If the ball if farther away than the distances we just calculated, we can be sure it will not intersect the bat
        if (xDistance > (Pong.PADDLE_WIDTH/2 + Pong.BALL_SIZE)) { return false; }
        if (yDistance > (Pong.PADDLE_HEIGHT/2 + Pong.BALL_SIZE)) { return false; }

        //If the distance is less than the width of the rect we can be sure it intersects
        if (xDistance <= (Pong.PADDLE_WIDTH/2)) { return true; }
        if (yDistance <= (Pong.PADDLE_HEIGHT/2)) { return true; }

        //Distance between the center of the circle and the center of the rectangle
        float cDist_sq = (float) (Math.pow(xDistance - Pong.PADDLE_WIDTH/2f,2) + Math.pow((yDistance - Pong.PADDLE_HEIGHT/2f),2));

        //If that is less than the radius we can return true
        return (cDist_sq <= (Pong.BALL_SIZE^2));
    }

    public void reset() {
        this.y = instance.width / 2;
    }

}
