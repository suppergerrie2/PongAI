package com.suppergerrie2.chaospong;

import com.suppergerrie2.ChaosNetClient.ChaosNetClient;
import com.suppergerrie2.ChaosNetClient.components.Organism;
import com.suppergerrie2.ChaosNetClient.components.Session;
import com.suppergerrie2.ChaosNetClient.components.TrainingRoom;
import com.suppergerrie2.ChaosNetClient.components.nnet.neurons.OutputNeuron;
import com.suppergerrie2.chaospong.chaosnet.PongInputNeuron;
import com.suppergerrie2.chaospong.chaosnet.PongOrganism;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class Main extends Canvas implements Runnable, KeyListener {

    private static final long serialVersionUID = 1L;
    private static JFrame frame;
    private Thread thread;
    private boolean running = false;

    public static int width = 800;
    @SuppressWarnings("SuspiciousNameCombination")
    public static int height = width;

    ChaosNetClient client = new ChaosNetClient();

    PongOrganism bestTested = null;

    public static void main(String[] args) throws IOException {

        //Create the frame and the main instance
        Main main = new Main();

        //Make the frame not resizable and set the titel
        frame.setResizable(false);
        frame.setTitle("ChaosPong");

        //Make main be able to draw on the frame
        frame.add(main);
        frame.pack();

        //By default we hide it, this is so we can run some code before the program stops
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        //Center it in the window and set it visible
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        //Add a listener to call main.stop when the frame is closed
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                main.stop();
            }
        });

        //Start!
        main.start();
    }

    public static Session session;

    /**
     * The main class is the main controller for everything.
     * From here the update and render methods are called.
     */
    private Main() throws IOException {
        client.registerCustomOrganismType(new PongOrganism());
        client.registerNeuronType("BallPositionX", new PongInputNeuron(PongInputNeuron.Type.BALL_X));
        client.registerNeuronType("BallPositionY", new PongInputNeuron(PongInputNeuron.Type.BALL_Y));
        client.registerNeuronType("BallVelocityX", new PongInputNeuron(PongInputNeuron.Type.BALL_VEL_X));
        client.registerNeuronType("BallVelocityY", new PongInputNeuron(PongInputNeuron.Type.BALL_VEL_Y));
        client.registerNeuronType("PlayerPositionY", new PongInputNeuron(PongInputNeuron.Type.PLAYER_Y));

        client.registerNeuronType("MoveUp", new OutputNeuron());
        client.registerNeuronType("StandStill", new OutputNeuron());
        client.registerNeuronType("MoveDown", new OutputNeuron());

        client.authenticate(Utils.getUsername(), Utils.getPassword(), true);
        TrainingRoom trainingRoom = client.getTrainingRoom("suppergerrie2", "supper-pong");
        session = client.startSession(trainingRoom);
        System.out.println(session.getNamespace());

        //Make sure the frame is the right size and request focus so we can listen to keyboard input
        setPreferredSize(new Dimension(width, height));
        this.setFocusable(true);
        frame = new JFrame();
        //Both Main and the game have to know when the user presses a key.
        this.addKeyListener(this);
    }

    /**
     * Starts the game thread
     */
    private synchronized void start() {
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    /**
     * Save the game and makes everything stop.
     */
    private synchronized void stop() {
        running = false;

        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    //Updates Per Second. Should be 60 in a normal run but can be higher when superspeed is enabled
    private int ups = 0;
    //Frames Per Second. Can be as high as the cpu/gpu can do
    private int fps = 0;
    //Makes the updates not wait but run whenever possible
    private boolean superSpeed = false;

    boolean showBest = false;

    Pong pong = null;

    @Override
    public void run() {
        //lastTime we did the while loop
        long lastTime = System.nanoTime();
        //How long 1 update should take
        double updateTime = 1000000000.0 / 60;
        //When this is 1 we need another update, if this is 0.5 we need to wait 0.5*updateTime
        double delta = 0;

        //LastTime since the ups and fps update
        long lastTimeMS = System.currentTimeMillis();

        //Count how often we have updated and rendered
        int upsCounter = 0;
        int fpsCounter = 0;

        while (running) {
            long now = System.nanoTime();

            //Reset delta to 20 if it gets to big, makes sure we dont forget to render if the update is too slow
            if (delta > 20) delta = 20;

            //If we have superspeed enabled we set delta to 1, else we calculate the fraction of updateTime we have waited
            if (!superSpeed) {
                delta += (now - lastTime) / updateTime;
            } else {
                delta = 1;
            }

            lastTime = now;

            //As long as delta is >= to 1 we need to update
            while (delta >= 1) {
                upsCounter++;
                update();
                delta--;
            }

            fpsCounter++;
            render();

            //Every second we update the fps and ups.
            if (System.currentTimeMillis() - lastTimeMS >= 1000) {
                lastTimeMS = System.currentTimeMillis();
                fps = fpsCounter;
                ups = upsCounter;
                upsCounter = 0;
                fpsCounter = 0;
            }
        }
    }

    Queue<Organism> organisms = new LinkedList<>();
    List<Organism> testedOrganisms = new ArrayList<>();

    /**
     * Update the game and run the train method from the AI
     */
    private void update() {

        if (!showBest || bestTested == null) {

            if (pong != null && pong.organism == bestTested) {
                pong = null;
            }

            if (pong == null || pong.getTotalPlayCount() > 25) {
                System.out.println(organisms.size());
                if (pong != null) {
                    testedOrganisms.add(pong.organism);
                    if (bestTested == null || pong.organism.getScore() > bestTested.getScore()) {
                        bestTested = pong.organism;
                    }
                }

                if (organisms.size() == 0) {
                    Organism[] organisms;
                    if (testedOrganisms.size() > 0) {
                        System.out.println(testedOrganisms.size());
                        organisms = client.getOrganisms(session, testedOrganisms.toArray(new Organism[0]));
                        testedOrganisms.clear();
                    } else {
                        organisms = client.getOrganisms(session);
                    }

                    System.out.println(organisms.length);

                    this.organisms.addAll(Arrays.asList(organisms));
                }

                Organism organism = organisms.poll();

                if (organism != null) {
                    System.out.println(organism.getNamespace());
                    pong = new Pong((PongOrganism) organism, width, height);
                    ((PongOrganism) organism).pongInstance = pong;
                }
            } else {
                pong.update();
            }

        } else {

            if (pong.organism != bestTested) {
                organisms.add(pong.organism);
                pong.organism.increaseScore(-pong.organism.getScore());

                pong = new Pong(bestTested, width, height);
                bestTested.pongInstance = pong;
                System.out.println(bestTested.getNamespace());
            }

            pong.update();

        }
    }

    /**
     * Reset the background and call the draw method from the runningGame
     */
    private void render() {

        BufferStrategy bs = this.getBufferStrategy();
        if (bs == null) {
            this.createBufferStrategy(3);
            return;
        }

        Graphics g = bs.getDrawGraphics();

        //Make the background black
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getWidth(), getHeight());

        if (pong != null) pong.draw(g);

        //Show the fps and ups
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("FPS: " + fps, 0, height);
        g.drawString("UPS: " + ups, 0, height - 20);
        if(showBest) g.drawString("BEST MODE", 0, height-40);

        g.dispose();
        bs.show();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
        //ctrl+shif+s will toggle superSpeed
        if (e.isControlDown() && e.isShiftDown() && e.getKeyCode() == KeyEvent.VK_S) {
            superSpeed = !superSpeed;
        } else if (e.getKeyCode() == KeyEvent.VK_B) {
            showBest = !showBest;
        }
    }
}