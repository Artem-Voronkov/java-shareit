package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ItemClient itemClient;

    @Test
    void create() throws Exception {
        ItemDto dto = new ItemDto(null, "Дрель", "Описание", true, null);
        when(itemClient.create(eq(1L), any())).thenReturn(ResponseEntity.ok(Map.of("id", 1, "name", "Дрель")));

        mvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createBlankName() throws Exception {
        ItemDto dto = new ItemDto(null, "", "Описание", true, null);

        mvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).create(anyLong(), any());
    }

    @Test
    void createNoAvailable() throws Exception {
        ItemDto dto = new ItemDto(null, "Дрель", "Описание", null, null);

        mvc.perform(post("/items")
                        .header(USER_ID_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).create(anyLong(), any());
    }

    @Test
    void update() throws Exception {
        ItemDto dto = new ItemDto(null, null, "Новое описание", null, null);
        when(itemClient.update(eq(1L), eq(1L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "description", "Новое описание")));

        mvc.perform(patch("/items/1")
                        .header(USER_ID_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Новое описание"));
    }

    @Test
    void getById() throws Exception {
        when(itemClient.getById(1L, 1L)).thenReturn(ResponseEntity.ok(Map.of("id", 1, "name", "Дрель")));

        mvc.perform(get("/items/1").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Дрель"));
    }

    @Test
    void getAllByOwner() throws Exception {
        when(itemClient.getAllByOwner(1L)).thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1, "name", "Дрель"))));

        mvc.perform(get("/items").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void search() throws Exception {
        when(itemClient.search(1L, "дрель"))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1, "name", "Дрель"))));

        mvc.perform(get("/items/search")
                        .header(USER_ID_HEADER, 1L)
                        .param("text", "дрель"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void addComment() throws Exception {
        CreateCommentDto dto = new CreateCommentDto("Отличная вещь!");
        when(itemClient.addComment(eq(1L), eq(1L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "text", "Отличная вещь!")));

        mvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("Отличная вещь!"));
    }

    @Test
    void addCommentBlankText() throws Exception {
        CreateCommentDto dto = new CreateCommentDto("");

        mvc.perform(post("/items/1/comment")
                        .header(USER_ID_HEADER, 1L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(itemClient, never()).addComment(anyLong(), anyLong(), any());
    }
}