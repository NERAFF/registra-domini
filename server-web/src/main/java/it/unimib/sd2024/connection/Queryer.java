package it.unimib.sd2024.connection;

import java.util.ArrayList;
import java.util.List;

import it.unimib.sd2024.models.User;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import it.unimib.sd2024.models.Domain;
import it.unimib.sd2024.models.Operation;
import it.unimib.sd2024.models.OperationType;

public class Queryer {
	/** queryFindDomainByName()
	 *  Queries the database for a find operation on domains with the same name as the one passed as a parameter.
	 *  Returns the domain found or null if not found.
	**/
	public static final Domain[] queryFindDomainByName(String domainName) {
		// Find the domain in the database
		String response = "";
		try {
			response = DatabaseConnector.Communicate("SELECT domains\nSEARCH name = \"" + domainName + "\"\nCOMMIT\n");
		} catch (Exception e) {
			System.err.println("[ERROR] Error while communicating with the database: " + e.getMessage());
			return null;
		}
		
		// Check the response from the database
		if (response.startsWith("[SUCCESS]")) {
			Jsonb jsonb = JsonbBuilder.create();
			Domain[] domains = jsonb.fromJson(response.split(": ")[1], Domain[].class);
			return domains;
		} else {
			System.err.println("[ERROR] Database response: " + response);
			return null;
		}
	}

	/** queryInsertDomain()
	 *  Queries the database for an insert operation on the domain passed as a parameter.
	 *  Returns the domain inserted or null if not inserted.
	**/
	public static final Domain queryInsertDomain(Domain domain) {
		// Insert the domain into the database
		String response = "";
		try {
			response = DatabaseConnector.Communicate("SELECT domains\nINSERT " + JsonbBuilder.create().toJson(domain) + "\nCOMMIT\n");
		} catch (Exception e) {
			System.err.println("[ERROR] Error while communicating with the database: " + e.getMessage());
			return null;
		}

		// Check the response from the database
		if (response.startsWith("[SUCCESS]")) {
			return domain;
		} else {
			System.err.println("[ERROR] Database response: " + response);
			return null;
		}
	}

	/** queryUpdateDomain()
	 *  Queries the database for an update operation on the domain passed as a parameter.
	 *  Returns the domain updated or null if not updated.
	**/
	public static final Domain queryUpdateDomain(Domain domain) {
		// Remove the domain from the database
		String response = "";
		try {
			response = DatabaseConnector.Communicate("SELECT \"domains\"\nREMOVE \"" + domain.getName() + "\"\nCOMMIT\n");
		} catch (Exception e) {
			System.err.println("[ERROR] Error while communicating with the database: " + e.getMessage());
			return null;
		}

		// Check the response from the database
		if (response.startsWith("[SUCCESS]")) {
			// Insert the updated domain
			try {
				response = DatabaseConnector.Communicate("SELECT \"domains\"\nINSERT " + JsonbBuilder.create().toJson(domain) + "\nCOMMIT\n");
			} catch (Exception e) {
				System.err.println("[ERROR] Error while communicating with the database: " + e.getMessage());
				return null;
			}

			// Check the response from the database
			if (response.startsWith("[SUCCESS]")) {
				return domain;
			} else {
				System.err.println("[ERROR] Database response: " + response);
				return null;
			}
		} else {
			System.err.println("[ERROR] Database response: " + response);
			return null;
		}
	}

	/** queryFindDomainsByOwner()
	 *  Queries the database for domains owned by a specific user.
	 *  Returns an array of domains found.
	**/
	public static final Domain[] queryFindDomainsByOwner(User owner) {
		if (owner == null) {
			return new Domain[0];
		}
		try {
			String command = "SELECT \"domains\"\nSEARCH \"lastContract.owner.id\" = \"" + owner.getId() + "\"\nCOMMIT\n";
			String response = DatabaseConnector.Communicate(command);

			if (response != null && response.startsWith("[SUCCESS]")) {
				String jsonPart = response.split(": ", 2)[1];
				if (jsonPart.trim().equals("[]")) {
					return new Domain[0];
				}
				return JsonbBuilder.create().fromJson(jsonPart, Domain[].class);
			}
		} catch (Exception e) {
			System.err.println("[ERROR] Error in queryFindDomainsByOwner: " + e.getMessage());
		}
		return new Domain[0];
	}

	/** queryFindUserByEmail()
	 *  Queries the database for a find operation on users with the same email as the one passed as a parameter.
	 *  Returns the user found or null if not found.
	**/
	public static final User[] queryFindUserByEmail(String email) {
		if (email == null || email.trim().isEmpty()) {
			return new User[0]; // input non valido → nessun risultato
		}

		try {
			// ⚠️ ESCAPE dell'email per evitare injection (vedi punto 2)
			String safeEmail = email.replace("\"", "\\\""); // minimo escape
			String command = "SELECT \"users\"\nSEARCH \"email\" = \"" + safeEmail + "\"\nCOMMIT\n";
			String response = DatabaseConnector.Communicate(command);

			if (response == null) {
				return new User[0];
			}

			if (response.startsWith("[SUCCESS]")) {
				String jsonPart = response.split(": ", 2)[1]; // split solo su prima occorrenza
				if (jsonPart.trim().equals("[]")) {
					return new User[0]; // array vuoto esplicito
				}
				Jsonb jsonb = JsonbBuilder.create();
				return jsonb.fromJson(jsonPart, User[].class);
			} else {
				// Esempio: [EMPTY] o [ERROR] → nessun utente
				System.out.println("[INFO] No user found for email: " + email);
				return new User[0];
			}
		} catch (Exception e) {
			System.err.println("[ERROR] Error in queryFindUserByEmail: " + e.getMessage());
			e.printStackTrace();
			return new User[0]; // mai null!
		}
	}

	/** queryFindUserById()
	 *  Queries the database for a find operation on users with the same id as the one passed as a parameter.
	 *  Returns the user found or null if not found.
	**/
	public static final User[] queryFindUserById(Long userId) {
		if (userId == null || userId <= 0) {
			return new User[0]; // input non valido
		}

		try {
			String command = "SELECT \"users\"\nSEARCH \"id\" = \"" + userId + "\"\nCOMMIT\n";
			String response = DatabaseConnector.Communicate(command);

			if (response == null) {
				return new User[0];
			}

			if (response.startsWith("[SUCCESS]")) {
				String jsonPart = response.split(": ", 2)[1];
				if (jsonPart.trim().equals("[]")) {
					return new User[0]; // array vuoto esplicito
				}
				Jsonb jsonb = JsonbBuilder.create();
				return jsonb.fromJson(jsonPart, User[].class);
			} else {
				// [EMPTY], [ERROR], ecc. → nessun risultato
				System.out.println("[INFO] No user found for id: " + userId);
				return new User[0];
			}
		} catch (Exception e) {
			System.err.println("[ERROR] Error in queryFindUserById: " + e.getMessage());
			e.printStackTrace();
			return new User[0]; // mai null!
		}
	}

	/** queryInsertUser()
	 *  Queries the database for an insert operation on the user passed as a parameter.
	 *  Returns the user inserted or null if not inserted.
	**/
	public static final User queryInsertUser(User user) {
		// Retrieve the response from the database for the prepared query sent
		String response = "";
		try {
			response = DatabaseConnector.Communicate("SELECT \"users\"\nINSERT "+ JsonbBuilder.create().toJson(user) + "\nCOMMIT\n");
		} catch (Exception e) {
			System.err.println("[ERROR] Error while communicating with the database: " + e.getMessage());
		}

		// Check the response from the database
		if (response.startsWith("[SUCCESS]")) {
			return user;
		} else {
			System.err.println("[ERROR] Database response: " + response);
			return null;
		}
	}

	/** queryFindAllUsers()
	 *  Queries the database to retrieve all users.
	 *  Returns an array of all users found.
	**/
	public static final User[] queryFindAllUsers() {
		try {
			String command = "SELECT \"users\"\nSEARCH *\nCOMMIT\n";
			String response = DatabaseConnector.Communicate(command);

			if (response == null) {
				return new User[0];
			}

			if (response.startsWith("[SUCCESS]")) {
				String jsonPart = response.split(": ", 2)[1];
				if (jsonPart.trim().equals("[]")) {
					return new User[0];
				}
				return JsonbBuilder.create().fromJson(jsonPart, User[].class);
			} else {
				return new User[0];
			}
		} catch (Exception e) {
			System.err.println("[ERROR] Error in queryFindAllUsers: " + e.getMessage());
			return new User[0];
		}
	}

	/** queryFindOperations()
	 *  Queries the database for a find operation on operations with the same user and domain as the ones passed as parameters.
	 *  Returns the list of operations found or an empty list if not found.
	**/
	public static final Operation[] queryFindOperations(User userFilter) {
		try {
			// Se l'utente non è specificato, non ci sono operazioni da trovare.
			if (userFilter == null) {
				return new Operation[0];
			}
			
			String query = "SELECT operations\nSEARCH \"owner.id\" = \"" + userFilter.getId() + "\"\nCOMMIT\n";
			String response = DatabaseConnector.Communicate(query);

			if (response == null) {
				return new Operation[0];
			}
			if (response.startsWith("[SUCCESS]")) {
				// Usa la stessa logica di split che funziona per le altre query.
				String jsonPart = response.split(": ", 2)[1];
				if (jsonPart.trim().equals("[]")) {
					return new Operation[0];
				}
				Jsonb jsonb = JsonbBuilder.create();
				return jsonb.fromJson(jsonPart, Operation[].class);
			} else {
				System.err.println("[ERROR] Database response for operations query: " + response);
				return new Operation[0];
			}
		} catch (Exception e) {
			System.err.println("[ERROR] Error in queryFindOperations: " + e.getMessage());
			e.printStackTrace();
			return new Operation[0];
		}
	}

	/** queryInsertOperation()
	 *  Queries the database for an insert operation on the operation passed as a parameter.
	 *  Returns the operation inserted or null if not inserted.
	**/
	public static final Operation queryInsertOperation(Operation operation) {
		// Insert the operation into the database
		String response = "";
		try {
			response = DatabaseConnector.Communicate("SELECT operations\nINSERT " + JsonbBuilder.create().toJson(operation) + "\nCOMMIT\n");
		} catch (Exception e) {
			System.err.println("[ERROR] Error while communicating with the database: " + e.getMessage());
			return null;
		}

		// Check the response from the database
		if (response.startsWith("[SUCCESS]")) {
			return operation;
		} else {
			System.err.println("[ERROR] Database response: " + response);
			return null;
		}
	}
}
