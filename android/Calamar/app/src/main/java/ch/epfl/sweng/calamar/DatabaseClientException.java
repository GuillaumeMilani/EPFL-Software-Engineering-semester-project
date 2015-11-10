package ch.epfl.sweng.calamar;

/**
 * Created by LPI on 19.10.2015.
 */
public class DatabaseClientException extends Throwable {
    public DatabaseClientException(String message) {
        super(message);
    }

    public DatabaseClientException(Throwable child) {
        super(child);
    }
}
