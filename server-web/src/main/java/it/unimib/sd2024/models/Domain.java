package it.unimib.sd2024.models;

import java.time.LocalDate;
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

	private String name; /* Primary Key */
	private DomainStatus status;
	private Acquiring lastAcquiring;
	private Contract lastContract;
	private float monthlyCost;
	private LocalDate lastUpdateDate;

	 // ✅ Costruttore di default per JSON-B
    public Domain() {}

	public Domain(String name,  float monthlyCost) throws IllegalArgumentException {
		if (!Pattern.matches("^" + DOMAIN_REGEX + "$", name)) {
			throw new IllegalArgumentException("Invalid name value. Must match the DOMAIN_REGEX");
		}
		if (monthlyCost < 0) {
			throw new IllegalArgumentException("Invalid monthly cost value. Must be greater than or equal to 0");
		}
		this.name = name;
		this.status = DomainStatus.AVAILABLE;
		this.lastAcquiring = null;
		this.lastContract = null;
		this.monthlyCost = monthlyCost;
		this.lastUpdateDate = LocalDate.now();
	}

	public void setName(String name) {
		// Opzionale: valida il nome qui se vuoi
		this.name = name;
		this.lastUpdateDate = LocalDate.now(); // opzionale: aggiorna lastUpdateDate?
	}
	public String getName() {
		return this.name;
	}
	
	public DomainStatus getStatus() {
		return this.status;
	}

	public void setStatus(DomainStatus status) {
		this.status = status;
		this.lastUpdateDate = LocalDate.now();
	}

	public Acquiring getLastAcquiring() {
		return this.lastAcquiring;
	}

	public void setLastAcquiring(Acquiring lastAcquiring) {
		this.lastAcquiring = lastAcquiring;
		this.lastUpdateDate = LocalDate.now();
	}

	public Contract getLastContract() {
		return this.lastContract;
	}

	public void setLastContract(Contract lastContract) {
		this.lastContract = lastContract;
		this.lastUpdateDate = LocalDate.now();
	}

	public float getMonthlyCost() {
		return this.monthlyCost;
	}

	public void setMonthlyCost(float monthlyCost) throws IllegalArgumentException {
		if (monthlyCost < 0) {
			throw new IllegalArgumentException("Invalid monthly cost value. Must be greater than or equal to 0,00");
		}
		this.monthlyCost = monthlyCost;
		this.lastUpdateDate = LocalDate.now();
	}

	// Setter per lastUpdateDate (necessario per leggere dal JSON)
	public void setLastUpdateDate(LocalDate lastUpdateDate) {
		this.lastUpdateDate = lastUpdateDate;
	}
	public LocalDate getLastUpdateDate() {
		return this.lastUpdateDate;
	}

	public DomainInfo info() {
		return new DomainInfo(this.name, this.status);
	}

	@Override
	public String toString() {
		return "Domain = {\n\tname=" + name + ",\n\tstatus=" + status + ",\n\tlastAcquiring=" + lastAcquiring + ",\n\tlastContract=" + lastContract + ",\n\tmonthlyCost=" + monthlyCost + ",\n\tlastUpdateDate=" + lastUpdateDate + "\n}";
	}
}