package ch.epfl.sweng.calamar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Quentin Jaquier, sciper 235825 on 23.10.2015.
 */
public class ConstantItemClient implements ItemClient{

    private User alice = new User(1,"Alice");
    private User bob = new User(2,"Bob");

    private Item itemFrom = new SimpleTextItem(1,alice,bob,new Date(1445198510),"Hello Bob, it's Alice !");
    private Item itemTo = new SimpleTextItem(1,bob,alice,new Date(1445198510),"Hello Alice, it's Bob !");
    
    @Override
    public List<Item> getAllItems(Recipient recipient, Date from) throws ItemClientException {
        List<Item> items = new ArrayList();
        items.add(itemFrom);
        items.add(itemTo);
        return items;
    }

    @Override
    public List<Item> getAllItems(Recipient recipient) throws ItemClientException {
        List<Item> items = new ArrayList();
        items.add(itemFrom);
        items.add(itemTo);
        return items;
    }

    @Override
    public void send(Item item) throws ItemClientException {

    }
}
