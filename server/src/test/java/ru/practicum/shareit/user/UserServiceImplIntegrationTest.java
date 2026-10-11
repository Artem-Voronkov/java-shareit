package ru.practicum.shareit.user;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void create() {
        UserDto dto = new UserDto(null, "Иван", "ivan@test.com");

        UserDto created = userService.create(dto);

        assertThat(created.getId()).isNotNull();
        assertThat(userRepository.findById(created.getId())).isPresent();
        assertThat(userRepository.findById(created.getId()).get().getEmail()).isEqualTo("ivan@test.com");
    }

    @Test
    void createDuplicateEmail() {
        userService.create(new UserDto(null, "Иван", "dup@test.com"));

        assertThatThrownBy(() -> userService.create(new UserDto(null, "Пётр", "dup@test.com")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void update() {
        UserDto created = userService.create(new UserDto(null, "Иван", "old@test.com"));

        UserDto updated = userService.update(created.getId(), new UserDto(null, "Иван Обновлённый", "new@test.com"));

        assertThat(updated.getName()).isEqualTo("Иван Обновлённый");
        assertThat(updated.getEmail()).isEqualTo("new@test.com");
    }

    @Test
    void updateNotFound() {
        assertThatThrownBy(() -> userService.update(999L, new UserDto(null, "Имя", "a@test.com")))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateDuplicateEmail() {
        userService.create(new UserDto(null, "Иван", "ivan@test.com"));
        UserDto petr = userService.create(new UserDto(null, "Пётр", "petr@test.com"));

        assertThatThrownBy(() -> userService.update(petr.getId(), new UserDto(null, "Пётр", "ivan@test.com")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void getById() {
        UserDto created = userService.create(new UserDto(null, "Иван", "ivan@test.com"));

        UserDto found = userService.getById(created.getId());

        assertThat(found.getEmail()).isEqualTo("ivan@test.com");
    }

    @Test
    void getByIdNotFound() {
        assertThatThrownBy(() -> userService.getById(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void getAll() {
        userService.create(new UserDto(null, "Иван", "ivan@test.com"));
        userService.create(new UserDto(null, "Пётр", "petr@test.com"));

        List<UserDto> all = userService.getAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void delete() {
        UserDto created = userService.create(new UserDto(null, "Иван", "ivan@test.com"));

        userService.delete(created.getId());

        assertThat(userRepository.findById(created.getId())).isEmpty();
    }

    @Test
    void deleteNotFound() {
        assertThatThrownBy(() -> userService.delete(999L))
                .isInstanceOf(NotFoundException.class);
    }
}