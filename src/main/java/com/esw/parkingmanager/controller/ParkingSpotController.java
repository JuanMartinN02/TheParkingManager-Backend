package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.dto.BulkSpotRequest;
import com.esw.parkingmanager.dto.SpotRequest;
import com.esw.parkingmanager.dto.SpotResponse;
import com.esw.parkingmanager.security.AccessGuard;
import com.esw.parkingmanager.service.ParkingSpotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.esw.parkingmanager.security.RoleGroups.*;

@RestController
@RequestMapping("/properties/{propertyId}/spots")
public class ParkingSpotController {

    private final ParkingSpotService parkingSpotService;
    private final AccessGuard access;

    public ParkingSpotController(ParkingSpotService parkingSpotService, AccessGuard access) {
        this.parkingSpotService = parkingSpotService;
        this.access = access;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SpotResponse create(@PathVariable UUID propertyId, @Valid @RequestBody SpotRequest request) {
        access.checkProperty(propertyId, ADMINS);
        return SpotResponse.from(parkingSpotService.create(propertyId, request.toEntity()));
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<SpotResponse> createBulk(@PathVariable UUID propertyId, @RequestBody BulkSpotRequest request) {
        access.checkProperty(propertyId, ADMINS);
        return parkingSpotService.createBulk(propertyId, request.labels()).stream()
                .map(SpotResponse::from)
                .toList();
    }

    @GetMapping
    public List<SpotResponse> list(@PathVariable UUID propertyId) {
        access.checkProperty(propertyId, STAFF);
        return parkingSpotService.list(propertyId).stream()
                .map(SpotResponse::from)
                .toList();
    }

    @GetMapping("/{spotId}")
    public SpotResponse getOne(@PathVariable UUID propertyId, @PathVariable UUID spotId) {
        access.checkProperty(propertyId, STAFF);
        return SpotResponse.from(parkingSpotService.getOne(propertyId, spotId));
    }

    @DeleteMapping("/{spotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID propertyId, @PathVariable UUID spotId) {
        access.checkProperty(propertyId, ADMINS);
        parkingSpotService.delete(propertyId, spotId);
    }
}