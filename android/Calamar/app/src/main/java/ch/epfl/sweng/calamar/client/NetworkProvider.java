package ch.epfl.sweng.calamar.client;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by LPI on 19.10.2015.
 */
public interface NetworkProvider {
    HttpURLConnection getConnection(URL url) throws IOException;
}
