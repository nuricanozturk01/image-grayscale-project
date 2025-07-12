package com.nuricanozturk.imageprocessor;

import java.net.Socket;

public class CommandInfo {
  String cmdText;
  BiConsumer<Socket, String> consumer;
  String path;

  CommandInfo(String cmdText) {
    this(cmdText, null, null);
  }

  CommandInfo(String cmdText, BiConsumer<Socket, String> consumer, String path) {
    this.cmdText = cmdText;
    this.consumer = consumer;
    this.path = path;
  }

  @Override
  public boolean equals(Object other) {
    return other instanceof CommandInfo ci && cmdText.equals(ci.cmdText);
  }
}