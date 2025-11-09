package it.unimib.sd2024.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.util.ArrayList;
import java.util.List;

import it.unimib.sd2024.models.User;
import it.unimib.sd2024.models.Domain;
import it.unimib.sd2024.connection.Queryer;
import it.unimib.sd2024.models.Operation;
import it.unimib.sd2024.models.OperationInfo;
import it.unimib.sd2024.models.OperationType;

/**
 * 
**/
@Path("operations")
public class OperationResource {
	/** GET ./operations[?<userId="userId">[&domainName="domainName"][&operationType="operationType"]]
	 *  Returns a list of operations with related infos. The list can be filtered by specifying one or more parameters among user, domain and type.
	**/
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getOperationsList(@QueryParam("userId") Long userId) {

		try {
			// Se l'userId non è fornito, non possiamo filtrare.
			// Potremmo restituire tutte le operazioni, ma è meglio richiedere l'ID.
			if (userId == null) {
				return Response.status(Status.BAD_REQUEST).entity("{\"error\":\"userId parameter is required\"}").build();
			}

			// Cerca l'utente per ID.
			User[] users = Queryer.queryFindUserById(userId);
			if (users == null || users.length == 0) {
				// Se l'utente non esiste, restituiamo una lista vuota perché non ci sono operazioni per lui.
				return Response.ok(new Operation[0]).build();
			}
			User userFilter = users[0];

			// Esegui la query per trovare tutte le operazioni per quell'utente.
			Operation[] operations = Queryer.queryFindOperations(userFilter);

			//ritorna operation.info di ogni operation
			List<OperationInfo> operationInfos = new ArrayList<>();
			for (Operation op : operations) {
				operationInfos.add(op.info());
			}
			return Response.ok(operationInfos).build();
			
		} catch (Exception e) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error retrieving operations: " + e.getMessage()).build();
		}
	}
}