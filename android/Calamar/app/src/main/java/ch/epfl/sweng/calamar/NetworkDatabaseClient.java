package ch.epfl.sweng.calamar;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by LPI on 19.10.2015.
 */
public class NetworkDatabaseClient implements DatabaseClient {

    private final String serverUrl;
    private final NetworkProvider networkProvider;
    private final static int HTTP_SUCCESS_START = 200;
    private final static int HTTP_SUCCESS_END = 299;
    private final static String SEND_PATH = "/items.php?action=send";
    private final static String RETRIEVE_PATH = "/items.php?action=retrieve";
    private final static String NEW_USER_PATH = "/users.php?action=add";

    public NetworkDatabaseClient(String serverUrl, NetworkProvider networkProvider)  {
        if(null == serverUrl || null == networkProvider) {
            throw new IllegalArgumentException("'serverUrl' or 'networkProvider' is null");
        }
        this.serverUrl = serverUrl;
        this.networkProvider = networkProvider;
    }

    @Override
    public List<Item> getAllItems(Recipient recipient, Date from) throws ItemClientException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(serverUrl + NetworkDatabaseClient.RETRIEVE_PATH);
            /*String jsonParameter = URLEncoder.encode(
                    "{ " +
                            "\"recipient\": " + recipient.toJSON().toString() +
                            "\",lastRefresh\": " + from.getTime() +
                            " }", "UTF-8");*/
            String jsonParameter = "{ " +
                    "\"recipient\": " + recipient.toJSON().toString() +
                    ",\"lastRefresh\": " + from.getTime() +
                    " }";
            connection = NetworkDatabaseClient.createConnection(networkProvider, url);
            String response = NetworkDatabaseClient.post(connection, jsonParameter);

            return NetworkDatabaseClient.itemsFromJSON(response);
        } catch (IOException | JSONException e) {
            throw new ItemClientException(e);
        } finally {
            NetworkDatabaseClient.close(connection);
        }
    }

    @Override
    public List<Item> getAllItems(Recipient recipient) throws ItemClientException {
        return getAllItems(recipient, new Date());
    }

    @Override
    public void send(Item item) throws ItemClientException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(serverUrl + NetworkDatabaseClient.SEND_PATH);
            String jsonParameter = item.toJSON().toString();
            connection = NetworkDatabaseClient.createConnection(networkProvider, url);
            String response = NetworkDatabaseClient.post(connection, jsonParameter);

            if (!response.contains("Ack")) {
                throw new ItemClientException("error: server couldn't retrieve the item");
            }
        } catch (IOException | JSONException e) {
            throw new ItemClientException(e);
        } finally {
            NetworkDatabaseClient.close(connection);
        }
    }

    @Override
    public int newUser(String email, String deviceId) throws ItemClientException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(serverUrl + NetworkDatabaseClient.NEW_USER_PATH);

            String jsonParameter = "{ " +
                    "\"DeviceID\": \"" + deviceId+"\"" +
                    ",\"name\": \"" + email+"\"" +
                    " }";
            connection = NetworkDatabaseClient.createConnection(networkProvider, url);
            String response = NetworkDatabaseClient.post(connection, jsonParameter);
            return idFromJson(response);
        } catch (IOException | JSONException e) {
            throw new ItemClientException(e);
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
            Log.d("HTTPFetchContent", "Fetched string of length "
                    + result.length());
            return result;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    /**
     * used to post data on connection
     * @param connection the connection used to post data
     * @param jsonParameter the data posted
     * @return the result of the request
     * @throws IOException
     * @throws ItemClientException
     */
    private static String post(HttpURLConnection connection, String jsonParameter)
            throws IOException, ItemClientException
    {
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type",
                "application/json");//TODO clarify
        connection.setRequestProperty("Content-Length",
                Integer.toString(jsonParameter.getBytes().length));
        connection.setDoInput(true);//to retrieve result
        connection.setDoOutput(true);//to send request

        //send request
        DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
        wr.writeBytes(jsonParameter);
        wr.flush();
        wr.close();

        int responseCode = connection.getResponseCode();
        if (responseCode < HTTP_SUCCESS_START || responseCode > HTTP_SUCCESS_END) {
            throw new ItemClientException("Invalid HTTP response code (" + responseCode + " )" );
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
            throws IOException
    {
        return networkProvider.getConnection(url);
    }

    private static List<Item> itemsFromJSON(String response) throws JSONException {
        List<Item> result = new ArrayList<>();
        if(!response.contains("No records found in the database")){
            //No new message !
            JSONArray array = new JSONArray(response);
            for(int i = 0; i < array.length(); ++i) {
                result.add(Item.fromJSON(array.getJSONObject(i)));
            }
        }
        return result;
    }

    private int idFromJson(String response) throws JSONException {
        Log.v("response : ", response);
        JSONObject object = new JSONObject(response);
        return object.getInt("ID");
    }


}
