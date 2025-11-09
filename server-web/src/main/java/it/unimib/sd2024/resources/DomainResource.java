package it.unimib.sd2024.resources;

import java.time.LocalDate;
import java.util.Random;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import it.unimib.sd2024.request.NewDomainRequestBody;
import it.unimib.sd2024.request.RenewDomainByNameRequestBody;
import it.unimib.sd2024.models.User;
import it.unimib.sd2024.models.UserInfo;
import it.unimib.sd2024.models.Domain;
import it.unimib.sd2024.models.Contract;
import it.unimib.sd2024.connection.Queryer;
import it.unimib.sd2024.models.Acquiring;
import it.unimib.sd2024.models.Operation;
import it.unimib.sd2024.models.DomainStatus;
import it.unimib.sd2024.models.DomainStatusInfo;
import it.unimib.sd2024.models.OperationType;
import it.unimib.sd2024.models.DomainRequestAction;

/**
 * 
**/
@Path("domains")
public class DomainResource {
	
	/** POST ./domains
	 *  Acquires/Register a new domain for a user
	**/
	@Path("/new")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response newDomain(NewDomainRequestBody body) {
		//print body for debug
		System.out.println("newDomain: " + body);

		// Check if the body is not null and has all the required fields valid
		if (body == null || body.getDomainName() == null || body.getUserId() == null) {
			return Response.status(Status.BAD_REQUEST.getStatusCode(), "newDomain :: body must be indicated and with all fields valid").build();
		}	

		// Get the domain finding it on the db by name
		Domain[] domainResult = Queryer.queryFindDomainByName(body.getDomainName());
		Domain d = (domainResult != null && domainResult.length > 0) ? domainResult[0] : null;

		// Check if the domain is new or existing
		boolean isNewDomain = (d == null);

		// Get the user finding it on the db by id
		User [] userResult = Queryer.queryFindUserById(body.getUserId());
		User u = (userResult != null && userResult.length > 0) ? userResult[0] : null;

		if (u == null) {
			return Response.status(Status.NOT_FOUND.getStatusCode(), "newDomain :: user not found").build();
		}

		// Determine whether the user is acquiring a domain or has finished acquiring a domain
		switch (body.getRequestAction()) {
			case DomainRequestAction.ACQUIRING:
				if (isNewDomain) {
					// If the domain is not found, create a new domain with a random price
					// and set last acquiring
					d = new Domain(body.getDomainName());
					d.setLastAcquiring(new Acquiring(u.info(), LocalDate.now()));
				} else {
					// If the domain is found, check if the domain can be acquired (it must be currently EXPIRED)
					if (d.getStatus() != DomainStatus.EXPIRED) {
						return Response.status(Status.CONFLICT).entity("{\"error\":\"Domain is not available for acquisition\"}").build();
					}
				}

				// Update the domain status to ACQUIRING and set the last acquiring
				d.setStatus(DomainStatus.ACQUIRING);
							
				// Se il dominio è nuovo, inseriscilo. Altrimenti, aggiornalo.
				if (isNewDomain) {
					Queryer.queryInsertDomain(d);
				} else {
					Queryer.queryUpdateDomain(d);
				}
				//ritorna il costo annuale del dominio
				return Response.ok(d.info()).build();
			case DomainRequestAction.ACQUIRED:
				if (d == null || d.getStatus() != DomainStatus.ACQUIRING) {
					return Response.status(Status.CONFLICT.getStatusCode(), "newDomain :: domain can not be acquired").build();
				}
				
				// Check if the duration of the acquisition is between 1 and 10 years
				if(body.getYearDuration() < 1 || body.getYearDuration() > 10) {
					return Response.status(Status.BAD_REQUEST.getStatusCode(), "newDomain :: yearDuration must be between 1 and 10").build();
				}

				// Check card and not save them
				// NOT IMPLEMENTED

				/// salva in operation
				

				if (u == null || !d.getLastAcquiring().getUser().id.equals(u.getId())) {
					return Response.status(Status.FORBIDDEN.getStatusCode(), "newDomain :: user is not authorized to complete the domain acquisition").build();
				}

				// Update the domain status to REGISTERED
				d.setStatus(DomainStatus.REGISTERED);
				
				LocalDate finishAcquisitionDate = LocalDate.now();
				final int originalYearCost = d.getyearCost(); // 1. Salva il costo originale

				d.setLastAcquiring(null);

				// Create the new contract and set it as the last contract of the domain
				d.setLastContract(new Contract(u.info(), finishAcquisitionDate, finishAcquisitionDate.plusYears(body.getYearDuration()).minusDays(1)));

				// Create the related operation
				Operation operation = new Operation(u, d, OperationType.REGISTRATION, body.getYearDuration()); // Usa il costo corretto

				// Update the data on the db
				Queryer.queryUpdateDomain(d); // 2. Esegui l'aggiornamento (che azzera yearCost nel file)
				Queryer.queryInsertOperation(operation);

				// 3. WORKAROUND: Ripristina il costo e aggiorna di nuovo il DB
				d.setyearCost(originalYearCost);
				Queryer.queryUpdateDomain(d);
				break;
			default:
				return Response.status(Status.BAD_REQUEST.getStatusCode(), "newDomain :: status must be either ACQUIRING or ACQUIRED").build();
		}

		// Return the updated domain
		return Response.ok(d.info()).build();
	}

	/** GET ./domains/{domain}
	 *  Returns the information of a domain by its name
	**/
	@Path("/{domainName}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getDomainByName(@PathParam("domainName") String domainName) {
		if (domainName == null || domainName.isBlank()) {
			return Response.status(Status.BAD_REQUEST).entity("Domain name is required").build();
		}
		if(!Domain.isNameValid(domainName)) {
			return Response.status(Status.BAD_REQUEST).entity("Invalid domain name format").build();
		}
		Domain[] result = Queryer.queryFindDomainByName(domainName);
		
		Domain d = (result != null && result.length > 0) ? result[0] : null;
		if (d == null) {
			// Domain not found, return AVAILABLE status
			return Response.ok(new DomainStatusInfo()).build(); // AVAILABLE
		}

		String status = d.getStatus().name();

		// Per stati ACQUIRING, REGISTERED, o EXPIRED, ritorna le informazioni di stato
		// che includono lo stato e il proprietario/acquirente.
		if ("ACQUIRING".equals(status) || "REGISTERED".equals(status) || "EXPIRED".equals(status)) {
			// Il metodo statusInfo() gestisce correttamente la logica per trovare
			// l'utente sia da lastAcquiring che da lastContract.
			//ritorna newDomainStatusInfo con tutte le info
			return Response.ok(d.statusInfo()).build();
		}
		// Caso fallback (es. AVAILABLE ma trovato in DB)
		return Response.ok(new DomainStatusInfo()).build();
	}

	/** POST ./domains/{domain}/renew
	 *  Extends the registration period of a domain by its name
	**/
	@Path("/{domainName}/renew")
	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response renewDomainByName(@PathParam("domainName") String domainName, RenewDomainByNameRequestBody body) {
		// Check if the body is not null and has all the required fields valid
		if (body == null || body.getUserId() == null || body.getYearsDuration() <= 0) {
			return Response.status(Status.BAD_REQUEST).entity("{\"error\":\"userId and a positive yearsDuration are required\"}").build();
		}

		// Get the domain finding it on the db by name
		Domain[] domainResult = Queryer.queryFindDomainByName(domainName);
		Domain d = (domainResult != null && domainResult.length > 0) ? domainResult[0] : null;

		if (d == null) {
			return Response.status(Status.NOT_FOUND).entity("{\"error\":\"Domain not found\"}").build();
		}

		// Check if the domain can be renewed (it must be currently REGISTERED)
		if (d.getStatus() != DomainStatus.REGISTERED) {
			return Response.status(Status.FORBIDDEN).entity("{\"error\":\"Domain is not registered\"}").build();
		}

		// Get the user finding it on the db by id
		User[] userResult = Queryer.queryFindUserById(body.getUserId());
		User u = (userResult != null && userResult.length > 0) ? userResult[0] : null;
		if (u == null) {
			return Response.status(Status.NOT_FOUND).entity("{\"error\":\"User not found\"}").build();
		}

		// Check if the domain is owned by the user (last contract owner must be the user)
		Contract lastContract = d.getLastContract();
		if (lastContract == null || !lastContract.getOwner().id.equals(u.getId())) {
			return Response.status(Status.FORBIDDEN).entity("{\"error\":\"User is not the owner of the domain\"}").build();
		}

		// Calculate the new expiration date
		LocalDate newExpirationDate = lastContract.getExpirationDate().plusYears(body.getYearsDuration());

		if (newExpirationDate.isAfter(lastContract.getAcquisitionDate().plusYears(10))) {
			return Response.status(Status.BAD_REQUEST).entity("{\"error\":\"Renewal exceeds maximum registration period of 10 years from the original acquisition date\"}").build();
		}

		// Update the expiration date on the existing contract
		lastContract.setExpirationDate(newExpirationDate);

		// Create the related operation
		Operation operation = new Operation(u, d, OperationType.RENEWAL, body.getYearsDuration());

		// Update the data on the db
		Queryer.queryUpdateDomain(d);
		Queryer.queryInsertOperation(operation);

		// Return the updated domain
		return Response.ok(d.info()).build();
	}
}