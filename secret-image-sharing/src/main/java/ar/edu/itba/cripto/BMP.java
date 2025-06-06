package ar.edu.itba.cripto;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

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

    public BMP(String filename) throws IOException {
        try (DataInputStream stream = new DataInputStream(new BufferedInputStream(Files.newInputStream(Paths.get(filename))))) {
            if (stream.readUnsignedByte() != 'B' || stream.readUnsignedByte() != 'M') {
                throw new IOException("Not a valid BMP file: " + filename);
            }

            stream.skipBytes(4);

            seed = Short.reverseBytes(stream.readShort()) & 0xFFFF;
            order = Short.reverseBytes(stream.readShort()) & 0xFFFF;


            int bitmapOffset = Integer.reverseBytes(stream.readInt());

            int bitmapInfoHeaderLength = Integer.reverseBytes(stream.readInt());
            if (bitmapInfoHeaderLength != 40) {
                throw new IOException("Unsupported BMP version (header size " + bitmapInfoHeaderLength + ", expected 40): " + filename);
            }

            width = Integer.reverseBytes(stream.readInt());
            height = Integer.reverseBytes(stream.readInt());

            stream.skipBytes(2);

            int bitsPerPixel = Short.reverseBytes(stream.readShort()) & 0xFFFF;
            if (bitsPerPixel != 8) {
                throw new IOException("Unsupported BMP format (bits per pixel " + bitsPerPixel + ", expected 8): " + filename);
            }

            int compressionType = Integer.reverseBytes(stream.readInt());
            if (compressionType != 0) {
                throw new IOException("BMP file is compressed (compression type " + compressionType + "): " + filename);
            }

            int rowSize = ((width + 3) / 4) * 4;
            int padding = rowSize - width;

            pixels = new byte[height][width];

            long skipped = stream.skip(bitmapOffset - HEADER_BYTES_READ);
            if (skipped != bitmapOffset - HEADER_BYTES_READ) {
                throw new IOException("Failed to reach pixel data (offset " + bitmapOffset + ", skipped " + skipped + " of " + (bitmapOffset - HEADER_BYTES_READ) + "): " + filename);
            }

            for (int row = height - 1; row >= 0; row--) {
                stream.readFully(pixels[row], 0, width);
                stream.skipBytes(padding);
            }

        } catch (FileNotFoundException e) {
            throw new IOException("File not found: " + filename);
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
                LSB.recover(pixels),
                order
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



    public static void main(String[] args) throws IOException{

        BMP secret = new BMP("testSecreto/Angelinassd.bmp");

        Shadow[] shadows = EncryptionAlgorithm.encrypt(secret, 8, 8, 10);


        BMP[] carriers = {
             new BMP("testSecreto/Angelinassd.bmp"),
               new BMP("testSecreto/Gracessd.bmp"),
               new BMP("testSecreto/Jimssd.bmp"),
             new BMP("testSecreto/Lizssd.bmp"),
              new BMP("testSecreto/Robertossd.bmp"),
               new BMP("testSecreto/Shakirassd.bmp"),
               new BMP("testSecreto/Susanassd.bmp"),
              new BMP("testSecreto/Whitneyssd.bmp")
        };

        for(int i = 0; i < shadows.length; i++){
            carriers[i] = new BMP(LSB.distribute(carriers[i].pixels, shadows[i].data()), 10, shadows[i].order());
            carriers[i].toFile("secretoOculto/" + i + ".bmp");
        }


        Shadow[] resultShadows = Arrays.stream(carriers).map(BMP::toShadow).toArray(Shadow[]::new);
        new BMP(EncryptionAlgorithm.decrypt(resultShadows, 10, carriers[0].width, carriers[0].height)).toFile("result.bmp");



        System.out.println();


    }
}
