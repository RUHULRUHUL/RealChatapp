package com.example.realchat.model.profile;

public class Profile {
    String name;
    String uid;
    String status;
    String phone;
    String state;
    ActiveStatus activeStatus;

    public Profile() {
    }

    public Profile(String name, String uid, String status, String phone, String state, ActiveStatus activeStatus) {
        this.name = name;
        this.uid = uid;
        this.status = status;
        this.phone = phone;
        this.state = state;
        this.activeStatus = activeStatus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public ActiveStatus getActiveStatus() {
        return activeStatus;
    }

    public void setActiveStatus(ActiveStatus activeStatus) {
        this.activeStatus = activeStatus;
    }
}
