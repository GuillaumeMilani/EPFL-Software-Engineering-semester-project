package ch.epfl.sweng.calamar.push;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import ch.epfl.sweng.calamar.client.NetworkProvider;

/**
 * Created by Tony on 09.11.2015.
 */
public class NetworkRegistrationClient implements RegistrationClient {

    private final String serverUrl;
    private final NetworkProvider networkProvider;
    private final static int HTTP_SUCCESS_START = 200;
    private final static int HTTP_SUCCESS_END = 299;
    private final static String SEND_PATH = "/users.php?action=log";

    public NetworkRegistrationClient(String serverUrl, NetworkProvider networkProvider) {
        if (null == serverUrl || null == networkProvider) {
            throw new IllegalArgumentException("'serverUrl' or 'networkProvider' is null");
        }
        this.serverUrl = serverUrl;
        this.networkProvider = networkProvider;
    }

    @Override
    public void send(String token,String userName) throws RegisterClientException {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(serverUrl + NetworkRegistrationClient.SEND_PATH);
            String jsonParameter = "{ " +
                    "\"token\": \"" + token + "\"" +
                    ",\"name\": " + "\"" + userName + "\"" +
                    " }";
            connection = NetworkRegistrationClient.createConnection(networkProvider, url);
            String response = NetworkRegistrationClient.post(connection, jsonParameter);

            if (!response.contains("Ack")) {
                throw new RegisterClientException("error: server couldn't retrieve the item");
            }
        } catch (IOException e) {
            throw new RegisterClientException(e);
        } finally {
            NetworkRegistrationClient.close(connection);
        }
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

    /**
     * used to post data on connection
     *
     * @param connection    the connection used to post data
     * @param jsonParameter the data posted
     * @return the result of the request
     * @throws IOException
     * @throws RegisterClientException
     */
    private static String post(HttpURLConnection connection, String jsonParameter)
            throws IOException, RegisterClientException {
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
            throw new RegisterClientException("Invalid HTTP response code");
        }

        //get result
        return NetworkRegistrationClient.fetchContent(connection);
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
            Log.d("RegHTTPFetchContent", "Fetched string of length "
                    + result.length());
            return result;
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
}
