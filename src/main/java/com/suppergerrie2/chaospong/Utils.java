package com.suppergerrie2.chaospong;

import com.suppergerrie2.ChaosNetClient.components.nnet.neurons.OutputNeuron;

import java.awt.*;

public class Utils {

    /**
     * Returns the index for which the array has the highest value.
     *
     * Ex:
     * [0.2,0.4,0.1,0.5,0.3]
     * will return 3
     *
     * @param array Array to get the index from
     * @return the index with the highest value
     */
    public static OutputNeuron oneHot(OutputNeuron[] array) {
        int maxIndex = 0;

        for(int i = 0; i < array.length; i++) {
            if(array[i].value>array[maxIndex].value) {
                maxIndex = i;
            }
        }

        return array[maxIndex];
    }

    /**
     * Create an array with only 0 except for the given index.
     * Calling oneHot with this array should return index
     *
     * @param index The index where the array should be 1
     * @param size The size of the array
     * @return The array with 1 index set to 1
     */
    public static float[] convertToOneHot(int index, int size) {
        if(index > size) {
            throw new ArrayIndexOutOfBoundsException("One hot index should be smaller than the size!");
        }

        float[] array = new float[size];
        for(int i = 0; i < size; i++) {
            array[i] = 0;
        }

        array[index] = 1;

        return array;
    }

    /**
     * Draw a string to the screen which is horizontally centered.
     * Takes into account the width of the string
     * @param string The string to draw
     * @param width The width of the screen
     * @param y The y position of the screen
     * @param g The graphics object to draw to.
     */
    public static void drawHorizontallyCenteredString(String string, int width, int y, Graphics g) {
        FontMetrics metrics = g.getFontMetrics();
        // Determine the X coordinate for the text
        int x =  (width - metrics.stringWidth(string)) / 2;
        // Draw the String
        g.drawString(string, x, y);
    }

    /**
     * Draw a string centered in the screen.
     * @param string The string to draw
     * @param width The width of the screen
     * @param height The height of the screen
     * @param g The graphics object to draw to
     */
    public static void drawCenteredString(String string, int width, int height, Graphics g) {
        FontMetrics metrics = g.getFontMetrics();
        // Determine the X coordinate for the text
        int x =  (width - metrics.stringWidth(string)) / 2;
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        int y = ((height - metrics.getHeight()) / 2) + metrics.getAscent();
        // Draw the String
        g.drawString(string, x, y);
    }

    public static String getUsername() {
        String username = System.getProperty("chaosnet_username");

        if (username != null) {
            return username;
        }

        return System.getenv("chaosnet_username");
    }

    public static String getPassword() {
        String password = System.getProperty("chaosnet_password");

        if (password != null) {
            return password;
        }

        return System.getenv("chaosnet_password");
    }
}
