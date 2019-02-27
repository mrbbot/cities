package com.mrbbot.civilisation.geometry;

import java.util.Random;

/**
 * Class containing static methods for generating random noise to be used by
 * the terrain generator. Code used is from this YouTube video:
 * https://youtu.be/qChQrNWU9Xw
 */
public class NoiseGenerator {
  /**
   * Random number generator used internally
   */
  private static final Random RANDOM = new Random();
  /**
   * Random seed to be used by the generator
   */
  private static final int SEED = RANDOM.nextInt(1000000000);

  /**
   * Gets some random noise for the specified coordinate. This function will
   * always return the same value for the same coordinate in the same program
   * execution.
   *
   * @param x x-coordinate of noise to get
   * @param y y-coordinate of noise to get
   * @return random noise in the range (-1, 1)
   */
  private static double getNoise(int x, int y) {
    // Set the seed of the random generator as a constant plus some multiple of
    // the x and y coordinates. This ensures the same coordinates generate the
    // same noise.
    RANDOM.setSeed((x * 49632) + (y * 325176) + SEED);
    // Return random number in the desired range
    return (RANDOM.nextDouble() * 2) - 1;
  }

  /**
   * Gets a smoothed version of the random noise for the specified coordinates.
   * Corners, edges, and the center are all taken into account with different
   * proportions.
   *
   * @param x x-coordinate of noise to get
   * @param y y-coordinate of noise to get
   * @return random noise in the range (-1, 1)
   */
  private static double getSmoothNoise(int x, int y) {
    double topLeft = getNoise(x - 1, y - 1);
    double topRight = getNoise(x + 1, y - 1);
    double bottomRight = getNoise(x + 1, y + 1);
    double bottomLeft = getNoise(x - 1, y + 1);

    double left = getNoise(x - 1, y);
    double top = getNoise(x, y - 1);
    double right = getNoise(x + 1, y);
    double bottom = getNoise(x, y + 1);

    double center = getNoise(x, y);

    return ((topLeft + topRight + bottomRight + bottomLeft) / 16.0)
      + ((left + top + right + bottom) / 8.0)
      + (center / 4.0);
  }

  /**
   * Interpolates between two values using a cos function
   *
   * @param a first value
   * @param b second value
   * @param t amount to interpolate in the interval [0, 1]
   * @return a value between in the range [a, b]
   */
  private static double cosInterpolate(double a, double b, double t) {
    t = (1.0 - Math.cos(Math.PI * t)) + 0.5;
    return (a * (1 - t)) + (b * t);
  }

  /**
   * Gets an interpolated version of the random noise. This function unlike the
   * others in this class takes doubles for the coordinates allowing for
   * decimal positions to be used.
   *
   * @param x x-coordinate of noise to get
   * @param y y-coordinate of noise to get
   * @return random noise in the range (-1, 1)
   */
  public static double getInterpolatedNoise(double x, double y) {
    // Gets the fractional components of the x and y coordinates
    double xf = x - Math.floor(x);
    double yf = y - Math.floor(y);

    // Gets the floors and ceilings of the x and y coordinates. These are the
    // vertices of the quadrant to get the noise from.
    int xMin = (int) Math.floor(x);
    int yMin = (int) Math.floor(y);
    int xMax = xMin + 1;
    int yMax = yMin + 1;

    // Get noise for the top of the quadrant
    double top = cosInterpolate(
      getSmoothNoise(xMin, yMin),
      getSmoothNoise(xMax, yMin),
      xf
    );
    // Get noise for the bottom of the quadrant
    double bottom = cosInterpolate(
      getSmoothNoise(xMin, yMax),
      getSmoothNoise(xMax, yMax),
      xf
    );
    // Get noise for the position in the quadrant proportional to the
    // fractional coordinate components
    return cosInterpolate(top, bottom, yf);
  }
}
