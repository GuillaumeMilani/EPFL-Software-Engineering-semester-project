package ch.epfl.sweng.calamar;

/**
 * Created by Tony on 09.11.2015.
 */
public interface RegistrationClient {

    /**
     * Register a token into the server
     * @param token the token to send
     */
    void send(String token) throws RegisterClientException;
}
