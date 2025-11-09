package it.unimib.sd2024.models;

import java.time.LocalDate;

public class OperationInfo {
	public String domainName;
	public LocalDate date;
	public OperationType type;
	public int cost;

	/**
	 * Costruttore di default richiesto da JSON-B per la deserializzazione.
	 */
	public OperationInfo() {
	}

	/**
	 * Costruttore per creare un DTO (Data Transfer Object) con le informazioni essenziali di un'operazione.
	 * @param domain Il dominio associato all'operazione.
	 * @param date La data in cui è avvenuta l'operazione.
	 * @param type Il tipo di operazione (es. REGISTRATION, RENEWAL).
	 * @param cost Il costo dell'operazione.
	 */
	public OperationInfo(Domain domain, LocalDate date, OperationType type, int cost) {
		this.domainName = domain != null ? domain.getName() : null;
		this.date = date;
		this.type = type;
		this.cost = cost;
	}

	// --- Getter e Setter (Buona pratica per i JavaBean) ---

	public String getDomainName() { return domainName; }
	public void setDomainName(String domainName) { this.domainName = domainName; }

	public LocalDate getDate() { return date; }
	public void setDate(LocalDate date) { this.date = date; }

	public OperationType getType() { return type; }
	public void setType(OperationType type) { this.type = type; }

	public int getCost() { return cost; }
	public void setCost(int cost) { this.cost = cost; }

	@Override
	public String toString() {
		return "OperationInfo = {\n\tdomainName=" + domainName + ",\n\tdate=" + date + ",\n\ttype=" + type + ",\n\tcost=" + cost + "\n}";
	}
}
