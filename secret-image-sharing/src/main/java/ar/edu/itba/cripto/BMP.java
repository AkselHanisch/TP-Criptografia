package ar.edu.itba.cripto;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BMP {

    private static final int HEADER_BYTES_READ = 34;

    public final int width;
    public final int height;
    public final byte[][] pixels;

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

    public BMP(String filename) throws IOException{

        try (DataInputStream stream = new DataInputStream(new BufferedInputStream(Files.newInputStream(Paths.get(filename))))){

            if (stream.readUnsignedByte() != 'B' || stream.readUnsignedByte() != 'M'){
                throw new IOException("Not a valid BMP file");
            }
            stream.skipBytes(4);
            seed = Short.reverseBytes(stream.readShort()) & 0xFFFF;
            order = Short.reverseBytes(stream.readShort()) & 0xFFFF;

            int bitmapOffset = Integer.reverseBytes(stream.readInt());

            int bitmapInfoHeaderLength = Integer.reverseBytes(stream.readInt());
            if (bitmapInfoHeaderLength != 40){
                throw new IOException("Unsupported BMP version (must be Windows 3.x format)");
            }

            width = Integer.reverseBytes(stream.readInt());
            height = Integer.reverseBytes(stream.readInt());

            stream.skipBytes(2);

            int bitsPerPixel = Short.reverseBytes(stream.readShort()) & 0xFFFF;
            if (bitsPerPixel != 8){
                throw new IOException("Unsupported BPM format (must be 8 bits per pixel)");
            }

            int compressionType = Integer.reverseBytes(stream.readInt());
            if (compressionType != 0){
                throw new IOException("BMP file must be uncompressed");
            }

            int rowSize = ((width + 3) / 4) * 4;
            int padding = rowSize - width;

            pixels = new byte[height][width];

            long skipped = stream.skip(bitmapOffset - HEADER_BYTES_READ);
            if (skipped < (bitmapOffset - HEADER_BYTES_READ)) {
                throw new IOException("Could not read BMP image data");
            }

            for (int row = height - 1; row >= 0; row--) {
                stream.readFully(pixels[row], 0, width);
                stream.skipBytes(padding);
            }

        }
    }

    public void toFile(String filename) throws IOException{
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
            out.writeInt(Integer.reverseBytes(fileSize)); // file size
            out.writeShort(Short.reverseBytes((short) seed)); // reserved1
            out.writeShort(Short.reverseBytes((short) order)); // reserved2
            out.writeInt(Integer.reverseBytes(dataOffset)); // offset to pixel array

            out.writeInt(Integer.reverseBytes(40)); // header size
            out.writeInt(Integer.reverseBytes(width));
            out.writeInt(Integer.reverseBytes(height));
            out.writeShort(Short.reverseBytes((short) 1)); // color planes
            out.writeShort(Short.reverseBytes((short) 8)); // bits per pixel
            out.writeInt(Integer.reverseBytes(0)); // compression (0 = none)
            out.writeInt(Integer.reverseBytes(pixelArraySize)); // size of pixel array
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

    public static void main(String[] args) throws IOException {
        BMP bmp1 = new BMP("secretoK8/Angelinassd.bmp");
        BMP bmp2 = new BMP("secretoK8/Gracessd.bmp");
        BMP bmp3 = new BMP("secretoK8/Jimssd.bmp");
        BMP bmp4 = new BMP("secretoK8/Lizssd.bmp");
        BMP bmp5 = new BMP("secretoK8/Robertossd.bmp");
        BMP bmp6 = new BMP("secretoK8/Shakirassd.bmp");
        BMP bmp7 = new BMP("secretoK8/Susanassd.bmp");
        BMP bmp8 = new BMP("secretoK8/Whitneyssd.bmp");

        bmp8.toFile("Copy.bmp");

        BMP copy8 = new BMP("Copy.bmp");

        System.out.println();
    }
}
