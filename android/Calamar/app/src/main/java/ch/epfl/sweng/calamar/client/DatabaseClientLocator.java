package ch.epfl.sweng.calamar.client;

/**
 * Created by Quentin Jaquier, sciper 235825 on 23.10.2015.
 */
public final class DatabaseClientLocator {

    private static final String SERVER_BASE_URL = "http://calamar.japan-impact.ch";

    private static DatabaseClient itemClient = new NetworkDatabaseClient(SERVER_BASE_URL, new DefaultNetworkProvider());

    /**
     * Returns the current DatabaseClient
     *
     * @return The current DatabaseClient, or the default one if it hasn't been set.
     */
    public static DatabaseClient getDatabaseClient() {
        return DatabaseClientLocator.itemClient;
    }

    /**
     * Sets the DatabaseClient
     *
     * @param client The client to be set
     */
    public static void setDatabaseClient(DatabaseClient client) {
        DatabaseClientLocator.itemClient = client;
    }

    /**
     * Resets the DatabaseClient to the default one
     */
    public static void resetDatabaseClient() {
        DatabaseClientLocator.itemClient = new NetworkDatabaseClient(SERVER_BASE_URL, new DefaultNetworkProvider());
    }

}
