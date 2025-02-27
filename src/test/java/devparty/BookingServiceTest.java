package devparty;

import devparty.api.BookingController;
import devparty.model.*;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.*;

class BookingServiceTest {


    @Test
    public void reserveBarWhenAtLeast60PercentOfDevsAreAvailable() {
        String indoorBarName = "Bar La Belle Equipe";
        List<Bar> indoorBars = List.of(
                barWith(indoorBarName, asList(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))
        );
        List<Dev> developers = asList(
                new Dev("Alice", asList(WEDNESDAY, THURSDAY, FRIDAY)),
                new Dev("Bob", List.of(THURSDAY)),
                new Dev("Chad", List.of(FRIDAY)),
                new Dev("Dan", Arrays.asList(WEDNESDAY, THURSDAY)),
                new Dev("Eve", List.of(THURSDAY))
        );

        BookingController controller = buildController(indoorBars, developers);
        var bookSuccess = controller.makeBooking();
        assertTrue(bookSuccess);
        BookingData result = controller.get().stream().findFirst().orElse(null);

        assertNotNull(result);
        assertEquals(THURSDAY, result.date());
        assertEquals(indoorBarName, result.bar().name());
    }

    @Test
    public void doNotReserveBarWhenOnly50PercentOfDevsAreAvailable() {
        String indoorBarName = "Bar La Belle Equipe";
        List<Bar> indoorBars = List.of(
                barWith(indoorBarName, asList(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))
        );

        List<Dev> developers = asList(
                new Dev("Alice", asList(WEDNESDAY, FRIDAY)),
                new Dev("Bob", List.of(THURSDAY)),
                new Dev("Chad", List.of(FRIDAY)),
                new Dev("Dan", List.of(WEDNESDAY)),
                new Dev("Eve", List.of(THURSDAY))
        );

        BookingController controller = buildController(indoorBars, developers);
        var bookSuccess = controller.makeBooking();
        assertFalse(bookSuccess);
    }

    @Test
    public void reserveBarOnlyWhenBarIsOpen() {
        String indoorBarName = "Bar La Belle Equipe";
        List<Bar> indoorBars = asList(
                barWith("another name", List.of(DayOfWeek.FRIDAY)),
                barWith(indoorBarName, List.of(DayOfWeek.THURSDAY))
        );
        List<Dev> developers = asList(
                new Dev("Bob", List.of(THURSDAY)),
                new Dev("Eve", List.of(THURSDAY))
        );

        BookingController controller = buildController(indoorBars, developers);
        var bookSuccess = controller.makeBooking();
        assertTrue(bookSuccess);
        BookingData result = controller.get().stream().findFirst().orElse(null);

        assertNotNull(result);
        assertEquals(THURSDAY, result.date());
        assertEquals(indoorBarName, result.bar().name());
    }

    @Test
    public void doNotReserveWhenBarsAreClosed() {
        String indoorBarName = "Bar La Belle Equipe";
        List<Bar> indoorBars = asList(
                barWith("another name", List.of(DayOfWeek.FRIDAY)),
                barWith(indoorBarName, List.of(DayOfWeek.THURSDAY))
        );
        List<Dev> developers = asList(
                new Dev("Bob", List.of(WEDNESDAY)),
                new Dev("Eve", List.of(WEDNESDAY))
        );

        BookingController controller = buildController(indoorBars, developers);
        var bookSuccess = controller.makeBooking();

        assertFalse(bookSuccess);

    }

    @Test
    void chooseABarWithEnoughSpace() {
        String indoorBarName = "Bar La Belle Equipe";
        List<Bar> indoorBars = List.of(
                barWith(indoorBarName, 2, List.of(DayOfWeek.THURSDAY))
        );
        List<Dev> developers = asList(
                new Dev("Bob", List.of(THURSDAY)),
                new Dev("Eve", List.of(THURSDAY)),
                new Dev("Fred", List.of(THURSDAY)),
                new Dev("Marie", List.of(THURSDAY))

        );

        BookingController controller = buildController(indoorBars, developers);
        var bookSuccess = controller.makeBooking();

        assertFalse(bookSuccess);

    }

    @Test
    void preferBoats() {
        String indoorBarName = "Bar La Belle Equipe";
        List<Bar> indoorBars = List.of(
                barWith(indoorBarName, 3, List.of(DayOfWeek.THURSDAY))
        );
        List<Boat> boats = List.of(
                new Boat(indoorBarName, 3)
        );
        List<Dev> developers = asList(
                new Dev("Bob", List.of(THURSDAY)),
                new Dev("Eve", List.of(THURSDAY))
        );

        BookingController controller = buildController(indoorBars, developers, boats);
        var bookSuccess = controller.makeBooking();

        assertTrue(bookSuccess);

        BookingData result = controller.get().stream().findFirst().orElse(null);

        assertNotNull(result);
        assertEquals(THURSDAY, result.date());
        assertEquals(indoorBarName, result.bar().name());

    }

    private static BookingController buildController(List<Bar> barData,
                                                     List<Dev> devData) {
        List<Boat> boats = Collections.emptyList();
        return buildController(barData, devData, boats);
    }

    private static BookingController buildController(List<Bar> barData, List<Dev> devData, List<Boat> boats) {
        var boatRepo = new FakeBoatRepository(boats);
        var bookingRepository = new FakeBookingRepository();
        return new BookingController(new BookingService(
                new FakeBarRepository(barData),
                new FakeDevRepository(devData),
                boatRepo,
                bookingRepository),
                bookingRepository);
    }


    private Bar barWith(String indoorBarName, List<DayOfWeek> list) {
        return new Bar(indoorBarName, 10, list);
    }

    private Bar barWith(String indoorBarName, int capacity, List<DayOfWeek> list) {
        return new Bar(indoorBarName, capacity, list);
    }

    private static final LocalDate WEDNESDAY = LocalDate.of(2022, 5, 11);
    private static final LocalDate THURSDAY = WEDNESDAY.plusDays(1);
    private static final LocalDate FRIDAY = WEDNESDAY.plusDays(2);

}