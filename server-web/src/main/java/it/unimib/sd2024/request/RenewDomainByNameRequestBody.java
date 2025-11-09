package it.unimib.sd2024.request;

public class RenewDomainByNameRequestBody {
    private Long userId;
    private int yearsDuration;

    public RenewDomainByNameRequestBody() {
    }
    public RenewDomainByNameRequestBody(Long userId, int yearsDuration) {
        this.userId = userId;
        this.yearsDuration = yearsDuration;
    }

    public Long getUserId() {
        return this.userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public int getYearsDuration() {
        return this.yearsDuration;
    }

    public void setYearsDuration(int yearsDuration) {
        this.yearsDuration = yearsDuration;
    }

    @Override
    public String toString() {
        return "RenewDomainByNameRequestBody = {\n\tuserId=" + userId + ",\n\tyearsDuration=" + yearsDuration + "\n}";
    }
}
