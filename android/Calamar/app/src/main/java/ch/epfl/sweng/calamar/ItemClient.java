package ch.epfl.sweng.calamar;

import java.util.Date;
import java.util.List;

/**
 * Created by LPI on 19.10.2015.
 */
public interface ItemClient {
    /**
     * gets from the (a) database all items that have <i>recipient</i> into their recipient field
     * AND whose date is greater than <i>from</i>
     *
     * @param recipient the items we want must have recipient in their destination field
     * @param from the items have been sent after from
     * @return a list of {@link Item items}
     */
    public abstract List<Item> getAllItems(Recipient recipient, Date from) throws ItemClientException;
    public abstract List<Item> getAllItems(Recipient recipient) throws ItemClientException;

    /**
     * send an item to the (a) database
     * @param item
     */
    public abstract void send(Item item) throws ItemClientException;
}
