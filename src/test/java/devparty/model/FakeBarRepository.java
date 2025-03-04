package devparty.model;

import devparty.IBarRepository;

import java.util.List;

public class FakeBarRepository implements IBarRepository {
    private final List<Bar> barData;

    public FakeBarRepository(List<Bar> barData) {
        this.barData = barData;
    }

    @Override
    public List<Bar> get() {
        return barData;
    }
}
