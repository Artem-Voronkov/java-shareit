package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemShortDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {

    private final ItemRequestRepository itemRequestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    public ItemRequestDto create(Long userId, CreateItemRequestDto dto) {
        User requestor = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с id=%d не найден", userId)));

        ItemRequest request = ItemRequestMapper.toItemRequest(dto, requestor);
        ItemRequest saved = itemRequestRepository.save(request);
        return ItemRequestMapper.toItemRequestDto(saved, List.of());
    }

    @Override
    public List<ItemRequestDto> getOwn(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с id=%d не найден", userId)));

        List<ItemRequest> requests = itemRequestRepository.findOwn(userId);
        return toDtoListWithItems(requests);
    }

    @Override
    public List<ItemRequestDto> getAll(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с id=%d не найден", userId)));

        List<ItemRequest> requests = itemRequestRepository.findOthers(userId);
        return toDtoListWithItems(requests);
    }

    @Override
    public ItemRequestDto getById(Long userId, Long requestId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Пользователь с id=%d не найден", userId)));

        ItemRequest request = itemRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Запрос с id=%d не найден", requestId)));

        List<ItemShortDto> items = itemRepository.findAllByRequest_Id(requestId).stream()
                .map(ItemRequestMapper::toItemShortDto)
                .collect(Collectors.toList());

        return ItemRequestMapper.toItemRequestDto(request, items);
    }

    private List<ItemRequestDto> toDtoListWithItems(List<ItemRequest> requests) {
        if (requests.isEmpty()) {
            return List.of();
        }

        List<Long> requestIds = requests.stream()
                .map(ItemRequest::getId)
                .collect(Collectors.toList());

        Map<Long, List<ItemShortDto>> itemsByRequestId = itemRepository.findAllByRequest_IdIn(requestIds).stream()
                .collect(Collectors.groupingBy(
                        item -> item.getRequest().getId(),
                        Collectors.mapping(ItemRequestMapper::toItemShortDto, Collectors.toList())));

        return requests.stream()
                .map(request -> ItemRequestMapper.toItemRequestDto(
                        request, itemsByRequestId.getOrDefault(request.getId(), List.of())))
                .collect(Collectors.toList());
    }
}