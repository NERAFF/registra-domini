package it.unimib.sd2024.models;

public class UserInfo {
	public Long id;
	public String name;
	public String surname;
	public String email;

	// ✅ Costruttore di default per JSON-B
	public UserInfo() {}
	public UserInfo(Long id, String name, String surname, String email) {
		this.id = id;
		this.name = name;
		this.surname = surname;
		this.email = email;
	}
	// fai get e set
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}

	@Override
	public String toString() {
		return "UserInfo = {\n\tid=" + id + ",\n\tname=" + name + ",\n\tsurname=" + surname + ",\n\temail=" + email + "\n}";
	}
}
