package ch.epfl.sweng.calamar.client;

import android.util.Log;

import com.google.android.gms.maps.model.VisibleRegion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.epfl.sweng.calamar.CalamarApplication;
import ch.epfl.sweng.calamar.R;
import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;
import ch.epfl.sweng.calamar.utils.Sorter;

/**
 * Created by LPI on 19.10.2015.
 */
public final class NetworkDatabaseClient implements DatabaseClient {

    private final static String UTF8_CHARSET = "UTF-8";

    private final static String TAG = NetworkDatabaseClient.class.getSimpleName();

    private final static int HTTP_SUCCESS_START = 200;
    private final static int HTTP_SUCCESS_END = 299;
    private final static String SEND_PATH = "/items.php?action=send";
    private final static String RETRIEVE_PATH = "/items.php?action=retrieve";
    private final static String RETRIEVE_USER_PATH = "/users.php?action=retrieve";
    private final static String NEW_USER_PATH = "/users.php?action=add";

    private final static String JSON_TOKEN = "token";
    private final static String JSON_ID = "ID";
    private final static String JSON_NAME = "name";
    private final static String JSON_USER = "user";
    private final static String JSON_RECIPIENT = "recipient";
    private final static String JSON_LAST_REFRESH = "lastRefresh";
    private final static String JSON_LONGITUDE_MIN = "longitudeMin";
    private final static String JSON_LONGITUDE_MAX = "longitudeMax";
    private final static String JSON_LATITUDE_MIN = "latitudeMin";
    private final static String JSON_LATITUDE_MAX = "latitudeMax";

    private final static String CONTENT_TYPE = "Content-Type";
    private final static String CONTENT_LENGTH = "Content-Length";

    private final static String CONNECTION_CONTENT_TYPE = "application/json";
    private final static String CONNECTION_REQUEST_METHOD = "POST";

    private final String serverUrl;
    private final NetworkProvider networkProvider;

    public NetworkDatabaseClient(String serverUrl, NetworkProvider networkProvider) {
        if (null == serverUrl || null == networkProvider) {
            throw new IllegalArgumentException(CalamarApplication.getInstance().getString(R.string.network_db_client_null));
        }
        this.serverUrl = serverUrl;
        this.networkProvider = networkProvider;
    }

    @Override
    public List<Item> getAllItems(Recipient recipient, Date from, VisibleRegion visibleRegion)
            throws DatabaseClientException {
        if (null == visibleRegion) {
            throw new IllegalArgumentException(CalamarApplication.getInstance().getString(R.string.network_db_client_visibleregion_null));
        }
        return getItems(recipient, from, visibleRegion);
    }

    @Override
    public List<Item> getAllItems(Recipient recipient, Date from) throws DatabaseClientException {
        return getItems(recipient, from, null);
    }

    @Override
    public Item send(Item item) throws DatabaseClientException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(serverUrl + NetworkDatabaseClient.SEND_PATH);
            JSONObject jsonParameter = item.toJSON();
            Log.v(NetworkDatabaseClient.TAG, "send json request : " + jsonParameter.toString());
            connection = NetworkDatabaseClient.createConnection(networkProvider, url);
            String response = NetworkDatabaseClient.post(connection, jsonParameter.toString());
            Log.v(NetworkDatabaseClient.TAG, "server response : " + response);
            return Item.fromJSON(new JSONObject(response));
        } catch (IOException | JSONException e) {
            throw new DatabaseClientException(e);
        } finally {
            NetworkDatabaseClient.close(connection);
        }
    }

    @Override
    public int newUser(String email, String token) throws DatabaseClientException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(serverUrl + NetworkDatabaseClient.NEW_USER_PATH);

            JSONObject jsonParameter = new JSONObject();
            jsonParameter.accumulate(JSON_TOKEN, token);
            jsonParameter.accumulate(JSON_NAME, email);
            Log.v(NetworkDatabaseClient.TAG, "newUser json request : " + jsonParameter.toString());
            connection = NetworkDatabaseClient.createConnection(networkProvider, url);
            String response = NetworkDatabaseClient.post(connection, jsonParameter.toString());

            Log.v(NetworkDatabaseClient.TAG, "server response : " + response);
            JSONObject object = new JSONObject(response);
            return object.getInt(JSON_ID);
        } catch (IOException | JSONException e) {
            throw new DatabaseClientException(e);
        } finally {
            NetworkDatabaseClient.close(connection);
        }
    }

    @Override
    public User findUserByName(String name) throws DatabaseClientException {
        HttpURLConnection connection = null;
        try {
            final URL url = new URL(serverUrl + NetworkDatabaseClient.RETRIEVE_USER_PATH);

            final JSONObject jsonParameter = new JSONObject();
            jsonParameter.accumulate(JSON_ID,CalamarApplication.getInstance().getCurrentUserID());
            jsonParameter.accumulate(JSON_NAME, name);

            connection = NetworkDatabaseClient.createConnection(networkProvider, url);
            String response = NetworkDatabaseClient.post(connection, jsonParameter.toString());
            JSONObject resp = new JSONObject(response);
            Log.v(NetworkDatabaseClient.TAG, "server response : " + response);
            return User.fromJSON(resp.getJSONObject(JSON_USER));
        } catch (IOException | JSONException e) {
            throw new DatabaseClientException(e);
        } finally {
            NetworkDatabaseClient.close(connection);
        }
    }

    private List<Item> getItems(Recipient recipient, Date from, VisibleRegion visibleRegion)
            throws DatabaseClientException {
        if(null == recipient || null == from) {
            throw new IllegalArgumentException("getItems: recipient or date null");
        }

        HttpURLConnection connection = null;
        try {
            URL url = new URL(serverUrl + NetworkDatabaseClient.RETRIEVE_PATH);

            JSONObject jsonParameter = new JSONObject();
            jsonParameter.accumulate(JSON_RECIPIENT, recipient.toJSON());
            jsonParameter.accumulate(JSON_LAST_REFRESH, from.getTime());

            if (visibleRegion != null) {
                double left = visibleRegion.latLngBounds.southwest.longitude;
                double top = visibleRegion.latLngBounds.northeast.latitude;
                double right = visibleRegion.latLngBounds.northeast.longitude;
                double bottom = visibleRegion.latLngBounds.southwest.latitude;
                jsonParameter.accumulate(JSON_LONGITUDE_MIN, left < right ? left : right);
                jsonParameter.accumulate(JSON_LATITUDE_MIN, top < bottom ? top : bottom);
                jsonParameter.accumulate(JSON_LONGITUDE_MAX, left < right ? right : left);
                jsonParameter.accumulate(JSON_LATITUDE_MAX, top < bottom ? bottom : top);
            }

            connection = NetworkDatabaseClient.createConnection(networkProvider, url);
            Log.v(NetworkDatabaseClient.TAG, "getItems json request : " + jsonParameter.toString());
            String response = NetworkDatabaseClient.post(connection, jsonParameter.toString());
            Log.v(NetworkDatabaseClient.TAG, "server response : " + response);
            return NetworkDatabaseClient.itemsFromJSON(response);
        } catch (IOException | JSONException e) {
            throw new DatabaseClientException(e);
        } finally {
            NetworkDatabaseClient.close(connection);
        }
    }

    private static String fetchContent(HttpURLConnection conn) throws IOException {
        StringBuilder out = new StringBuilder();
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line).append("\n");
            }

            String result = out.toString();
            Log.d("HTTPFetchContent", CalamarApplication.getInstance().getString(R.string.network_db_client_log_fetched_length, result.length()));
            return result;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * used to post data on connection
     *
     * @param connection    the connection used to post data
     * @param jsonParameter the data posted
     * @return the result of the request
     * @throws IOException
     * @throws DatabaseClientException
     */
    private static String post(HttpURLConnection connection, String jsonParameter)
            throws IOException, DatabaseClientException
    {
        String toSend = URLEncoder.encode(jsonParameter, UTF8_CHARSET);
        connection.setRequestMethod(CONNECTION_REQUEST_METHOD);
        connection.setRequestProperty(CONTENT_TYPE,
                CONNECTION_CONTENT_TYPE);
        connection.setRequestProperty(CONTENT_LENGTH,
                Integer.toString(toSend.getBytes().length));
        connection.setDoInput(true);//to retrieve result
        connection.setDoOutput(true);//to send request

        //send request
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(toSend);
        wr.flush();
        wr.close();

        int responseCode = connection.getResponseCode();

        // TODO use implemented server response code
        if (responseCode < HTTP_SUCCESS_START || responseCode > HTTP_SUCCESS_END) {
            throw new DatabaseClientException(CalamarApplication.getInstance().getString(R.string.invalid_http_response, responseCode));
        }

        //get result
        return NetworkDatabaseClient.fetchContent(connection);
    }

    private static void close(HttpURLConnection connection) {
        if (connection != null) {
            connection.disconnect();
        }
    }

    private static HttpURLConnection createConnection(NetworkProvider networkProvider, URL url)
            throws IOException {
        return networkProvider.getConnection(url);
    }

    private static List<Item> itemsFromJSON(String response) throws JSONException {
        List<Item> result = new ArrayList<>();

        JSONArray array = new JSONArray(response);
        for (int i = 0; i < array.length(); ++i) {
            result.add(Item.fromJSON(array.getJSONObject(i)));
        }
        return Sorter.sortItemList(result);
    }
}
