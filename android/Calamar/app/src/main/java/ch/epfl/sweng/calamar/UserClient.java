package ch.epfl.sweng.calamar;

/**
 * Created by Quentin Jaquier, sciper 235825 on 26.10.2015.
 */
public interface UserClient {


    /**
     * Create a new user on the database
     *
     * @param email email of the new user.
     * @param deviceId device id of the new user.
     * @return the id of the new user given by the database
     * @throws ItemClientException
     */
    public abstract int newUser(String email,String deviceId) throws ItemClientException;
}
