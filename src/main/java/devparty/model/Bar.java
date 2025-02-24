package devparty.model;

import java.time.DayOfWeek;
import java.util.List;

public record Bar(String name, int capacity, List<DayOfWeek> open) {
}
