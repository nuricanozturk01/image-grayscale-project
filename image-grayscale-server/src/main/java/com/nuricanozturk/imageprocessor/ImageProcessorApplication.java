package com.nuricanozturk.imageprocessor;

import com.karandev.io.util.console.CommandPrompt;
import com.nuricanozturk.nucleus.NucleusApplication;
import com.nuricanozturk.nucleus.annotation.EntryPoint;
import com.nuricanozturk.nucleus.annotation.NucleusFramework;

/**
 * YOU SHOULD NOT STOP SERVER BECAUSE MY NUCLEUS FRAMEWORK DOES NOT SUPPORT IT NOW!
 */
@NucleusFramework
@EntryPoint
public class ImageProcessorApplication {
  private final CommandManager commandManager;

  public ImageProcessorApplication(final CommandManager commandManager) {
    this.commandManager = commandManager;
  }

  public static void main(final String[] args) {
    NucleusApplication.run(ImageProcessorApplication.class);
  }

  public void run() {
    CommandPrompt.createBuilder()
            .setPrompt("image-processing-server")
            .registerObject(commandManager)
            .create()
            .run();
  }
}
