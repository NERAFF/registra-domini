package it.unimib.sd2024.models;

import java.util.regex.Pattern;

/** CLASS Domain
 *  Represents the raw entity structure of a domain resource.
 *  It contains the domain name (primary key) and the current status of it:
 * 		- "AVAILABLE": The domain wasn't previusly registered but now is created and available for purchase (available for purchase)
 *  	- "ACQUIRING": The domain is currently being acquired by someone (not available for purchase)
 *  	- "REGISTERED": The domain is currently registered by someone (not available for purchase)
 *  	- "EXPIRED": The domain was previusly registered but now is available for purchase (available for purchase)
 *  It contains the last acquiring and the last contract to keep track of wich user can be do some operations on the domain.
 *  It also contains the monthly cost of the domain for cost calculation purposes.
**/
public class Domain {
	public static final String DOMAIN_REGEX = "[a-zA-Z0-9][a-zA-Z0-9-]*\\.[a-zA-Z0-9][a-zA-Z0-9-]*"; // A first character only alphanumerical then alphanumerical characters and hyphens, followed by a dot and a first character only alphanumerical then alphanumerical characters and hyphens

	private static final int RANDOM_PRICE_MIN = 10;
	private static final int RANDOM_PRICE_MAX = 50;

	private String name; /* Primary Key */
	private DomainStatus status;
	private Acquiring lastAcquiring;
	private Contract lastContract;
	private int yearCost;

	 // ✅ Costruttore di default per JSON-B
    public Domain() {
		this.yearCost = 0;
	}

	public Domain(String name) throws IllegalArgumentException {
		this.name = name;
		this.status = DomainStatus.AVAILABLE;
		this.lastAcquiring = null;
		this.lastContract = null;
		// Il costo viene generato UNA SOLA VOLTA qui.
		this.yearCost = RANDOM_PRICE_MIN + (int)(Math.random() * (RANDOM_PRICE_MAX - RANDOM_PRICE_MIN + 1));
	}

	public static boolean isNameValid(String name) {
		return Pattern.matches("^" + DOMAIN_REGEX + "$", name);
	}

	public static boolean isyearCostValid(int yearCost) {
		return yearCost >= 0;
	}

	public void setName(String name) {
		this.name = name;
		
	}
	public String getName() {
		return this.name;
	}
	
	public DomainStatus getStatus() {
		return this.status;
	}

	public void setStatus(DomainStatus status) {
		this.status = status;
	}

	public Acquiring getLastAcquiring() {
		return this.lastAcquiring;
	}

	public void setLastAcquiring(Acquiring lastAcquiring) {
		this.lastAcquiring = lastAcquiring;
	}

	public Contract getLastContract() {
		return this.lastContract;
	}

	public void setLastContract(Contract lastContract) {
		this.lastContract = lastContract;
	}

	public int getyearCost() {
		return this.yearCost;
	}

	public void setyearCost(int cost) {
		this.yearCost = cost;
	}

	public DomainInfo info() {
		return new DomainInfo(this.name, this.status);
	}

	public DomainStatusInfo statusInfo() {
		// REGISTERED 
		if (this.status == DomainStatus.REGISTERED) {
			return new DomainStatusInfo(this.status.name(), this.lastContract.getOwner(), this.lastContract.getExpirationDate());
		}
		// ACQUIRING
		if (this.status == DomainStatus.ACQUIRING) {
			return new DomainStatusInfo(this.status.name(), this.lastAcquiring.getUser(), null);
		}
		return new DomainStatusInfo(this.status.name(), null, null);
	}
	@Override
	public String toString() {
		return "Domain = {\n\tname=" + name + ",\n\tstatus=" + status + ",\n\tlastAcquiring=" + lastAcquiring + ",\n\tlastContract=" + lastContract + ",\n\tyearCost=" + yearCost + "\n}";
	}
}