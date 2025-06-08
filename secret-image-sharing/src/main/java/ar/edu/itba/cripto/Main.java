package ar.edu.itba.cripto;

import ar.edu.itba.cripto.exception.InvalidParameterException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

public class Main {

    public static void main(String[] args){
        Arguments arguments = null;

        try {
            arguments = new Arguments(args);
        } catch (InvalidParameterException e){
            System.out.println("Usage: visualSSS [-d | -r] -secret image -k number [-n number] [-dir directory]");
            System.exit(1);
        }

        String[] carrierCandidates = null;
        try (Stream<Path> files = Files.list(Path.of(arguments.dir))) {
            List<String> bmpPaths = files
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(".bmp"))
                    .map(Path::toString)
                    .toList();

            carrierCandidates = bmpPaths.toArray(new String[0]);

        } catch (IOException e) {
            System.err.println("Error reading directory: " + e.getMessage());
            System.exit(1);
        }

        if (arguments.execMode == Arguments.ExecMode.DISTRIBUTE){
            int seed = new Random().nextInt(Short.MAX_VALUE + 1);
            BMP secret = null;
            try {
                secret = new BMP(arguments.secretPath);
            } catch (IOException e){
                System.out.println("Error reading secret image: " + e.getMessage());
                System.exit(1);
            }
            if ((secret.height * secret.width) % arguments.k != 0){
                System.out.println("Error: secret image pixel count must be divisible by k");
                System.exit(1);
            }

            if (arguments.k == 8){
                BMP[] carriers = new BMP[arguments.n];
                int count = 0;

                for (String path: carrierCandidates){
                    try {
                        BMP bmp = new BMP(path);
                        if (bmp.width == secret.width && bmp.height == secret.height) {
                            carriers[count++] = bmp;
                        }
                        if (count == arguments.n){
                            break;
                        }
                    } catch (Exception ignored) {}
                }
                if (count < arguments.n) {
                    System.err.println("Error: Only " + count + " valid BMP files of the same size as the secret found in directory. Expected " + arguments.n);
                    System.exit(1);
                }


                Shadow[] shadows = EncryptionAlgorithm.encrypt(secret.pixels, arguments.n, arguments.k, seed);

                try {
                    for(int i = 0; i < arguments.n; i++){
                        carriers[i].setSeed(seed);
                        carriers[i].setOrder(shadows[i].order());
                        carriers[i].pixels = LSB.distribute(carriers[i].pixels, shadows[i].data());
                        carriers[i].save();
                    }
                } catch (IOException e){
                    System.out.println("Error: could not embed the shadow into the carrier");
                    System.exit(1);
                }
            } else {
                BMP[] carriers = new BMP[arguments.n];
                int count = 0;
                int targetSize = 32 + 8*(secret.height * secret.width)/arguments.k;

                for (String path: carrierCandidates){
                    try {
                        BMP bmp = new BMP(path);
                        if (bmp.width * bmp.height >= targetSize) {
                            carriers[count++] = bmp;
                        }
                        if (count == arguments.n){
                            break;
                        }
                    } catch (Exception ignored) {}
                }
                if (count < arguments.n) {
                    System.err.println("Error: Only " + count + " valid BMP files with enough size to hide the secret found in directory. Expected " + arguments.n);
                    System.exit(1);
                }


                Shadow[] shadows = EncryptionAlgorithm.encrypt(secret.pixels, arguments.n, arguments.k, seed);

                try {
                    for(int i = 0; i < arguments.n; i++){
                        carriers[i].setSeed(seed);
                        carriers[i].setOrder(shadows[i].order());
                        carriers[i].pixels = LSB.distributeWithMetadata(carriers[i].pixels, shadows[i].data(), secret.height, secret.width);
                        carriers[i].save();
                    }
                } catch (IOException e){
                    System.out.println("Error: could not embed the shadow into the carrier");
                    System.exit(1);
                }
            }
        } else {
            if (arguments.k == 8){
                BMP[] carriers = new BMP[arguments.k];
                int count = 0;

                int targetWidth = 0, targetHeight = 0;
                for (String path: carrierCandidates){
                    try {
                        BMP bmp = new BMP(path);
                        if (count == 0){
                            targetHeight = bmp.height;
                            targetWidth = bmp.width;
                        }
                        if (bmp.width == targetWidth && bmp.height == targetHeight) {
                            carriers[count++] = bmp;
                        }
                        if (count == arguments.k){
                            break;
                        }
                    } catch (Exception ignored) {}
                }
                if (count < arguments.k) {
                    System.err.println("Error: Only " + count + " valid BMP files of the same size found in directory. Expected " + arguments.k);
                    System.exit(1);
                }

                int seed = carriers[0].seed;
                Shadow[] shadows = new Shadow[arguments.k];
                for(int i = 0; i < arguments.k; i++){
                    shadows[i] = new Shadow(
                            LSB.recover(carriers[i].pixels),
                            carriers[i].order
                    );
                }

                byte[] decrypted = EncryptionAlgorithm.decrypt(shadows, seed);

                try {
                    new BMP(decrypted, targetWidth, targetHeight).toFile(arguments.secretPath, carriers[0].header);
                } catch (IOException e){
                    System.out.println("Error: could not write the secret image file");
                    System.exit(1);
                }
            } else {

                BMP[] carriers = new BMP[arguments.k];
                int count = 0;

                for (String path: carrierCandidates){
                    try {
                        BMP bmp = new BMP(path);
                        carriers[count++] = bmp;
                        if (count == arguments.k){
                            break;
                        }
                    } catch (Exception ignored) {}
                }
                if (count < arguments.k) {
                    System.err.println("Error: Only " + count + " valid BMP files found in directory. Expected " + arguments.k);
                    System.exit(1);
                }

                int seed = carriers[0].seed;
                final int k = arguments.k;

                Shadow[] shadows = new Shadow[arguments.k];
                int width = 0, height = 0;

                for(int i = 0; i < arguments.k; i++){
                    LSB.RecoveryResult result = LSB.recoverWithMetadata(carriers[i].pixels, arguments.k);
                    shadows[i] = new Shadow(
                            result.shadow,
                            carriers[i].order
                    );
                    width = result.secretWidth;
                    height = result.secretHeight;
                }

                byte[] decrypted = EncryptionAlgorithm.decrypt(shadows, seed);


                try {
                    new BMP(decrypted, width, height).toFile(arguments.secretPath, carriers[0].header);
                } catch (IOException e) {
                    System.out.println("Error: could not write the secret image file");
                    System.exit(1);
                }

            }
        }

    }
}
