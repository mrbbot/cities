package com.mrbbot.civilisation.net;

import com.mrbbot.generic.net.Client;

import java.io.IOException;
import java.util.Scanner;

public class ChatClient {
  private static Scanner scanner;

  public static void main(String[] args) throws IOException {
    scanner = new Scanner(System.in);

    System.out.print("ID> ");
    String id = scanner.nextLine();

    Client<String> chatClient = new Client<>("127.0.0.1", 1234, id, ((connection, data) -> {
      System.out.println(data);
    }));

    new Thread(() -> {
      while(true) {
        String line = scanner.nextLine();
        chatClient.broadcast(line);
      }
    }).start();
  }
}
