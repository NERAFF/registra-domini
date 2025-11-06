package it.unimib.sd2024.models;

import java.util.regex.Pattern;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class User {
    public static final String EMAIL_REGEX = "[a-zA-Z0-9-\\.]+@" + Domain.DOMAIN_REGEX;
    public static final String PASSWORD_REGEX = "(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-_]).{8,}";
    private static Long lastId = 0L;

    private Long id;
    private String name;
    private String surname;
    private String email;
    private String password; // conterrà l'hash in Base64
    private LocalDate creationDate;
    private LocalDate lastUpdateDate;

    // ✅ Costruttore di default per JSON-B
    public User() {}

    // Costruttore per registrazione
    public User(String name, String surname, String email, String password) {
        if (!Pattern.matches("^" + EMAIL_REGEX + "$", email)) {
            throw new IllegalArgumentException("Invalid email format");
        }
        if (!Pattern.matches("^" + PASSWORD_REGEX + "$", password)) {
            throw new IllegalArgumentException("Invalid password value. Must match the PASSWORD_REGEX");
        }
        this.id = ++lastId;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.password = hashPasswordSHA256(password); // ← hash coerente
        this.creationDate = LocalDate.now();
        this.lastUpdateDate = this.creationDate;
    }

    // Getter
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getSurname() { return surname; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public LocalDate getCreationDate() { return creationDate; }
    public LocalDate getLastUpdateDate() { return lastUpdateDate; }

    // ✅ Setter SENZA validazione (per JSON-B)
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; this.lastUpdateDate = LocalDate.now(); }
    public void setSurname(String surname) { this.surname = surname; this.lastUpdateDate = LocalDate.now(); }
    public void setEmail(String email) { this.email = email; this.lastUpdateDate = LocalDate.now(); }
    public void setPassword(String password) { this.password = password; this.lastUpdateDate = LocalDate.now(); }
    public void setCreationDate(LocalDate creationDate) { this.creationDate = creationDate; }
    public void setLastUpdateDate(LocalDate lastUpdateDate) { this.lastUpdateDate = lastUpdateDate; }

    // ✅ Metodo statico per hashare con SHA-256 + Base64
    public static String hashPasswordSHA256(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes); // ← Base64 sicuro
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    // ✅ Metodo per verificare una password in chiaro
    public boolean verifyPassword(String rawPassword) {
        String hashedInput = hashPasswordSHA256(rawPassword);
        return hashedInput.equals(this.password);
    }

    public UserInfo info() {
        return new UserInfo(this.id, this.name, this.surname, this.email);
    }

    @Override
    public String toString() {
        return "User = {\n\tid=" + id + ",\n\tname=" + name + ",\n\tsurname=" + surname +
               ",\n\temail=" + email + ",\n\tpassword=" + password + 
               ",\n\tcreationDate=" + creationDate + ",\n\tlastUpdateDate=" + lastUpdateDate + "\n}";
    }
}