package devparty.model;

import devparty.IBoatRepository;

import java.util.List;

public class FakeBoatRepository implements IBoatRepository {
    private final List<Boat> boats;

    public FakeBoatRepository(List<Boat> boats) {
        this.boats = boats;
    }

    @Override
    public List<Boat> get() {
        return boats;
    }
}
