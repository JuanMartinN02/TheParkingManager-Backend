package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.dto.VehicleRequest;
import com.esw.parkingmanager.dto.VehicleResponse;
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
    public VehicleResponse create(@PathVariable UUID propertyId, @PathVariable UUID apartmentId,
                                  @Valid @RequestBody VehicleRequest request) {
        access.checkApartment(propertyId, apartmentId, VEHICLE_EDITORS);
        return VehicleResponse.from(vehicleService.create(propertyId, apartmentId, request.toEntity()));
    }

    @GetMapping
    public List<VehicleResponse> list(@PathVariable UUID propertyId, @PathVariable UUID apartmentId) {
        access.checkApartment(propertyId, apartmentId, EVERYONE);
        return vehicleService.list(propertyId, apartmentId).stream()
                .map(VehicleResponse::from)
                .toList();
    }

    @GetMapping("/{vehicleId}")
    public VehicleResponse getOne(@PathVariable UUID propertyId, @PathVariable UUID apartmentId,
                                  @PathVariable UUID vehicleId) {
        access.checkApartment(propertyId, apartmentId, EVERYONE);
        return VehicleResponse.from(vehicleService.getOne(propertyId, apartmentId, vehicleId));
    }

    @DeleteMapping("/{vehicleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID propertyId, @PathVariable UUID apartmentId, @PathVariable UUID vehicleId) {
        access.checkApartment(propertyId, apartmentId, VEHICLE_EDITORS);
        vehicleService.delete(propertyId, apartmentId, vehicleId);
    }
}