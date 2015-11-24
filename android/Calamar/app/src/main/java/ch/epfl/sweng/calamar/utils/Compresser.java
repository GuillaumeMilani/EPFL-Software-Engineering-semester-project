package ch.epfl.sweng.calamar.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class Compresser {

    public static byte[] compress(byte[] data) {

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

        return outputStream.toByteArray();

    }

    public static byte[] decompress(byte[] data) {

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

    }

}