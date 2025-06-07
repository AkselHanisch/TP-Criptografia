package ar.edu.itba.cripto.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.*;
import java.io.*;

public class ImageBMPConverter {

    public static void main(String[] args) throws IOException {
        File inputFile = new File("input.jpg"); // or "input.png"
        BufferedImage original = ImageIO.read(inputFile);

        // Resize the image to your desired dimensions
        int targetWidth = 450;
        int targetHeight = 300;
        BufferedImage resized = resizeImage(original, targetWidth, targetHeight);

        // Convert to 8-bit grayscale BMP
        BufferedImage grayImage = convertTo8BitGrayscale(resized);

        File outputFile = new File("output.bmp");
        ImageIO.write(grayImage, "bmp", outputFile);

        System.out.println("Conversion complete: output.bmp");
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resized;
    }

    public static BufferedImage convertTo8BitGrayscale(BufferedImage original) {
        int width = original.getWidth();
        int height = original.getHeight();

        // Create grayscale palette (256 shades)
        byte[] grayPalette = new byte[256];
        for (int i = 0; i < 256; i++) {
            grayPalette[i] = (byte) i;
        }

        // Color model using grayscale palette
        IndexColorModel colorModel = new IndexColorModel(
                8, 256, grayPalette, grayPalette, grayPalette
        );

        // Create image with 8-bit indexed color model
        BufferedImage grayImage = new BufferedImage(width, height,
                BufferedImage.TYPE_BYTE_INDEXED, colorModel);

        // Draw original image in grayscale
        Graphics2D g = grayImage.createGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();

        return grayImage;
    }
}
