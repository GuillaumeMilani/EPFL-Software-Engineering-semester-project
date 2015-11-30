package ch.epfl.sweng.calamar.utils;

import ch.epfl.sweng.calamar.item.Item;

public interface StorageCallbacks {

    /**
     * Does things with the item retrieved by the StorageManager
     *
     * @param i the item
     */
    public void onItemRetrieved(Item i);

    /**
     * Does things with the data retrieved by the StorageManager
     *
     * @param data the data
     */
    public void onDataRetrieved(byte[] data);
}
