package ar.edu.itba.cripto;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class VisualSSS {
    private static boolean distribute = false;
    private static boolean recover = false;
    private static String secretImage = "";
    private static int k = 0;
    private static int n = -1;
    private static String dir = ".";

    public static void main(String[] args) {
        // Parse command-line arguments
        if (!parseArguments(args)) {
            printUsage();
            System.exit(1);
        }

        try {
            if (distribute) {
                distributeSecret();
            } else if (recover) {
                recoverSecret();
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            printUsage();
            System.exit(1);
        }
    }

    private static boolean parseArguments(String[] args) {
        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "-d":
                        distribute = true;
                        break;
                    case "-r":
                        recover = true;
                        break;
                    case "-secret":
                        secretImage = args[++i];
                        break;
                    case "-k":
                        k = Integer.parseInt(args[++i]);
                        break;
                    case "-n":
                        n = Integer.parseInt(args[++i]);
                        break;
                    case "-dir":
                        dir = args[++i];
                        break;
                    default:
                        return false;
                }
            }
            // Validate parameters
            if ((distribute == recover) || secretImage.isEmpty() || k < 2 || k > 10) {
                return false;
            }
            if (distribute && n != -1 && (n < k || n < 2)) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static void printUsage() {
        System.err.println("Usage: visualsss -d|-r -secret <image.bmp> -k <number> [-n <number>] [-dir <directory>]");
        System.err.println("Examples:");
        System.err.println("  visualsss -d -secret clave.bmp -k 2 -n 4 -dir varias");
        System.err.println("  visualsss -r -secret secreta.bmp -k 2 -n 4 -dir varias");
    }

    private static void distributeSecret() throws Exception {
        // Read secret BMP image
        File secretFile = new File(dir, secretImage);
        if (!secretFile.exists()) {
            throw new Exception("Secret image does not exist");
        }
        byte[] bmpData = readBMP(secretFile);
        int offset = getPixelOffset(bmpData);
        int width = getBMPWidth(bmpData);
        int height = getBMPHeight(bmpData);

        // TODO: Implement Shamir's secret sharing
        // TODO: Generate permutation table
        // TODO: Embed shares into carrier images using LSB
    }

    private static void recoverSecret() throws Exception {
        // TODO: Read k shadow BMPs
        // TODO: Extract shares using LSB
        // TODO: Reconstruct secret using Lagrange interpolation
        // TODO: Write recovered BMP
    }

    // Read BMP file into byte array
    private static byte[] readBMP(File file) throws IOException {
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            return data;
        }
    }

    // Get pixel data offset from BMP header
    private static int getPixelOffset(byte[] bmpData) {
        // Offset to pixel data is at bytes 10-13 (little-endian)
        return ByteBuffer.wrap(bmpData, 10, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    // Get BMP width (bytes 18-21, little-endian)
    private static int getBMPWidth(byte[] bmpData) {
        return ByteBuffer.wrap(bmpData, 18, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    // Get BMP height (bytes 22-25, little-endian)
    private static int getBMPHeight(byte[] bmpData) {
        return ByteBuffer.wrap(bmpData, 22, 4).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    // Example: LSB replacement to hide a byte in carrier BMP
    private static void hideByte(byte[] carrier, int offset, byte value) {
        // Hide 8 bits of 'value' in LSBs of 8 consecutive bytes
        for (int i = 0; i < 8; i++) {
            int bit = (value >> (7 - i)) & 1;
            carrier[offset + i] = (byte) ((carrier[offset + i] & 0xFE) | bit);
        }
    }
}