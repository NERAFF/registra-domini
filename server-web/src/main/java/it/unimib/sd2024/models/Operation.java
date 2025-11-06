package it.unimib.sd2024.models;

import java.time.LocalDate;

/** CLASS Whois
 *  Represents the raw entity structure of a whois resource, a contract associated with an operation carried out by a user for a specific domain.
 *  It contains the domain and the user involved in the operation, the creation and expiration date of the contract and the operation type that can be:
 *  	- "ACQUISITION": The user is acquiring a domain (reserved for future registrations)	
 *  	- "REGISTRATION": The user is registering a new domain
 *  	- "RENEWAL": The user is renewing a domain
 *  It also contains a flag to hide the owner information for privacy purposes.
**/
public class Operation {
	private User owner;
	private Domain domain;
	private OperationType type;
	private float cost;
	private LocalDate date;

	// ✅ Costruttore di default per JSON-B
	public Operation() {}
	public Operation(User owner, Domain domain, OperationType type, int monthDuration) {
		// Set up basic domain purchase information
		this.owner = owner;
		this.domain = domain;
		this.type = type;
		this.cost = domain.getMonthlyCost()*monthDuration;
		this.date = LocalDate.now();
	}
	
	public User getOwner() {
		return this.owner;
	}
	
	public Domain getDomain() {
		return this.domain;
	}
	
	public LocalDate getDate() {
		return this.date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public OperationType getType() {
		return this.type;
	}

	public float getCost() {
		return this.cost;
	}

	public OperationInfo info() {
		return new OperationInfo(this.owner, this.domain, this.type, this.cost);
	}

	@Override
	public String toString() {
		return "Operation = {\n\towner=" + owner + ",\n\tdomain=" + domain + ",\n\ttype=" + type + ",\n\tcost=" + cost + ",\n\tdate=" + date + "\n}";
	}
}