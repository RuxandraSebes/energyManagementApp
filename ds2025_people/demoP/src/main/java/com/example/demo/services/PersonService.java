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
import org.springframework.http.*;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PersonService {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersonService.class);
    private final PersonRepository personRepository;
    private final RestTemplate restTemplate;

    @Value("${service.auth.base-url}")
    private String AUTH_SERVICE_BASE_URL;

    @Value("${service.devices.base-url}")
    private String DEVICES_SERVICE_BASE_URL;

    @Autowired
    public PersonService(PersonRepository personRepository, RestTemplate restTemplate) {
        this.personRepository = personRepository;
        this.restTemplate = restTemplate;
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

//    public void delete(UUID id){
//        Person person = personRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        Person.class.getSimpleName() + " with id: " + id));
//
//        personRepository.delete(person);
//        LOGGER.debug("Person with id {} was deleted from db", id);
//        return;
//    }

    @Transactional
    public void delete(UUID id) {
        Optional<Person> personOptional = personRepository.findById(id);
        if (!personOptional.isPresent()) {
            LOGGER.error("Persoana cu id-ul {} nu a fost găsită în baza de date pentru ștergere.", id);
            throw new ResourceNotFoundException(Person.class.getSimpleName() + " cu id-ul: " + id);
        }

        Person personToDelete = personOptional.get();
        Long authIdToDelete = personToDelete.getAuthUserId(); // <--- NOU: Obținem ID-ul Long

        // 1. Șterge asocierile din microserviciul Devices (UserDevice)
        try {
            deleteUserDevices(id); // Rămâne cu UUID-ul Persoanei
            LOGGER.info("Asocierile dispozitivelor pentru persoana cu id-ul {} au fost șterse din microserviciul Devices.", id);
        } catch (Exception e) {
            // ...
        }

        // 2. Șterge utilizatorul din microserviciul Auth
        try {
            // ORIGINAL: deleteUserFromAuth(id);
            deleteUserFromAuth(authIdToDelete); // <--- MODIFICAT: Trimitem ID-ul Long
            LOGGER.info("Utilizatorul cu id-ul Long {} a fost șters din microserviciul Auth.", authIdToDelete);
        } catch (Exception e) {
            // ...
        }

        // 3. Șterge persoana din baza de date locală (People DB)
        personRepository.delete(personToDelete);
        LOGGER.debug("Persoana cu id-ul {} a fost ștearsă din baza de date locală.", id);
    }

    // Modificăm semnătura și logica metodei
    private void deleteUserFromAuth(Long authUserId) { // <--- MODIFICAT: Acceptă Long
        // Endpoint: DELETE http://auth:8082/auth/{authUserId}
        String url = AUTH_SERVICE_BASE_URL + "/" + authUserId; // <--- MODIFICAT: Trimitem ID-ul Long

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.warn("Utilizatorul cu id-ul Long {} nu a fost găsit în microserviciul Auth. (404 OK)", authUserId);
        } catch (HttpClientErrorException e) {
            LOGGER.error("Eroare HTTP la ștergerea utilizatorului din Auth service. Status: {}", e.getStatusCode(), e);
            throw new RuntimeException("Eroare la ștergerea utilizatorului din Auth: " + e.getMessage());
        }
    }

    // Metodă privată pentru a șterge intrările UserDevice asociate din microserviciul Devices
    private void deleteUserDevices(UUID personId) {
        // Endpoint: DELETE http://devices:8081/devices/user/{personId}
        String url = DEVICES_SERVICE_BASE_URL + "/user/" + personId;

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.warn("Nu au fost găsite asocieri de dispozitive de șters pentru persoana cu id-ul {}. (404 OK)", personId);
        } catch (HttpClientErrorException e) {
            LOGGER.error("Eroare HTTP la ștergerea dispozitivelor asociate (Devices service). Status: {}", e.getStatusCode(), e);
            throw new RuntimeException("Eroare la ștergerea dispozitivelor asociate: " + e.getMessage());
        }
    }

    // Metodă privată pentru a șterge utilizatorul din microserviciul Auth
    private void deleteUserFromAuth(UUID personId) {
        // Endpoint: DELETE http://auth:8082/auth/{personId}
        String url = AUTH_SERVICE_BASE_URL + "/" + personId;

        try {
            restTemplate.exchange(url, HttpMethod.DELETE, null, Void.class);
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.warn("Utilizatorul cu id-ul {} nu a fost găsit în microserviciul Auth. (404 OK)", personId);
        } catch (HttpClientErrorException e) {
            LOGGER.error("Eroare HTTP la ștergerea utilizatorului din Auth service. Status: {}", e.getStatusCode(), e);
            throw new RuntimeException("Eroare la ștergerea utilizatorului din Auth: " + e.getMessage());
        }
    }
}
