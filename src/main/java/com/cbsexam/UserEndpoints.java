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

  //Denne linje instantieres vores Cache 1 gang så vi kan bruge den
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
    json = Encryption.encryptDecryptXOR(json);

    // Return the user with the status code 200
    // TODO: What should happen if something breaks down?
    return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity(json).build();
  }

  /** @return Responses */
  @GET
  @Path("/")
  public Response getUsers() {

    // Write to log that we are here
    Log.writeLog(this.getClass().getName(), this, "Get all users", 0);

    // Get a list of users
    // Istedet for controller-layer henter vi det fra vores Cache
    ArrayList<User> users = userCache.getUsers(true);

    // TODO: Add Encryption to JSON : fix
    // Transfer users to json in order to return it to the user
    String json = new Gson().toJson(users);
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

    //Body har noget at gøre med når vi tester i postman?
    User loginUser = new Gson().fromJson(body, User.class);

    String token = UserController.loginUser(loginUser);

    if (token != null){
      // Returnerer svar hvis user er succesfuldt logget ind
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("User has logged in and token is\n" +  token).build();
    } else {
      // Returnerer svar hvis brugeren ikke kunne slettes
      return Response.status(400).entity("Could not log user in").build();
    }
  }

  // TODO: Make the system able to delete users : fix
  //Samme princip som ved login metoden
  @DELETE
  @Path("/delete/{userId}")
  public Response deleteUser(@PathParam("userId") int id, String body) {

    Log.writeLog(this.getClass().getName(), this, "Deleted user", 0);

    //Henter vores token fra verifyToken i Usercontrolleren, den ligger i vores body, da det er der vi skriver den
    DecodedJWT token = UserController.verifyToken(body);

    //Returnerer true eller false efter hvorvidt delete metoden er kørt fra UserController
    Boolean delete = UserController.delete(token.getClaim("test").asInt());

    if (delete){
      //Henter users fra vores Cache
      userCache.getUsers(true);
      // Returnerer svar hvis user er blevet slettet
      // Return a response with status 200 and JSON as type
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("Deleting user with id " + id).build();
    } else {
      // Returnerer svar hvis brugeren ikke kunne slettes
      return Response.status(400).entity("Could not delete user").build();
    }
  }

  // TODO: Make the system able to update users : fix
  @POST
  @Path("/update/{userId}/{token}")
  public Response updateUser(@PathParam("userId") int id, @PathParam("token") String token, String body) {

    Log.writeLog(this.getClass().getName(), this, "Updated user", 0);

    //Konverterer user fra json til gson
    User user = new Gson().fromJson(body, User.class);

    //Sørger for at vores bruger er logget ind så vi får den brugers specifikke token,
    //i modsætning til deleteUser, så gemmer vi den nu i et String objekt der hedder token
    DecodedJWT jwt = UserController.verifyToken(token);

    Boolean update = UserController.update(user, jwt.getClaim("test").asInt());

    // Opdaterer vores Cache
    userCache.getUsers(true);

    if (update){
      return Response.status(200).type(MediaType.APPLICATION_JSON_TYPE).entity("Updated user with id" + id).build();
    } else {
      // Returnerer svar hvis brugeren ikke kunne opdateres
      return Response.status(400).entity("Could not update user").build();
    }
  }
}
