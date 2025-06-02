package ar.edu.itba.cripto;

import ar.edu.itba.cripto.exception.InvalidParameterException;
import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private ExecMode execMode;
    private String imagePath, dirPath;
    private int minimumShadows, totalShadows;

    private enum ExecMode {
        DISTRIBUTE("-d"),
        RESTORE("-r");

        private final String arg;

        ExecMode(String arg) {
            this.arg = arg;
        }

        public static ExecMode fromArg(String arg) {
            return Arrays.stream(ExecMode.values())
                    .filter(a -> arg.equals(a.arg))
                    .findAny().orElse(null);
        }
    }

    public static void main(String[] args) throws InvalidParameterException {
        Main main = new Main(args);
        try {
            // Begin processing BMPs
            if (main.execMode == ExecMode.DISTRIBUTE) {
                BMP secret = new BMP(main.imagePath);
                System.out.println("Secret image: " + main.imagePath + ", " + secret.width + "x" + secret.height);
                File dir = new File("ar\\edu\\itba\\cripto\\" + (main.dirPath != null ? main.dirPath : "."));
                List<BMP> carriers = new ArrayList<>();
                File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".bmp"));
                if (files == null || files.length < main.totalShadows) {
                    System.err.println("Error: Not enough BMPs in " + dir.getPath() + " (need " + main.totalShadows + ")");
                    System.exit(1);
                }
                for (int i = 0; i < main.totalShadows; i++) {
                    BMP carrier = new BMP(files[i].getPath());
                    carriers.add(carrier);
                    System.out.println("Carrier " + (i + 1) + ": " + files[i].getName() + ", " + carrier.width + "x" + carrier.height);
                }
                byte firstPixel = secret.pixels[0][0];
                System.out.println("First pixel value: " + (firstPixel & 0xFF));
                distributePixel(firstPixel, main.minimumShadows, main.totalShadows, carriers);
            } else {
                System.out.println("Restore mode not implemented yet");
            }
        } catch (IOException e) {
            System.err.println("BMP error: " + e.getMessage());
            System.exit(1);
        }
    }

    // Constructor
    public Main(String[] args) throws InvalidParameterException {
        for (int i = 0; i < args.length; i++) {
            boolean last = (i == args.length - 1);
            switch (args[i]) {
                case "-d": case "-r":
                    if (execMode == null) {
                        execMode = ExecMode.fromArg(args[i]);
                    } else {
                        throw new InvalidParameterException();
                    }
                    break;
                case "-secret":
                    if (imagePath == null && !last) {
                        imagePath = args[++i];
                    } else {
                        throw new InvalidParameterException();
                    }
                    break;
                case "-k":
                    if (minimumShadows == 0 && !last) {
                        minimumShadows = Integer.parseInt(args[++i]);
                        if (minimumShadows < 2 || minimumShadows > 10 || (totalShadows != 0 && minimumShadows > totalShadows)) {
                            throw new InvalidParameterException();
                        }
                    } else {
                        throw new InvalidParameterException();
                    }
                    break;
                case "-n":
                    if (totalShadows == 0 && !last) {
                        totalShadows = Integer.parseInt(args[++i]);
                        if (totalShadows < 2 || minimumShadows > totalShadows) {
                            throw new InvalidParameterException();
                        }
                    } else {
                        throw new InvalidParameterException();
                    }
                    break;
                case "-dir":
                    if (dirPath == null && !last) {
                        dirPath = args[++i];
                    } else {
                        throw new InvalidParameterException();
                    }
                    break;
                default:
                    throw new InvalidParameterException();
            }
        }
        if (execMode == null || imagePath == null || minimumShadows == 0) {
            throw new InvalidParameterException();
        }
    }

    // Helper method
    public static void printHelpMessage(String name) {
        System.out.println("Usage: " + name + " [-d | -r] -secret image -k number [-n number] [-dir directory]");
    }

    // Added methods
    private static int[] generateShares(int secret, int k, int n) {
        int a1 = (int) (Math.random() * 256); // Random coefficient
        int[] shares = new int[n];
        for (int x = 1; x <= n; x++) {
            shares[x - 1] = (secret + a1 * x) % 257; // Mod 257
        }
        return shares;
    }

// Replace the distributePixel method in Main.java
    private static void distributePixel(byte pixel, int k, int n, List<BMP> carriers) throws IOException {
        int pixelValue = pixel & 0xFF;
        System.out.println("Splitting pixel " + pixelValue + " into " + n + " shares, k=" + k);
        int[] shares = generateShares(pixelValue, k, n);
        for (int i = 0; i < n; i++) {
            int share = shares[i];
            byte[][] carrierPixels = carriers.get(i).pixels; // 2D array
            carrierPixels[0][0] = (byte) ((carrierPixels[0][0] & 0xFE) | (share & 0x01)); // Modify LSB of [0][0]
            try {
                carriers.get(i).toFile("ar\\edu\\itba\\cripto\\varias\\carrier" + (i + 1) + "_modified.bmp"); // Save
            } catch (IOException e) {
                System.err.println("Error saving carrier " + (i + 1) + ": " + e.getMessage());
                throw e;
            }
            System.out.println("Share " + (i + 1) + ": " + share + " embedded in carrier " + (i + 1));
        }
    }
}