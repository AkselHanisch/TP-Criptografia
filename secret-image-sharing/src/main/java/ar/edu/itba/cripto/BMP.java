package ar.edu.itba.cripto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

public class BMP {
    public static final int OFFSET_PIXEL_ARRAY_POSITION = 10;

    public String filename;

    public int seed;
    public int order;

    public int width;
    public int height;

    public byte[] header;
    public byte[] pixels;

    public BMP(byte[] pixels, int width, int height){
        this.pixels = pixels;
        this.width = width;
        this.height = height;
    }

    public BMP(String filename) throws IOException {
        this.filename = filename;

        byte[] data = Files.readAllBytes(Path.of(filename));

        if (data[0] != 'B' || data[1] != 'M') {
            throw new IOException("Not a valid BMP file: " + filename);
        }

        int bitsPerPixel = Byte.toUnsignedInt(data[28]) | (Byte.toUnsignedInt(data[29]) << 8);
        if (bitsPerPixel != 8) {
            throw new IOException("Unsupported BMP format: " + bitsPerPixel + " bits per pixel (expected 8)");
        }

        int offset = (data[OFFSET_PIXEL_ARRAY_POSITION] & 0xFF)
                | ((data[OFFSET_PIXEL_ARRAY_POSITION + 1] & 0xFF) << 8)
                | ((data[OFFSET_PIXEL_ARRAY_POSITION + 2] & 0xFF) << 16)
                | ((data[OFFSET_PIXEL_ARRAY_POSITION + 3] & 0xFF) << 24);

        if (offset <= 0 || offset > data.length) {
            throw new IOException("Invalid BMP pixel offset: " + offset);
        }

        header = Arrays.copyOfRange(data, 0, offset);

        seed = Byte.toUnsignedInt(data[6]) | (Byte.toUnsignedInt(data[7]) << 8);
        order = Byte.toUnsignedInt(data[8]) | (Byte.toUnsignedInt(data[9]) << 8);


        width = (data[18] & 0xFF) | ((data[19] & 0xFF) << 8);
        height = (data[22] & 0xFF) | ((data[23] & 0xFF) << 8);

        pixels = Arrays.copyOfRange(data, offset, data.length);
    }

    public void setSeed(int seed){
        header[6] = (byte) (seed & 0xFF);
        header[7] = (byte) ((seed >> 8) & 0xFF);
    }

    public void setOrder(int order){
        header[8] = (byte) (order & 0xFF);
        header[9] = (byte) ((order >> 8) & 0xFF);
    }

    public void save() throws IOException{
        toFile(filename, header);
    }

    public void toFile(String filename, byte[] header) throws IOException {
        header[18] = (byte) (width & 0xFF);
        header[19] = (byte) ((width >> 8) & 0xFF);
        header[20] = (byte) ((width >> 16) & 0xFF);
        header[21] = (byte) ((width >> 24) & 0xFF);

        header[22] = (byte) (height & 0xFF);
        header[23] = (byte) ((height >> 8) & 0xFF);
        header[24] = (byte) ((height >> 16) & 0xFF);
        header[25] = (byte) ((height >> 24) & 0xFF);

        byte[] result = new byte[header.length + pixels.length];
        System.arraycopy(header, 0, result, 0, header.length);
        System.arraycopy(pixels, 0, result, header.length, pixels.length);
        Files.write(Paths.get(filename), result);
    }
}
