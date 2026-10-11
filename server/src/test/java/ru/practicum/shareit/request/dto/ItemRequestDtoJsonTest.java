package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoJsonTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void serialize() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(1L, "Нужна дрель",
                LocalDateTime.of(2026, 1, 1, 10, 0, 0),
                List.of(new ItemShortDto(1L, "Дрель", 2L), new ItemShortDto(2L, "Шуруповёрт", 3L)));

        var result = json.write(dto);

        assertThat(result).extractingJsonPathNumberValue("$.id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo("Нужна дрель");
        assertThat(result).extractingJsonPathStringValue("$.created").isEqualTo("2026-01-01T10:00:00");
        assertThat(result).extractingJsonPathArrayValue("$.items").hasSize(2);
        assertThat(result).extractingJsonPathNumberValue("$.items[0].id").isEqualTo(1);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("Дрель");
        assertThat(result).extractingJsonPathNumberValue("$.items[0].ownerId").isEqualTo(2);
        assertThat(result).extractingJsonPathStringValue("$.items[1].name").isEqualTo("Шуруповёрт");
    }

    @Test
    void serializeWithoutItems() throws Exception {
        ItemRequestDto dto = new ItemRequestDto(1L, "Нужна дрель",
                LocalDateTime.of(2026, 1, 1, 10, 0, 0), List.of());

        var result = json.write(dto);

        assertThat(result).extractingJsonPathArrayValue("$.items").isEmpty();
    }

    @Test
    void deserialize() throws Exception {
        String content = "{"
                + "\"id\":1,"
                + "\"description\":\"Нужна дрель\","
                + "\"created\":\"2026-01-01T10:00:00\","
                + "\"items\":[{\"id\":1,\"name\":\"Дрель\",\"ownerId\":2}]"
                + "}";

        ItemRequestDto dto = json.parseObject(content);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getCreated()).isEqualTo(LocalDateTime.of(2026, 1, 1, 10, 0, 0));
        assertThat(dto.getItems()).hasSize(1);
        assertThat(dto.getItems().get(0).getName()).isEqualTo("Дрель");
        assertThat(dto.getItems().get(0).getOwnerId()).isEqualTo(2L);
    }
}