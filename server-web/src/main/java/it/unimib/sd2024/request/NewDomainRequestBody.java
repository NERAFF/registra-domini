package it.unimib.sd2024.request;

import it.unimib.sd2024.models.DomainRequestAction;

public class NewDomainRequestBody {
	private String domainName;
	private DomainRequestAction requestAction;
	private Long userId;
	private Integer yearDuration; // Modificato da int a Integer per renderlo opzionale

	public NewDomainRequestBody() {}
	public NewDomainRequestBody(String name, DomainRequestAction status, Long userId, Integer yearsDuration) {
		this.domainName = name;
		this.requestAction = status;
		this.userId = userId;
		this.yearDuration = yearsDuration;
	}

	public String getDomainName() {
		return this.domainName;
	}

	public void setDomainName(String name) {
		this.domainName = name;
	}

	public DomainRequestAction getRequestAction() {
		return this.requestAction;
	}

	public void setRequestAction(DomainRequestAction status) {
		this.requestAction = status;
	}

	public Long getUserId() {
		return this.userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Integer getYearDuration() {
		return this.yearDuration;
	}

	public void setYearDuration(Integer yearDuration) {
		this.yearDuration = yearDuration;
	}

	@Override
	public String toString() {
		return "NewDomainRequestBody = {\n\tdomainName=" + domainName + ",\n\trequestAction=" + requestAction + ",\n\tuserId=" + userId + ",\n\tyearDuration=" + yearDuration + "\n}";
	}
}
