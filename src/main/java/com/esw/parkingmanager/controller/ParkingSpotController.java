package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.dto.BulkSpotRequest;
import com.esw.parkingmanager.model.ParkingSpot;
import com.esw.parkingmanager.security.AccessGuard;
import com.esw.parkingmanager.service.ParkingSpotService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

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
    public ParkingSpot create(@PathVariable UUID propertyId, @RequestBody ParkingSpot spot) {
        access.checkProperty(propertyId);
        return parkingSpotService.create(propertyId, spot);
    }

    @PostMapping("/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ParkingSpot> createBulk(@PathVariable UUID propertyId, @RequestBody BulkSpotRequest request) {
        access.checkProperty(propertyId);
        return parkingSpotService.createBulk(propertyId, request.labels());
    }

    @GetMapping
    public List<ParkingSpot> list(@PathVariable UUID propertyId) {
        access.checkProperty(propertyId);
        return parkingSpotService.list(propertyId);
    }

    @GetMapping("/{spotId}")
    public ParkingSpot getOne(@PathVariable UUID propertyId, @PathVariable UUID spotId) {
        access.checkProperty(propertyId);
        return parkingSpotService.getOne(propertyId, spotId);
    }

    @DeleteMapping("/{spotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID propertyId, @PathVariable UUID spotId) {
        access.checkProperty(propertyId);
        parkingSpotService.delete(propertyId, spotId);
    }
}