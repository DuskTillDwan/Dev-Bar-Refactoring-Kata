package devparty;

import devparty.api.BookingController;
import devparty.model.*;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
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
        List<DevData> developers = asList(
                new DevData("Alice", asList(WEDNESDAY, THURSDAY, FRIDAY)),
                new DevData("Bob", List.of(THURSDAY)),
                new DevData("Chad", List.of(FRIDAY)),
                new DevData("Dan", asList(WEDNESDAY, THURSDAY)),
                new DevData("Eve", List.of(THURSDAY))
        );

        BookingController controller = buildController(indoorBars, developers);
        boolean bookSuccess = controller.makeBooking();
        assertTrue(bookSuccess);
        BookingData result = controller.get().stream().findFirst().orElse(null);

        assertNotNull(result);
        assertEquals(THURSDAY, result.date());
        assertEquals(indoorBarName, result.bar().getName());
    }

    @Test
    public void doNotReserveBarWhenOnly50PercentOfDevsAreAvailable() {
        String indoorBarName = "Bar La Belle Equipe";
        List<Bar> indoorBars = List.of(
                barWith(indoorBarName, asList(DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY))
        );

        List<DevData> developers = asList(
                new DevData("Alice", asList(WEDNESDAY, FRIDAY)),
                new DevData("Bob", List.of(THURSDAY)),
                new DevData("Chad", List.of(FRIDAY)),
                new DevData("Dan", List.of(WEDNESDAY)),
                new DevData("Eve", List.of(THURSDAY))
        );

        BookingController controller = buildController(indoorBars, developers);
        boolean bookSuccess = controller.makeBooking();
        assertFalse(bookSuccess);
    }

    @Test
    public void reserveBarOnlyWhenBarIsOpen() {
        String indoorBarName = "Bar La Belle Equipe";
        List<Bar> indoorBars = asList(
                barWith("another name", List.of(DayOfWeek.FRIDAY)),
                barWith(indoorBarName, List.of(DayOfWeek.THURSDAY))
        );
        List<DevData> developers = asList(
                new DevData("Bob", List.of(THURSDAY)),
                new DevData("Eve", List.of(THURSDAY))
        );

        BookingController controller = buildController(indoorBars, developers);
        boolean bookSuccess = controller.makeBooking();
        assertTrue(bookSuccess);
        BookingData result = controller.get().stream().findFirst().orElse(null);

        assertNotNull(result);
        assertEquals(THURSDAY, result.date());
        assertEquals(indoorBarName, result.bar().getName());
    }

    @Test
    public void doNotReserveWhenBarsAreClosed() {
        String indoorBarName = "Bar La Belle Equipe";
        List<Bar> indoorBars = asList(
                barWith("another name", List.of(DayOfWeek.FRIDAY)),
                barWith(indoorBarName, List.of(DayOfWeek.THURSDAY))
        );
        List<DevData> developers = asList(
                new DevData("Bob", List.of(WEDNESDAY)),
                new DevData("Eve", List.of(WEDNESDAY))
        );

        BookingController controller = buildController(indoorBars, developers);
        boolean bookSuccess = controller.makeBooking();

        assertFalse(bookSuccess);

    }

    @Test
    void chooseABarWithEnoughSpace() {
        String indoorBarName = "Bar La Belle Equipe";
        List<Bar> indoorBars = List.of(
                barWith(indoorBarName, 2, List.of(DayOfWeek.THURSDAY))
        );
        List<DevData> developers = asList(
                new DevData("Bob", List.of(THURSDAY)),
                new DevData("Eve", List.of(THURSDAY)),
                new DevData("Fred", List.of(THURSDAY)),
                new DevData("Marie", List.of(THURSDAY))

        );

        BookingController controller = buildController(indoorBars, developers);
        boolean bookSuccess = controller.makeBooking();

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
        List<DevData> developers = asList(
                new DevData("Bob", List.of(THURSDAY)),
                new DevData("Eve", List.of(THURSDAY))
        );

        BookingController controller = buildController(indoorBars, developers, boats);
        boolean bookSuccess = controller.makeBooking();

        assertTrue(bookSuccess);

        BookingData result = controller.get().stream().findFirst().orElse(null);

        assertNotNull(result);
        assertEquals(THURSDAY, result.date());
        assertEquals(indoorBarName, result.bar().getName());

    }

    private static BookingController buildController(List<Bar> barData,
                                                     List<DevData> devData) {
        List<Boat> boats = Collections.emptyList();
        return buildController(barData, devData, boats);
    }

    private static BookingController buildController(List<Bar> barData, List<DevData> devData, List<Boat> boats) {
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