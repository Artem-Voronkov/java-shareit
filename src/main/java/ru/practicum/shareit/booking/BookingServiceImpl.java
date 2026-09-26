package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    @Override
    public BookingDto create(Long userId, BookItemRequestDto dto) {
        User booker = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с id=%d не найден", userId)));

        Item item = itemRepository.findById(dto.getItemId())
                .orElseThrow(() -> new NotFoundException(
                        String.format("Вещь с id=%d не найдена", dto.getItemId())));

        if (!item.getAvailable()) {
            throw new ValidationException(
                    String.format("Вещь с id=%d недоступна для бронирования", item.getId()));
        }

        if (item.getOwner().getId().equals(userId)) {
            throw new NotFoundException(
                    String.format("Вещь с id=%d не найдена", item.getId()));
        }

        if (!dto.getEnd().isAfter(dto.getStart())) {
            throw new ValidationException("Дата окончания бронирования должна быть позже даты начала");
        }

        Booking booking = BookingMapper.toBooking(dto, item, booker);
        Booking saved = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(saved);
    }

    @Override
    public BookingDto approve(Long ownerId, Long bookingId, boolean approved) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Бронирование с id=%d не найдено", bookingId)));

        if (!booking.getItem().getOwner().getId().equals(ownerId)) {
            throw new ValidationException(String.format(
                    "Пользователь с id=%d не является владельцем вещи, к которой относится бронирование id=%d",
                    ownerId, bookingId));
        }

        if (booking.getStatus() != BookingStatus.WAITING) {
            throw new ValidationException("Статус бронирования уже был изменён и не может быть пересмотрен");
        }

        booking.setStatus(approved ? BookingStatus.APPROVED : BookingStatus.REJECTED);
        Booking updated = bookingRepository.save(booking);
        return BookingMapper.toBookingDto(updated);
    }

    @Override
    public BookingDto getById(Long userId, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Бронирование с id=%d не найдено", bookingId)));

        boolean isBooker = booking.getBooker().getId().equals(userId);
        boolean isOwner = booking.getItem().getOwner().getId().equals(userId);

        if (!isBooker && !isOwner) {
            throw new NotFoundException(
                    String.format("Бронирование с id=%d не найдено", bookingId));
        }

        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllByBooker(Long bookerId, BookingState state) {
        userRepository.findById(bookerId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с id=%d не найден", bookerId)));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case CURRENT -> bookingRepository
                    .findByBooker_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(bookerId, now, now);
            case PAST -> bookingRepository.findByBooker_IdAndEndIsBeforeOrderByStartDesc(bookerId, now);
            case FUTURE -> bookingRepository.findByBooker_IdAndStartIsAfterOrderByStartDesc(bookerId, now);
            case WAITING -> bookingRepository
                    .findByBooker_IdAndStatusOrderByStartDesc(bookerId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository
                    .findByBooker_IdAndStatusOrderByStartDesc(bookerId, BookingStatus.REJECTED);
            case ALL -> bookingRepository.findByBooker_IdOrderByStartDesc(bookerId);
        };

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllByOwner(Long ownerId, BookingState state) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с id=%d не найден", ownerId)));

        LocalDateTime now = LocalDateTime.now();
        List<Booking> bookings = switch (state) {
            case CURRENT -> bookingRepository
                    .findByItem_Owner_IdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(ownerId, now, now);
            case PAST -> bookingRepository.findByItem_Owner_IdAndEndIsBeforeOrderByStartDesc(ownerId, now);
            case FUTURE -> bookingRepository.findByItem_Owner_IdAndStartIsAfterOrderByStartDesc(ownerId, now);
            case WAITING -> bookingRepository
                    .findByItem_Owner_IdAndStatusOrderByStartDesc(ownerId, BookingStatus.WAITING);
            case REJECTED -> bookingRepository
                    .findByItem_Owner_IdAndStatusOrderByStartDesc(ownerId, BookingStatus.REJECTED);
            case ALL -> bookingRepository.findByItem_Owner_IdOrderByStartDesc(ownerId);
        };

        return bookings.stream()
                .map(BookingMapper::toBookingDto)
                .collect(Collectors.toList());
    }
}