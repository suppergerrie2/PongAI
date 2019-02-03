package com.suppergerrie2.chaospong;

import com.suppergerrie2.ChaosNetClient.components.Organism;
import com.suppergerrie2.ChaosNetClient.components.TrainingRoom;
import com.suppergerrie2.ChaosNetClient.components.TrainingRoomStats;
import com.suppergerrie2.ChaosNetClient.components.nnet.neurons.InputNeuron;
import com.suppergerrie2.ChaosNetClient.components.nnet.neurons.OutputNeuron;
import com.suppergerrie2.chaospong.chaosnet.PongOrganism;

import javax.rmi.CORBA.Util;
import java.awt.*;

public class Pong {

    enum Direction {UP, STILL, DOWN}

    static final int PADDLE_WIDTH = 10;
    static final int PADDLE_HEIGHT = 50;
    static final int BALL_SIZE = 10;

    public final Paddle player;
    final Paddle enemy;
    public final Ball ball;

    public final int width;
    public final int height;

    private Direction playerMove = Direction.STILL;

    final PongOrganism organism;

    public Pong(PongOrganism organism, int width, int height) {
        this.width = width;
        this.height = height;
        this.organism = organism;

        enemy = new Enemy(this);
        player = new Paddle(20, height / 2, this);
        ball = new Ball(this);
    }

    public void update() {
        OutputNeuron[] outputs = organism.evaluate();

        OutputNeuron move = Utils.oneHot(outputs);

        switch (move.getType()) {
            case "MoveUp":
                player.move(Direction.UP);
                break;
            case "StandStill":
                player.move(Direction.STILL);
                break;
            case "MoveDown":
                player.move(Direction.DOWN);
                break;
        }

        player.update();
        enemy.update();
        ball.update();

        TrainingRoom room = Main.session.getTrainingRoom();
        if (room.getScoreEffect("X_BALL_DISTANCE") != 0) {
            organism.increaseScore(room.getScoreEffect("X_BALL_DISTANCE") / Math.abs(player.x - ball.x));
        }

        if (room.getScoreEffect("Y_BALL_DISTANCE") != 0) {
//            System.out.println(room.getScoreEffect("Y_BALL_DISTANCE"));
            organism.increaseScore(room.getScoreEffect("Y_BALL_DISTANCE") / Math.abs(player.x - ball.x));
        }
    }

    public void draw(Graphics g) {
        g.setColor(Color.WHITE);
        g.drawRect(player.getX() - PADDLE_WIDTH / 2, player.getY() - PADDLE_HEIGHT / 2, PADDLE_WIDTH, PADDLE_HEIGHT);
        g.drawRect(enemy.getX() - PADDLE_WIDTH / 2, enemy.getY() - PADDLE_HEIGHT / 2, PADDLE_WIDTH, PADDLE_HEIGHT);
        g.drawOval((int) ball.getX() - BALL_SIZE / 2, (int) ball.getY() - BALL_SIZE / 2, BALL_SIZE, BALL_SIZE);

        g.drawLine((int) ball.getX(), (int) ball.getY(), (int) (ball.getX() + ball.getXVell() * 200), (int) (ball.getY() + ball.getYVell() * 200));

        Utils.drawHorizontallyCenteredString(player.points + " - " + enemy.points, width, 30, g);
        Utils.drawHorizontallyCenteredString(organism.getName() + ", gen: " + organism.getGeneration(), width, 50, g);

        g.drawString("Score: " + organism.getScore(), 10, 40);
        TrainingRoom room = Main.session.getTrainingRoom();
        if(room.getStats()!=null) {
            TrainingRoomStats stats = room.getStats();
            g.drawString(stats.getGenerationProgress() + "%", 10, 20);
        }
    }

    public void reset(boolean playerWon) {
        TrainingRoom room = Main.session.getTrainingRoom();
        if (playerWon) {
            player.points++;
            organism.increaseScore(room.getScoreEffect("PLAYER_POINT_EARNED"));
        } else {
            enemy.points++;
            organism.increaseScore(room.getScoreEffect("ENEMY_POINT_EARNED"));
        }

        player.reset();
        enemy.reset();
        ball.reset();
    }


    public int getTotalPlayCount() {
        return player.points + enemy.points;
    }
}
