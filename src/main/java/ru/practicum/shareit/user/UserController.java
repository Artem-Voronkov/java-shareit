package ru.practicum.shareit.user;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private static final String USER_ID_PATH = "/{userId}";

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        return userService.create(userDto);
    }

    @PatchMapping(USER_ID_PATH)
    public UserDto update(@PathVariable Long userId, @RequestBody UserDto userDto) {
        return userService.update(userId, userDto);
    }

    @GetMapping(USER_ID_PATH)
    public UserDto getById(@PathVariable Long userId) {
        return userService.getById(userId);
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @DeleteMapping(USER_ID_PATH)
    public void delete(@PathVariable Long userId) {
        userService.delete(userId);
    }
}
