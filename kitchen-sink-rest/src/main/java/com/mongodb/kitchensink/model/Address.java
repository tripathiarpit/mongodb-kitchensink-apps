package com.mongodb.kitchensink.model;
public class Address {
    private String street;
    private String city;
    private String state;
    private String pincode;
    private String country;

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPincode() {
        return pincode;
    }

    public void setPincode(String pincode) {
        this.pincode = pincode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }
    public Address() {

    }
    public Address(String country, String pincode, String state, String city, String street) {
        this.country = country;
        this.pincode = pincode;
        this.state = state;
        this.city = city;
        this.street = street;
    }
    private Address(Builder builder) {
        this.street = builder.street;
        this.city = builder.city;
        this.state = builder.state;
        this.pincode = builder.pincode;
        this.country = builder.country;
    }
    public static Builder builder() {
        return new Builder();
    }
    public static class Builder {
        private String street;
        private String city;
        private String state;
        private String pincode;
        private String country;

        public Builder street(String street) {
            this.street = street;
            return this;
        }

        public Builder city(String city) {
            this.city = city;
            return this;
        }

        public Builder state(String state) {
            this.state = state;
            return this;
        }

        public Builder pincode(String pincode) {
            this.pincode = pincode;
            return this;
        }

        public Builder country(String country) {
            this.country = country;
            return this;
        }

        public Address build() {
            // Can add validation logic here before building
            return new Address(this);
        }
    }
}