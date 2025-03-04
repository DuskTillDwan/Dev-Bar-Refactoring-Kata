package devparty.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public class Bar {
    private final String name;
    private final int capacity;
    private final List<DayOfWeek> open;

    public boolean barIsOpenAndHasCapacity(int maxNumberOfDevs, LocalDate bestDate) {
        return getCapacity() >= maxNumberOfDevs && getOpen().contains(bestDate.getDayOfWeek());
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public List<DayOfWeek> getOpen() {
        return open;
    }

    public Bar(String name, int capacity, List<DayOfWeek> openDays) {
        this.name = name;
        this.capacity = capacity;
        this.open = openDays;
    }
}
