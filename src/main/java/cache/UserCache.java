package cache;

import controllers.UserController;
import model.User;
import utils.Config;

import java.util.ArrayList;

//TODO: Build this cache and use it. : fix
//Bruge samme opbygning som i ProductCache til at bygge den.
public class UserCache {

    private ArrayList<User> users;

    private long ttl;

    private long created;

    public UserCache() {
        this.ttl = Config.getUserTtl();
    }

    public ArrayList <User> getUsers(boolean forceUpdate) {

        if(forceUpdate
           ||((this.created + this.ttl) >= (System.currentTimeMillis() / 1000L))
           || this.users == null)  {

            ArrayList<User> users = UserController.getUsers();

            this.users = users;
            this.created = System.currentTimeMillis() / 1000L;
        }

        return this.users;

    }
}
