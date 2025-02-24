package devparty.model;

import java.util.List;
import java.util.Optional;

public record Boats(List<Boat> elements) {
    public Optional<Boat> findFirstAvailableBoat(int maxNumberOfDevs) {
        return elements.stream().filter(boatData -> boatData.hasEnoughCapacity(maxNumberOfDevs)).findFirst();
    }
}
