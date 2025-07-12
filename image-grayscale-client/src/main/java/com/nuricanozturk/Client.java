package com.nuricanozturk;

import com.karandev.util.console.Console;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;

public class Client {

  private final String host;
  private final int port;

  public Client(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void run() throws IOException {
    final var file = new File("image.jpeg");

    if (!file.exists()) {
      Console.Error.writeLine("File not found");
      return;
    }

    try (final var socket = new Socket(this.host, this.port);
         final var fis = new FileInputStream(file)) {
      final var is = socket.getInputStream();
      final var os = socket.getOutputStream();
      var dos = new DataOutputStream(os);

      dos.writeLong(file.length());

      int bytesRead = 0;
      while ((bytesRead = fis.read()) != -1) {
        os.write(bytesRead);
      }

      var result = is.read();

      if (result == 1) {
        Console.writeLine("File read");
      } else {
        Console.Error.writeLine("File read error");
      }
    }
  }

  public static void main(String[] args) throws IOException {
    final var client = new Client("192.168.0.101", 2121);
    client.run();
  }
}
