package devparty.model;

public record Boat(String name, int maxPeople) {

    public boolean hasEnoughCapacity(int maxNumberOfDevs) {
        return maxPeople() >= maxNumberOfDevs;
    }
}
