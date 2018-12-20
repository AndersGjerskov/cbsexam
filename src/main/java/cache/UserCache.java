package cache;

import controllers.UserController;
import model.User;
import utils.Config;

import java.util.ArrayList;

//TODO: Build this cache and use it. : fix
//Building this Cache just like the ProductCache which was already made
public class UserCache {

    private ArrayList<User> users;

    private long ttl;

    private long created;

    public UserCache() {
        this.ttl = Config.getUserTtl();
    }

    public ArrayList <User> getUsers(boolean forceUpdate) {

        if(forceUpdate
                //Changed ">" to "<" to make the cache work
           ||((this.created + this.ttl) <= (System.currentTimeMillis() / 1000L))
           || this.users == null)  {

            ArrayList<User> users = UserController.getUsers();

            this.users = users;
            this.created = System.currentTimeMillis() / 1000L;
        }

        return this.users;

    }
}
