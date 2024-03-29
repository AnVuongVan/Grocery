package com.vietis.eatit.models;

public class Shop {
    private String uid, email, name, shopName, phone, deliveryFee,
            country, state, city, address, profileImage;
    private boolean shopOpen, online;

    public Shop() {
    }

    public Shop(String uid, String email, String name, String shopName, String phone, String deliveryFee, boolean online, String country, String state, String city, String address, String profileImage, boolean shopOpen) {
        this.uid = uid;
        this.email = email;
        this.name = name;
        this.shopName = shopName;
        this.phone = phone;
        this.deliveryFee = deliveryFee;
        this.online = online;
        this.country = country;
        this.state = state;
        this.city = city;
        this.address = address;
        this.profileImage = profileImage;
        this.shopOpen = shopOpen;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(String deliveryFee) {
        this.deliveryFee = deliveryFee;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public boolean isShopOpen() {
        return shopOpen;
    }

    public void setShopOpen(boolean shopOpen) {
        this.shopOpen = shopOpen;
    }
}
