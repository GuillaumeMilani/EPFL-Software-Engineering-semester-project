package ch.epfl.sweng.calamar;

/**
 * Created by LPI on 19.10.2015.
 */
public class ItemClientException extends Throwable {
    public ItemClientException(String message) {
        super(message);
    }
    public ItemClientException(Throwable child) {
        super(child);
    }
}
