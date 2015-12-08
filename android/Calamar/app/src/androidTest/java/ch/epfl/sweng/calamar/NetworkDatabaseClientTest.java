package ch.epfl.sweng.calamar;

import android.support.test.espresso.core.deps.guava.collect.ImmutableSet;
import android.util.Log;

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
        // to shut his mouth
        CalamarApplication.getInstance().setGoogleApiClient(mock(GoogleApiClient.class));


        DatabaseClient client = new NetworkDatabaseClient(URL,
                networkProvider);

        java.net.URL url = new URL(URL + "/items.php?action=retrieve");

        InputStream response = new ByteArrayInputStream(("[\n" +
                "\t{\n" +
                "\t\t\"type\":\"SIMPLETEXTITEM\",\n" +
                "\t\t\"ID\":1,\n" +
                "\t\t\"message\":\"Hello test, it's Alice !\",\n" +
                "\t\t\"from\": {\n" +
                "\t\t\t\"name\":\"Alice\",\n" +
                "\t\t\t\"ID\":1,\n" +
                "\t\t\t\"type\":\"user\"\n" +
                "\t\t},\n" +
                "\t\t\"to\": {\n" +
                "\t\t\t\"name\":\"test\",\n" +
                "\t\t\t\"ID\":42,\n" +
                "\t\t\t\"type\":\"user\"\n" +
                "\t\t},\n" +
                "\t\t\"date\":1445198510,\n" +
                "\t\t\"condition\": {\n" +
                "\t\t   \"type\":\"ORCONDITION\",\n" +
                "\t\t   \"a\":{\n" +
                "\t\t\t\t\"type\":\"POSITIONCONDITION\",\n" +
                "\t\t\t\t\"latitude\":46.495,\n" +
                "\t\t\t\t\"longitude\":6.513,\n" +
                "\t\t\t\t\"radius\":10\n" +
                "\t\t   },\n" +
                "\t\t   \"b\":{\n" +
                "\t\t\t\t\"type\":\"POSITIONCONDITION\",\n" +
                "\t\t\t\t\"latitude\":47.495,\n" +
                "\t\t\t\t\"longitude\":7.513,\n" +
                "\t\t\t\t\"radius\":10\n" +
                "\t\t   }\n" +
                "\t   }\n" +
                "   },\n" +
                "   {\n" +
                "\t\"type\":\"SIMPLETEXTITEM\",\n" +
                "\t\t\"ID\":2,\n" +
                "\t\t\"message\":\"Hello test, it's Carol !\",\n" +
                "\t\t\"from\": {\n" +
                "\t\t\t\"name\":\"Carol\",\n" +
                "\t\t\t\"ID\":3,\n" +
                "\t\t\t\"type\":\"user\"\n" +
                "\t\t},\n" +
                "\t\t\"to\": {\n" +
                "\t\t\t\"name\":\"test\",\n" +
                "\t\t\t\"ID\":42,\n" +
                "\t\t\t\"type\":\"user\"\n" +
                "\t\t},\n" +
                "\t\t\"date\":1445198520\t\t\n" +
                "   }," +
                "   {\n" +
                "\t\"type\":\"SIMPLETEXTITEM\", \n" +
                "\t\"ID\":3, \n" +
                "\t\"message\":\"Hello test, it's Bob !\", \n" +
                "\t\"from\": { \n" +
                "\t\t\"name\":\"Bob\", \n" +
                "\t\t\"ID\":2, \n" +
                "\t\t\"type\":\"user\" \n" +
                "\t}, \n" +
                "\t\"to\": { \n" +
                "\t\t\"name\":\"test\", \n" +
                "\t\t\"ID\":42, \n" +
                "\t\t\"type\":\"user\" \n" +
                "\t}, \n" +
                "\t\"date\":1445198530 \n" +
                "}\n]").getBytes());


        User alice = new User(1, "Alice");
        User bob = new User(2, "Bob");
        User carol = new User(3, "Carol");
        Condition cond1 = new PositionCondition(46.495, 6.513, 10);
        Condition cond2 = new PositionCondition(47.495, 7.513, 10);

        Item item1 = new SimpleTextItem(1, alice, recipient, new Date(1445198510), Condition.or(cond1, cond2), "Hello test, it's Alice !");
        Item item2 = new SimpleTextItem(2, carol, recipient, new Date(1445198520), "Hello test, it's Carol !");
        Item item3 = new SimpleTextItem(3, bob, recipient, new Date(1445198530), "Hello test, it's Bob !");
        List<Item> items = Arrays.asList(item1, item2, item3);


        // ~"server configuration" through mock objects
        doReturn(response).when(mockConnection).getInputStream();
        doReturn(new ByteArrayOutputStream()).when(mockConnection).getOutputStream();  // wtf...
        doReturn(200).when(mockConnection).getResponseCode();
        doReturn(mockConnection).when(networkProvider).getConnection(url);


        List<Item> receivedItems = client.getAllItems(recipient, new Date(0));
        assertTrue(receivedItems.size() == items.size());
        Set received = ImmutableSet.copyOf(receivedItems);
        Set itemsSet = ImmutableSet.copyOf(items);
        //Log.e("junit", received.equals(itemsSet)+"");
        assertTrue(received.equals(itemsSet));
    }


}
