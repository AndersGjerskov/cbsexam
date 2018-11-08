package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import model.User;
import utils.Hashing;
import utils.Log;

public class UserController {

  private static DatabaseController dbCon;

  public UserController() {
    dbCon = new DatabaseController();
  }

  public static User getUser(int id) {

    // Check for connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build the query for DB
    String sql = "SELECT * FROM user where id=" + id;

    // Actually do the query
    ResultSet rs = dbCon.query(sql);
    User user = null;

    try {
      // Get first object, since we only have one
      if (rs.next()) {
        user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        // return the create object
        return user;
      } else {
        System.out.println("No user found");
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return null
    return user;
  }

  /**
   * Get all users in database
   *
   * @return
   */
  public static ArrayList <User> getUsers() {

    // Check for DB connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Build SQL
    String sql = "SELECT * FROM user";

    // Do the query and initialyze an empty list for use if we don't get results
    ResultSet rs = dbCon.query(sql);
    ArrayList <User> users = new ArrayList <User>();

    try {
      // Loop through DB Data
      while (rs.next()) {
        User user =
                new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("password"),
                        rs.getString("email"));

        // Add element to list
        users.add(user);
      }
    } catch (SQLException ex) {
      System.out.println(ex.getMessage());
    }

    // Return the list of users
    return users;
  }

  public static User createUser(User user) {

    // Write in log that we've reach this step
    Log.writeLog(UserController.class.getName(), user, "Actually creating a user in DB", 0);

    // Set creation time for user.
    user.setCreatedTime(System.currentTimeMillis() / 1000L);

    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Insert the user in the DB
    // TODO: Hash the user password before saving it. : fix
    int userID = dbCon.insert(
            "INSERT INTO user(first_name, last_name, password, email, created_at) VALUES('"
                    + user.getFirstname()
                    + "', '"
                    + user.getLastname()
                    + "', '"
                /*
                Sørger for at den hasher passworded inden den gemmer det
                Kunne også have brugt sha hashing ved at skrive følgende istedet for:
                + Hashing.sha(user.getPassword())
                */
                    + Hashing.md5(user.getPassword())
                    + "', '"
                    + user.getEmail()
                    + "', "
                    + user.getCreatedTime()
                    + ")");

    if (userID != 0) {
      //Update the userid of the user before returning
      user.setId(userID);
    } else {
      // Return null if user has not been inserted into database
      return null;
    }

    // Return user
    return user;
  }

  public static boolean delete(int id) {
    //Kopieret fra CreateUser længgere oppe:
    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //Henter user ud fra id.
    User user = UserController.getUser(id);

    //sletter user fra databasen ud fra ID og returnerer true hvis det lykkes
    if (user != null) {
      dbCon.updateDelete("DELETE from user WHERE id =" + id);
      return true;
    } else {
      return false;
    }
  }

  public static boolean update(int id) {
    //Kopieret fra CreateUser længgere oppe:
    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    //Henter user ud fra id.
    User user = UserController.getUser(id);

    //sletter user fra databasen ud fra ID og returnerer true hvis det lykkes
    if (user != null) {
      dbCon.updateDelete("UPDATE user SET first_name ='" + user.getFirstname() +
                              "', last_name = '" + user.getLastname() +
                              "', email = '" + user.getEmail() +
                              "ø, password = '" + user.getPassword() +
                              "'where id=" + id);
      return true;
    } else {
      return false;

    }
  }
}
