package com.example.demo.services;


import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.dtos.PersonDetailsDTO;
import com.example.demo.dtos.builders.DeviceBuilder;
import com.example.demo.entities.Device;
import com.example.demo.entities.UserDevice;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.DeviceRepository;
import com.example.demo.repositories.UserDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.example.demo.security.UserAuthInfo; // <--- NEW IMPORT
import org.springframework.beans.factory.annotation.Value; // <--- NEW IMPORT
import org.springframework.web.client.RestTemplate; // <--- NEW IMPORT
import org.springframework.web.client.HttpClientErrorException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class DeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);

    private final RestTemplate restTemplate;

    @Value("${service.people.base-url}") // Assuming this property is set in application.properties
    private String PEOPLE_SERVICE_BASE_URL;

    private final DeviceRepository deviceRepository;
    private final UserDeviceRepository userDeviceRepository;

//    @Autowired
//    public DeviceService(DeviceRepository deviceRepository,
//                         UserDeviceRepository userDeviceRepository) {
//        this.deviceRepository = deviceRepository;
//        this.userDeviceRepository = userDeviceRepository;
//    }
@Autowired
public DeviceService(DeviceRepository deviceRepository,
                     UserDeviceRepository userDeviceRepository,
                     RestTemplate restTemplate) { // <--- MODIFIED CONSTRUCTOR
    this.deviceRepository = deviceRepository;
    this.userDeviceRepository = userDeviceRepository;
    this.restTemplate = restTemplate;
}

    private UUID getPersonUuidByAuthId(Long authUserId) {
        String url = PEOPLE_SERVICE_BASE_URL + "/by-auth/" + authUserId;
        try {
            // Note: This relies on a new endpoint /people/by-auth/{authUserId} in the People service
            ResponseEntity<PersonDetailsDTO> response = restTemplate.getForEntity(url, PersonDetailsDTO.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody().getId();
            }
        } catch (HttpClientErrorException.NotFound e) {
            LOGGER.warn("Person not found in People service for Auth ID: {}", authUserId);
            // Treat as not found/not authorized for device visibility
            throw new ResourceNotFoundException("Person for Auth ID " + authUserId + " not found.");
        } catch (Exception e) {
            LOGGER.error("Error calling People service to get Person UUID for Auth ID: {}", authUserId, e);
            throw new RuntimeException("External service error during authorization.", e);
        }
        return null; // Should be unreachable
    }

//    public List<DeviceDetailsDTO> findAllDeviceDetails() {
//        return deviceRepository.findAll().stream()
//                .map(this::toDeviceDetailsDTOWithUsers)
//                .collect(Collectors.toList());
//    }

    public List<DeviceDetailsDTO> findAllDeviceDetails(UserAuthInfo userAuthInfo) { // <--- MODIFIED SIGNATURE
        if (userAuthInfo.isAdmin()) {
            return deviceRepository.findAll().stream()
                    .map(this::toDeviceDetailsDTOWithUsers)
                    .collect(Collectors.toList());
        } else {
            // User: Filter devices assigned to this person (UUID)
            UUID personId = getPersonUuidByAuthId(userAuthInfo.getUserId()); // Gets Person's UUID

            // Find all UserDevice relations for this person
            List<UserDevice> userDevices = userDeviceRepository.findByIdUser(personId);

            // Collect the Device IDs
            List<UUID> deviceIds = userDevices.stream()
                    .map(UserDevice::getIdDevice)
                    .collect(Collectors.toList());

            // Fetch the Device entities by their IDs
            return deviceRepository.findAllById(deviceIds).stream()
                    .map(this::toDeviceDetailsDTOWithUsers)
                    .collect(Collectors.toList());
        }
    }

    public DeviceDetailsDTO findDeviceById(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device with id " + id));

        return toDeviceDetailsDTOWithUsers(device);
    }

    public UUID insert(DeviceDetailsDTO deviceDTO) {
        Device device = DeviceBuilder.toEntity(deviceDTO);
        device = deviceRepository.save(device);

        if (deviceDTO.getAssignedUserIds() != null) {
            for (UUID userId : deviceDTO.getAssignedUserIds()) {
                UserDevice ud = new UserDevice(userId, device.getId());
                userDeviceRepository.save(ud);
            }
        }

        LOGGER.debug("Device with id {} was inserted in db", device.getId());
        return device.getId();
    }

    public DeviceDetailsDTO update(UUID id, DeviceDetailsDTO deviceDetails) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device with id " + id));

        device.setName(deviceDetails.getName());
        device.setLocation(deviceDetails.getLocation());
        device.setMaxConsumption(deviceDetails.getMaxConsumption());
        deviceRepository.save(device);

        List<UserDevice> oldRelations = userDeviceRepository.findAllByIdDevice(id);
        if (!oldRelations.isEmpty()) {
            userDeviceRepository.deleteAll(oldRelations);
        }


        if (deviceDetails.getAssignedUserIds() != null) {
            for (UUID userId : deviceDetails.getAssignedUserIds()) {
                userDeviceRepository.save(new UserDevice(userId, id));
            }
        }

        return findDeviceById(id);
    }

    public void delete(UUID id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Device with id " + id));

        List<UserDevice> oldRelations = userDeviceRepository.findAllByIdDevice(id);
        if (!oldRelations.isEmpty()) {
            userDeviceRepository.deleteAll(oldRelations);
        }


        deviceRepository.delete(device);
        LOGGER.debug("Device with id {} was deleted from db", id);
    }

    public void deleteUserDeviceAssociations(UUID personId) {
        int deletedCount = userDeviceRepository.deleteByPersonId(personId);
        LOGGER.info("Au fost șterse {} asocieri de dispozitive (UserDevice) pentru persoana cu id-ul {}.", deletedCount, personId);
        // Nu aruncăm excepție dacă nu se găsesc înregistrări.
    }

    private DeviceDetailsDTO toDeviceDetailsDTOWithUsers(Device device) {
        List<UUID> assignedUserIds = userDeviceRepository.findByIdDevice(device.getId())
                .stream()
                .map(UserDevice::getIdUser)
                .collect(Collectors.toList());

        return new DeviceDetailsDTO(
                device.getId(),
                device.getName(),
                device.getLocation(),
                device.getMaxConsumption(),
                assignedUserIds
        );
    }
}

