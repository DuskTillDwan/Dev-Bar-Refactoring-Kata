package devparty.model;

import java.util.List;
import java.util.Optional;

public record Boats(List<Boat> boatsList) {
    public Optional<Boat> findFirstAvailableBoat(int maxNumberOfDevs) {
        return boatsList.stream().filter(boatData -> boatData.hasEnoughCapacity(maxNumberOfDevs)).findFirst();
    }

}
