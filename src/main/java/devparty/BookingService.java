package devparty;

import devparty.model.Bar;
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
        LocalDate bestDate = found
                .map(Map.Entry::getKey)
                .orElse(null);

        for (var boatData : boats) {
            if (new Bar().hasEnoughCapacity(boatData, maxNumberOfDevs)) {
                String name = boatData.getName();
                System.out.println("Bar booked: " + name + " at " + bestDate);
                BarData barData = new BarData(boatData.getName(), boatData.getMaxPeople(), allDays());
                bookingRepo.save(new BookingData(
                        barData, bestDate
                ));
            }
        }
        if (findFirstAvailableBoat(boats, maxNumberOfDevs)) return true;

        Optional<BarData> firstAvailableBar = findFirstAvailableBar(bars, maxNumberOfDevs, bestDate);

        firstAvailableBar
                .ifPresent(barData -> printAndSaveBooking(barData, bestDate));

        return firstAvailableBar.isPresent();
    }

    private static boolean findFirstAvailableBoat(List<BoatData> boats, int maxNumberOfDevs) {
        for (var boatData : boats) {
            if (new Bar().hasEnoughCapacity(boatData, maxNumberOfDevs)) {
                return true;
            }
        }
        return false;
    }

    private void printAndSaveBooking(BarData barData, LocalDate bestDate) {
        String name = barData.getName();
        System.out.println("Bar booked: " + name + " at " + bestDate);
        bookingRepo.save(new BookingData(barData, bestDate));
    }

    private Optional<BarData> findFirstAvailableBar(List<BarData> bars, int maxNumberOfDevs, LocalDate bestDate) {
        return bars.stream().filter(barData -> barData.getCapacity() >= maxNumberOfDevs && barData.getOpen().contains(bestDate.getDayOfWeek())).findFirst();
    }

    private static List<DayOfWeek> allDays() {
        return Arrays.asList(DayOfWeek.values());
    }


}
