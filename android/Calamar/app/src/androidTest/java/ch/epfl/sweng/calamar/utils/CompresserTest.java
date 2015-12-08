package ch.epfl.sweng.calamar.utils;


import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.item.ImageItem;
import ch.epfl.sweng.calamar.recipient.User;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class CompresserTest {

    private static final byte[] testContent = {(byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47,
            (byte) 0x0d, (byte) 0x0a, (byte) 0x1a, (byte) 0x0a, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x0d, (byte) 0x49, (byte) 0x48, (byte) 0x44, (byte) 0x52,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x05, (byte) 0x08, (byte) 0x02, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x02, (byte) 0x0d, (byte) 0xb1, (byte) 0xb2, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x25, (byte) 0x49, (byte) 0x44, (byte) 0x41,
            (byte) 0x54, (byte) 0x08, (byte) 0x99, (byte) 0x4d, (byte) 0x8a, (byte) 0xb1,
            (byte) 0x0d, (byte) 0x00, (byte) 0x20, (byte) 0x0c, (byte) 0x80, (byte) 0xc0,
            (byte) 0xff, (byte) 0x7f, (byte) 0xc6, (byte) 0xc1, (byte) 0xc4, (byte) 0x96,
            (byte) 0x81, (byte) 0x05, (byte) 0xa8, (byte) 0x80, (byte) 0x67, (byte) 0xe0,
            (byte) 0xb0, (byte) 0xa8, (byte) 0xfc, (byte) 0x65, (byte) 0xba, (byte) 0xaa,
            (byte) 0xce, (byte) 0xb3, (byte) 0x97, (byte) 0x0b, (byte) 0x2b, (byte) 0xd9,
            (byte) 0x11, (byte) 0xfa, (byte) 0xa5, (byte) 0xad, (byte) 0x00, (byte) 0x06,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x49, (byte) 0x45,
            (byte) 0x4e, (byte) 0x44, (byte) 0xae, (byte) 0x42, (byte) 0x60, (byte) 0x82};

    @Test
    public void testCompressDecompress() {
        assertTrue(Arrays.equals(testContent, Compresser.decompress(Compresser.compress(testContent))));
        assertTrue(Arrays.equals(testContent, Compresser.decompress(testContent)));
    }

    @Test
    public void testCantDecompress() {
        final byte[] notCompressed = {(byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47,
                (byte) 0x0d, (byte) 0x0a, (byte) 0x1a, (byte) 0x0a, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x0d, (byte) 0x49};
        assertTrue(Arrays.equals(notCompressed, Compresser.decompress(notCompressed)));
    }

    @Test
    public void testCantCompress() {
        final byte[] tooShort = {0x44, 0x44, 0x22, 0x23, 0x45, 0x44, 0x44, 0x22, 0x23, 0x45
                , 0x44, 0x44, 0x22, 0x23, 0x45, 0x44, 0x44, 0x22, 0x23, 0x45, 0x44, 0x44, 0x22, 0x23, 0x45};
        final byte[] notCompressed = {(byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47,
                (byte) 0x0d, (byte) 0x0a, (byte) 0x1a, (byte) 0x0a, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x0d, (byte) 0x49, (byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47,
                (byte) 0x0d, (byte) 0x0a, (byte) 0x1a, (byte) 0x0a, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x0d, (byte) 0x49, (byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47,
                (byte) 0x0d, (byte) 0x0a, (byte) 0x1a, (byte) 0x0a, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x0d, (byte) 0x49, (byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47,
                (byte) 0x0d, (byte) 0x0a, (byte) 0x1a, (byte) 0x0a, (byte) 0x00, (byte) 0x00,
                (byte) 0x00, (byte) 0x0d, (byte) 0x49};
        final byte[] compressed = {(byte) 0x78, (byte) 0xDA, (byte) 0x12, 0x10, 0x23, 0x47, 0x12, 0x45, (byte) 0xa7, (byte) 0xd3, (byte) 0xef, (byte) 0xaa, (byte) 0xfa, 0x02, 0x21, 0x33, 0x22};
        assertTrue(Arrays.equals(Compresser.compress(compressed), compressed));
        assertFalse(Arrays.equals(notCompressed, Compresser.compress(notCompressed)));
        assertTrue(Arrays.equals(Compresser.compress(tooShort), tooShort));
        assertTrue(Arrays.equals(Compresser.decompress(tooShort), tooShort));
    }

    @Test
    public void testGetThumbnailSmallPicture() throws IOException {
        Bitmap small = getBitmapFromAsset("testSmall.jpg");
        byte[] data;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        small.compress(Bitmap.CompressFormat.PNG, 100, stream);
        data = stream.toByteArray();
        ImageItem item = new ImageItem(0, new User(0, "Alice"), new User(1, "Bob"), new Date(), data, "/blabla");
        assertTrue(Arrays.equals(data, Compresser.getImageThumbnail(item)));
    }

    @Test
    public void testGetThumbnail() throws IOException {
        Bitmap big = getBitmapFromAsset("testImage.jpg");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        big.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] data = stream.toByteArray();
        ImageItem item = new ImageItem(0, new User(0, "Alice"), new User(1, "Bob"), new Date(), data, "/blabla");
        assertFalse(Arrays.equals(data, Compresser.getImageThumbnail(item)));
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap big2 = Bitmap.createBitmap(big, 0, 0, big.getWidth(), big.getHeight(), matrix, true);
        ByteArrayOutputStream stream2 = new ByteArrayOutputStream();
        big2.compress(Bitmap.CompressFormat.PNG, 100, stream2);
        byte[] data2 = stream2.toByteArray();
        ImageItem item2 = new ImageItem(0, new User(0, "Alice"), new User(1, "Bob"), new Date(), data2, "/blabla");
        assertFalse(Arrays.equals(data2, Compresser.getImageThumbnail(item2)));
    }

    private Bitmap getBitmapFromAsset(String filePath) throws IOException {
        AssetManager assetManager = CalamarApplication.getInstance().getAssets();
        InputStream istr;
        istr = assetManager.open(filePath);
        return BitmapFactory.decodeStream(istr);
    }
}
