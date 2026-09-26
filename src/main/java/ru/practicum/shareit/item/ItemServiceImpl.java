package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.BookingShortDto;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDetailsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    @Override
    public ItemDto create(Long ownerId, ItemDto itemDto) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + ownerId + " не найден"));

        Item item = ItemMapper.toItem(itemDto, owner);
        Item saved = itemRepository.save(item);
        return ItemMapper.toItemDto(saved);
    }

    @Override
    public ItemDto update(Long ownerId, Long itemId, ItemDto itemDto) {
        Item existing = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        if (existing.getOwner() == null || !existing.getOwner().getId().equals(ownerId)) {
            throw new NotFoundException("Пользователь с id=" + ownerId
                    + " не является владельцем вещи id=" + itemId);
        }

        if (itemDto.getName() != null) {
            existing.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            existing.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            existing.setAvailable(itemDto.getAvailable());
        }

        Item updated = itemRepository.save(existing);
        return ItemMapper.toItemDto(updated);
    }

    @Override
    public ItemDetailsDto getById(Long userId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь с id=" + itemId + " не найдена"));

        BookingShortDto last = null;
        BookingShortDto next = null;

        boolean isOwner = item.getOwner() != null && item.getOwner().getId().equals(userId);
        if (isOwner) {
            LocalDateTime now = LocalDateTime.now();
            List<Booking> bookings = bookingRepository.findByItem_IdOrderByStartAsc(item.getId());
            last = findLastBooking(bookings, now);
            next = findNextBooking(bookings, now);
        }

        return ItemMapper.toItemDetailsDto(item, last, next);
    }

    @Override
    public List<ItemBookingDto> getAllByOwner(Long ownerId) {
        LocalDateTime now = LocalDateTime.now();

        return itemRepository.findAllByOwnerId(ownerId).stream()
                .map(item -> {
                    List<Booking> bookings = bookingRepository.findByItem_IdOrderByStartAsc(item.getId());
                    BookingShortDto last = findLastBooking(bookings, now);
                    BookingShortDto next = findNextBooking(bookings, now);
                    return ItemMapper.toItemBookingDto(item, last, next);
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> search(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.search(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    private BookingShortDto findLastBooking(List<Booking> bookings, LocalDateTime now) {
        return bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .filter(b -> !b.getStart().isAfter(now))
                .max(Comparator.comparing(Booking::getStart))
                .map(this::toBookingShortDto)
                .orElse(null);
    }

    private BookingShortDto findNextBooking(List<Booking> bookings, LocalDateTime now) {
        return bookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.APPROVED)
                .filter(b -> b.getStart().isAfter(now))
                .min(Comparator.comparing(Booking::getStart))
                .map(this::toBookingShortDto)
                .orElse(null);
    }

    private BookingShortDto toBookingShortDto(Booking booking) {
        return new BookingShortDto(
                booking.getId(),
                booking.getBooker().getId(),
                booking.getStart(),
                booking.getEnd()
        );
    }
}