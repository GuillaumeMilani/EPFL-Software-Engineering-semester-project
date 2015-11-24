package ch.epfl.sweng.calamar.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Compresser {

    private static final byte header1 = 0x78;
    private static final byte header2 = (byte) 0xDA;
    private static final byte footer1 = 0x17;
    private static final byte footer2 = 0x37;
    private static final byte footer3 = 0x67;
    private static final byte footer4 = (byte) 0xa7;

    public static byte[] compress(byte[] data) {

        if (isCompressed(data)) {
            return data;
        } else {
            Deflater deflater = new Deflater(Deflater.BEST_COMPRESSION);

            deflater.setInput(data);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);

            deflater.finish();

            byte[] buffer = new byte[1024];

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
            byte[] toReturn = new byte[result.length + 4];
            System.arraycopy(result, 0, toReturn, 0, result.length);
            toReturn[toReturn.length - 4] = footer1;
            toReturn[toReturn.length - 3] = footer2;
            toReturn[toReturn.length - 2] = footer3;
            toReturn[toReturn.length - 1] = footer4;
            return toReturn;
        }
    }

    public static byte[] decompress(byte[] data) {

        if (isCompressed(data)) {
            Arrays.copyOf(data, data.length - 4);

            Inflater inflater = new Inflater();

            inflater.setInput(data);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);

            byte[] buffer = new byte[1024];

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
        return (data[data.length - 1] == footer4 && data[data.length - 2] == footer3
                && data[data.length - 3] == footer2 && data[data.length - 4] == footer1
                && data[0] == header1 && data[1] == header2);
    }

}