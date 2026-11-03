package com.shopsmart.dto;

import jakarta.validation.constraints.*;

/**
 * Carries the registration form fields. Using a DTO (not the User entity)
 * means the browser can never set fields like "roles" or "active" - it can
 * only send exactly what is below. This is a deliberate security choice.
 */
public class RegisterForm {

    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please enter a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Please confirm your password")
    private String confirmPassword;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String address;

    @Size(max = 80)
    private String city;

    /** True only when both password fields match - checked by the service. */
    public boolean passwordsMatch() {
        return password != null && password.equals(confirmPassword);
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmPassword() { return confirmPassword; }
    public void setConfirmPassword(String confirmPassword) { this.confirmPassword = confirmPassword; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
