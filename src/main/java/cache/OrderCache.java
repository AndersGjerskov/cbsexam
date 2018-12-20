package cache;

import controllers.OrderController;
import model.Order;
import utils.Config;

import java.util.ArrayList;

//TODO: Build this cache and use it. : fix
//Building this Cache just like the ProductCache which was already made
public class OrderCache {

    private ArrayList<Order> orders;

    private long ttl;

    private long created;

    public OrderCache() {
        this.ttl = Config.getOrderTtl();
    }

    public ArrayList <Order> getOrders(boolean forceUpdate) {

        if(forceUpdate
                //Changed ">" to "<" to make the cache work
                ||((this.created + this.ttl) <= (System.currentTimeMillis() / 1000L))
                || this.orders.isEmpty())  {

            ArrayList<Order> orders = OrderController.getOrders();

            this.orders = orders;
            this.created = System.currentTimeMillis() / 1000L;
        }

        return this.orders;

    }

}
