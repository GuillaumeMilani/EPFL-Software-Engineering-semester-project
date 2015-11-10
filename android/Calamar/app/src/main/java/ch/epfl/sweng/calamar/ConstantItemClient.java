package ch.epfl.sweng.calamar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Quentin Jaquier, sciper 235825 on 23.10.2015.
 */
public class ConstantItemClient implements DatabaseClient {

    private final User alice = new User(1,"Alice");
    private final User bob = new User(2,"Bob");

    private final Item itemFrom = new SimpleTextItem(1,alice,bob,new Date(1445198510),"Hello Bob, it's Alice !");
    private final Item itemTo = new SimpleTextItem(1,bob,alice,new Date(1445198510),"Hello Alice, it's Bob !");

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
    public void send(Item item) throws DatabaseClientException {
        //Do nothing
    }

    @Override
    public int newUser(String email, String deviceId) throws DatabaseClientException {
        return 0;
    }
}
