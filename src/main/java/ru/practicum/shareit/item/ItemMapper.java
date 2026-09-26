package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.User;

import java.util.List;
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

    public static Item toItem(ItemDto itemDto, User owner) {
        Item item = new Item();
        item.setId(itemDto.getId());
        item.setName(itemDto.getName());
        item.setDescription(itemDto.getDescription());
        item.setAvailable(itemDto.getAvailable());
        item.setOwner(owner);
        return item;
    }

    public static ItemBookingDto toItemBookingDto(Item item,
                                                  BookingShortDto lastBooking,
                                                  BookingShortDto nextBooking,
                                                  List<CommentDto> comments) {
        Long requestId = Optional.ofNullable(item.getRequest())
                .map(ItemRequest::getId)
                .orElse(null);

        return new ItemBookingDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                requestId,
                lastBooking,
                nextBooking,
                comments
        );
    }

    public static ItemDetailsDto toItemDetailsDto(Item item,
                                                  BookingShortDto lastBooking,
                                                  BookingShortDto nextBooking,
                                                  List<CommentDto> comments) {
        Long requestId = Optional.ofNullable(item.getRequest())
                .map(ItemRequest::getId)
                .orElse(null);

        return new ItemDetailsDto(
                item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                requestId,
                lastBooking,
                nextBooking,
                comments
        );
    }

    public static CommentDto toCommentDto(Comment comment) {
        return new CommentDto(
                comment.getId(),
                comment.getText(),
                comment.getAuthor().getName(),
                comment.getCreated()
        );
    }
}