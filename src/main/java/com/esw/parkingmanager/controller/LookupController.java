package com.esw.parkingmanager.controller;

import com.esw.parkingmanager.dto.PlateLookupResponse;
import com.esw.parkingmanager.dto.SpotLookupResponse;
import com.esw.parkingmanager.service.PlateLookupService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/properties/{propertyId}/lookup")
public class LookupController {

    private final PlateLookupService plateLookupService;

    public LookupController(PlateLookupService plateLookupService) {
        this.plateLookupService = plateLookupService;
    }

    // Lookup for plates (tower)
    // Plate goes in the query string so spaces and dashes don't break the URL path
    @GetMapping("/plate")
    public PlateLookupResponse lookupPlate(@PathVariable UUID propertyId, @RequestParam String plate) {
        return plateLookupService.lookupPlate(propertyId, plate);
    }

    // Lookup for spots (tower)
    @GetMapping("/spot")
    public SpotLookupResponse lookupSpot(@PathVariable UUID propertyId, @RequestParam String label) {
        return plateLookupService.lookupSpot(propertyId, label);
    }
}