package com.mrbbot.civilisation.logic.interfaces;

public interface Traversable extends Positionable {
    int getCost();
    boolean canTraverse();
}
