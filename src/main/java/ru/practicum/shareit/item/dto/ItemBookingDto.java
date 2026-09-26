package ru.practicum.shareit.item.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemBookingDto {
    Long id;
    String name;
    String description;
    Boolean available;
    Long requestId;
    BookingShortDto lastBooking;
    BookingShortDto nextBooking;
}