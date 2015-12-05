package ch.epfl.sweng.calamar.client;

/**
 * Created by LPI on 19.10.2015.
 */
public final class DatabaseClientException extends Throwable {

    public DatabaseClientException(String message) {
        super(message);
    }

    public DatabaseClientException(Throwable child) {
        super(child);
    }
}
