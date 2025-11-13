package com.example.demo.repositories;

import com.example.demo.entities.UserDevice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UserDeviceRepository extends JpaRepository<UserDevice, UUID> {

    List<UserDevice> findByIdDevice(UUID idDevice);

    List<UserDevice> findByIdUser(UUID idUser);

    List<UserDevice> findAllByIdDevice(UUID idDevice);
}
