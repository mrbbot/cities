package com.mrbbot.civilisation.geometry;

/**
 * Represents an operation that accepts part of a hexagon grid and then performs an action with it.
 * @param <T> type of the hexagon grid
 */
@FunctionalInterface
public interface HexagonConsumer<T> {
   void accept(T t, Hexagon hex, int x, int y);
}
