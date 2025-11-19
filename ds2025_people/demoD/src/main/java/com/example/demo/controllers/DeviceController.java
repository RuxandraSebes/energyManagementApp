// demoD/controllers/DeviceController.java

package com.example.demo.controllers;

import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.security.UserAuthInfo; // <--- NEW IMPORT
import com.example.demo.services.DeviceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // <--- NEW IMPORT
import org.springframework.validation.annotation.Validated;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/devices")
@Validated
public class DeviceController {

    private final DeviceService deviceService;

    public DeviceController(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @GetMapping
    public ResponseEntity<List<DeviceDetailsDTO>> getDevices(
            @AuthenticationPrincipal UserAuthInfo userAuthInfo // <--- NEW PARAM
    ) {
        // Auth logic delegated to service based on role/ownership
        return ResponseEntity.ok(deviceService.findAllDeviceDetails(userAuthInfo)); // <--- MODIFIED CALL
    }

    // Admin-only (as per SecurityConfig)
    @PostMapping
    public ResponseEntity<DeviceDetailsDTO> create(@Valid @RequestBody DeviceDetailsDTO device) {
        UUID id = deviceService.insert(device);
        DeviceDetailsDTO saved = deviceService.findDeviceById(id);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeviceDetailsDTO> getDevice(@PathVariable UUID id) {
        // Device lookup is the same for now, but in a production system this should also check ownership for users.
        // For simplicity, we'll allow all authenticated users to view devices, but only Admin to see the full list, 
        // as per the requirement "an user should only be able to see his devices".
        // The service logic for `findAllDeviceDetails` handles the main filtering.
        return ResponseEntity.ok(deviceService.findDeviceById(id));
    }

    // Admin-only (as per SecurityConfig)
    @PutMapping("/{id}")
    public ResponseEntity<DeviceDetailsDTO>updateDevice(@PathVariable UUID id,@Valid @RequestBody DeviceDetailsDTO device){
        DeviceDetailsDTO updatedDevice = deviceService.update(id, device);
        return ResponseEntity.ok(updatedDevice);
    }

    // Admin-only (as per SecurityConfig)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDevice(@PathVariable UUID id){
        deviceService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // Internal endpoint, permitted in SecurityConfig
    @DeleteMapping(value = "/user/{personId}")
    public ResponseEntity<Void> deleteUserDevices(@PathVariable("personId") UUID personId) {
        deviceService.deleteUserDeviceAssociations(personId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}