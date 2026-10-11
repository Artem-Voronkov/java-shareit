package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookingServiceImplIntegrationTest {

    @Autowired
    private BookingService bookingService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User owner;
    private User booker;
    private Item item;
    private Booking currentBooking;
    private Booking pastBooking;
    private Booking futureWaitingBooking;
    private Booking rejectedBooking;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User(null, "Owner", "owner@test.com"));
        booker = userRepository.save(new User(null, "Booker", "booker@test.com"));
        item = itemRepository.save(new Item(null, "Дрель", "Описание", true, owner, null));
    }

    @BeforeEach
    void setUpBookings() {
        currentBooking = bookingRepository.save(new Booking(null,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1),
                item, booker, BookingStatus.APPROVED));
        pastBooking = bookingRepository.save(new Booking(null,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(3),
                item, booker, BookingStatus.APPROVED));
        futureWaitingBooking = bookingRepository.save(new Booking(null,
                LocalDateTime.now().plusDays(3), LocalDateTime.now().plusDays(5),
                item, booker, BookingStatus.WAITING));
        rejectedBooking = bookingRepository.save(new Booking(null,
                LocalDateTime.now().plusDays(10), LocalDateTime.now().plusDays(12),
                item, booker, BookingStatus.REJECTED));
    }

    @Test
    void create() {
        BookItemRequestDto dto = new BookItemRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        BookingDto created = bookingService.create(booker.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getStatus()).isEqualTo(BookingStatus.WAITING);
    }

    @Test
    void createItemNotAvailable() {
        item.setAvailable(false);
        itemRepository.save(item);
        BookItemRequestDto dto = new BookItemRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void createOwnerBooksOwnItem() {
        BookItemRequestDto dto = new BookItemRequestDto(item.getId(),
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));

        assertThatThrownBy(() -> bookingService.create(owner.getId(), dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createEndBeforeStart() {
        BookItemRequestDto dto = new BookItemRequestDto(item.getId(),
                LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(1));

        assertThatThrownBy(() -> bookingService.create(booker.getId(), dto))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approve() {
        Booking booking = bookingRepository.save(new Booking(null,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.WAITING));

        BookingDto approved = bookingService.approve(owner.getId(), booking.getId(), true);

        assertThat(approved.getStatus()).isEqualTo(BookingStatus.APPROVED);
    }

    @Test
    void approveReject() {
        Booking booking = bookingRepository.save(new Booking(null,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.WAITING));

        BookingDto rejected = bookingService.approve(owner.getId(), booking.getId(), false);

        assertThat(rejected.getStatus()).isEqualTo(BookingStatus.REJECTED);
    }

    @Test
    void approveNotOwner() {
        Booking booking = bookingRepository.save(new Booking(null,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.WAITING));

        assertThatThrownBy(() -> bookingService.approve(booker.getId(), booking.getId(), true))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void approveAlreadyDecided() {
        Booking booking = bookingRepository.save(new Booking(null,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.APPROVED));

        assertThatThrownBy(() -> bookingService.approve(owner.getId(), booking.getId(), false))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void getById() {
        Booking booking = bookingRepository.save(new Booking(null,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.WAITING));

        BookingDto found = bookingService.getById(booker.getId(), booking.getId());

        assertThat(found.getId()).isEqualTo(booking.getId());
    }

    @Test
    void getByIdNotFound() {
        Booking booking = bookingRepository.save(new Booking(null,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.WAITING));
        User stranger = userRepository.save(new User(null, "Stranger", "stranger@test.com"));

        assertThatThrownBy(() -> bookingService.getById(stranger.getId(), booking.getId()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByBooker() {
        int before = bookingService.getAllByBooker(booker.getId(), BookingState.ALL).size();

        Booking added = bookingRepository.save(new Booking(null,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.WAITING));

        List<BookingDto> bookings = bookingService.getAllByBooker(booker.getId(), BookingState.ALL);

        assertThat(bookings).hasSize(before + 1);
        assertThat(bookings).extracting("id").contains(added.getId());
    }

    @Test
    void getAllByOwner() {
        int before = bookingService.getAllByOwner(owner.getId(), BookingState.ALL).size();

        Booking added = bookingRepository.save(new Booking(null,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                item, booker, BookingStatus.WAITING));

        List<BookingDto> bookings = bookingService.getAllByOwner(owner.getId(), BookingState.ALL);

        assertThat(bookings).hasSize(before + 1);
        assertThat(bookings).extracting("id").contains(added.getId());
    }

    @Test
    void getAllByBookerCurrent() {
        assertThat(bookingService.getAllByBooker(booker.getId(), BookingState.CURRENT))
                .extracting("id").containsExactly(currentBooking.getId());
    }

    @Test
    void getAllByBookerPast() {
        assertThat(bookingService.getAllByBooker(booker.getId(), BookingState.PAST))
                .extracting("id").containsExactly(pastBooking.getId());
    }

    @Test
    void getAllByBookerFuture() {
        assertThat(bookingService.getAllByBooker(booker.getId(), BookingState.FUTURE))
                .hasSize(2);
    }

    @Test
    void getAllByBookerWaiting() {
        assertThat(bookingService.getAllByBooker(booker.getId(), BookingState.WAITING))
                .extracting("id").containsExactly(futureWaitingBooking.getId());
    }

    @Test
    void getAllByBookerRejected() {
        assertThat(bookingService.getAllByBooker(booker.getId(), BookingState.REJECTED))
                .extracting("id").containsExactly(rejectedBooking.getId());
    }

    @Test
    void getAllByOwnerCurrent() {
        assertThat(bookingService.getAllByOwner(owner.getId(), BookingState.CURRENT))
                .extracting("id").containsExactly(currentBooking.getId());
    }

    @Test
    void getAllByOwnerPast() {
        assertThat(bookingService.getAllByOwner(owner.getId(), BookingState.PAST))
                .extracting("id").containsExactly(pastBooking.getId());
    }

    @Test
    void getAllByOwnerFuture() {
        assertThat(bookingService.getAllByOwner(owner.getId(), BookingState.FUTURE))
                .hasSize(2);
    }

    @Test
    void getAllByOwnerWaiting() {
        assertThat(bookingService.getAllByOwner(owner.getId(), BookingState.WAITING))
                .extracting("id").containsExactly(futureWaitingBooking.getId());
    }

    @Test
    void getAllByOwnerRejected() {
        assertThat(bookingService.getAllByOwner(owner.getId(), BookingState.REJECTED))
                .extracting("id").containsExactly(rejectedBooking.getId());
    }
}