package com.esw.parkingmanager;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface PropertyRepository extends JpaRepository<Property, UUID> {
}
