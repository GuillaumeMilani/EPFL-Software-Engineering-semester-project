package ch.epfl.sweng.calamar.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import ch.epfl.sweng.calamar.client.NetworkProvider;

/**
 * Created by LPI on 19.10.2015.
 */
public class DefaultNetworkProvider implements NetworkProvider {
    @Override
    public HttpURLConnection getConnection(URL url) throws IOException {
        return (HttpURLConnection) url.openConnection();
    }
}
