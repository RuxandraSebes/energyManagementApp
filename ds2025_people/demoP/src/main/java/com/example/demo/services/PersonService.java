package com.example.demo.services;


import com.example.demo.dtos.PersonAuthRequestDTO;
import com.example.demo.dtos.PersonDTO;
import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.dtos.builders.PersonBuilder;
import com.example.demo.entities.Person;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.PersonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PersonService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);
    private final PersonRepository personRepository;

    @Autowired
    public PersonService(PersonRepository personRepository) {
        this.personRepository = personRepository;
    }

    public List<PersonDetailsDTO> findPersonsAllDetails() {
        List<Person> personList = personRepository.findAll();
        return personList.stream()
                .map(PersonBuilder::toPersonDetailsDTO)
                .collect(Collectors.toList());
    }

    public PersonDetailsDTO findPersonById(UUID id) {
        Optional<Person> prosumerOptional = personRepository.findById(id);
        if (!prosumerOptional.isPresent()) {
            LOGGER.error("Person with id {} was not found in db", id);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id);
        }
        return PersonBuilder.toPersonDetailsDTO(prosumerOptional.get());
    }

    public UUID insert(PersonDetailsDTO personDTO) {
        Person person = PersonBuilder.toEntity(personDTO);
        person = personRepository.save(person);
        LOGGER.debug("Person with id {} was inserted in db", person.getId());
        return person.getId();
    }

    public UUID insertFromAuth(PersonAuthRequestDTO authRequestDTO) {
        Person person = PersonBuilder.toEntity(authRequestDTO);
        person = personRepository.save(person);
        LOGGER.debug("Person linked to Auth ID {} was inserted in db", authRequestDTO.getAuthUserId());
        return person.getId();
    }

    public PersonDetailsDTO update(UUID id, PersonDetailsDTO personDetails) {
        return personRepository.findById(id)
                .map(existingPerson -> {
                    existingPerson.setName(personDetails.getName());
                    existingPerson.setAge(personDetails.getAge());
                    existingPerson.setAddress(personDetails.getAddress());

                    Person savedPerson = personRepository.save(existingPerson);

                    return PersonBuilder.toPersonDetailsDTO(savedPerson);
                })
                .orElseThrow(() -> {
                    LOGGER.error("Person with id {} was not found in db", id);
                    return new ResourceNotFoundException(Person.class.getSimpleName() + " with id: " + id);
                });
    }

    public void delete(UUID id){
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        Person.class.getSimpleName() + " with id: " + id));

        personRepository.delete(person);
        LOGGER.debug("Person with id {} was deleted from db", id);
        return;
    }

}
