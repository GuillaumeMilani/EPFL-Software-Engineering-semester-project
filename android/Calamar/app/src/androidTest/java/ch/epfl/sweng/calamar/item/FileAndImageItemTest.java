package ch.epfl.sweng.calamar.item;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

import static junit.framework.Assert.assertEquals;

@RunWith(JUnit4.class)
public class FileAndImageItemTest {

    private static final String strContent = "abcdefghijklmnopqrstuvwxyz";

    private static final Date testDate = new Date(5);
    private static final User testFrom = new User(1, "bob");
    private static final Recipient testTo = new User(2, "alice");
    private static final int testId = 1;

    @Rule
    public TemporaryFolder testFolder = new TemporaryFolder();

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
        FileItem f2 = new FileItem(testId, testFrom, testTo, testDate, data, "f");
        FileItem f3 = builder.setData(strContent).setName("f").build();
        assertEquals(f1, f2);
        assertEquals(f1, f3);
        assertEquals(f2, f3);
    }

}
