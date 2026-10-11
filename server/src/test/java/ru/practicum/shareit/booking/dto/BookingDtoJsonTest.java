package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.booking.BookingStatus;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingDtoJsonTest {

    @Autowired
    private JacksonTester<BookingDto> json;

    @Test
    void serialize() throws Exception {
        BookingDto dto = new BookingDto(1L,
                LocalDateTime.of(2026, 1, 1, 10, 0, 0),
                LocalDateTime.of(2026, 1, 2, 10, 0, 0),
                new BookingItemDto(1L, "Дрель"),
                new BookingBookerDto(2L, "Иван"),
                BookingStatus.WAITING);

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.start").isEqualTo("2026-01-01T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.end").isEqualTo("2026-01-02T10:00:00");
        assertThat(result).extractingJsonPathStringValue("$.status").isEqualTo("WAITING");
        assertThat(result).extractingJsonPathNumberValue("$.item.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.item.name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathNumberValue("$.booker.id").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.booker.name").isEqualTo("Иван");
    }

    @Test
    void deserialize() throws Exception {
        String content = "{"
                + "\"id\":1,"
                + "\"start\":\"2026-01-01T10:00:00\","
                + "\"end\":\"2026-01-02T10:00:00\","
                + "\"item\":{\"id\":1,\"name\":\"Дрель\"},"
                + "\"booker\":{\"id\":2,\"name\":\"Иван\"},"
                + "\"status\":\"WAITING\""
                + "}";

        BookingDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getStart()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 0, 0));
        assertThat(dto.getEnd()).isEqualTo(LocalDateTime.of(2026, 1, 2, 10, 0, 0));
        assertThat(dto.getItem().getName()).isEqualTo("Дрель");
        assertThat(dto.getBooker().getName()).isEqualTo("Иван");
        assertThat(dto.getStatus()).isEqualTo(BookingStatus.WAITING);
    }
}