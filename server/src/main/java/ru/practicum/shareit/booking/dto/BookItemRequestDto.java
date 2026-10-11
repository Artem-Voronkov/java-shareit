package ru.practicum.shareit.booking.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.*;
import lombok.experimental.FieldDefaults;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookItemRequestDto {

    @NotNull(message = "Не указана вещь для бронирования")
    Long itemId;

    @NotNull(message = "Не указана дата начала бронирования")
    @FutureOrPresent(message = "Дата начала бронирования не может быть в прошлом")
    LocalDateTime start;

    @NotNull(message = "Не указана дата окончания бронирования")
    @Future(message = "Дата окончания бронирования должна быть в будущем")
    LocalDateTime end;
}
