package com.esw.parkingmanager.repository;

import com.esw.parkingmanager.model.AppUser;
import com.esw.parkingmanager.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    // For login
    Optional<AppUser> findByEmail(String email);

    boolean existsByRole(Role role);

    boolean existsByEmail(String email);

    List<AppUser> findByPropertyId(UUID propertyId);

    // For move-out / move-in
    List<AppUser> findByApartmentIdAndActiveTrue(UUID apartmentId);
}