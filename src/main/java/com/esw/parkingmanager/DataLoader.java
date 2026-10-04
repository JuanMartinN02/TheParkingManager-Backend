package com.esw.parkingmanager;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {

    private final PropertyRepository propertyRepository;

    public DataLoader(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    public void run(String... args) {
        if (propertyRepository.count() == 0) {
            Property property = new Property();
            property.setName("Residencias El Bosque");
            property.setParkingMode(ParkingMode.NUMBERED);
            property.setVisitorSpotCapacity(10);

            propertyRepository.save(property);
            System.out.println(">>> Propiedad guardada con id: " + property.getId());
        }

        System.out.println(">>> Propiedades en la base: " + propertyRepository.count());
        propertyRepository.findAll().forEach(p ->
                System.out.println(">>> " + p.getId() + " | " + p.getName() + " | " + p.getParkingMode()));
    }
}
