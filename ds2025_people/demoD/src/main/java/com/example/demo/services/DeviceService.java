package com.example.demo.services;


import com.example.demo.dtos.DeviceDTO;
import com.example.demo.dtos.DeviceDetailsDTO;
import com.example.demo.dtos.builders.DeviceBuilder;
import com.example.demo.entities.Device;
import com.example.demo.entities.UserDevice;
import com.example.demo.handlers.exceptions.model.ResourceNotFoundException;
import com.example.demo.repositories.DeviceRepository;
import com.example.demo.repositories.UserDeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
@Service
public class DeviceService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository deviceRepository;
    private final UserDeviceRepository userDeviceRepository;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository,
                         UserDeviceRepository userDeviceRepository) {
        this.deviceRepository = deviceRepository;
        this.userDeviceRepository = userDeviceRepository;
    }

    public List<DeviceDetailsDTO> findAllDeviceDetails() {
        return deviceRepository.findAll().stream()
                .map(this::toDeviceDetailsDTOWithUsers)
                .collect(Collectors.toList());
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

