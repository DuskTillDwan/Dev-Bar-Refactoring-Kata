package devparty.model;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public record Bars(List<Bar> barsList) {
    public Optional<Bar> findFirstAvailableBar(int maxNumberOfDevs, LocalDate bestDate) {
        return barsList.stream().filter(bar -> bar.barIsOpenAndHasCapacity(maxNumberOfDevs, bestDate)).findFirst();
    }

}
