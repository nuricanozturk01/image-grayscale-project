package com.nuricanozturk.imageprocessor;

import com.karandev.io.util.console.Console;
import com.karandev.io.util.console.annotation.Command;
import com.nuricanozturk.nucleus.annotation.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class CommandManager {
  private final ImageProcesserServer server;
  private final ExecutorService executor;

  public CommandManager(final ImageProcesserServer server) {
    this.server = server;
    executor = Executors.newCachedThreadPool();
  }

  @Command("start")
  private void startServer() throws InterruptedException {
    executor.execute(server::run);
    Thread.sleep(250);
  }

  @Command("stop")
  private void stopServer() throws InterruptedException {
    executor.execute(executor::close);
    Thread.sleep(1000);
  }

  @Command("quit")
  @Command
  private void exit() {
    Console.writeLine("Thanks");
    Console.writeLine("C and System Programmers Association");
    System.exit(0);
  }
}
