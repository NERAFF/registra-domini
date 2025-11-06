// it/unimib/sd2024/models/DomainStatusInfo.java
package it.unimib.sd2024.models;

public class DomainStatusInfo {
    private String status;
    private Long ownerId;
    private String ownerName;
    private String ownerSurname;
    private String ownerEmail;
    private String expirationDate; // solo se applicabile

    // Costruttore per AVAILABLE
    public DomainStatusInfo() {
        this.status = "AVAILABLE";
    }

    // Costruttore per non disponibile
    public DomainStatusInfo(String status, Long ownerId, String ownerName, String ownerSurname, String ownerEmail, String expirationDate) {
        this.status = status;
        this.ownerId = ownerId;
        this.ownerName = ownerName;
        this.ownerSurname = ownerSurname;
        this.ownerEmail = ownerEmail;
        this.expirationDate = expirationDate;
    }

    // Getter (obbligatori per JSON-B)
    public String getStatus() { return status; }
    public Long getOwnerId() { return ownerId; }
    public String getOwnerName() { return ownerName; }
    public String getOwnerSurname() { return ownerSurname; }
    public String getOwnerEmail() { return ownerEmail; }
    public String getExpirationDate() { return expirationDate; }
}