package com.shopsmart.dto;

import jakarta.validation.constraints.*;

/** Fields a customer may edit on their profile. Note: email and role are NOT here,
 *  so a customer can never change their own role or hijack another email. */
public class ProfileForm {

    @NotBlank(message = "Full name is required")
    @Size(max = 100)
    private String fullName;

    @Size(max = 20)
    private String phone;

    @Size(max = 255)
    private String address;

    @Size(max = 80)
    private String city;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
