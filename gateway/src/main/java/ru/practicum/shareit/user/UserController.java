package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private static final String USER_ID_PATH = "/{userId}";

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody @Valid UserDto userDto) {
        log.info("Creating user {}", userDto);
        return userClient.create(userDto);
    }

    @PatchMapping(USER_ID_PATH)
    public ResponseEntity<Object> update(@PathVariable Long userId, @RequestBody UserDto userDto) {
        log.info("Updating user {}, userId={}", userDto, userId);
        return userClient.update(userId, userDto);
    }

    @GetMapping(USER_ID_PATH)
    public ResponseEntity<Object> getById(@PathVariable Long userId) {
        log.info("Get user {}", userId);
        return userClient.getById(userId);
    }

    @GetMapping
    public ResponseEntity<Object> getAll() {
        log.info("Get all users");
        return userClient.getAll();
    }

    @DeleteMapping(USER_ID_PATH)
    public ResponseEntity<Object> delete(@PathVariable Long userId) {
        log.info("Delete user {}", userId);
        return userClient.delete(userId);
    }
}