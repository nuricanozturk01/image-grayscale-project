package com.nuricanozturk.imageprocessor;

import com.karandev.io.util.console.Console;
import com.karandev.util.net.TcpUtil;
import com.karandev.util.net.exception.NetworkException;
import com.nuricanozturk.nucleus.annotation.Component;
import org.csystem.image.CImage;
import org.csystem.image.CImageFormat;
import org.csystem.net.tcp.server.ConcurrentServer;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static org.csystem.util.exception.ExceptionUtil.subscribeRunnable;

@Component
public class ImageProcesserServer implements AutoCloseable {
  private static final int PORT = 8080;
  private static final int BACKLOG = 1024;
  private static final int SOCKET_TIMEOUT = 10000;

  private final List<CommandInfo> COMMANDS = List.of(
          new CommandInfo("gs", this::doGrayScaleFile, GS_IMAGE_PATH.getAbsolutePath()),
          new CommandInfo("bin", this::doBinaryImageFile, BIN_IMAGE_PATH.getAbsolutePath())
  );

  private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyy_HH-mm-ss-n");
  private static final File GS_IMAGE_PATH = new File("grayscale_images");
  private static final File BIN_IMAGE_PATH = new File("binary_images");
  private final ConcurrentServer concurrentServer;


  public ImageProcesserServer() throws IOException {
    concurrentServer = ConcurrentServer.builder()
            .setPort(PORT)
            .setBacklog(BACKLOG)
            .setInitRunnable(this::initRunnableCallback)
            .setBeforeAcceptRunnable(() -> Console.writeLine("ImageProcessingServer server is waiting for a client on port:%d", PORT))
            .setClientSocketConsumer(this::handleClient)
            .build();
  }

  private void doBinaryImageFileNetworkExceptionCallback(Socket socket, Throwable ex) {
    Console.Error.writeLine("Network problem:%s", ex.getMessage());
    TcpUtil.sendLine(socket, "ERR_BIN");
    TcpUtil.sendLine(socket, "Problem occurred while making binary!...");
  }

  private void doBinaryImageFile(Socket socket, String path) throws IOException {
    subscribeRunnable(() -> doBinaryImageFileCallback(socket, path),
            ex -> doBinaryImageFileNetworkExceptionCallback(socket, ex));
  }

  private File doBinaryImage(File file, int threshold) throws IOException {
    var image = new CImage(file);
    var path = file.getAbsolutePath();

    file = new File(path.substring(0, path.lastIndexOf('.') + 1) + "-bin.png");

    image.binary(threshold);
    image.save(file, CImageFormat.PNG);

    return file;
  }

  private void nameValidCallback(Socket socket, String name, CommandInfo commandInfo) {
    try {
      var hostAddress = socket.getInetAddress().getHostAddress();
      var port = socket.getPort();

      TcpUtil.sendLine(socket, "SUC_N");

      var path = String.format("%s_%s_%d_%s", name.substring(0, 3), hostAddress, port, FORMATTER.format(LocalDateTime.now()));

      commandInfo.consumer.accept(socket, path);
    } catch (IOException ex) {
      Console.Error.writeLine("ImageProcessingServer:IO Exception Occurred:%s", ex.getMessage());
    } catch (NetworkException ex) {
      Console.Error.writeLine("ImageProcessingServer:Network Exception Occurred:%s", ex.getMessage());
    } catch (Throwable ex) {
      Console.Error.writeLine("ImageProcessingServer:Exception Occurred:%s", ex.getMessage());
    }
  }

  private void nameInValidCallback(Socket socket) {
    try {
      TcpUtil.sendLine(socket, "ERR_N");
      TcpUtil.sendLine(socket, "Length of name must be greater than 2(three) AND less then 10(ten)");
    } catch (NetworkException ex) {
      Console.Error.writeLine("ImageProcessingServer:nameNotValidCallback:%s", ex.getMessage());
    }
  }

  private void commandValidCallback(Socket socket, CommandInfo ci) {
    try {
      TcpUtil.sendLine(socket, "SUC_CMD");

      Optional.ofNullable(TcpUtil.receiveLine(socket))
              .filter(n -> 3 <= n.length())
              .filter(n -> n.length() <= 10)
              .ifPresentOrElse(n -> nameValidCallback(socket, n, ci), () -> nameInValidCallback(socket));
    } catch (Throwable ex) {
      Console.Error.writeLine("ImageProcessingServer:Exception Occurred:%s", ex.getMessage());
    }
  }

  private void commandInvalidCallback(Socket socket) {
    TcpUtil.sendLine(socket, "ERR_CMD");
    TcpUtil.sendLine(socket, "gs, bin");
  }


  private void handleClient(Socket socket) {
    try (socket) {
      socket.setSoTimeout(SOCKET_TIMEOUT);
      var hostAddress = socket.getInetAddress().getHostAddress();
      var port = socket.getPort();
      Console.writeLine("Client connected to grayscale image server via %s:%d", hostAddress, port);

      checkCommand(Optional.ofNullable(TcpUtil.receiveLine(socket)))
              .ifPresentOrElse(c -> commandValidCallback(socket, c), () -> commandInvalidCallback(socket));
    } catch (Throwable ex) {
      Console.Error.writeLine("ImageProcessingServer:Exception Occurred:%s", ex.getMessage());
    }
  }

  private void initRunnableCallback() {
    var gsDirStatus = GS_IMAGE_PATH.mkdirs();
    var binDirStatus = BIN_IMAGE_PATH.mkdirs();

    if ((!GS_IMAGE_PATH.exists() && !gsDirStatus) || (!BIN_IMAGE_PATH.exists() && !binDirStatus)) {
      Console.Error.writeLine("Directories can not be created!...");
      System.exit(1);
    }
  }

  private Optional<CommandInfo> checkCommand(final Optional<String> stringOptional) {
    if (stringOptional.isEmpty())
      return Optional.empty();

    var ci = new CommandInfo(stringOptional.get());
    var index = COMMANDS.indexOf(ci);

    return index != -1 ? Optional.of(COMMANDS.get(index)) : Optional.empty();
  }

  private void doGrayScaleFileCallback(Socket socket, String path) throws IOException {
    var file = new File(GS_IMAGE_PATH, String.format("%s.png", new File(path).getName()));

    TcpUtil.receiveFile(socket, file);
    file = doGrayscale(file);
    TcpUtil.sendLine(socket, "SUC_GS");
    TcpUtil.sendFile(socket, file, 1024);
  }

  private void doGrayScaleFileNetworkExceptionCallback(Socket socket, Throwable ex) {
    Console.Error.writeLine("Network problem:%s", ex.getMessage());
    TcpUtil.sendLine(socket, "ERR_GS");
    TcpUtil.sendLine(socket, "Problem occurred while making grayscale!...");
  }

  private void doGrayScaleFile(Socket socket, String path) throws IOException {
    subscribeRunnable(() -> doGrayScaleFileCallback(socket, path),
            ex -> doGrayScaleFileNetworkExceptionCallback(socket, ex));
  }

  private File doGrayscale(File file) throws IOException {
    var image = new CImage(file);

    var path = file.getAbsolutePath();
    file = new File(path.substring(0, path.lastIndexOf('.') + 1) + "-gs.png");

    image.grayScale();
    image.save(file, CImageFormat.PNG);

    return file;
  }

  private void doBinaryImageFileCallback(Socket socket, String path) throws IOException {
    var file = new File(BIN_IMAGE_PATH, String.format("%s.png", new File(path).getName()));

    TcpUtil.receiveFile(socket, file);
    var threshold = TcpUtil.receiveInt(socket);

    file = doBinaryImage(file, threshold);

    TcpUtil.sendLine(socket, "SUC_BIN");
    TcpUtil.sendFile(socket, file, 1024);
  }

  @Override
  public void close() throws Exception {
    concurrentServer.stop();
  }

  public void run() {
    concurrentServer.start();
  }
}
