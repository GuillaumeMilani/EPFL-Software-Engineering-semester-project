package ch.epfl.sweng.calamar.utils;


import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ch.epfl.sweng.calamar.item.Item;
import ch.epfl.sweng.calamar.recipient.Recipient;


public class Sorter {
    private static final Comparator<Item> itemComparator = new Comparator<Item>() {
        @Override
        public int compare(Item lhs, Item rhs) {
            if (lhs.getID() < rhs.getID()) {
                return -1;
            } else if (lhs.getID() == rhs.getID()) {
                return 0;
            } else {
                return 1;
            }
        }

    };

    private static final Comparator<Recipient> recipientComparator = new Comparator<Recipient>() {
        @Override
        public int compare(Recipient lhs, Recipient rhs) {
            if (lhs.getID() < rhs.getID()) {
                return -1;
            } else if (lhs.getID() == rhs.getID()) {
                return 0;
            } else {
                return 1;
            }
        }
    };

    private Sorter() {
    }


    public static List<Item> sortItemList(List<Item> items) {
        Collections.sort(items, itemComparator);
        return items;
    }

    public static List<Recipient> sortRecipientList(List<Recipient> recipients) {
        Collections.sort(recipients, recipientComparator);
        return recipients;
    }

}
