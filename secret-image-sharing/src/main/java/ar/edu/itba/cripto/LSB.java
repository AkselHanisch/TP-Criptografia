package ar.edu.itba.cripto;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

public class LSB {


    public static byte[][] distribute(byte[][] image, byte[] shadow) {
        int height = image.length;
        int width = image[0].length;
        int totalPixels = height * width;
        int requiredPixels = shadow.length * 8;

        if (requiredPixels > totalPixels) {
            throw new IllegalArgumentException("Image too small to hold the shadow data.");
        }

        byte[][] result = new byte[height][width];

        for (int y = 0; y < height; y++) {
            System.arraycopy(image[y], 0, result[y], 0, width);
        }

        int bitIndex = 0;
        outer:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (bitIndex >= requiredPixels) break outer;

                int byteIndex = bitIndex / 8;
                int bitInByte = 7 - (bitIndex % 8);
                int bit = (shadow[byteIndex] >> bitInByte) & 1;

                result[y][x] = (byte) ((result[y][x] & 0xFE) | bit);
                bitIndex++;
            }
        }

        return result;
    }
    public static byte[] recover(byte[][] image) {
        int height = image.length;
        int width = image[0].length;
        int totalPixels = height * width;
        int resultLength = totalPixels / 8;
        byte[] result = new byte[resultLength];

        int bitIndex = 0;
        int byteIndex = 0;
        byte currentByte = 0;

        for (byte[] bytes : image) {
            for (int x = 0; x < width; x++) {
                int lsb = bytes[x] & 1;
                currentByte = (byte) ((currentByte << 1) | lsb);
                bitIndex++;

                if (bitIndex == 8) {
                    result[byteIndex++] = currentByte;
                    currentByte = 0;
                    bitIndex = 0;
                }
            }
        }

        return result;
    }


    public static void main(String[] args) throws IOException {

        Random random = new Random(10);

        BMP test = new BMP("target.bmp");

        byte[] secret = new byte[(test.width*test.height)/8];
        for(int i = 0; i < secret.length; i++){
            secret[i] = (byte) random.nextInt(256);
        }

        byte[][] distributed = distribute(test.pixels, secret);

        new BMP(distributed).toFile("distributed.bmp");

        byte[] recovered = recover(distributed);


        System.out.println(Arrays.toString(recovered));

        random = new Random(10);
        for(int i  = 0; i < secret.length; i++){
            if (recovered[i] != (byte) random.nextInt(256)){
                throw new RuntimeException("bad");
            }
        }


    }
}
