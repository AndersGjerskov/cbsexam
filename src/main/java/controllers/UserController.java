package controllers;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;

import cache.UserCache;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
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
                // Making sure that the password is hashed before it is saved in the database
                    + Hashing.HashWithSalt(user.getPassword())
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

  public static Boolean delete(int id) {
    //Kopieret fra CreateUser længgere oppe:
    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Getting our user from our user id
    User user = UserController.getUser(id);

    // Return true if the user has been deleted from the database
    if (user != null) {
      dbCon.updateDelete("DELETE FROM user WHERE id =" + id);
      return true;
    } else {
      return false;
    }
  }

  public static Boolean update(User user, int id) {
    // Check for DB Connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Return true if the user has been updated in the database
    if (user != null) {
      dbCon.updateDelete("UPDATE user SET first_name ='" + user.getFirstname() +
              "', last_name = '" + user.getLastname() +
              "', email = '" + user.getEmail() +
              "', password = '" + Hashing.HashWithSalt(user.getPassword()) +
              "'WHERE id=" + id);
      return true;
    } else {
      return false;

    }
  }

  public static String loginUser(User loginUser) {

    //Checker for db connection
    if (dbCon == null) {
      dbCon = new DatabaseController();
    }

    // Creating a timestamp to use for our token
    Timestamp timestamp = new Timestamp(System.currentTimeMillis());

    UserCache userCache = new UserCache();

    // Getting users from our cache
    ArrayList <User> users = userCache.getUsers(false);

    for (User user : users) {
      // Checking if the email and password matches
      if (user.getEmail().equals(loginUser.getEmail())
              && user.getPassword().equals(Hashing.HashWithSalt(loginUser.getPassword()))) {
        try {
          // Creating a token with the HMAC256 algorithm and returning it.
          Algorithm algorithmHS = Algorithm.HMAC256("secret");
          String token = JWT.create().withIssuer("auth0").withClaim("ANDKEY", timestamp).withClaim("test", user.getId()).sign(algorithmHS);
          user.setToken(token);
          return token;
        } catch (JWTCreationException exception) {

          // Should be an action here if it fails

        }
      }

    }return null;
  }

  public static DecodedJWT verifyToken (String userToken){

    String token = userToken;
    try{
      Algorithm algorithm = Algorithm.HMAC256("secret");
      JWTVerifier verifier = JWT.require(algorithm).withIssuer("auth0").build();
      DecodedJWT jwt = verifier.verify(token);
      return jwt;
    } catch (JWTVerificationException exception) {
      exception.getMessage();
    } return null;
  }
}
