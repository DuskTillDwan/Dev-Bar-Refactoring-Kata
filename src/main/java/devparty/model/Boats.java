package devparty.model;

import java.util.List;

public record Boats(List<Boat> boatsList) {
    public boolean findFirstAvailableBoat(int maxNumberOfDevs) {
        for (var boatData : boatsList) {
            if (boatData.hasEnoughCapacity(maxNumberOfDevs)) {
                return true;
            }
        }
        return false;
    }

}
