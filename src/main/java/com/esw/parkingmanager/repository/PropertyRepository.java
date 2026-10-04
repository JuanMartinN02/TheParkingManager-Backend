package com.esw.parkingmanager.repository;

import com.esw.parkingmanager.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {
}
