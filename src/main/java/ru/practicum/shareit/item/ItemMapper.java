package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;

import java.util.Optional;

public class ItemMapper {
    public static ItemDto toItemDto(Item item) {
        Long requestId = Optional.ofNullable(item.getRequest())
                .map(ItemRequest::getId)
                .orElse(null);

        return new ItemDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                requestId
        );
    }
}
