package ch.epfl.sweng.calamar;

/**
 * Created by Tony on 09.11.2015.
 */
public class RegisterClientException extends Throwable {
    public RegisterClientException(String message) {
        super(message);
    }
    public RegisterClientException(Throwable child) {
        super(child);
    }
}
