package com.nuricanozturk;

import com.karandev.io.util.console.CommandPrompt;
import com.karandev.io.util.console.Console;

public class Main {

  private final String host;
  private final int port;

  public Main(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public void run() {
    try {
      CommandPrompt.createBuilder()
              .registerObject(new CommandsInfo(host, port))
              .setPrompt("image-client")
              .setSuffix("$")
              .create()
              .run();
    } catch (NumberFormatException ignore) {
      Console.Error.writeLine("Invalid arguments!....");
    }
  }

  public static void main(String[] args) {
    final var client = new Main("localhost", 8080);
    client.run();
  }
}
