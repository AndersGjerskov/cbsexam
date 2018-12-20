package com.cbsexam;

import cache.UserCache;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.Gson;
import controllers.UserController;
import java.util.ArrayList;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import model.User;
import utils.Encryption;
import utils.Log;

@Path("user")
public class UserEndpoints {

  //Creating an instance of our Cache once, so we can use it in this endpoint
  private static UserCache userCache = new UserCache();

  /**
   * @param idUser
   * @return Responses
   */
  @GET
  @Path("/{idUser}")
  public Response getUser(@PathParam("idUser") int idUser) {

    // Use the ID to get the user from the controller.
    User user = UserController.getUser(idUser);

    // TODO: Add Encryption to JSON : fix
    // Convert the user object to json in order to return the object
    String json = new Gson().toJson(user);
    // Using XOR to encrypt the json
    json = Encryption.encryptDecryptXOR(json);

    // Return the user with the status code 200
    // TODO: What should happen if something breaks down? : fix
    if (user != null){
    return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    //If we cant return a user, we will return a message with the status code 400
    } else{
      return Response.status(400).entity("Could not return user").build();
    }
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users
    // Istedet for controller-layer henter vi det fra vores Cache
    ArrayList<User> users = userCache.getUsers(false);

    // TODO: Add Encryption to JSON : fix
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);
    // Using XOR to encrypt the json
    json = Encryption.encryptDecryptXOR(json);

    // Return the users with the status code 200
    return Response.status(200).type(MediaType.APPLICATION_JSON).entity(json).build();
  }

  @POST
  @Path("/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response createUser(String body) {

    // Read the json from body and transfer it to a user class
    User newUser = new Gson().fromJson(body, User.class);

    // Use the controller to add the user
    User createUser = UserController.createUser(newUser);

    // Get the user back with the added ID and return it to the user
    String json = new Gson().toJson(createUser);

    // Return the data to the user
    if (createUser != null) {
      //Updating the cache if the user has been created
      userCache.getUsers(true);
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
    } else {
      return Response.status(400).entity("Could not create user").build();
    }
  }

  // TODO: Make the system able to login users and assign them a token to use throughout the system. : fix
  @POST
  @Path("/login/")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response loginUser(String body) {

    Log.writeLog(this.getClass().getName(), this, "User login", 0);

    // Reading the json from body and adding it to loginUser
    User loginUser = new Gson().fromJson(body, User.class);

    // Setting token as the token that was given to the user which has logged in
    String token = UserController.loginUser(loginUser);

    if (token != null){
      // Return an answer, with the given token, if the login was succesfull
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("User has logged in and the users token is\n" +  token).build();

    } else {
      // If the login was not succesfull, return a message with the status code 400
      return Response.status(400).entity("Could not log user in").build();
    }
  }

  // TODO: Make the system able to delete users : fix
  // Using the same idea as in the loginUser
  @DELETE
  @Path("/delete/{userId}")
  public Response deleteUser(@PathParam("userId") int id, String body) {

    Log.writeLog(this.getClass().getName(), this, "Deleted user", 0);

    // Verifying our token which we get from the body
    DecodedJWT token = UserController.verifyToken(body);

    // Setting delete as either true or false, if the delete method from the UserController was succesfull or not
    Boolean delete = UserController.delete(token.getClaim("test").asInt());

    // Checking if delete=true
    if (delete){

      // Updating our Cache
      userCache.getUsers(true);

      // Return an answer, with the deleted user id, if the was user was succesfully deleted
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("Deleting user with id " + id).build();

    } else {
      // Return a message with the status code 400 if the user was not deleted.
      return Response.status(400).entity("Could not delete user").build();
    }
  }

  // TODO: Make the system able to update users : fix
  @POST
  @Path("/update/{userId}/{token}")
  public Response updateUser(@PathParam("userId") int id, @PathParam("token") String token, String body) {

    Log.writeLog(this.getClass().getName(), this, "Updated user", 0);

    // Reading the json from body and adding it to the user object
    User user = new Gson().fromJson(body, User.class);

    // Opposite to the deleteUser, we now get our token from the Path, save it as a String and use it
    DecodedJWT jwt = UserController.verifyToken(token);

    // Setting update as either true or false, if the update method from the UserController was succesfull or not
    Boolean update = UserController.update(user, jwt.getClaim("test").asInt());

    // Checking if update=true
    if (update){

      // Updating our Cache
      userCache.getUsers(true);

      //Return a response with the id of the user which has been updated.
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("Updated user with id" + id).build();
    } else {
      // Returnerer svar hvis brugeren ikke kunne opdateres
      return Response.status(400).entity("Could not update user").build();
    }
  }
}
