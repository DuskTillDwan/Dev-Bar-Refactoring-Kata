package devparty;

import devparty.model.Boat;

import java.util.List;

public interface IBoatRepository {
    List<Boat> get();
}
