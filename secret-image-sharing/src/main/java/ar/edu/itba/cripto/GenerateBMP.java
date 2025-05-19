import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class GenerateBMP {
    public static void main(String[] args) {
        try {
            new java.io.File("varias").mkdirs();

            // Generate clave.bmp (secret image with diagonal gradient)
            generateBMP("varias/clave.bmp", 100, 100, true);
            // Generate 4 carrier BMPs (random noise)
            for (int i = 1; i <= 4; i++) {
                generateBMP("varias/carrier" + i + ".bmp", 100, 100, false);
            }
            System.out.println("BMPs generated in varias/");
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static void generateBMP(String path, int width, int height, boolean isSecret) throws Exception {
        // BMP header (54 bytes) + 256-color palette (1024 bytes)
        int paletteSize = 256 * 4; // 256 colors, 4 bytes each (RGBA)
        int fileSize = 54 + paletteSize + width * height; // Header + palette + pixels
        int offset = 54 + paletteSize; // Pixel data starts after header + palette
        byte[] header = new byte[54];

        // BMP file header (14 bytes)
        header[0] = 'B'; header[1] = 'M'; // Signature
        writeInt(header, 2, fileSize, ByteOrder.LITTLE_ENDIAN); // File size
        writeInt(header, 6, 0, ByteOrder.LITTLE_ENDIAN); // Reserved
        writeInt(header, 10, offset, ByteOrder.LITTLE_ENDIAN); // Pixel data offset

        // DIB header (40 bytes, BITMAPINFOHEADER)
        writeInt(header, 14, 40, ByteOrder.LITTLE_ENDIAN); // DIB header size
        writeInt(header, 18, width, ByteOrder.LITTLE_ENDIAN); // Width
        writeInt(header, 22, height, ByteOrder.LITTLE_ENDIAN); // Height
        writeShort(header, 26, 1, ByteOrder.LITTLE_ENDIAN); // Color planes
        writeShort(header, 28, 8, ByteOrder.LITTLE_ENDIAN); // Bits per pixel
        writeInt(header, 30, 0, ByteOrder.LITTLE_ENDIAN); // No compression
        writeInt(header, 34, width * height, ByteOrder.LITTLE_ENDIAN); // Image size
        writeInt(header, 38, 2835, ByteOrder.LITTLE_ENDIAN); // X pixels per meter
        writeInt(header, 42, 2835, ByteOrder.LITTLE_ENDIAN); // Y pixels per meter
        writeInt(header, 46, 256, ByteOrder.LITTLE_ENDIAN); // Colors in palette
        writeInt(header, 50, 256, ByteOrder.LITTLE_ENDIAN); // Important colors

        // Grayscale palette (256 colors, 1024 bytes: RGBA)
        byte[] palette = new byte[paletteSize];
        for (int i = 0; i < 256; i++) {
            palette[i * 4] = (byte) i;     // Blue
            palette[i * 4 + 1] = (byte) i; // Green
            palette[i * 4 + 2] = (byte) i; // Red
            palette[i * 4 + 3] = 0;        // Alpha
        }

        // Pixel data (8-bit indices, bottom-up)
        byte[] pixels = new byte[width * height];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = (height - 1 - y) * width + x; // Bottom-up
                if (isSecret) {
                    // Diagonal gradient for secret image
                    pixels[index] = (byte) ((x + y) % 256);
                } else {
                    // Random noise for carriers
                    pixels[index] = (byte) (Math.random() * 256);
                }
            }
        }

        // Write BMP file
        try (FileOutputStream fos = new FileOutputStream(path)) {
            fos.write(header);
            fos.write(palette);
            fos.write(pixels);
        }
    }

    private static void writeInt(byte[] buffer, int offset, int value, ByteOrder order) {
        ByteBuffer.wrap(buffer, offset, 4).order(order).putInt(value);
    }

    private static void writeShort(byte[] buffer, int offset, int value, ByteOrder order) {
        ByteBuffer.wrap(buffer, offset, 2).order(order).putShort((short) value);
    }
}