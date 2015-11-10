package ch.epfl.sweng.calamar;

/**
 * Created by Quentin Jaquier, sciper 235825 on 23.10.2015.
 */
public class DatabaseClientLocator {

    private static final String SERVER_BASE_URL = "http://calamar.japan-impact.ch";

    private static DatabaseClient itemClient = new NetworkDatabaseClient(SERVER_BASE_URL, new DefaultNetworkProvider());

    public static DatabaseClient getDatabaseClient() {
        return DatabaseClientLocator.itemClient;
    }

    public static void setDatabaseClient(DatabaseClient quizClient) {
        DatabaseClientLocator.itemClient = quizClient;
    }

    public static void resetDatabaseClient() {
        DatabaseClientLocator.itemClient = new NetworkDatabaseClient(SERVER_BASE_URL, new DefaultNetworkProvider());
    }

}
