package ch.epfl.sweng.calamar;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

import ch.epfl.sweng.calamar.client.DatabaseClient;
import ch.epfl.sweng.calamar.client.DatabaseClientException;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.client.NetworkDatabaseClient;
import ch.epfl.sweng.calamar.client.NetworkProvider;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * Created by LPI on 30.11.2015.
 */
@RunWith(JUnit4.class)
public class NetworkDatabaseClientTest {

    private static String URL = "http://calamar.japan-impact.ch";
    private Recipient recipient = new User(42, "test");

    @Test
    public void testConstructorThrowsOnInvalidInput() {
        try {
            new NetworkDatabaseClient("", null);
            fail("IllegalArgumentException not thrown on invalid input");
        } catch (IllegalArgumentException e) {
            // ok
        }
        try {
            new NetworkDatabaseClient(null, null);
            fail("IllegalArgumentException not thrown on invalid input");
        } catch (IllegalArgumentException e) {
            // ok
        }
    }

    @Test
    public void testConstructorDontThrowsOnValidInput() {
        try {
            new NetworkDatabaseClient("", new NetworkProvider() {
                @Override
                public HttpURLConnection getConnection(URL url) throws IOException {
                    return null;
                }
            });

        } catch (IllegalArgumentException e) {
            fail("IllegalArgumentException was thrown on valid input");
        }
    }

    @Test
    public void getAllItemsThrowsOnInvalidInput()  {
        try {
            DatabaseClientLocator.getDatabaseClient().getAllItems(recipient, new Date(0), null);
            fail("IllegalArgumentException not thrown on invalid input");
        } catch (IllegalArgumentException e) {
            // ok
        } catch (DatabaseClientException e) {
            fail();
        }

        try {
            DatabaseClientLocator.getDatabaseClient().getAllItems(recipient, null, null);
            fail("IllegalArgumentException not thrown on invalid input");
        } catch (IllegalArgumentException e) {
            // ok
        } catch (DatabaseClientException e) {
            fail();
        }

        try {
            DatabaseClientLocator.getDatabaseClient().getAllItems(null, null, null);
            fail("IllegalArgumentException not thrown on invalid input");
        } catch (IllegalArgumentException e) {
            // ok
        } catch (DatabaseClientException e) {
            fail();
        }
    }

    @Test(expected = DatabaseClientException.class)
    public void getAllItemsThrowsOnIOError() throws IOException, DatabaseClientException {
        NetworkProvider disabledNetProvider = mock(NetworkProvider.class);
        doThrow(new IOException()).when(disabledNetProvider).getConnection((URL) any());
        DatabaseClient client = new NetworkDatabaseClient(URL,
                disabledNetProvider);
        client.getAllItems(recipient, new Date(0));
    }

    @Test(expected = DatabaseClientException.class)
    public void getAllItemsThrowsOnMalformedResponse() throws IOException, DatabaseClientException {
        NetworkProvider disabledNetProvider = mock(NetworkProvider.class);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        java.net.URL url = new URL(URL + "/items.php?action=retrieve");

        // todo insert better wrong json....
        InputStream response = new ByteArrayInputStream("malformed response".getBytes());
        doReturn(response).when(mockConnection).getInputStream();
        doReturn(new ByteArrayOutputStream()).when(mockConnection).getOutputStream();  // wtf...
        doReturn(mockConnection).when(disabledNetProvider).getConnection(url);
        DatabaseClient client = new NetworkDatabaseClient(URL,
                disabledNetProvider);

        client.getAllItems(recipient, new Date(0));
    }

    @Test(expected = DatabaseClientException.class)
    public void getAllItemsThrowsOnInvalidResponseCode() throws IOException, DatabaseClientException {
        NetworkProvider disabledNetProvider = mock(NetworkProvider.class);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        java.net.URL url = new URL(URL + "/items.php?action=retrieve");
        // todo insert better wrong json....
        InputStream response = new ByteArrayInputStream("malformed response".getBytes());
        doReturn(response).when(mockConnection).getInputStream();
        doReturn(new ByteArrayOutputStream()).when(mockConnection).getOutputStream();  // wtf...
        doReturn(404).when(mockConnection).getResponseCode();
        doReturn(mockConnection).when(disabledNetProvider).getConnection(url);
        DatabaseClient client = new NetworkDatabaseClient(URL,
                disabledNetProvider);

        client.getAllItems(recipient, new Date(0));
    }


}
