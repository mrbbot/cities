package com.mrbbot.civilisation.net.packet;

import java.io.Serializable;

/**
 * Base packet class. All packet types extend this. Packets are sent between
 * clients and the server to keep the game state synchronised.
 * <p>
 * Implements serializable so
 * that any subclass can be sent over the network with
 * {@link java.io.ObjectInputStream} and {@link java.io.ObjectOutputStream}
 * both of which allow Java objects to be sent/received. Implementing
 * serializable means that all class fields must themselves be serializable.
 */
public abstract class Packet implements Serializable {
}
