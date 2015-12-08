package ch.epfl.sweng.calamar.item;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;
import ch.epfl.sweng.calamar.utils.Compresser;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(JUnit4.class)
public class FileAndImageItemTest {

    private static final String strContent = "abcdefghijklmnopqrstuvwxyz";

    private static final Date testDate = new Date(5);
    private static final User testFrom = new User(1, "bob");
    private static final Recipient testTo = new User(2, "alice");
    private static final int testId = 1;
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

    @Rule
    public final TemporaryFolder testFolder = new TemporaryFolder();

    @Test
    public void testSetFileAndSetData() throws IOException {
        File f = testFolder.newFile("f");
        FileWriter fw = new FileWriter(f.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);
        bw.write(strContent);
        bw.close();
        FileItem.Builder builder = new FileItem.Builder();
        builder.setFile(f);
        builder.setDate(testDate);
        builder.setFrom(testFrom);
        builder.setTo(testTo);
        builder.setID(testId);
        byte[] data = strContent.getBytes(Charset.forName("UTF-8"));
        FileItem f1 = builder.build();
        FileItem f2 = new FileItem(testId, testFrom, testTo, testDate, data, f.getAbsolutePath());
        FileItem f3 = builder.setData(strContent).setPath(f.getAbsolutePath()).build();
        assertEquals(f1, f2);
        assertEquals(f1, f3);
        assertEquals(f2, f3);
    }

    @Test
    public void testDataIsCompressed() {
        FileItem f = new FileItem(testId, testFrom, testTo, testDate, testContent, "/bla");
        assertFalse(Arrays.equals(testContent, f.getData()));
    }

    @Test
    public void testCanGetBitmap() throws IOException {
        Bitmap bitmap = getBitmapFromAsset("testImage.jpg");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapData = stream.toByteArray();
        ImageItem i = new ImageItem(testId, testFrom, testTo, testDate, bitmapData, "/bla");
        assertFalse(Arrays.equals(bitmapData, i.getData()));
        assertTrue(bitmap.sameAs(i.getBitmap()));
    }

    private Bitmap getBitmapFromAsset(String filePath) throws IOException {
        AssetManager assetManager = CalamarApplication.getInstance().getAssets();
        InputStream istr;
        istr = assetManager.open(filePath);
        return BitmapFactory.decodeStream(istr);
    }

}
