package com.nuricanozturk.imageprocessing;

import com.karandev.util.console.Console;
import com.nuricanozturk.csd.image.CImage;
import com.nuricanozturk.csd.image.CImageFormat;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerByte {
  private final ExecutorService threadPool;
  private final int port;
  private final ServerSocket serverSocket;
  private final static int SOCKET_TIMEOUT = 100_000;
  private final static File BASE_PATH = new File("images");

  public ServerByte(final int port, final int backlog) throws IOException {
    threadPool = Executors.newCachedThreadPool();
    this.port = port;
    serverSocket = new ServerSocket(this.port, backlog);
    BASE_PATH.mkdirs();
  }

  private void saveFile(final String path, final Socket socket) throws IOException {
    byte result = 0;
    final var file = new File(BASE_PATH, path);

    try (final var fos = new FileOutputStream(file)) {
      final var is = socket.getInputStream();
      final var dis = new DataInputStream(is);

      final var length = dis.readLong();

      for (int i = 0; i < length; i++)
        fos.write(is.read());

      grayScale(file);

      result = 1;
    } catch (final Throwable ignored) {
      file.delete();
    }

    socket.getOutputStream().write(result);
  }

  private void grayScale(File file) throws IOException {
    final var img = new CImage(file);
    img.grayScale();

    final var originalName = file.getName();
    final var extensionIndex = originalName.lastIndexOf('.');
    final String grayscaleFileName;

    if (extensionIndex != -1) {
      final var nameWithoutExt = originalName.substring(0, extensionIndex);
      final var extension = originalName.substring(extensionIndex);
      grayscaleFileName = nameWithoutExt + "-gs" + extension;
    } else {
      grayscaleFileName = originalName + "-gs.jpeg";
    }

    final var grayscaleFile = new File(file.getParent(), grayscaleFileName);
    img.save(grayscaleFile, CImageFormat.JPEG);
  }

  private void serverThreadCallback() {
    try {
      while (true) {
        Console.writeLine("Image Processing Server listening on port %d", this.port);
        final var socket = serverSocket.accept();

        threadPool.execute(() -> this.handleClient(socket));
      }
    } catch (final IOException e) {
      Console.Error.writeLine("Server error: %s", e.getMessage());
    } catch (final Throwable e) {
      Console.Error.writeLine("Unexpected error: %s", e.getMessage());
    } finally {
      threadPool.shutdown();
    }
  }

  private void handleClient(final Socket socket) {
    try (socket) {
      socket.setSoTimeout(SOCKET_TIMEOUT);
      final var hostAddress = socket.getInetAddress().getHostAddress();
      final var port = socket.getPort();

      Console.writeLine("Client connected via %s:%d", hostAddress, port);

      final var path = String.format("%s:%d_%s.jpg", hostAddress, port, Instant.now());

      saveFile(path, socket);
    } catch (IOException e) {
      Console.Error.writeLine("Client error: %s", e.getMessage());
    }
  }

  public void run() {
    threadPool.execute(this::serverThreadCallback);
  }

  public static void main(String[] args) throws IOException {
    final var server = new ServerByte(2121, 1000);
    server.run();
  }
}