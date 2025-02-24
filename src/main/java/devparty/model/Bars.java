package devparty.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record Bars(List<Bar> elements) {
    public Optional<Bar> findFirstAvailableBar(int maxNumberOfDevs, LocalDate bestDate) {
        return elements.stream().filter(bar -> bar.capacity() >= maxNumberOfDevs && bar.open().contains(bestDate.getDayOfWeek())).findFirst();
    }
}
