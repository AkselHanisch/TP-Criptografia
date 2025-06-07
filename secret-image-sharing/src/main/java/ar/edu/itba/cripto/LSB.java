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
    public static RecoveryResult recover(byte[][] image) {
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

        return new RecoveryResult(result, height, width);
    }

    public static byte[][] distributeWithMetadata(byte[][] image, byte[] shadow, int secretHeight, int secretWidth){
        int rows = image.length;
        int cols = image[0].length;

        byte[] meta = new byte[4];
        meta[0] = (byte) ((secretHeight >> 8) & 0xFF);
        meta[1] = (byte) (secretHeight & 0xFF);
        meta[2] = (byte) ((secretWidth >> 8) & 0xFF);
        meta[3] = (byte) (secretWidth & 0xFF);

        int totalBits = 32 + shadow.length * 8;

        byte[][] result = new byte[rows][cols];
        for (int i = 0; i < rows; i++) {
            result[i] = image[i].clone();
        }

        int bitIndex = 0;

        for (int i = 0; i < 4; i++) {
            for (int b = 7; b >= 0; b--) {
                int bit = (meta[i] >> b) & 1;
                int row = bitIndex / cols;
                int col = bitIndex % cols;
                result[row][col] = (byte) ((result[row][col] & 0xFE) | bit);
                bitIndex++;
            }
        }

        for (byte value : shadow) {
            for (int b = 7; b >= 0; b--) {
                int bit = (value >> b) & 1;
                int row = bitIndex / cols;
                int col = bitIndex % cols;
                result[row][col] = (byte) ((result[row][col] & 0xFE) | bit);
                bitIndex++;
            }
        }

        return result;
    }

    public static class RecoveryResult {
        public final byte[] shadow;
        public final int secretHeight;
        public final int secretWidth;

        public RecoveryResult(byte[] shadow, int secretHeight, int secretWidth) {
            this.shadow = shadow;
            this.secretHeight = secretHeight;
            this.secretWidth = secretWidth;
        }
    }

    public static RecoveryResult recoverWithMetadata(byte[][] image, int k){
        int cols = image[0].length;

        int bitIndex = 0;

        int secretHeight = 0, secretWidth = 0;
        for (int i = 0; i < 16; i++) {
            int row = bitIndex / cols;
            int col = bitIndex % cols;
            int bit = image[row][col] & 1;
            secretHeight = (secretHeight << 1) | bit;
            bitIndex++;
        }
        for (int i = 0; i < 16; i++) {
            int row = bitIndex / cols;
            int col = bitIndex % cols;
            int bit = image[row][col] & 1;
            secretWidth = (secretWidth << 1) | bit;
            bitIndex++;
        }

        int shadowSize = (secretHeight * secretWidth) / k;
        byte[] shadow = new byte[shadowSize];

        for (int i = 0; i < shadowSize; i++) {
            int value = 0;
            for (int b = 0; b < 8; b++) {
                int row = bitIndex / cols;
                int col = bitIndex % cols;
                int bit = image[row][col] & 1;
                value = (value << 1) | bit;
                bitIndex++;
            }
            shadow[i] = (byte) value;
        }

        return new RecoveryResult(shadow, secretHeight, secretWidth);
    }

}
