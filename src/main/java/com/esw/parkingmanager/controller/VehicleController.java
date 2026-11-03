package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.model.Vehicle;
import com.esw.parkingmanager.security.AccessGuard;
import com.esw.parkingmanager.service.VehicleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static com.esw.parkingmanager.security.RoleGroups.*;

@RestController
@RequestMapping("/properties/{propertyId}/apartments/{apartmentId}/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final AccessGuard access;

    public VehicleController(VehicleService vehicleService, AccessGuard access) {
        this.vehicleService = vehicleService;
        this.access = access;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vehicle create(@PathVariable UUID propertyId, @PathVariable UUID apartmentId, @Valid @RequestBody Vehicle vehicle) {
        access.checkApartment(propertyId, apartmentId, VEHICLE_EDITORS);
        return vehicleService.create(propertyId, apartmentId, vehicle);
    }

    @GetMapping
    public List<Vehicle> list(@PathVariable UUID propertyId, @PathVariable UUID apartmentId) {
        access.checkApartment(propertyId, apartmentId, EVERYONE);
        return vehicleService.list(propertyId, apartmentId);
    }

    @GetMapping("/{vehicleId}")
    public Vehicle getOne(@PathVariable UUID propertyId, @PathVariable UUID apartmentId, @PathVariable UUID vehicleId) {
        access.checkApartment(propertyId, apartmentId, EVERYONE);
        return vehicleService.getOne(propertyId, apartmentId, vehicleId);
    }

    @DeleteMapping("/{vehicleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID propertyId, @PathVariable UUID apartmentId, @PathVariable UUID vehicleId) {
        access.checkApartment(propertyId, apartmentId, VEHICLE_EDITORS);
        vehicleService.delete(propertyId, apartmentId, vehicleId);
    }
}