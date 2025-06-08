package ar.edu.itba.cripto;

import java.util.Arrays;

public class LSB {
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

    public static byte[] recover(byte[] image){
        int len = image.length/8;

        byte[] result = new byte[len];

        int b = 0;
        for(int i = 0; i < len; i++){
            int v = 0;
            for (int bit = 7; bit >= 0; bit--) {
                v |= (image[b++] & 1) << bit;
            }
            result[i] = (byte) v;
        }

        return result;
    }

    public static RecoveryResult recoverWithMetadata(byte[] image, int k){
        int bitIndex = 0;
        int secretHeight = 0, secretWidth = 0;

        for (int i = 0; i < 16; i++) {
            int bit = image[bitIndex] & 1;
            secretHeight = (secretHeight << 1) | bit;
            bitIndex++;
        }
        for (int i = 0; i < 16; i++) {
            int bit = image[bitIndex] & 1;
            secretWidth = (secretWidth << 1) | bit;
            bitIndex++;
        }

        int shadowSize = (secretHeight * secretWidth) / k;
        byte[] result = new byte[shadowSize];

        for (int i = 0; i < shadowSize; i++) {
            int value = 0;
            for (int b = 0; b < 8; b++) {
                int bit = image[bitIndex] & 1;
                value = (value << 1) | bit;
                bitIndex++;
            }
            result[i] = (byte) value;
        }

        return new RecoveryResult(result, secretHeight, secretWidth);
    }

    public static byte[] distribute(byte[] image, byte[] shadow){
        byte[] result = Arrays.copyOf(image, image.length);

        for (int i = 0; i < shadow.length; i++) {
            byte b = shadow[i];
            for (int bit = 7; bit >= 0; bit--) {
                int imageIndex = i * 8 + (7 - bit);
                int bitValue = (b >> bit) & 1;

                result[imageIndex] = (byte) ((result[imageIndex] & 0xFE) | bitValue);
            }
        }

        return result;
    }


    public static byte[] distributeWithMetadata(byte[] image, byte[] shadow, int secretHeight, int secretWidth){

        byte[] meta = new byte[4];
        meta[0] = (byte) ((secretHeight >> 8) & 0xFF);
        meta[1] = (byte) (secretHeight & 0xFF);
        meta[2] = (byte) ((secretWidth >> 8) & 0xFF);
        meta[3] = (byte) (secretWidth & 0xFF);

        int totalBits = 32 + shadow.length * 8;

        byte[] result = Arrays.copyOf(image, image.length);

        int bitIndex = 0;

        for (int i = 0; i < 4; i++) {
            for (int b = 7; b >= 0; b--) {
                int bit = (meta[i] >> b) & 1;
                result[bitIndex] = (byte) ((result[bitIndex] & 0xFE) | bit);
                bitIndex++;
            }
        }

        for (byte value : shadow) {
            for (int b = 7; b >= 0; b--) {
                int bit = (value >> b) & 1;
                result[bitIndex] = (byte) ((result[bitIndex] & 0xFE) | bit);
                bitIndex++;
            }
        }

        return result;
    }

}
