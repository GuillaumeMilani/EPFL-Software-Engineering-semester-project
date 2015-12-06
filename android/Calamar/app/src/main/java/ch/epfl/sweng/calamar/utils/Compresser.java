package ch.epfl.sweng.calamar.utils;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;
import ch.epfl.sweng.calamar.item.FileItem;
import ch.epfl.sweng.calamar.item.ImageItem;

/**
 * Utilitary class used to compress and decompress byte arrays of data using the native zlib implementation of java
 */
public final class Compresser {

    private static final int MIN_SIZE_FOR_COMPRESSION = 30;
    private static final int THUMBNAIL_SIZE = 100;
    private static final int BUFFER_SIZE = 1024;
    private static final byte HEADER_1 = 0x78;
    private static final byte HEADER_2 = (byte) 0xDA;
    private static final byte[] FOOTER = {0x10, 0x23, 0x47, 0x12, 0x45, (byte) 0xa7, (byte) 0xd3, (byte) 0xef, (byte) 0xaa, (byte) 0xfa, 0x02, 0x21, 0x33, 0x22};

    private Compresser() {
    }

    /**
     * Compresses the data if it is not already compressed
     *
     * @param data The data to be compressed
     * @return The compressed data, or the original data if it is already compressed
     */
    public static byte[] compress(byte[] data) {

        if (isCompressed(data) || data.length < MIN_SIZE_FOR_COMPRESSION) {
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

    /**
     * Decompresses the data if it is compressed
     *
     * @param data the data to be decompressed
     * @return The decompressed data, or the original data if it is not compressed
     */
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

    /**
     * Compresses (or removes) data from a FileItem, to reduce the size of the database.
     *
     * @param f The FileItem to be compressed
     * @return The compressed FileItem
     */
    public static FileItem compressDataForDatabase(FileItem f) {
        switch (f.getType()) {
            case FILEITEM:
                return new FileItem(f.getID(), f.getFrom(), f.getTo(), f.getDate(), f.getCondition(), null, f.getPath());
            case IMAGEITEM:
                return new ImageItem(f.getID(), f.getFrom(), f.getTo(), f.getDate(), f.getCondition(), getImageThumbnail((ImageItem) f), f.getPath());
            default:
                throw new IllegalArgumentException(CalamarApplication.getInstance().getString(R.string.expected_fileitem));
        }
    }

    /**
     * Returns a 100x100 thumbnail of the image as a byte array, or the image itself if it is smaller.
     *
     * @param i The ImageItem
     * @return a byte array representing the thumbnail
     */
    public static byte[] getImageThumbnail(ImageItem i) {
        final ByteArrayOutputStream stream = new ByteArrayOutputStream();
        final Bitmap bitmap = i.getBitmap();
        if (bitmap != null) {
            final int width = bitmap.getWidth();
            final int height = bitmap.getHeight();
            if (width <= THUMBNAIL_SIZE && height <= THUMBNAIL_SIZE) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            } else {
                Bitmap thumbnail;
                if (width > height) {
                    final double ratio = width / height;
                    thumbnail = ThumbnailUtils.extractThumbnail(bitmap, THUMBNAIL_SIZE, (int) Math.floor(THUMBNAIL_SIZE / ratio));
                } else {
                    final double ratio = height / width;
                    thumbnail = ThumbnailUtils.extractThumbnail(bitmap, (int) Math.floor(THUMBNAIL_SIZE / ratio), THUMBNAIL_SIZE);
                }
                thumbnail.compress(Bitmap.CompressFormat.PNG, 100, stream);
            }
            return stream.toByteArray();
        }
        Log.d("Bitmap", CalamarApplication.getInstance().getString(R.string.bitmap_of_is_null, i));
        return null;
    }

    /**
     * Checks if the data is compressed by this Compresser.
     *
     * @param data The data
     * @return true if it is compressed, false otherwise
     */
    private static boolean isCompressed(byte[] data) {
        if (data.length < FOOTER.length + 2) {
            return false;
        } else {
            final byte[] dataFooter = Arrays.copyOfRange(data, data.length - FOOTER.length, data.length);
            return (Arrays.equals(dataFooter, FOOTER) && data[0] == HEADER_1 && data[1] == HEADER_2);
        }
    }

}