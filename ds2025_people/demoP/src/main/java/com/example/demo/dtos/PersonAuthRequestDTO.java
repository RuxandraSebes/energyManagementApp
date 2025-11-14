package com.example.demo.dtos;

// DTO-ul pe care PersonController îl va primi de la Auth Service.
// Conține ID-ul de legătură (authUserId) și detaliile personale.
public class PersonAuthRequestDTO {
    // Câmpul unic de legătură
    private Long authUserId;

    // Câmpuri de detaliu (din frontend)
    private String name;
    private int age;
    private String address;

    // Getteri și Setteri (obligatorii pentru @RequestBody)
    public Long getAuthUserId() { return authUserId; }
    public void setAuthUserId(Long authUserId) { this.authUserId = authUserId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}