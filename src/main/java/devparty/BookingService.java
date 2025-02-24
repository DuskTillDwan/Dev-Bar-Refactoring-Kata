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
        var barDataList = barRepo.get();
        var devs = new ArrayList<>(devRepo.get());
        var boatDataList = boatRepo.get();

        Map<LocalDate, Integer> numberOfAvailableDevsByDate = getNumberOfAvailableDevsByDate(devs);

        int maxNumberOfDevs = Collections.max(numberOfAvailableDevsByDate.values());

        if (maxNumberOfDevs <= devs.size() * 0.6) {
            return false;
        }

        Optional<Map.Entry<LocalDate, Integer>> found = numberOfAvailableDevsByDate.entrySet().stream().filter(entry -> entry.getValue() == maxNumberOfDevs).findFirst();

        LocalDate bestDate = found
                .map(Map.Entry::getKey)
                .orElse(null);
        var boats = new Boats(boatDataList);
        Optional<Boat> firstAvailableBoat = boats.findFirstAvailableBoat(maxNumberOfDevs);

        firstAvailableBoat.ifPresent(boatData -> printAndSaveBoatBooking(boatData, bestDate));

        if (firstAvailableBoat.isPresent()) return true;

        var bars = new Bars(barDataList);
        Optional<Bar> firstAvailableBar = bars.findFirstAvailableBar(maxNumberOfDevs, bestDate);

        firstAvailableBar
                .ifPresent(bar -> printAndSaveBooking(bar, bestDate));

        return firstAvailableBar.isPresent();
    }

    private Map<LocalDate, Integer> getNumberOfAvailableDevsByDate(ArrayList<DevData> devs) {
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


    private void printAndSaveBoatBooking(Boat boat, LocalDate bestDate) {
        String name = boat.name();
        System.out.println("Bar booked: " + name + " at " + bestDate);
        Bar bar = new Bar(name, boat.maxPeople(), allDays());
        bookingRepo.save(new BookingData(
                bar, bestDate
        ));
    }

    private void printAndSaveBooking(Bar bar, LocalDate bestDate) {
        System.out.println("Bar booked: " + bar.name() + " at " + bestDate);
        bookingRepo.save(new BookingData(bar, bestDate));
    }

    private static List<DayOfWeek> allDays() {
        return Arrays.asList(DayOfWeek.values());
    }


}
