package ch.epfl.sweng.calamar;

/**
 * Created by Quentin Jaquier, sciper 235825 on 23.10.2015.
 */
public class ItemClientLocator {


    private static final String SERVER_BASE_URL = "http://calamar.japan-impact.ch";

    private static ItemClient itemClient = new NetworkDatabaseClient(SERVER_BASE_URL,new DefaultNetworkProvider());

    public static ItemClient getItemClient() {
        return ItemClientLocator.itemClient;
    }

    public static void setItemClient(ItemClient quizClient) {
        ItemClientLocator.itemClient = quizClient;
    }

    public static void resetItemClient() {
        ItemClientLocator.itemClient = new NetworkDatabaseClient(SERVER_BASE_URL,new DefaultNetworkProvider());
    }

}
