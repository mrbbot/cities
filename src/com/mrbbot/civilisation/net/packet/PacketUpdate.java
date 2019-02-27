package com.mrbbot.civilisation.net.packet;

/**
 * Abstract class extending Packet that describes a packet containing
 * information relating to game state. On receiving these types of packets, the
 * server should send them to all connected clients but the sender as they will
 * be handled locally there.
 */
public abstract class PacketUpdate extends Packet {
}
