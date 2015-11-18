package ch.epfl.sweng.calamar.client;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.item.SimpleTextItem;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

/**
 * Created by Quentin Jaquier, sciper 235825 on 23.10.2015.
 */
public class ConstantDatabaseClient implements DatabaseClient {

    private final User alice = new User(1, "Alice");
    private final User bob = new User(2, "Bob");

    private final Item itemFrom = new SimpleTextItem(1, alice, bob, new Date(1445198510), "Hello Bob, it's Alice !");
    private final Item itemTo = new SimpleTextItem(1, bob, alice, new Date(1445198510), "Hello Alice, it's Bob !");

    @Override
    public List<Item> getAllItems(Recipient recipient, Date from) throws DatabaseClientException {
        List<Item> items = new ArrayList<>();
        items.add(itemFrom);
        items.add(itemTo);
        return items;
    }

    @Override
    public List<Item> getAllItems(Recipient recipient) throws DatabaseClientException {
        List<Item> items = new ArrayList<>();
        items.add(itemFrom);
        items.add(itemTo);
        return items;
    }

    @Override
    public Item send(Item item) throws DatabaseClientException {
        return new SimpleTextItem(123,alice,bob,new Date(),"Hello");
    }

    @Override
    public User findUserByName(String name) throws DatabaseClientException {
        return new User(1, "Bob");
    }

    @Override
    public int newUser(String email, String deviceId) throws DatabaseClientException {
        return 0;
    }
}
