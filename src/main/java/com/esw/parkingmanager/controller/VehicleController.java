package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.model.Vehicle;
import com.esw.parkingmanager.service.VehicleService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/properties/{propertyId}/apartments/{apartmentId}/vehicles")
public class VehicleController {
    private final VehicleService vehicleService;

    public VehicleController(VehicleService vehicleService) {
        this.vehicleService = vehicleService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Vehicle create(@PathVariable UUID propertyId, @PathVariable UUID apartmentId, @RequestBody Vehicle vehicle){
        return vehicleService.create(propertyId, apartmentId, vehicle);
    }

    @GetMapping
    public List<Vehicle> list(@PathVariable UUID propertyId, @PathVariable UUID apartmentId){
        return vehicleService.list(propertyId, apartmentId);
    }

    @GetMapping("/{vehicleId}")
    public Vehicle getOne(@PathVariable UUID propertyId, @PathVariable UUID apartmentId, @PathVariable UUID vehicleId){
        return vehicleService.getOne(propertyId, apartmentId, vehicleId);
    }

    @DeleteMapping("/{vehicleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID propertyId, @PathVariable UUID apartmentId, @PathVariable UUID vehicleId) {
        vehicleService.delete(propertyId, apartmentId, vehicleId);
    }
}
