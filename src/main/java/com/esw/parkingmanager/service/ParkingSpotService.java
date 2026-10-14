package com.esw.parkingmanager.service;

import com.esw.parkingmanager.model.ParkingSpot;
import com.esw.parkingmanager.model.Property;
import com.esw.parkingmanager.repository.ParkingSpotRepository;
import com.esw.parkingmanager.repository.PropertyRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
public class ParkingSpotService {

    private static final int MAX_BULK_LABELS = 1000;

    private final ParkingSpotRepository parkingSpotRepository;
    private final PropertyRepository propertyRepository;

    public ParkingSpotService(ParkingSpotRepository parkingSpotRepository, PropertyRepository propertyRepository) {
        this.parkingSpotRepository = parkingSpotRepository;
        this.propertyRepository = propertyRepository;
    }

    @Transactional
    public ParkingSpot create(UUID propertyId, ParkingSpot parkingSpot){
        Property property = findProperty(propertyId);

        String label = SpotLabelNormalizer.normalize(parkingSpot.getLabel());

        if (label.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Label is required");
        }
        if (parkingSpotRepository.existsByPropertyIdAndLabel(propertyId, label)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Spot " + label + " already exists in this property");
        }

        parkingSpot.setLabel(label);
        parkingSpot.setProperty(property);

        return parkingSpotRepository.save(parkingSpot);
    }

    @Transactional
    public List<ParkingSpot> createBulk(UUID propertyId, List<String> rawLabels){
        Property property = findProperty(propertyId);

        // Rule: something to create, but not more than MAX_BULK_LABELS
        if (rawLabels == null || rawLabels.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No labels provided");
        }
        if (rawLabels.size() > MAX_BULK_LABELS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Too many labels: " + rawLabels.size() + " (max " + MAX_BULK_LABELS + ")");
        }

        // Rule: normalize every label; none empty, no duplicates inside the request
        Set<String> labels = new LinkedHashSet<>();
        Set<String> duplicates = new LinkedHashSet<>();
        for (String raw : rawLabels) {
            String label = SpotLabelNormalizer.normalize(raw);
            if (label.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Label '" + raw + "' is empty after normalization");
            }
            if (!labels.add(label)) {
                duplicates.add(label);
            }
        }
        if (!duplicates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate labels in request: " + duplicates);
        }

        // Rule: none may already exist in this property (ONE query)
        List<String> existing = parkingSpotRepository.findExistingLabels(propertyId, labels);
        if (!existing.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Labels already exist in this property: " + existing);
        }

        // If everything is valid, create them all one by one (indv queries)
        List<ParkingSpot> spots = new ArrayList<>();
        for (String label : labels) {
            ParkingSpot spot = new ParkingSpot();
            spot.setLabel(label);
            spot.setProperty(property);
            spots.add(spot);
        }
        return parkingSpotRepository.saveAll(spots);
    }

    public List<ParkingSpot> list(UUID propertyId) {
        return parkingSpotRepository.findByPropertyId(propertyId);
    }

    public ParkingSpot getOne(UUID propertyId, UUID spotId) {
        return parkingSpotRepository.findByIdAndPropertyId(spotId, propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Spot not found"));
    }

    // Step 4 will add a rule here: can't delete a spot with an active authorization
    @Transactional
    public void delete(UUID propertyId, UUID spotId) {
        ParkingSpot spot = getOne(propertyId, spotId);
        parkingSpotRepository.delete(spot);
    }

    private Property findProperty(UUID propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Property not found"));
    }
}
