package devparty;

import devparty.model.BarData;
import devparty.model.BoatData;
import devparty.model.BookingData;

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

        for (var boatData : boats) {
            if (boatData.hasEnoughCapacity(maxNumberOfDevs)) {
                printAndSaveReservedBoat(boatData, bestDate);
            }
        }

        if (findFirstAvailableBoat(boats, maxNumberOfDevs)) return true;

        for (var barData : bars) {
            if (barIsOpenAndHasCapacity(barData, maxNumberOfDevs, bestDate)) {
                printAndSaveReservedBar(barData, bestDate);
            }
        }

        return findFirstAvailableBar(bars, maxNumberOfDevs, bestDate);
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

    private static boolean findFirstAvailableBoat(List<BoatData> boats, int maxNumberOfDevs) {
        for (var boatData : boats) {
            if (boatData.hasEnoughCapacity(maxNumberOfDevs)) {
                return true;
            }
        }
        return false;
    }

    private static boolean findFirstAvailableBar(List<BarData> bars, int maxNumberOfDevs, LocalDate bestDate) {
        for (BarData barData : bars) {
            if (barIsOpenAndHasCapacity(barData, maxNumberOfDevs, bestDate)) {
                return true;
            }
        }
        return false;
    }

    private void printAndSaveReservedBoat(BoatData boatData, LocalDate bestDate) {
        String name = boatData.getName();
        System.out.println("Bar booked: " + name + " at " + bestDate);
        BarData barData = new BarData(boatData.getName(), boatData.getMaxPeople(), allDays());
        bookingRepo.save(new BookingData(
                barData, bestDate
        ));
    }

    private void printAndSaveReservedBar(BarData barData, LocalDate bestDate) {
        String name = barData.getName();
        System.out.println("Bar booked: " + name + " at " + bestDate);
        bookingRepo.save(new BookingData(barData, bestDate));
    }

    private static boolean barIsOpenAndHasCapacity(BarData barData, int maxNumberOfDevs, LocalDate bestDate) {
        return barData.getCapacity() >= maxNumberOfDevs && barData.getOpen().contains(bestDate.getDayOfWeek());
    }

    private static List<DayOfWeek> allDays() {
        return Arrays.asList(DayOfWeek.values());
    }


}
