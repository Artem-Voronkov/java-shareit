package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemRequestController.class)
class ItemRequestControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemRequestService itemRequestService;

    @Test
    void create() throws Exception {
        CreateItemRequestDto dto = new CreateItemRequestDto("Нужна дрель");
        ItemRequestDto saved = new ItemRequestDto(1L, "Нужна дрель", LocalDateTime.now(), List.of());
        when(itemRequestService.create(eq(1L), any())).thenReturn(saved);

        mvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }

    @Test
    void createBlankDescription() throws Exception {
        CreateItemRequestDto dto = new CreateItemRequestDto("");

        mvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getOwn() throws Exception {
        ItemRequestDto request = new ItemRequestDto(1L, "Нужна дрель", LocalDateTime.now(), List.of());
        when(itemRequestService.getOwn(1L)).thenReturn(List.of(request));

        mvc.perform(get("/requests").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAll() throws Exception {
        ItemRequestDto request = new ItemRequestDto(1L, "Нужна дрель", LocalDateTime.now(), List.of());
        when(itemRequestService.getAll(2L)).thenReturn(List.of(request));

        mvc.perform(get("/requests/all").header(USER_ID_HEADER, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getById() throws Exception {
        ItemRequestDto request = new ItemRequestDto(1L, "Нужна дрель", LocalDateTime.now(), List.of());
        when(itemRequestService.getById(1L, 1L)).thenReturn(request);

        mvc.perform(get("/requests/1").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }
}