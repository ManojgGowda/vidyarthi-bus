package com.vidyarthi.bus.models;

public class Alternative {
    private String name;
    private String detail;
    private String phoneNumber;
    private String type;   // "auto" | "bus" | "cycle"

    public Alternative() {}

    public Alternative(String name, String detail, String phoneNumber, String type) {
        this.name        = name;
        this.detail      = detail;
        this.phoneNumber = phoneNumber;
        this.type        = type;
    }

    public String getName()             { return name; }
    public void setName(String v)       { this.name = v; }

    public String getDetail()           { return detail; }
    public void setDetail(String v)     { this.detail = v; }

    public String getPhoneNumber()      { return phoneNumber; }
    public void setPhoneNumber(String v){ this.phoneNumber = v; }

    public String getType()             { return type; }
    public void setType(String v)       { this.type = v; }
}
