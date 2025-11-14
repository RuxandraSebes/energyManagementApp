package com.example.demo.dtos.builders;

import com.example.demo.dtos.PersonAuthRequestDTO;
import com.example.demo.dtos.PersonDTO;
import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.entities.Person;

public class PersonBuilder {

    private PersonBuilder() {
    }

    public static PersonDTO toPersonDTO(Person person) {
        return new PersonDTO(person.getId(), person.getName(), person.getAge());
    }

    public static PersonDetailsDTO toPersonDetailsDTO(Person person) {
        return new PersonDetailsDTO(person.getId(), person.getName(), person.getAddress(), person.getAge());
    }

    public static Person toEntity(PersonDetailsDTO personDetailsDTO) {
        return new Person(personDetailsDTO.getName(),
                personDetailsDTO.getAddress(),
                personDetailsDTO.getAge());
    }

    public static Person toEntity(PersonAuthRequestDTO authRequestDTO) {
        // Se folosește constructorul existent pentru detaliile de bază
        Person person = new Person(
                authRequestDTO.getName(),
                authRequestDTO.getAddress(),
                authRequestDTO.getAge()
        );

        // Setarea câmpului unic de legătură
        person.setAuthUserId(authRequestDTO.getAuthUserId());

        return person;
    }
}
