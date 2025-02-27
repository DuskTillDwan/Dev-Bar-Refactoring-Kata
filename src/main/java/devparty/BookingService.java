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


        Map<LocalDate, Integer> numberOfAvailableDevsByDate = DevAvailabilityCalendar.getNumberOfAvailableDevsByDate(devs);

        int maxNumberOfDevs = DevAvailabilityCalendar.getMaxNumberOfDevsByDate(numberOfAvailableDevsByDate);

        if (maxNumberOfDevs <= devs.size() * 0.6) return false;

        LocalDate bestDate = DevAvailabilityCalendar.findBestDateIfExists(numberOfAvailableDevsByDate, maxNumberOfDevs);

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
