package com.example.demo.dto;

public class RegisterRequest {
    // Campuri pentru Auth Service
    private String username;
    private String password;
    private String role;

    // Campuri suplimentare pentru People Service
    private String name;
    private int age;
    private String address;

    // Getteri si Setteri sunt obligatorii pentru deserializarea JSON
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}