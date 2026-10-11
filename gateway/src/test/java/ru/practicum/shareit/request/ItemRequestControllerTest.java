package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
    private ItemRequestClient itemRequestClient;

    @Test
    void create() throws Exception {
        CreateItemRequestDto dto = new CreateItemRequestDto("Нужна дрель");
        when(itemRequestClient.create(eq(1L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "description", "Нужна дрель")));

        mvc.perform(post("/requests")
                        .header(USER_ID_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
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

        verify(itemRequestClient, never()).create(anyLong(), any());
    }

    @Test
    void getOwn() throws Exception {
        when(itemRequestClient.getOwn(1L)).thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1))));

        mvc.perform(get("/requests").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAll() throws Exception {
        when(itemRequestClient.getAll(2L)).thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1))));

        mvc.perform(get("/requests/all").header(USER_ID_HEADER, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getById() throws Exception {
        when(itemRequestClient.getById(1L, 1L))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "description", "Нужна дрель")));

        mvc.perform(get("/requests/1").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Нужна дрель"));
    }
}