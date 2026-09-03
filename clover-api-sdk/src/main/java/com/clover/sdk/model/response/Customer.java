package com.clover.sdk.model.response;

import com.clover.sdk.internal.CloverList;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Customer {
    private String id;
    private String firstName;
    private String lastName;
    private CloverList<PhoneNumber> phoneNumbers;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public CloverList<PhoneNumber> getPhoneNumbers() { return phoneNumbers; }
    public void setPhoneNumbers(CloverList<PhoneNumber> phoneNumbers) { this.phoneNumbers = phoneNumbers; }

    public String getFullName() {
        if (firstName == null && lastName == null) return null;
        return ((firstName != null ? firstName : "") + " " + (lastName != null ? lastName : "")).trim();
    }

    public String getFirstPhoneNumber() {
        if (phoneNumbers == null || phoneNumbers.getElements().isEmpty()) return null;
        return phoneNumbers.getElements().get(0).getPhoneNumber();
    }

    @Override
    public String toString() {
        return "Customer{id='" + id + "', name='" + getFullName() + "'}";
    }
}
