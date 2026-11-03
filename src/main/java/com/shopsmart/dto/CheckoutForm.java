package com.shopsmart.dto;

import com.shopsmart.entity.PaymentMethod;
import jakarta.validation.constraints.*;

/** Delivery + payment details collected on the checkout page. */
public class CheckoutForm {

    @NotBlank(message = "Name is required")
    @Size(max = 100)
    private String deliveryName;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^(\\+?254|0)?[17]\\d{8}$",
             message = "Enter a valid Kenyan phone number, e.g. 0712345678")
    private String deliveryPhone;

    @NotBlank(message = "Delivery address is required")
    @Size(max = 255)
    private String deliveryAddress;

    @NotBlank(message = "City/town is required")
    @Size(max = 80)
    private String deliveryCity;

    @NotNull(message = "Please choose a payment method")
    private PaymentMethod paymentMethod;

    public String getDeliveryName() { return deliveryName; }
    public void setDeliveryName(String deliveryName) { this.deliveryName = deliveryName; }
    public String getDeliveryPhone() { return deliveryPhone; }
    public void setDeliveryPhone(String deliveryPhone) { this.deliveryPhone = deliveryPhone; }
    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
    public String getDeliveryCity() { return deliveryCity; }
    public void setDeliveryCity(String deliveryCity) { this.deliveryCity = deliveryCity; }
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
}
