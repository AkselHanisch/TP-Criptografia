package ar.edu.itba.cripto;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class BMP {

    public static final int OFFSET_PIXEL_ARRAY_POSITION = 10;

    public String filename;

    public int width;
    public int height;
    public byte[][] pixels;

    public int seed;
    public int order;

    public BMP(byte[][] pixels, int seed, int order){
        this.pixels = pixels;
        this.height = pixels.length;
        this.width = pixels[0].length;
        this.seed = seed;
        this.order = order;
    }

    public BMP(byte[][] pixels){
        this(pixels, 0, 0);
    }

    public BMP(String filename) throws IOException {
        this.filename = filename;

        byte[] data = Files.readAllBytes(Path.of(filename));

        int offset = (data[OFFSET_PIXEL_ARRAY_POSITION] & 0xFF)
                | ((data[OFFSET_PIXEL_ARRAY_POSITION + 1] & 0xFF) << 8)
                | ((data[OFFSET_PIXEL_ARRAY_POSITION + 2] & 0xFF) << 16)
                | ((data[OFFSET_PIXEL_ARRAY_POSITION + 3] & 0xFF) << 24);

        if (offset <= 0 || offset > data.length) {
            throw new IOException("Invalid BMP pixel offset: " + offset);
        }

        byte[] header = Arrays.copyOfRange(data, 0, offset);
        byte[] pixelsTEMP = Arrays.copyOfRange(data, offset, data.length);
    }

    public void toFile(String filename) throws IOException{
        this.filename = filename;
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(Files.newOutputStream(Paths.get(filename))))){

            int rowSize = ((width + 3) / 4) * 4;
            int padding = rowSize - width;
            int pixelArraySize = rowSize * height;
            int palleteSize = 256 * 4;
            int headerSize = 54;
            int fileSize = headerSize + palleteSize + pixelArraySize;
            int dataOffset = headerSize + palleteSize;

            out.writeByte('B');
            out.writeByte('M');

            out.writeInt(Integer.reverseBytes(fileSize));

            out.writeShort(Short.reverseBytes((short) seed));
            out.writeShort(Short.reverseBytes((short) order));

            out.writeInt(Integer.reverseBytes(dataOffset));

            out.writeInt(Integer.reverseBytes(40)); //header size

            out.writeInt(Integer.reverseBytes(width));
            out.writeInt(Integer.reverseBytes(height));

            out.writeShort(Short.reverseBytes((short) 1)); // color planes

            out.writeShort(Short.reverseBytes((short) 8)); // bits per pixel

            out.writeInt(Integer.reverseBytes(0)); //compresion

            out.writeInt(Integer.reverseBytes(pixelArraySize));
            out.writeInt(Integer.reverseBytes(2835)); // horizontal resolution (pixels/meter)
            out.writeInt(Integer.reverseBytes(2835)); // vertical resolution (pixels/meter)
            out.writeInt(Integer.reverseBytes(256)); // colors in palette
            out.writeInt(Integer.reverseBytes(0));

            for (int i = 0; i < 256; i++) {
                out.writeByte(i);     // Blue
                out.writeByte(i);     // Green
                out.writeByte(i);     // Red
                out.writeByte(0);     // Reserved
            }

            byte[] paddingBytes = new byte[padding];
            for (int row = height - 1; row >= 0; row--) {
                out.write(pixels[row], 0, width);
                out.write(paddingBytes);
            }
        }
    }

    public Shadow toShadow(){
        return new Shadow(
                LSB.recover(pixels).shadow,
                order,
                height,
                width
        );
    }

    public Shadow toShadowWithMetadata(int k){
        LSB.RecoveryResult recovery = LSB.recoverWithMetadata(pixels, k);
        return new Shadow(
                recovery.shadow,
                order,
                recovery.secretHeight,
                recovery.secretWidth
        );
    }

    public void printAscii(){
        for (int i = 0; i < height; i++){
            for (int j = 0; j < width; j++){
                int v = pixels[i][j] & 0xFF;
                char c = v > 200 ? ' ' : v > 150 ? '.' : v > 100 ? '*' : v > 50 ? 'x' : '#';
                System.out.print(c);
            }

            System.out.println();
        }
    }

}
