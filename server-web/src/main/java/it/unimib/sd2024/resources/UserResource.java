package it.unimib.sd2024.resources;

import java.util.List;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import it.unimib.sd2024.request.SigninUserRequestBody;
import it.unimib.sd2024.request.SignupUserRequestBody;
import it.unimib.sd2024.models.User;
import it.unimib.sd2024.connection.Queryer;
import it.unimib.sd2024.models.Domain;
import it.unimib.sd2024.models.Operation;
import it.unimib.sd2024.models.DomainStatus;

/** Resource "./users"
 *  This class is a RESTfull resource that allows to manage users. It allows to:
 * 		- Sign-in to a previously registered account "/signin"
 * 		- Sign-up to a new account "/signup"
 * 		- Get the information of a user by ID "/{id}"
 * 		- Get the domains registered by a user "/{id}/domains"
 *  The resource provides private methods to query the database for the operations needed.
**/
@Path("users") /* root path of the resource */
public class UserResource {
	/** GET ./users/signin
	 *  Allows to sign-in to a previously registered account
	**/
	@Path("/signin")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response signinUser(SigninUserRequestBody body) {
		 // Validazione input
		if (body == null || body.getEmail() == null || body.getPassword() == null) {
			return Response.status(Status.BAD_REQUEST)
					.entity("signinUser :: body must be indicated and with all fields valid")
					.build();
		}

		// Esegui la query
		User[] users = Queryer.queryFindUserByEmail(body.getEmail());

		// Controlla se l'utente esiste
		if (users == null || users.length == 0) {
			return Response.status(Status.UNAUTHORIZED)
					.entity("signinUser :: email or password may be incorrect")
					.build();
		}

		User u = users[0];

		// Verifica la password
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] hashedPassword = md.digest(body.getPassword().getBytes(StandardCharsets.UTF_8));
			String hashedPasswordStr = new String(hashedPassword, StandardCharsets.UTF_8);

			if (!u.getPassword().equals(hashedPasswordStr)) {
				return Response.status(Status.UNAUTHORIZED)
							.entity("signinUser :: email or password may be incorrect")
							.build();
			}
		} catch (NoSuchAlgorithmException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
						.entity("signinUser :: failed to validate the user")
						.build();
		}

		return Response.ok(u.info()).build();
	}

	/** POST ./users/signup
	 *  Allows to sign-up to a new account
	**/
	@Path("/signup")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response signupUser(SignupUserRequestBody body) {
		System.out.println("signupUser: " + body.toString());

		// Check if the user is not null and has all the required fields valid
		if (body == null || body.getName() == null || body.getSurname() == null || body.getEmail() == null || body.getPassword() == null) {
			return Response.status(Status.BAD_REQUEST.getStatusCode(), "signupUser :: body must be indicated and with all fields valid").build();
		}
		
		// Get the user finding it on the db by email
		User u = Queryer.queryFindUserByEmail(body.getEmail())[0];

		System.out.println("signupUser: " + u.toString());
		
		// Check if the user email isn't already registered
		if (u != null && u.getEmail().equals(body.getEmail())) {
			return Response.status(Status.CONFLICT.getStatusCode(), "signupUser :: email already registered").build();
		}

		// Add the user to the db
		User user = null;
		try {
			user = Queryer.queryInsertUser(new User(body.getName(), body.getSurname(), body.getEmail(), body.getPassword()));
		} catch (InstantiationException e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode(), "signupUser :: failed to create the new user").build();
		}

		// If the user can be correctly obtained, the response return the success, otherwise return an error
		u = Queryer.queryFindUserById(user.getId())[0];
		
		if(u == null || u.getId() != user.getId()) {
			return Response.status(Status.INTERNAL_SERVER_ERROR.getStatusCode(), "signupUser :: failed to create the new user").build();
		}
		return Response.ok(u.info()).build();
	}

	/** GET ./users/{id}
	 *  Returns the information of a user by ID
	**/
	@Path("/{userId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserById(@PathParam("userId") Long userId) {
		// Get the user finding it on the db by id
		User u = Queryer.queryFindUserById(userId)[0];
		
		// Check if the user is correctly obtained
		if (u == null || u.getId() != userId) {
			return Response.status(Status.NOT_FOUND.getStatusCode(), "getUserById :: user not found").build();
		}
		return Response.ok(u.info()).build();
	}

	/** GET ./user/{id}/domains
	 *  Returns the domains registered by a user (currently active and expired but not buyed again by someone else)
	**/
	@Path("/{userId}/domains")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getUserDomainsByID(@PathParam("userId") Long userId) {
		// Get the user finding it on the db by id
		User u = Queryer.queryFindUserById(userId)[0];
		if(u == null) {
			return Response.status(Status.NOT_FOUND.getStatusCode(), "getUserDomainsByID :: user not found").build();
		}

		// Get all the operations made by the user
		Operation[] operations = Queryer.queryFindOperations(u, null, null);
		List<Domain> domains = new ArrayList<Domain>();
		for(Operation operation : operations) {
			// Consider only the domain that are currently registered or expired (not buyed again by someone else)
			Domain operationDomain = operation.getDomain();
			if(operationDomain != null && (operationDomain.getStatus() == DomainStatus.REGISTERED || operationDomain.getStatus() == DomainStatus.EXPIRED)) {
				// Check if the last contract for the domain is one of the user
				if (operationDomain.getLastContract().getOwner().getId() == u.getId()) {
					// Check if the domain is not already in the list
					if (!domains.contains(operationDomain)) {
						domains.add(operationDomain);
					}
				}
			}
		}
		return Response.ok(domains).build();
	}
}