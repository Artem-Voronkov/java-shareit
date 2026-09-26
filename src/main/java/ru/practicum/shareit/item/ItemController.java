package ru.practicum.shareit.item;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";
    private static final String ITEM_ID_PATH = "/{itemId}";

    private final ItemService itemService;

    @PostMapping
    public ItemDto create(@RequestHeader(USER_ID_HEADER) Long ownerId,
                          @Valid @RequestBody ItemDto itemDto) {
        return itemService.create(ownerId, itemDto);
    }

    @PatchMapping(ITEM_ID_PATH)
    public ItemDto update(@RequestHeader(USER_ID_HEADER) Long ownerId,
                          @PathVariable Long itemId,
                          @RequestBody ItemDto itemDto) {
        return itemService.update(ownerId, itemId, itemDto);
    }

    @GetMapping(ITEM_ID_PATH)
    public ItemDetailsDto getById(@RequestHeader(USER_ID_HEADER) Long userId,
                                  @PathVariable Long itemId) {
        return itemService.getById(userId, itemId);
    }

    @GetMapping
    public List<ItemBookingDto> getAllByOwner(@RequestHeader(USER_ID_HEADER) Long ownerId) {
        return itemService.getAllByOwner(ownerId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestHeader(USER_ID_HEADER) Long userId,
                                @RequestParam String text) {
        return itemService.search(text);
    }
}