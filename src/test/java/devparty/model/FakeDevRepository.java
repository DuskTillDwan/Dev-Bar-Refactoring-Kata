package devparty.model;

import devparty.IDevRepository;

import java.util.List;

public class FakeDevRepository implements IDevRepository {
    private final List<Dev> devData;

    public FakeDevRepository(List<Dev> devData) {
        this.devData = devData;
    }

    @Override
    public List<Dev> get() {
        return devData;
    }
}
