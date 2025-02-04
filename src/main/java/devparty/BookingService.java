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

        Optional<Map.Entry<LocalDate, Integer>> found = Optional.empty();
        for (Map.Entry<LocalDate, Integer> entry : numberOfAvailableDevsByDate.entrySet()) {
            if (entry.getValue() == maxNumberOfDevs) {
                found = Optional.of(entry);
                break;
            }
        }
        LocalDate bestDate = found.map(Map.Entry::getKey).orElse(null);

        for (var boatData : boats) {
            if (applesauce(boatData, maxNumberOfDevs, bestDate)) return true;
        }

        if (findAvailableBarAndMakeReservation(bars, maxNumberOfDevs, bestDate)) return true;

        return false;
    }

    private boolean applesauce(BoatData boatData, int maxNumberOfDevs, LocalDate bestDate) {
        if (boatData.hasEnoughCapacity(maxNumberOfDevs)) {
            printReservation(boatData.getName(), bestDate);
            BarData barData = new BarData(boatData.getName(), boatData.getMaxPeople(), allDays());
            bookingRepo.save(new BookingData(barData, bestDate));
            return true;
        }
        return false;
    }

    private boolean findAvailableBarAndMakeReservation(List<BarData> bars, int maxNumberOfDevs, LocalDate bestDate) {
        return bars.stream()
                .anyMatch(barData -> makeReservationIfBarIsAvailableAndSaveToDatabase(maxNumberOfDevs, bestDate, barData));
    }

    private boolean makeReservationIfBarIsAvailableAndSaveToDatabase(int maxNumberOfDevs, LocalDate bestDate, BarData barData) {
        if (barIsAvailable(maxNumberOfDevs, bestDate, barData)) {
            printReservation(barData.getName(), bestDate);
            bookingRepo.save(new BookingData(barData, bestDate));
            return true;
        }
        return false;
    }

    private static boolean barIsAvailable(int maxNumberOfDevs, LocalDate bestDate, BarData barData) {
        return barData.getCapacity() >= maxNumberOfDevs && barData.getOpen().contains(bestDate.getDayOfWeek());
    }

    private static List<DayOfWeek> allDays() {
        return Arrays.asList(DayOfWeek.values());
    }

    private void printReservation(String name, LocalDate dateTime) {
        System.out.println("Bar booked: " + name + " at " + dateTime);
    }


}
