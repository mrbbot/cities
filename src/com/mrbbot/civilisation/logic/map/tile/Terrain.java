package com.mrbbot.civilisation.logic.map.tile;

import com.mrbbot.civilisation.geometry.NoiseGenerator;
import com.mrbbot.civilisation.net.serializable.SerializablePoint2D;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;

import java.io.Serializable;

public class Terrain implements Serializable {
    public double height; // 0 <= height <= 1
    public final Level level;

    Terrain(SerializablePoint2D p) {
        this.height = (NoiseGenerator.getInterpolatedNoise(p.getX(), p.getY()) + 1) / 2;
        level = Level.of(this.height);
        if (level.fixToMax) this.height = level.maxHeight;
    }
}
