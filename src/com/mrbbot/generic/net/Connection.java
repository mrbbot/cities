package com.mrbbot.generic.net;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Date;
import java.util.function.Predicate;

public class Connection<T> implements Runnable, Broadcaster<T> {
  private ObjectOutputStream outputStream;
  private ObjectInputStream inputStream;
  private final IdHandler<T> idHandler;
  private final Handler<T> inputHandler;
  private boolean open;
  private String id;
  private Thread thread;
  private Broadcaster<T> broadcaster;

  Connection(Socket socket, IdHandler<T> idHandler, Handler<T> inputHandler, Broadcaster<T> broadcaster) throws IOException {
    outputStream = new ObjectOutputStream(socket.getOutputStream());
    inputStream = new ObjectInputStream(socket.getInputStream());
    this.idHandler = idHandler;
    this.inputHandler = inputHandler;
    this.broadcaster = broadcaster;
    open = true;

    thread = new Thread(this);
    thread.setName("Connection");
    thread.start();
  }

  public void send(Object object) throws IOException {
    System.out.println(String.format("[%s] %s -> %s", new Date().toString(), object.getClass().getSimpleName(), id));
    outputStream.writeObject(object);
    outputStream.flush();
  }

  @Override
  public void run() {
    while(open) {
      try {
        Object object = inputStream.readObject();
        System.out.println(String.format("[%s] %s <- %s", new Date().toString(), object.getClass().getSimpleName(), id));
        if(id == null) {
          idHandler.accept(this, (String)object);
        } else {
          //noinspection unchecked
          inputHandler.accept(this, (T) object);
        }
      } catch (IOException e) {
        System.out.println(String.format("[%s] Connection with \"%s\" closed: %s", new Date().toString(), id, e.getMessage()));
        inputHandler.accept(this, null);
        open = false;
      } catch (ClassNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  public String getId() {
    return id;
  }

  void setId(String id) {
    this.id = id;
    thread.setName("Connection:"+id);
  }

  @Override
  public void broadcast(T data) {
    broadcaster.broadcast(data);
  }

  @Override
  public void broadcastWhere(T data, Predicate<String> test) {
    broadcaster.broadcastWhere(data, test);
  }

  public void broadcastExcluding(T data) {
    broadcaster.broadcastExcluding(data, id);
  }

  @Override
  public void broadcastExcluding(T data, String id) {
    broadcaster.broadcastExcluding(data, id);
  }

  public void broadcastTo(T data) {
    broadcaster.broadcastTo(data, id);
  }

  @Override
  public void broadcastTo(T data, String id) {
    broadcaster.broadcastTo(data, id);
  }
}
