package com.example.demo.service;

import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.example.demo.dto.PeopleRegistrationRequest;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value; // Import necesar pentru @Value
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.transaction.annotation.Transactional; // Necesar pentru operatii de stergere

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate;

    // ⬅️ MUTAT ÎN CONFIGURAȚIE: Adresa URL este luată din application.properties/yml
    @Value("${microservice.people.url}")
    private String peopleServiceUrl;

    // ⬅️ AM ELIMINAT CONSTRUCTORUL INCOMPLET (a rămas doar cel complet)
    public AuthService(UserRepository userRepository, JwtUtil jwtUtil, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.restTemplate = restTemplate;
    }

    public String register(RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return "User already exists!";
        }

        // 1. Salvarea în DB-ul Auth
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User newUser = new User(request.getUsername(), encodedPassword, request.getRole());
        userRepository.save(newUser);

        // 2. Apel către People Service
        try {
            // Aici trimitem toate detaliile necesare serviciului People
            PeopleRegistrationRequest peopleRequest = new PeopleRegistrationRequest(
                    newUser.getId(),
                    request.getUsername(),
                    request.getRole(),
                    request.getName(), // ⬅️ NOU
                    request.getAge(),   // ⬅️ NOU
                    request.getAddress() // ⬅️ NOU
            );

            ResponseEntity<String> response = restTemplate.postForEntity(
                    peopleServiceUrl,
                    peopleRequest,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return "User registered successfully in Auth and People Service!";
            } else {
                return "User registered in Auth, but People Service failed: " + response.getBody();
            }

        } catch (ResourceAccessException e) {
            return "User registered, but People Service is unreachable (Check Docker network/URL): " + e.getMessage();
        } catch (Exception e) {
            return "User registered, but error linking to People Service: " + e.getMessage();
        }
    }


    public String login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return "User not found!";
        }

        User user = userOpt.get();
        if (passwordEncoder.matches(password, user.getPassword())) {
            return jwtUtil.generateToken(user.getUsername(), user.getRole());
        } else {
            return "Invalid password!";
        }
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);
    }

    @Transactional
    public void deleteUserByAuthUserId(Long authUserId) { // <--- METODĂ NOUĂ
        Optional<User> userOptional = userRepository.findById(authUserId); // ID-ul Long

        if (userOptional.isPresent()) {
            userRepository.delete(userOptional.get());
        }
        // Dacă utilizatorul nu este găsit (Optional.isEmpty()), nu facem nimic.
    }
}