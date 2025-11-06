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
	private static final float RANDOM_PRICE_MIN = 0.10f;
	private static final float RANDOM_PRICE_MAX = 25.00f;

	/** POST ./domains
	 *  Acquires/Register a new domain for a user
	**/
	@Path("/new")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response newDomain(NewDomainRequestBody body) {
		// Check if the body is not null and has all the required fields valid
		if (body == null || body.getRequestAction() == null || body.getDomainName() == null || body.getUserId() == null) {
			return Response.status(Status.BAD_REQUEST.getStatusCode(), "newDomain :: body must be indicated and with all fields valid").build();
		}

		// Get the domain finding it on the db by name
		Domain d = Queryer.queryFindDomainByName(body.getDomainName())[0];
		
		// Determine whether the user is acquiring a domain or has finished acquiring a domain
		switch (body.getRequestAction()) {
			case DomainRequestAction.ACQUIRING:
				boolean isNewDomain = (d == null);
				if (d == null) {
					// If the domain is not found, create a new domain with a random price
					d = new Domain(body.getDomainName(), RANDOM_PRICE_MIN + new Random().nextFloat() * (RANDOM_PRICE_MAX - RANDOM_PRICE_MIN));
				} else {
					// If the domain is found, check if the domain can be acquired (it must be currently EXPIRED)
					if (d.getStatus() != DomainStatus.EXPIRED) {
						return Response.status(Status.CONFLICT).entity("{\"error\":\"Domain is not available for acquisition\"}").build();
					}
				}

				// Update the domain status to ACQUIRING and set the last acquiring
				d.setStatus(DomainStatus.ACQUIRING);
				d.setLastAcquiring(new Acquiring(Queryer.queryFindUserById(body.getUserId())[0].info(), LocalDate.now()));
				
				// Se il dominio è nuovo, inseriscilo. Altrimenti, aggiornalo.
				if (isNewDomain) {
					Queryer.queryInsertDomain(d);
				} else {
					Queryer.queryUpdateDomain(d);
				}
				break;
			case DomainRequestAction.ACQUIRED:
				if (d == null || d.getStatus() != DomainStatus.ACQUIRING) {
					return Response.status(Status.CONFLICT.getStatusCode(), "newDomain :: domain can not be acquired").build();
				}
				
				// Check if the duration of the acquisition is at least 12 months (1 year) and at most 120 months (10 years)
				if(body.getMonthsDuration() < 12 || body.getMonthsDuration() > 120) {
					return Response.status(Status.BAD_REQUEST.getStatusCode(), "newDomain :: monthDuration must be between 12 and 120").build();
				}
				
				// Check if the acquisition was started by the user
				User u = Queryer.queryFindUserById(body.getUserId())[0];
				if (u == null || !d.getLastAcquiring().getUser().id.equals(u.getId())) {
					return Response.status(Status.FORBIDDEN.getStatusCode(), "newDomain :: user is not authorized to complete the domain acquisition").build();
				}

				// Update the domain status to REGISTERED
				d.setStatus(DomainStatus.REGISTERED);
				
				LocalDate finishAcquisitionDate = LocalDate.now();
				// Update the FinishAquisitionDate of the last acquiring
				d.getLastAcquiring().setFinishAquisitionDate(finishAcquisitionDate);

				// Create the new contract and set it as the last contract of the domain
				d.setLastContract(new Contract(u.info(), finishAcquisitionDate, finishAcquisitionDate.plusMonths(body.getMonthsDuration())));

				// Create the related operation
				Operation operation = new Operation(u, d, OperationType.REGISTRATION, 0);

				// Update the data on the db
				Queryer.queryUpdateDomain(d);
				Queryer.queryInsertOperation(operation);
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
		Domain[] result = Queryer.queryFindDomainByName(domainName);
		for (Domain d : result) {
			System.out.println(d.toString());
		}
		Domain d = (result != null && result.length > 0) ? result[0] : null;
		if (d == null) {
			return Response.ok(new DomainStatusInfo()).build(); // AVAILABLE
		}
		String status = d.getStatus().name();

		// Caso 1: ACQUIRING → mostra chi sta acquistando
		if ("ACQUIRING".equals(status) && d.getLastAcquiring() != null && d.getLastAcquiring().getUser() != null) {
			UserInfo user = d.getLastAcquiring().getUser();
			return Response.ok(new DomainStatusInfo(
				status,
				user.getId(),
				user.getName(),
				user.getSurname(),
				user.getEmail(),
				null // nessuna scadenza durante l'acquisto
			)).build();
		}

		// Caso 2: REGISTERED o EXPIRED → mostra proprietario e scadenza
		if (("REGISTERED".equals(status) || "EXPIRED".equals(status)) 
			&& d.getLastContract() != null 
			&& d.getLastContract().getOwner() != null) {
			
			UserInfo owner = d.getLastContract().getOwner();
			String expDate = d.getLastContract().getExpirationDate() != null 
				? d.getLastContract().getExpirationDate().toString() 
				: null;

			return Response.ok(new DomainStatusInfo(
				status,
				owner.getId(),
				owner.getName(),
				owner.getSurname(),
				owner.getEmail(),
				expDate
			)).build();
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
		// Get the domain finding it on the db by name
		Domain d = Queryer.queryFindDomainByName(domainName)[0];

		// Check if the domain is correctly obtained
		if (d == null || !d.getName().equals(domainName)) {
			return Response.status(Status.NOT_FOUND.getStatusCode(), "renewDomainByName :: domain not found").build();
		}

		// Check if the domain can be renewed (it must be currently REGISTERED)
		if (d.getStatus() != DomainStatus.REGISTERED) {
			return Response.status(Status.FORBIDDEN.getStatusCode(), "renewDomainByName :: domain is not registered").build();
		}

		// Get the user finding it on the db by id
		User u = Queryer.queryFindUserById(body.getUserId())[0];
		if (u == null) {
			return Response.status(Status.NOT_FOUND.getStatusCode(), "renewDomainByName :: user not found").build();
		}

		// Check if the domain is owned by the user (last contract owner must be the user)
		Contract lastContract = d.getLastContract();
		if (!lastContract.getOwner().id.equals(u.getId())) {
			return Response.status(Status.FORBIDDEN.getStatusCode(), "renewDomainByName :: user is not the owner of the domain").build();
		}

		// Calculate the new acquisition and expiration dates
		LocalDate newAcquisitionDate = lastContract.getExpirationDate().plusDays(1);
		LocalDate newExpirationDate = newAcquisitionDate.plusMonths(body.getMonthsDuration());

		// Create the new contract and set it as the last contract of the domain
		d.setLastContract(new Contract(u.info(), newAcquisitionDate, newExpirationDate));

		// Create the related operation
		Operation operation = new Operation(u, d, OperationType.RENEWAL, body.getMonthsDuration());

		// Update the data on the db
		Queryer.queryUpdateDomain(d);
		Queryer.queryInsertOperation(operation);

		// Return the updated domain
		return Response.ok(d.info()).build();
	}
}