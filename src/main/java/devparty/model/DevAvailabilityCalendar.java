package devparty.model;

import java.time.LocalDate;
import java.util.*;

public class DevAvailabilityCalendar {

    public DevAvailabilityCalendar(ArrayList<DevData> devDataList) {
        getNumberOfAvailableDevsByDate(devDataList);
    }

    public static Map<LocalDate, Integer> getNumberOfAvailableDevsByDate(ArrayList<DevData> devs) {
        Map<LocalDate, Integer> numberOfAvailableDevsByDate = new HashMap<>();
        for (var devData : devs) {
            for (var date : devData.onSite()) {
                if (numberOfAvailableDevsByDate.containsKey(date)) {
                    numberOfAvailableDevsByDate.put(date, numberOfAvailableDevsByDate.get(date) + 1);
                    continue;
                }
                numberOfAvailableDevsByDate.put(date, 1);
            }
        }
        return numberOfAvailableDevsByDate;
    }

    public static int getMaxNumberOfDevsByDate(Map<LocalDate, Integer> numberOfAvailableDevsByDate) {
        return Collections.max(numberOfAvailableDevsByDate.values());
    }

    public static LocalDate findBestDateIfExists(Map<LocalDate, Integer> numberOfAvailableDevsByDate, int maxNumberOfDevs) {
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
}
