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
	public Response signinUser(SigninUserRequestBody body) {
		// Validazione input
		if (body == null || body.getEmail() == null || body.getPassword() == null) {
			return Response.status(Status.BAD_REQUEST)
					.entity("{\"signinUser\": \"Email and password are required\"}")
					.build();
		}

		// Cerca utente per email
		User[] users = Queryer.queryFindUserByEmail(body.getEmail());
		if (users == null || users.length == 0) {
			return Response.status(Status.UNAUTHORIZED)
					.entity("{\"signinUser\": \"Email or password may be incorrect\"}")
					.build();
		}

		User user = users[0];

		// Verifica la password usando il metodo verifyPassword() della classe User
		if (!user.verifyPassword(body.getPassword())) {
			return Response.status(Status.UNAUTHORIZED)
					.entity("{\"signinUser\": \"Email or password may be incorrect\"}")
					.build();
		}

		// Login riuscito: restituisci solo i dati pubblici (senza password)
		return Response.ok(user.info()).build();
	}

	/** POST ./users/signup
	 *  Allows to sign-up to a new account
	**/
	@Path("/signup")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response signupUser(SignupUserRequestBody body) {
		System.out.println("signupUser: " + body);

		// Validazione input
		if (body == null || body.getName() == null || body.getSurname() == null || 
			body.getEmail() == null || body.getPassword() == null) {
			return Response.status(Status.BAD_REQUEST)
					.entity("signupUser :: body must be indicated and with all fields valid")
					.build();
		}

		// Cerca utente per email
		User[] usersByEmail = Queryer.queryFindUserByEmail(body.getEmail());
		User existingUser = null;
		if (usersByEmail != null && usersByEmail.length > 0) {
			existingUser = usersByEmail[0];
		}

		// Controlla se l'email è già registrata
		if (existingUser != null) {
			return Response.status(Status.CONFLICT)
					.entity("signupUser :: email already registered")
					.build();
		}

		// Inserisci nuovo utente
		User newUser;
		try {
			newUser = Queryer.queryInsertUser(new User(
				body.getName(), 
				body.getSurname(), 
				body.getEmail(), 
				body.getPassword()
			));
		} catch (Exception e) {
			e.printStackTrace();
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("signupUser :: failed1 to create the new user")
					.build();
		}

		if (newUser == null || newUser.getId() < 0) {
			return Response.status(Status.INTERNAL_SERVER_ERROR)
					.entity("signupUser :: failed2 to create the new user")
					.build();
		}


		return Response.status(Status.CREATED).entity(newUser.info()).build();
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