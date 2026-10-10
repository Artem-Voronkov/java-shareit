package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {

    ItemRequestDto create(Long userId, CreateItemRequestDto dto);

    List<ItemRequestDto> getOwn(Long userId);

    List<ItemRequestDto> getAll(Long userId);

    ItemRequestDto getById(Long userId, Long requestId);
}