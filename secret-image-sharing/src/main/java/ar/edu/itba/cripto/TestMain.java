package ar.edu.itba.cripto;

import ar.edu.itba.cripto.exception.InvalidParameterException;
import ar.edu.itba.cripto.math.Lagrange;
import ar.edu.itba.cripto.math.Pair;

import java.io.*;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;

public class TestMain {

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
        TestMain main = new TestMain(args);
        try {
            // Begin processing BMPs
            if (main.execMode == ExecMode.DISTRIBUTE) {
                BMP secret = new BMP(main.imagePath);
                System.out.println("Secret image: " + main.imagePath + ", " + secret.width + "x" + secret.height);
                File dir = new File("ar\\edu\\itba\\cripto\\" + (main.dirPath != null ? main.dirPath : "."));
                List<BMP> carriers = new ArrayList<>();
                File[] files = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".bmp") && !name.contains("_modified"));
                if (files == null || files.length < main.totalShadows) {
                    System.err.println("Error: Not enough BMPs in " + dir.getPath() + " (need " + main.totalShadows + ")");
                    System.exit(1);
                }
                for (int i = 0; i < main.totalShadows; i++) {
                    BMP carrier = new BMP(files[i].getPath());
                    carriers.add(carrier);
                    System.out.println("Carrier " + (i + 1) + ": " + files[i].getName() + ", " + carrier.width + "x" + carrier.height);
                }
                // Process all pixels
                int pixelIndex = 0;
                for (int row = 0; row < secret.height; row++) {
                    for (int col = 0; col < secret.width; col++) {
                        byte pixel = secret.pixels[row][col];
                        distributePixel(pixel, main.minimumShadows, main.totalShadows, carriers, pixelIndex);
                        pixelIndex++;
                    }
                }
                // Save carriers at the end
                for (int i = 0; i < main.totalShadows; i++) {
                    try {
                        carriers.get(i).toFile("ar\\edu\\itba\\cripto\\varias\\carrier" + (i + 1) + "_modified.bmp");
                    } catch (IOException e) {
                        System.err.println("Error saving carrier " + (i + 1) + ": " + e.getMessage());
                        System.exit(1);
                    }
                }
                System.out.println("Distribution complete for all " + secret.width + "x" + secret.height + " pixels.");
            }
            else {
                BMP restored = new BMP(main.imagePath); // Create BMP to restore into
                List<BMP> carriers = new ArrayList<>();
            
                for (int i = 0; i < main.minimumShadows; i++) {
                    String carrierPath = "ar\\edu\\itba\\cripto\\varias\\carrier" + (i + 1) + "_modified.bmp";
                    carriers.add(new BMP(carrierPath));
                }
            
                // Validate that carriers are large enough
                int carrierHeight = carriers.get(0).pixels.length;
                int carrierWidth = carriers.get(0).pixels[0].length;
                int totalAvailablePixels = (carrierHeight * carrierWidth) / 8;
            
                int requiredPixels = restored.height * restored.width;
                if (requiredPixels > totalAvailablePixels) {
                    System.err.println("Error: Not enough data in carrier images to restore full image.");
                    System.exit(1);
                }
            
                // Process all pixels
                int pixelIndex = 0;
                for (int row = 0; row < restored.height; row++) {
                    for (int col = 0; col < restored.width; col++) {
                        // Extract 8-bit shares from k carriers
                        Pair[] shadows = new Pair[main.minimumShadows];
                        int baseBitIndex = pixelIndex * 8;
            
                        for (int i = 0; i < main.minimumShadows; i++) {
                            int x = i + 1;
                            int share = 0;
                            byte[][] carrierPixels = carriers.get(i).pixels;
            
                            for (int bit = 0; bit < 8; bit++) {
                                int bitIndex = baseBitIndex + bit;
                                int r = bitIndex / carrierWidth;
                                int c = bitIndex % carrierWidth;
            
                                int bitValue = carrierPixels[r][c] & 0x01;
                                share = (share << 1) | bitValue;
                            }
            
                            shadows[i] = new Pair(x, share);
                        }
            
                        // Recover secret using Lagrange interpolation
                        Lagrange lagrange = new Lagrange(shadows, 257);
                        int secret = lagrange.eval(0);
                        restored.pixels[row][col] = (byte) (secret & 0xFF); // Store recovered pixel
                        pixelIndex++;
                    }
                }
            
                // Save restored image
                try {
                    restored.toFile("ar\\edu\\itba\\cripto\\restored_clave.bmp");
                    System.out.println("Restored image saved as restored_clave.bmp");
                } catch (IOException e) {
                    System.err.println("Error saving restored image: " + e.getMessage());
                    System.exit(1);
                }
            }
            
        } catch (IOException e) {
            System.err.println("BMP error: " + e.getMessage());
            System.exit(1);
        }
    }

    // Constructor
    public TestMain(String[] args) throws InvalidParameterException {
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

    private static void distributePixel(byte pixel, int k, int n, List<BMP> carriers, int pixelIndex) throws IOException {
        int pixelValue = pixel & 0xFF;
        int[] shares = generateShares(pixelValue, k, n);
    
        for (int i = 0; i < n; i++) {
            int share = shares[i]; // 0–256
            byte[][] carrierPixels = carriers.get(i).pixels;
            int carrierHeight = carrierPixels.length;
            int carrierWidth = carrierPixels[0].length;
    
            // Each pixel consumes 8 bits → 8 locations per carrier
            int totalAvailableBits = carrierHeight * carrierWidth;
            int totalAvailablePixels = totalAvailableBits / 8;
    
            if (pixelIndex >= totalAvailablePixels) {
                System.err.println("Error: Not enough space in carrier image to store all pixels.");
                System.exit(1);
            }
    
            int baseBitIndex = pixelIndex * 8;
            for (int bit = 0; bit < 8; bit++) {
                int bitIndex = baseBitIndex + bit;
                int row = bitIndex / carrierWidth;
                int col = bitIndex % carrierWidth;
    
                int bitValue = (share >> (7 - bit)) & 0x01; // MSB to LSB
                carrierPixels[row][col] = (byte) ((carrierPixels[row][col] & 0xFE) | bitValue); // Replace LSB
            }
        }
    }    
}