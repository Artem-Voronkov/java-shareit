package ru.practicum.shareit.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemDto {
    Long id;

    @NotBlank(message = "Название вещи не должно быть пустым")
    String name;

    @NotBlank(message = "Описание вещи не должно быть пустым")
    String description;

    @NotNull(message = "Статус доступности должен быть указан")
    Boolean available;

    Long requestId;
}