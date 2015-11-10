package ch.epfl.sweng.calamar;

import java.util.Date;
import java.util.List;

/**
 * Created by LPI on 19.10.2015.
 */
public interface DatabaseClient {
    /**
     * gets from a database all items that have <i>recipient</i> into their recipient field
     * AND whose date is greater than <i>from</i>
     *
     * @param recipient the items we want must have recipient in their destination field
     * @param from      the items have been sent after from
     * @return a list of {@link Item items}
     */
    List<Item> getAllItems(Recipient recipient, Date from) throws DatabaseClientException;

    List<Item> getAllItems(Recipient recipient) throws DatabaseClientException;

    /**
     * send an item to a database
     *
     * @param item the item to send
     */
    void send(Item item) throws DatabaseClientException;

    /**
     * Create a new user on the database
     *
     * @param email    email of the new user.
     * @param deviceId device id of the new user.
     * @return the id of the new user given by the database
     * @throws DatabaseClientException
     */
    public abstract int newUser(String email, String deviceId) throws DatabaseClientException;
}
