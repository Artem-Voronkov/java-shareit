package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService itemRequestService;

    @Autowired
    private ItemRequestRepository itemRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRepository itemRepository;

    private User requestor;
    private User otherUser;

    @BeforeEach
    void setUp() {
        requestor = userRepository.save(new User(null, "Requestor", "requestor@test.com"));
        otherUser = userRepository.save(new User(null, "Other", "other@test.com"));
    }

    @Test
    void create() {
        CreateItemRequestDto dto = new CreateItemRequestDto("Нужна дрель");

        ItemRequestDto created = itemRequestService.create(requestor.getId(), dto);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getDescription()).isEqualTo("Нужна дрель");
        assertThat(created.getItems()).isEmpty();
    }

    @Test
    void createUserNotFound() {
        CreateItemRequestDto dto = new CreateItemRequestDto("Нужна дрель");

        assertThatThrownBy(() -> itemRequestService.create(999L, dto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getOwn() {
        ItemRequest request = itemRequestRepository.save(
                new ItemRequest(null, "Нужна дрель", requestor, LocalDateTime.now()));
        itemRepository.save(new Item(null, "Дрель", "Описание", true, otherUser, request));

        List<ItemRequestDto> own = itemRequestService.getOwn(requestor.getId());

        assertThat(own).hasSize(1);
        assertThat(own.get(0).getItems()).hasSize(1);
    }

    @Test
    void getOwnUserNotFound() {
        assertThatThrownBy(() -> itemRequestService.getOwn(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAll() {
        itemRequestRepository.save(new ItemRequest(null, "Нужна дрель", requestor, LocalDateTime.now()));

        List<ItemRequestDto> all = itemRequestService.getAll(otherUser.getId());

        assertThat(all).hasSize(1);
    }

    @Test
    void getAllExcludesOwn() {
        itemRequestRepository.save(new ItemRequest(null, "Нужна дрель", requestor, LocalDateTime.now()));

        List<ItemRequestDto> all = itemRequestService.getAll(requestor.getId());

        assertThat(all).isEmpty();
    }

    @Test
    void getAllUserNotFound() {
        assertThatThrownBy(() -> itemRequestService.getAll(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getById() {
        ItemRequest request = itemRequestRepository.save(
                new ItemRequest(null, "Нужна дрель", requestor, LocalDateTime.now()));
        itemRepository.save(new Item(null, "Дрель", "Описание", true, otherUser, request));

        ItemRequestDto found = itemRequestService.getById(otherUser.getId(), request.getId());

        assertThat(found.getId()).isEqualTo(request.getId());
        assertThat(found.getItems()).hasSize(1);
    }

    @Test
    void getByIdRequestNotFound() {
        assertThatThrownBy(() -> itemRequestService.getById(requestor.getId(), 999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getByIdUserNotFound() {
        ItemRequest request = itemRequestRepository.save(
                new ItemRequest(null, "Нужна дрель", requestor, LocalDateTime.now()));

        assertThatThrownBy(() -> itemRequestService.getById(999L, request.getId()))
                .isInstanceOf(NotFoundException.class);
    }
}