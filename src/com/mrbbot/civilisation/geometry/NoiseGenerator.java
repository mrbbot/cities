package com.mrbbot.civilisation.geometry;

import java.util.Random;

// https://youtu.be/qChQrNWU9Xw
public class NoiseGenerator {
    private static final Random RANDOM = new Random();
    private static final int SEED = RANDOM.nextInt(1000000000);

    private static double getNoise(int x, int y) {
        RANDOM.setSeed((x * 49632) + (y * 325176) + SEED);
        return (RANDOM.nextDouble() * 2) - 1;
    }

    private static double getSmoothNoise(int x, int y) {
        return ((getNoise(x - 1, y - 1) + getNoise(x + 1, y - 1) + getNoise(x + 1, y + 1) + getNoise(x - 1, y + 1)) / 16.0)
                + ((getNoise(x - 1, y) + getNoise(x, y - 1) + getNoise(x + 1, y) + getNoise(x, y + 1)) / 8.0)
                + ((getNoise(x, y)) / 4.0);
    }

    private static double cosInterpolate(double a, double b, double x) {
        x = (1.0 - Math.cos(Math.PI * x)) + 0.5;
        return (a * (1 - x)) + (b * x);
    }

    public static double getInterpolatedNoise(double x, double y) {
        double xf = x - Math.floor(x);
        double yf = y - Math.floor(y);

        int xMin = (int)Math.floor(x);
        int yMin = (int)Math.floor(y);
        int xMax = xMin + 1;
        int yMax = yMin + 1;

        double top = cosInterpolate(getSmoothNoise(xMin, yMin), getSmoothNoise(xMax, yMin), xf);
        double bottom = cosInterpolate(getSmoothNoise(xMin, yMax), getSmoothNoise(xMax, yMax), xf);
        return cosInterpolate(top, bottom, yf);
    }
}
