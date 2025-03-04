package devparty;

import devparty.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;

public class BookingService {


    private final IBarRepository barRepo;
    private final IDevRepository devRepo;
    private final IBoatRepository boatRepo;
    private final IBookingRepository bookingRepo;

    public BookingService(IBarRepository barRepo, IDevRepository devRepo, IBoatRepository boatRepo, IBookingRepository bookingRepo) {
        this.barRepo = barRepo;
        this.devRepo = devRepo;
        this.boatRepo = boatRepo;
        this.bookingRepo = bookingRepo;
    }

    public boolean reserveBar() {
        var bars = barRepo.get();
        var devs = new ArrayList<>(devRepo.get());
        var boats = boatRepo.get();

        Map<LocalDate, Integer> numberOfAvailableDevsByDate = new HashMap<>();
        for (var devData : devs) {
            for (var date : devData.getOnSite()) {
                if (numberOfAvailableDevsByDate.containsKey(date)) {
                    numberOfAvailableDevsByDate.put(date, numberOfAvailableDevsByDate.get(date) + 1);
                } else {
                    numberOfAvailableDevsByDate.put(date, 1);
                }
            }
        }

        int maxNumberOfDevs = Collections.max(numberOfAvailableDevsByDate.values());

        if (maxNumberOfDevs <= devs.size() * 0.6) {
            return false;
        }

        Optional<Map.Entry<LocalDate, Integer>> found = findBestDateByAvailableDevs(numberOfAvailableDevsByDate, maxNumberOfDevs);

        LocalDate bestDate = getBestDate(found);

        Boats boatList = new Boats(boats);
        Optional<Boat> firstAvailableBoat = boatList.findFirstAvailableBoat(maxNumberOfDevs);
        firstAvailableBoat
                .ifPresent(boat -> printAndSaveReservedBoat(boat, bestDate));

        if (firstAvailableBoat.isPresent()) return true;

        Bars barList = new Bars(bars);
        Optional<Bar> firstAvailableBar = barList.findFirstAvailableBar(maxNumberOfDevs, bestDate);
        firstAvailableBar
                .ifPresent(bar -> printAndSaveReservedBar(bar, bestDate));

        return firstAvailableBar.isPresent();
    }

    private static LocalDate getBestDate(Optional<Map.Entry<LocalDate, Integer>> found) {
        return found
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private static Optional<Map.Entry<LocalDate, Integer>> findBestDateByAvailableDevs(Map<LocalDate, Integer> numberOfAvailableDevsByDate, int maxNumberOfDevs) {
        return numberOfAvailableDevsByDate.entrySet().stream().filter(entry -> entry.getValue() == maxNumberOfDevs).findFirst();
    }

    private void printAndSaveReservedBoat(Boat boat, LocalDate bestDate) {
        String name = boat.name();
        System.out.println("Bar booked: " + name + " at " + bestDate);
        Bar bar = new Bar(name, boat.maxPeople(), allDays());
        bookingRepo.save(new BookingData(
                bar, bestDate
        ));
    }

    private void printAndSaveReservedBar(Bar bar, LocalDate bestDate) {
        String name = bar.getName();
        System.out.println("Bar booked: " + name + " at " + bestDate);
        bookingRepo.save(new BookingData(bar, bestDate));
    }

    private static List<DayOfWeek> allDays() {
        return Arrays.asList(DayOfWeek.values());
    }


}
