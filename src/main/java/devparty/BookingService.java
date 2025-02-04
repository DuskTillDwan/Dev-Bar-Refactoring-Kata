package devparty;

import devparty.model.BarData;
import devparty.model.BoatData;
import devparty.model.BookingData;
import devparty.model.DevData;

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

        Result result = getResult(devs);
        if (result == null) return false;

        if (findLargeEnoughBoatAndMakeReservation(boats, result.maxNumberOfDevs, result.bestDate)) return true;

        if (findAvailableBarAndMakeReservation(bars, result.maxNumberOfDevs, result.bestDate)) return true;

        return false;
    }

    private static Result getResult(ArrayList<DevData> devs) {
        Map<LocalDate, Integer> numberOfAvailableDevsByDate = buildMapOfWorkingDaysToNumberOfDevsIThink(devs);
        int maxNumberOfDevs = Collections.max(numberOfAvailableDevsByDate.values());
        if (maxNumberOfDevs <= devs.size() * 0.6) {
            return null;
        }
        Optional<Map.Entry<LocalDate, Integer>> found = applesauce(numberOfAvailableDevsByDate, maxNumberOfDevs);
        LocalDate bestDate = found.map(Map.Entry::getKey).orElse(null);
        Result result = new Result(maxNumberOfDevs, bestDate);
        return result;
    }

    private static Map<LocalDate, Integer> buildMapOfWorkingDaysToNumberOfDevsIThink(ArrayList<DevData> devs) {
        Map<LocalDate, Integer> numberOfWorkingDevsByDateIThink = new HashMap<>();
        for (var devData : devs) {
            for (var workingDays : devData.getWorkingDaysIThink()) {
                if (numberOfWorkingDevsByDateIThink.containsKey(workingDays)) {
                    numberOfWorkingDevsByDateIThink.put(workingDays, numberOfWorkingDevsByDateIThink.get(workingDays) + 1);
                } else {
                    numberOfWorkingDevsByDateIThink.put(workingDays, 1);
                }
            }
        }
        return numberOfWorkingDevsByDateIThink;
    }

    private static class Result {
        public final int maxNumberOfDevs;
        public final LocalDate bestDate;

        public Result(int maxNumberOfDevs, LocalDate bestDate) {
            this.maxNumberOfDevs = maxNumberOfDevs;
            this.bestDate = bestDate;
        }
    }

    private static Optional<Map.Entry<LocalDate, Integer>> applesauce(Map<LocalDate, Integer> numberOfAvailableDevsByDate, int maxNumberOfDevs) {
        return numberOfAvailableDevsByDate.entrySet().stream()
                .filter(entry -> entry.getValue() == maxNumberOfDevs)
                .findFirst();
    }

    private boolean findLargeEnoughBoatAndMakeReservation(List<BoatData> boats, int maxNumberOfDevs, LocalDate bestDate) {
        for (var boatData : boats) {
            if (makeReservationIfBoatHasCapacityAndSaveToDatabase(boatData, maxNumberOfDevs, bestDate)) return true;
        }
        return false;
    }

    private boolean makeReservationIfBoatHasCapacityAndSaveToDatabase(BoatData boatData, int maxNumberOfDevs, LocalDate bestDate) {
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
