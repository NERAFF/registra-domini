package it.unimib.sd2024.models;

import java.time.LocalDate;

public class Contract {
	private UserInfo owner;
	private LocalDate acquisitionDate;
	private LocalDate expirationDate;

	// ✅ Costruttore di default per JSON-B
	public Contract() {}
	public Contract(UserInfo owner, LocalDate acquisitionDate, LocalDate expirationDate) {
		this.owner = owner;
		this.acquisitionDate = acquisitionDate;
		this.expirationDate = expirationDate;
	}

	public void setOwner(UserInfo owner) {
		this.owner = owner;
	}
	public UserInfo getOwner() {
		return this.owner;
	}

	public void setAcquisitionDate(LocalDate acquisitionDate) {
		this.acquisitionDate = acquisitionDate;
	}
	public LocalDate getAcquisitionDate() {
		return this.acquisitionDate;
	}
	public void setExpirationDate(LocalDate expirationDate) {
		this.expirationDate = expirationDate;
	}
	public LocalDate getExpirationDate() {
		return this.expirationDate;
	}

	@Override
	public String toString() {
		return "Contract = {\n\towner=" + owner + ",\n\tacquisitionDate=" + acquisitionDate + ",\n\texpirationDate=" + expirationDate + "\n}";
	}
}
