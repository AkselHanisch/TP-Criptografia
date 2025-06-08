package ar.edu.itba.cripto;

import ar.edu.itba.cripto.math.Lagrange;
import ar.edu.itba.cripto.math.Mod;
import ar.edu.itba.cripto.math.Pair;

import java.util.Arrays;
import java.util.Random;

public class EncryptionAlgroithm2 {

    private static final int PRIME_MOD = 257;

    public static Shadow[] encrypt(byte[] image, int n, int k, int seed, int height, int width) {
        byte[] data = xorImage(image, seed);

        Shadow[] shadows = new Shadow[n];
        int totalPolynomials = data.length / k;

        for(int i = 0; i < n; i++){
            shadows[i] = new Shadow(
                    new byte[totalPolynomials],
                    i+1,
                    height,
                    width
            ); //TODO
        }

        int j = 0;
        int pixels = data.length;
        int[][] polinomials = new int[totalPolynomials][k];
        int pol = 0;
        while (j < pixels){
            for(int i = 0; i < k; i++){
                polinomials[pol][i] = data[j] & 0xFF;
                j++;
            }
            pol++;
        }
        for(int p = 0; p < polinomials.length; p++){
            for(Shadow shadow: shadows){
                int f = evaluatePol(polinomials[p], shadow.order());
                while(f==256){
                    for(int i = 0; i < k; i++){
                        if (polinomials[p][i] > 0){
                            polinomials[p][i]--;
                            break;
                        }
                    }
                    f = evaluatePol(polinomials[p], shadow.order());
                }

                shadow.data()[p] = (byte) f;
            }
        }

        return shadows;
    }

    public static byte[] decrypt(Shadow[] shadows, int seed, int width, int height){
        int k = shadows.length;
        int imagePixels = height * width;

        int blocks = imagePixels/k;

        Pair[][] points = new Pair[blocks][k];
        for(int j = 0; j < blocks; j++){
            for(int i = 0; i < k; i++){
                Shadow shadow = shadows[i];
                points[j][i] = new Pair(shadow.order(), shadow.data()[j] & 0xFF);
            }
        }

        byte[] data = new byte[imagePixels];
        int idx = 0;
        for (int b = 0; b < blocks; b++) {
            int[] coefficients = new Lagrange(points[b], PRIME_MOD).getCoefficients();

            for (int i = 0; i < k; i++) {
                data[idx] = (byte) coefficients[i];
                idx++;
            }
        }

        return xorImage(data, seed);
    }


    private static int evaluatePol(int[] coefficients, int x) {
        int result = coefficients[0];
        for (int i = 1; i < coefficients.length; i++) {
            result = Mod.mod(result + Mod.mod(coefficients[i] * Mod.modPow(x, i, PRIME_MOD), PRIME_MOD), PRIME_MOD);
        }
        return Mod.mod(result, PRIME_MOD);
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
