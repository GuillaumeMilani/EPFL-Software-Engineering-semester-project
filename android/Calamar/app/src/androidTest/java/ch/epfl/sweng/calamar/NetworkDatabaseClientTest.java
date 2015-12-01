package ch.epfl.sweng.calamar;

import android.support.test.espresso.core.deps.guava.collect.ImmutableSet;

import com.google.android.gms.common.api.GoogleApiClient;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Set;

import ch.epfl.sweng.calamar.client.DatabaseClient;
import ch.epfl.sweng.calamar.client.DatabaseClientException;
import ch.epfl.sweng.calamar.client.DatabaseClientLocator;
import ch.epfl.sweng.calamar.client.NetworkDatabaseClient;
import ch.epfl.sweng.calamar.client.NetworkProvider;
import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.condition.PositionCondition;
import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.item.SimpleTextItem;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

import static junit.framework.Assert.assertTrue;
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
        NetworkProvider networkProvider = mock(NetworkProvider.class);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        DatabaseClient client = new NetworkDatabaseClient(URL,
                networkProvider);
        java.net.URL url = new URL(URL + "/items.php?action=retrieve");

        // todo insert better wrong json....
        InputStream response = new ByteArrayInputStream(("malformed response").getBytes());
        doReturn(response).when(mockConnection).getInputStream();
        doReturn(new ByteArrayOutputStream()).when(mockConnection).getOutputStream();  // wtf...
        doReturn(mockConnection).when(networkProvider).getConnection(url);


        client.getAllItems(recipient, new Date(0));
    }

    @Test(expected = DatabaseClientException.class)
    public void getAllItemsThrowsOnInvalidResponseCode() throws IOException, DatabaseClientException {
        NetworkProvider networkProvider = mock(NetworkProvider.class);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        DatabaseClient client = new NetworkDatabaseClient(URL,
                networkProvider);

        java.net.URL url = new URL(URL + "/items.php?action=retrieve");
        doReturn(new ByteArrayOutputStream()).when(mockConnection).getOutputStream();  // wtf...
        doReturn(404).when(mockConnection).getResponseCode();
        doReturn(mockConnection).when(networkProvider).getConnection(url);


        client.getAllItems(recipient, new Date(0));
    }

    @Test
    public void getAllItemsWorksOnValidResponse() throws IOException, DatabaseClientException {
        NetworkProvider networkProvider = mock(NetworkProvider.class);
        HttpURLConnection mockConnection = mock(HttpURLConnection.class);
        DatabaseClient client = new NetworkDatabaseClient(URL,
                networkProvider);

        java.net.URL url = new URL(URL + "/items.php?action=retrieve");

        InputStream response = new ByteArrayInputStream(("[\n" +
                "  {\n" +
                "    \"type\":\"SIMPLETEXTITEM\",\n" +
                "    \"ID\":1,\n" +
                "    \"message\":\"Hello Bob, it's Alice !\",\n" +
                "    \"from\": {\n" +
                "        \"name\":\"Alice\",\n" +
                "        \"ID\":1,\n" +
                "        \"type\":\"user\"\n" +
                "    },\n" +
                "   \"to\": {\n" +
                "        \"name\":\"Bob\",\n" +
                "        \"ID\":2,\n" +
                "        \"type\":\"user\"\n" +
                "    },\n" +
                "   \"date\":1445198510,\n" +
                "   \"condition\": {\n" +
                "       \"type\":\"or\",\n" +
                "       \"a\":{\n" +
                "           \"type\":\"POSITIONCONDITION\",\n" +
                "           \"latitude\":46.495,\n" +
                "           \"longitude\":6.513,\n" +
                "           \"radius\":10\n" +
                "       },\n" +
                "       \"b\":{\n" +
                "           \"type\":\"POSITIONCONDITION\",\n" +
                "           \"latitude\":47.495,\n" +
                "           \"longitude\":7.513,\n" +
                "           \"radius\":10\n" +
                "       }\n" +
                "   }\n" +
                "  },\n" +
                "  {\n" +
                "    \"type\":\"SIMPLETEXTITEM\",\n" +
                "    \"ID\":2,\n" +
                "    \"message\":\"Hello Bob, it's Carol !\",\n" +
                "    \"from\": {\n" +
                "        \"name\":\"Carol\",\n" +
                "        \"ID\":3,\n" +
                "        \"type\":\"user\"\n" +
                "    },\n" +
                "   \"to\": {\n" +
                "        \"name\":\"Bob\",\n" +
                "        \"ID\":2,\n" +
                "        \"type\":\"user\"\n" +
                "    },\n" +
                "   \"date\":1445198520,\n" +
                "  }\n" +
                "]").getBytes());


        User alice = new User(1, "Alice");
        User bob = new User(2, "Bob");
        User carol = new User(3, "Carol");


        // to shut his mouth
        CalamarApplication.getInstance().setGoogleApiClient(mock(GoogleApiClient.class));
        Condition cond1 = new PositionCondition(46.495, 6.513, 10);
        Condition cond2 = new PositionCondition(47.495, 7.513, 10);


        Item item1 = new SimpleTextItem(1, alice, bob, new Date(1445198510), Condition.or(cond1, cond2), "Hello Bob, it's Alice !");
        Item item2 = new SimpleTextItem(2, carol, bob, new Date(1445198520), "Hello Bob, it's Carol !");
        List<Item> items = Arrays.asList(item1, item2);

        doReturn(response).when(mockConnection).getInputStream();
        doReturn(new ByteArrayOutputStream()).when(mockConnection).getOutputStream();  // wtf...
        doReturn(201).when(mockConnection).getResponseCode();
        doReturn(mockConnection).when(networkProvider).getConnection(url);


        List<Item> receivedItems = client.getAllItems(recipient, new Date(0));
        assertTrue(receivedItems.size() == items.size());
        Set received = ImmutableSet.copyOf(receivedItems);
        Set itemsSet = ImmutableSet.copyOf(items);

        assertTrue(received.equals(itemsSet));
    }


}
