package ch.epfl.sweng.calamar.client;

import android.location.Location;

import com.google.android.gms.maps.model.VisibleRegion;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ch.epfl.sweng.calamar.condition.Condition;
import ch.epfl.sweng.calamar.condition.PositionCondition;
import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.item.SimpleTextItem;
import ch.epfl.sweng.calamar.recipient.Recipient;
import ch.epfl.sweng.calamar.recipient.User;

/**
 * Created by Quentin Jaquier, sciper 235825 on 23.10.2015.
 */
public class ConstantDatabaseClient implements DatabaseClient {

    private static final User OTHER = new User(3, "Other");
    private static final User ALICE = new User(1, "Alice");
    private static final User BOB = new User(2, "Bob");
    private static final String ALICE_MESSAGE = "Hello Bob, it's Alice !";
    private static final String BOB_MESSAGE = "Hello Alice, it's Bob !";
    private static final Location LOCATION = new Location("abc");
    private static final Item ITEM_FROM = new SimpleTextItem(0, ALICE, BOB, new Date(1445198510), Condition.trueCondition(), ALICE_MESSAGE);
    private static final Item ITEM_TO = new SimpleTextItem(1, BOB, ALICE, new Date(1445198510), Condition.and(Condition.falseCondition(), new PositionCondition(LOCATION)), BOB_MESSAGE);

    private static final Item ITEM_OTHER = new SimpleTextItem(2, OTHER, BOB, new Date(1445198510), Condition.and(Condition.falseCondition(), new PositionCondition(LOCATION)), "Hello Bob, it's me !");

    private final List<Item> toRetrieve = new ArrayList<>();

    @Override
    public List<Item> getAllItems(Recipient recipient, Date from, VisibleRegion visibleRegion)
            throws DatabaseClientException {
        return getAllItems(null, null);
    }

    @Override
    public List<Item> getAllItems(Recipient recipient, Date from) throws DatabaseClientException {
        List<Item> items = new ArrayList<>();
        items.add(ITEM_FROM);
        items.add(ITEM_TO);
        items.add(ITEM_OTHER);
        items.addAll(toRetrieve);
        return items;
    }

    @Override
    public Item send(Item item) throws DatabaseClientException {
        return item;
    }

    @Override
    public User findUserByName(String name) throws DatabaseClientException {
        return BOB;
    }

    @Override
    public int newUser(String email, String deviceId) throws DatabaseClientException {
        return 0;
    }

    /**
     * Adds an item to the list of items "on the server"
     *
     * @param i The item to add
     */
    public void addItem(Item i) {
        toRetrieve.add(i);
    }
}
