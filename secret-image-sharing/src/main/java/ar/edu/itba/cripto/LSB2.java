package ar.edu.itba.cripto;

import java.util.Arrays;

public class LSB2 {
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

    public static LSB2.RecoveryResult recover(byte[] image){
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

        return new LSB2.RecoveryResult(result, 0, 0); //TODO
    }

    public byte[] distribute(byte[] image, byte[] shadow){
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

}
