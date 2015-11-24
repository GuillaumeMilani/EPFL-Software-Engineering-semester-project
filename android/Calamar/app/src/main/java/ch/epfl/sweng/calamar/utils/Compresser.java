package ch.epfl.sweng.calamar.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Compresser {

    private static final int BUFFER_SIZE = 1024;
    private static final byte HEADER_1 = 0x78;
    private static final byte HEADER_2 = (byte) 0xDA;
    private static final byte[] FOOTER = {0x10, 0x23, 0x47, 0x12, 0x45, (byte) 0xa7, (byte) 0xd3, (byte) 0xef, (byte) 0xaa, (byte) 0xfa, 0x02, 0x21, 0x33, 0x22};

    public static byte[] compress(byte[] data) {

        if (isCompressed(data)) {
            return data;
        } else {
            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);

            deflater.setInput(data);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);

            deflater.finish();

            byte[] buffer = new byte[BUFFER_SIZE];

            try {
                while (!deflater.finished()) {

                    int count = deflater.deflate(buffer); // returns the generated code... index

                    outputStream.write(buffer, 0, count);

                }
                deflater.end();

                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //Set footer
            byte[] result = outputStream.toByteArray();
            byte[] toReturn = new byte[result.length + FOOTER.length];
            System.arraycopy(result, 0, toReturn, 0, result.length);
            System.arraycopy(FOOTER, 0, toReturn, toReturn.length - FOOTER.length, FOOTER.length);
            return toReturn;
        }
    }

    public static byte[] decompress(byte[] data) {

        if (isCompressed(data)) {
            byte[] toDecode = Arrays.copyOf(data, data.length - FOOTER.length);

            Inflater inflater = new Inflater();

            inflater.setInput(toDecode);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(toDecode.length);

            byte[] buffer = new byte[BUFFER_SIZE];

            try {
                while (!inflater.finished()) {

                    int count = inflater.inflate(buffer);

                    outputStream.write(buffer, 0, count);

                }

                outputStream.close();

                return outputStream.toByteArray();
            } catch (IOException | DataFormatException e) {
                e.printStackTrace();
            }
            return null;
        } else {
            return data;
        }

    }

    private static boolean isCompressed(byte[] data) {
        if (data.length < FOOTER.length + 2) {
            return false;
        } else {
            byte[] dataFooter = Arrays.copyOfRange(data, data.length - FOOTER.length, data.length);
            return (Arrays.equals(dataFooter, FOOTER) && data[0] == HEADER_1 && data[1] == HEADER_2);
        }
    }

}