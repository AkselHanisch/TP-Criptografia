package ar.edu.itba.cripto;

import ar.edu.itba.cripto.math.Lagrange;
import ar.edu.itba.cripto.math.Pair;

import java.io.IOException;
import java.util.Random;

public class EncryptionAlgorithm {

    private static final int PRIME_MOD = 257;
    public static Shadow[] encrypt(byte[] image, int n, int k, int seed) {
        byte[] data = xorImage(image, seed);

        int totalPolynomials = data.length / k;

        Shadow[] shadows = new Shadow[n];
        for (int i = 0; i < n; i++) {
            shadows[i] = new Shadow(new byte[totalPolynomials], i + 1);
        }

        for (int p = 0; p < totalPolynomials; p++) {
            int[] coeffs = new int[k];
            for (int i = 0; i < k; i++) {
                coeffs[i] = Byte.toUnsignedInt(data[p * k + i]);
            }

            boolean ok;
            do {
                ok = true;
                for (int j = 0; j < n; j++) {
                    int y = evaluatePol(coeffs, j + 1);
                    if (y == 256) {
                        ok = false;
                        for (int t = 0; t < k; t++) {
                            if (coeffs[t] > 0) {
                                coeffs[t]--;
                                break;
                            }
                        }
                        break;
                    }
                }
            } while (!ok);

            for (int j = 0; j < n; j++) {
                int y = evaluatePol(coeffs, j + 1);
                shadows[j].data()[p] = (byte) y;
            }
        }

        return shadows;
    }

    public static byte[] decrypt(Shadow[] shadows, int seed){
        int k = shadows.length;
        int blocks = shadows[0].data().length;

        byte[] result = new byte[blocks * k];

        Pair[][] points = new Pair[blocks][k];
        for(int j = 0; j < blocks; j++){
            for(int i = 0; i < k; i++){
                Shadow shadow = shadows[i];
                points[j][i] = new Pair(shadow.order(), shadow.data()[j] & 0xFF);
            }
        }

        int idx = 0;
        for (int b = 0; b < blocks; b++) {
            int[] coefficients = new Lagrange(points[b], PRIME_MOD).getCoefficients();

            for (int i = 0; i < k; i++) {
                result[idx] = (byte) coefficients[i];
                idx++;
            }
        }

        return xorImage(result, seed);
    }


    private static int evaluatePol(int[] coefficients, int x) {
        long result = 0;
        long powX = 1;

        for (int coef : coefficients) {
            result = (result + (coef * powX) % PRIME_MOD) % PRIME_MOD;
            powX = (powX * x) % PRIME_MOD;
        }

        return (int) ((result + PRIME_MOD) % PRIME_MOD);
    }


    private static byte[] xorImage(byte[] data, int seed) {
        Random random = new Random();
        random.setSeed(seed);

        byte[] result = new byte[data.length];

        for (int i = 0; i < data.length; i++) {
            int randomByte = random.nextInt(256);
            result[i] = (byte) (((int) data[i] & 0xFF) ^ randomByte);
        }

        return result;
    }

}
