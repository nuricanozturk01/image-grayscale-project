package com.nuricanozturk.csd.image;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class CImage {
  private final File m_path;
  private BufferedImage m_bufferedImage;

  public CImage(String path) throws IOException {
    this(new File(path));
  }

  public CImage(File path) throws IOException {
    if (!path.exists()) {
      throw new IOException("image not found");
    } else {
      this.m_path = path;
      this.m_bufferedImage = ImageIO.read(this.m_path);
    }
  }

  public int getWidth() {
    return this.m_bufferedImage.getWidth();
  }

  public int getHeight() {
    return this.m_bufferedImage.getHeight();
  }

  public BufferedImage getBufferedImage() {
    return this.m_bufferedImage;
  }

  public void grayScale() {
    int width = this.m_bufferedImage.getWidth();
    int height = this.m_bufferedImage.getHeight();

    for(int i = 0; i < width; ++i) {
      for(int k = 0; k < height; ++k) {
        Color c = new Color(this.m_bufferedImage.getRGB(i, k));
        int avg = (int)Math.floor((double)(c.getRed() + c.getGreen() + c.getBlue()) / (double)3.0F);
        this.m_bufferedImage.setRGB(i, k, (new Color(avg, avg, avg)).getRGB());
      }
    }

  }

  public void binary(int threshold) {
    int width = this.m_bufferedImage.getWidth();
    int height = this.m_bufferedImage.getHeight();

    for(int i = 0; i < width; ++i) {
      for(int k = 0; k < height; ++k) {
        Color c = new Color(this.m_bufferedImage.getRGB(i, k));
        int value = c.getRed() > threshold ? 255 : 0;
        this.m_bufferedImage.setRGB(i, k, (new Color(value, value, value)).getRGB());
      }
    }

  }

  public void save(String output, CImageFormat format) throws IOException {
    this.save(new File(output), format);
  }

  public void save(File output, CImageFormat format) throws IOException {
    ImageIO.write(this.m_bufferedImage, format.toString().toLowerCase(), output);
  }

  public void reset() throws IOException {
    this.m_bufferedImage = ImageIO.read(this.m_path);
  }
}
