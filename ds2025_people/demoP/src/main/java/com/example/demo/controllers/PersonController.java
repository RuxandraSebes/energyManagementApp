package com.example.demo.controllers;

import com.example.demo.dtos.PersonDTO;
import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.services.PersonService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/people")
@Validated
public class PersonController {

    private final PersonService personService;

    public PersonController(PersonService personService) {
        this.personService = personService;
    }

    @GetMapping
    public ResponseEntity<List<PersonDetailsDTO>> getPeople() {
        return ResponseEntity.ok(personService.findPersonsAllDetails());
    }

    @PostMapping
    public ResponseEntity<PersonDetailsDTO> create(@Valid @RequestBody PersonDetailsDTO person) {
        UUID id = personService.insert(person);
        PersonDetailsDTO saved = personService.findPersonById(id);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PersonDetailsDTO> getPerson(@PathVariable UUID id) {
        return ResponseEntity.ok(personService.findPersonById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PersonDetailsDTO>updatePerson(@PathVariable UUID id,@Valid @RequestBody PersonDetailsDTO person){
        PersonDetailsDTO updatedPerson = personService.update(id, person);
        return ResponseEntity.ok(updatedPerson);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable UUID id){
        personService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
