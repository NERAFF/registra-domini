package it.unimib.sd2024.models;

import java.time.LocalDate;

public class Acquiring {
    private UserInfo user;
	private LocalDate startAquisitionDate;
    private LocalDate finishAquisitionDate;

    // ✅ Costruttore di default per JSON-B
    public Acquiring() {}
    public Acquiring(UserInfo user, LocalDate startAquisitionDate) {
        this.user = user;
        this.startAquisitionDate = startAquisitionDate;
        this.finishAquisitionDate = null;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }
    public UserInfo getUser() {
        return this.user;
    }
    public void setStartAquisitionDate(LocalDate startAquisitionDate) {
        this.startAquisitionDate = startAquisitionDate;
    }
    public LocalDate getStartAquisitionDate() {
        return this.startAquisitionDate;
    }
    public void setFinishAquisitionDate(LocalDate finishAquisitionDate) {
        this.finishAquisitionDate = finishAquisitionDate;
    }

    public LocalDate getFinishAquisitionDate() {
        return this.finishAquisitionDate;
    }

    @Override
    public String toString() {
        return "Acquiring = {\n\tuser=" + user + ",\n\tstartAquisitionDate=" + startAquisitionDate + ",\n\tfinishAquisitionDate=" + finishAquisitionDate + "\n}";
    }
}