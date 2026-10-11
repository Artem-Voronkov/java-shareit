package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.BookingBookerDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private BookingService bookingService;

    private BookingDto bookingDto(BookingStatus status) {
        return new BookingDto(1L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                new BookingItemDto(1L, "Дрель"),
                new BookingBookerDto(2L, "Booker"),
                status);
    }

    @Test
    void create() throws Exception {
        BookItemRequestDto dto = new BookItemRequestDto(1L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        when(bookingService.create(eq(2L), any())).thenReturn(bookingDto(BookingStatus.WAITING));

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
    }

    @Test
    void approve() throws Exception {
        when(bookingService.approve(eq(1L), eq(1L), eq(true))).thenReturn(bookingDto(BookingStatus.APPROVED));

        mvc.perform(patch("/bookings/1")
                        .header(USER_ID_HEADER, 1L)
                        .param("approved", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void getById() throws Exception {
        when(bookingService.getById(2L, 1L)).thenReturn(bookingDto(BookingStatus.WAITING));

        mvc.perform(get("/bookings/1").header(USER_ID_HEADER, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getAllByBooker() throws Exception {
        when(bookingService.getAllByBooker(2L, BookingState.ALL))
                .thenReturn(List.of(bookingDto(BookingStatus.WAITING)));

        mvc.perform(get("/bookings").header(USER_ID_HEADER, 2L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllByOwner() throws Exception {
        when(bookingService.getAllByOwner(1L, BookingState.ALL))
                .thenReturn(List.of(bookingDto(BookingStatus.WAITING)));

        mvc.perform(get("/bookings/owner").header(USER_ID_HEADER, 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}