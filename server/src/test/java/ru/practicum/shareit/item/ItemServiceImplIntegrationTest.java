package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    private User owner;
    private User booker;

    @BeforeEach
    void setUp() {
        owner = userRepository.save(new User(null, "Owner", "owner@test.com"));
        booker = userRepository.save(new User(null, "Booker", "booker@test.com"));
    }

    @Test
    void create() {
        ItemDto dto = new ItemDto(null, "Дрель", "Простая дрель", true, null);

        ItemDto created = itemService.create(owner.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(itemRepository.findById(created.getId())).isPresent();
    }

    @Test
    void createOwnerNotFound() {
        ItemDto dto = new ItemDto(null, "Дрель", "Простая дрель", true, null);

        assertThatThrownBy(() -> itemService.create(999L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createWithRequest() {
        ItemRequest request = itemRequestRepository.save(
                new ItemRequest(null, "Нужна дрель", booker, LocalDateTime.now()));
        ItemDto dto = new ItemDto(null, "Дрель", "Простая дрель", true, request.getId());

        ItemDto created = itemService.create(owner.getId(), dto);

        assertThat(created.getRequestId()).isEqualTo(request.getId());
    }

    @Test
    void createRequestNotFound() {
        ItemDto dto = new ItemDto(null, "Дрель", "Простая дрель", true, 999L);

        assertThatThrownBy(() -> itemService.create(owner.getId(), dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update() {
        Item item = itemRepository.save(new Item(null, "Дрель", "Старое описание", true, owner, null));

        ItemDto updated = itemService.update(owner.getId(), item.getId(),
                new ItemDto(null, null, "Новое описание", false, null));

        assertThat(updated.getDescription()).isEqualTo("Новое описание");
        assertThat(updated.getAvailable()).isFalse();
    }

    @Test
    void updateNotOwner() {
        Item item = itemRepository.save(new Item(null, "Дрель", "Описание", true, owner, null));

        assertThatThrownBy(() -> itemService.update(booker.getId(), item.getId(),
                new ItemDto(null, "Новое имя", null, null, null)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAllByOwner() {
        itemRepository.save(new Item(null, "Дрель", "Описание", true, owner, null));
        itemRepository.save(new Item(null, "Отвёртка", "Описание", true, owner, null));

        List<ItemBookingDto> items = itemService.getAllByOwner(owner.getId());

        assertThat(items).hasSize(2);
    }

    @Test
    void search() {
        itemRepository.save(new Item(null, "Дрель", "Мощная дрель", true, owner, null));
        itemRepository.save(new Item(null, "Отвёртка", "Простая отвёртка", true, owner, null));

        List<ItemDto> found = itemService.search("дрель");

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getName()).isEqualTo("Дрель");
    }

    @Test
    void searchBlank() {
        assertThat(itemService.search("")).isEmpty();
    }

    @Test
    void addComment() {
        Item item = itemRepository.save(new Item(null, "Дрель", "Описание", true, owner, null));
        bookingRepository.save(new Booking(null,
                LocalDateTime.now().minusDays(5), LocalDateTime.now().minusDays(1),
                item, booker, BookingStatus.APPROVED));

        CommentDto comment = itemService.addComment(booker.getId(), item.getId(),
                new CreateCommentDto("Отличная вещь!"));

        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getText()).isEqualTo("Отличная вещь!");
        assertThat(comment.getAuthorName()).isEqualTo("Booker");
    }

    @Test
    void addCommentNoBooking() {
        Item item = itemRepository.save(new Item(null, "Дрель", "Описание", true, owner, null));

        assertThatThrownBy(() -> itemService.addComment(booker.getId(), item.getId(),
                new CreateCommentDto("Отличная вещь!")))
                .isInstanceOf(ValidationException.class);
    }
}