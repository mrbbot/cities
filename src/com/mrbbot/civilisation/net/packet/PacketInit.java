package com.mrbbot.civilisation.net.packet;

/**
 * Packet emitted by the client requesting the game state. On receiving this
 * packet, the server should send a {@link PacketGame} containing the game
 * state.
 */
public class PacketInit extends Packet {
}
