package devparty.model;

import java.time.LocalDate;
import java.util.List;

public record Bars(List<Bars> barsList) {
    public static boolean findFirstAvailableBar(List<Bar> bars, int maxNumberOfDevs, LocalDate bestDate) {
        for (Bar bar : bars) {
            if (barIsOpenAndHasCapacity(bar, maxNumberOfDevs, bestDate)) {
                return true;
            }
        }
        return false;
    }

    public static boolean barIsOpenAndHasCapacity(Bar bar, int maxNumberOfDevs, LocalDate bestDate) {
        return bar.getCapacity() >= maxNumberOfDevs && bar.getOpen().contains(bestDate.getDayOfWeek());
    }
}
