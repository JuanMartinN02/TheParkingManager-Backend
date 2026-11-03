package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.dto.PlateLookupResponse;
import com.esw.parkingmanager.dto.SpotLookupResponse;
import com.esw.parkingmanager.security.AccessGuard;
import com.esw.parkingmanager.service.PlateLookupService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.esw.parkingmanager.security.RoleGroups.*;

@RestController
@RequestMapping("/properties/{propertyId}/lookup")
public class LookupController {

    private final PlateLookupService plateLookupService;
    private final AccessGuard access;

    public LookupController(PlateLookupService plateLookupService, AccessGuard access) {
        this.plateLookupService = plateLookupService;
        this.access = access;
    }

    // Lookup by plate (towers)
    // Plate goes in the query string so spaces and dashes don't break the URL path
    @GetMapping("/plate")
    public PlateLookupResponse lookupPlate(@PathVariable UUID propertyId, @RequestParam String plate) {
        access.checkProperty(propertyId, STAFF);
        return plateLookupService.lookupPlate(propertyId, plate);
    }

    // Lookup by spot (towers)
    @GetMapping("/spot")
    public SpotLookupResponse lookupSpot(@PathVariable UUID propertyId, @RequestParam String label) {
        access.checkProperty(propertyId, STAFF);
        return plateLookupService.lookupSpot(propertyId, label);
    }
}