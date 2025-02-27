package devparty.model;

import java.time.LocalDate;
import java.util.*;

public class DevAvailabilityCalendar {
    private static final double BOOKING_THRESHOLD = 0.6;

    private final Map<LocalDate, Integer> numberOfAvailableDevsByDate = new HashMap<>();

    public DevAvailabilityCalendar(ArrayList<Dev> devList) {
        availableDevsByDate(devList);
    }

    public void availableDevsByDate(ArrayList<Dev> devs) {
        for (var devData : devs) {
            for (var date : devData.onSite()) {
                if (numberOfAvailableDevsByDate.containsKey(date)) {
                    numberOfAvailableDevsByDate.put(date, numberOfAvailableDevsByDate.get(date) + 1);
                    continue;
                }
                numberOfAvailableDevsByDate.put(date, 1);
            }
        }
    }

    public int getMaxNumberOfDevsByDate() {
        return Collections.max(numberOfAvailableDevsByDate.values());
    }

    public LocalDate findBestDateIfExists(int maxNumberOfDevs) {
        Optional<Map.Entry<LocalDate, Integer>> found = Optional.empty();
        for (Map.Entry<LocalDate, Integer> entry : numberOfAvailableDevsByDate.entrySet()) {
            if (entry.getValue() == maxNumberOfDevs) {
                found = Optional.of(entry);
                break;
            }
        }

        return found
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    public boolean notEnoughAvailableDevs(ArrayList<Dev> devs) {
        return getMaxNumberOfDevsByDate() <= devs.size() * BOOKING_THRESHOLD;
    }
}
