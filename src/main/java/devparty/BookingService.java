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

        for (var boatData : boats) {
            if (boatData.hasEnoughCapacity(maxNumberOfDevs)) {
                printAndSaveReservedBoat(boatData, bestDate);
            }
        }

        if (boatList.findFirstAvailableBoat(maxNumberOfDevs)) return true;

        for (var barData : bars) {
            if (Bars.barIsOpenAndHasCapacity(barData, maxNumberOfDevs, bestDate)) {
                printAndSaveReservedBar(barData, bestDate);
            }
        }

        return Bars.findFirstAvailableBar(bars, maxNumberOfDevs, bestDate);
    }

    private static LocalDate getBestDate(Optional<Map.Entry<LocalDate, Integer>> found) {
        return found
                .map(Map.Entry::getKey)
                .orElse(null);
    }

    private static Optional<Map.Entry<LocalDate, Integer>> findBestDateByAvailableDevs(Map<LocalDate, Integer> numberOfAvailableDevsByDate, int maxNumberOfDevs) {
        Optional<Map.Entry<LocalDate, Integer>> found = Optional.empty();
        for (Map.Entry<LocalDate, Integer> entry : numberOfAvailableDevsByDate.entrySet()) {
            if (entry.getValue() == maxNumberOfDevs) {
                found = Optional.of(entry);
                break;
            }
        }
        return found;
    }

    private void printAndSaveReservedBoat(Boat boat, LocalDate bestDate) {
        String name = boat.name();
        System.out.println("Bar booked: " + name + " at " + bestDate);
        Bar bar = new Bar(boat.name(), boat.maxPeople(), allDays());
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
