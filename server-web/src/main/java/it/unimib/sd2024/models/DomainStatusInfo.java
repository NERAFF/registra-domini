// it/unimib/sd2024/models/DomainStatusInfo.java
package it.unimib.sd2024.models;

import java.time.LocalDate;

public class DomainStatusInfo {
    private String status;
    private UserInfo owner;
    private LocalDate expirationDate;
    
    // Costruttore per AVAILABLE
    public DomainStatusInfo() {
        this.status = "AVAILABLE";
    }

    // Costruttore per non disponibile
    public DomainStatusInfo(String status, UserInfo owner, LocalDate expirationDate) {
        this.status = status;
        this.owner = owner;
        this.expirationDate = expirationDate;
    }

    // Getter e Setter
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UserInfo getOwner() {
        return owner;
    }

    public void setOwner(UserInfo owner) {
        this.owner = owner;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

}