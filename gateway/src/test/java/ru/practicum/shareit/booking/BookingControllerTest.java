package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;
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

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookingClient bookingClient;

    @Test
    void create() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(1L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        when(bookingClient.bookItem(eq(2L), any()))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "status", "WAITING")));

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 2L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void createInvalidBody() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(null, null, null);

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 2L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @Test
    void createPastStart() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(1L,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2));

        mvc.perform(post("/bookings")
                        .header(USER_ID_HEADER, 2L)
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).bookItem(anyLong(), any());
    }

    @Test
    void approve() throws Exception {
        when(bookingClient.approve(1L, 1L, true))
                .thenReturn(ResponseEntity.ok(Map.of("id", 1, "status", "APPROVED")));

        mvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getById() throws Exception {
        when(bookingClient.getById(2L, 1L)).thenReturn(ResponseEntity.ok(Map.of("id", 1)));

        mvc.perform(get("/bookings/1").header(USER_ID_HEADER, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllByBooker() throws Exception {
        when(bookingClient.getAllByBooker(2L, BookingState.ALL))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1))));

        mvc.perform(get("/bookings").header(USER_ID_HEADER, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllByBookerUnknownState() throws Exception {
        mvc.perform(get("/bookings")
                        .header(USER_ID_HEADER, 2L)
                        .param("state", "UNKNOWN"))
                .andExpect(status().isBadRequest());

        verify(bookingClient, never()).getAllByBooker(anyLong(), any());
    }

    @Test
    void getAllByOwner() throws Exception {
        when(bookingClient.getAllByOwner(1L, BookingState.ALL))
                .thenReturn(ResponseEntity.ok(List.of(Map.of("id", 1))));

        mvc.perform(get("/bookings/owner").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}