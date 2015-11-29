package ch.epfl.sweng.calamar.client;

import android.provider.ContactsContract;

import com.google.android.gms.maps.model.VisibleRegion;

import java.util.Date;
import java.util.List;

import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

/**
 * Created by Quentin Jaquier, sciper 235825 on 29.11.2015.
 */
public class FaultyDatabaseClient implements DatabaseClient {
    @Override
    public List<Item> getAllItems(Recipient recipient, Date from, VisibleRegion visibleRegion) throws DatabaseClientException {
        throw new DatabaseClientException("Impossible to get all item");
    }

    @Override
    public List<Item> getAllItems(Recipient recipient, Date from) throws DatabaseClientException {
        throw new DatabaseClientException("Impossible to get all item");
    }

    @Override
    public Item send(Item item) throws DatabaseClientException {
        throw new DatabaseClientException("Impossible to send an item");
    }

    @Override
    public User findUserByName(String name) throws DatabaseClientException {
        throw new DatabaseClientException("Impossible to find this user");
    }

    @Override
    public int newUser(String email, String deviceId) throws DatabaseClientException {
        throw new DatabaseClientException("Impossible to create a new user");
    }
}
