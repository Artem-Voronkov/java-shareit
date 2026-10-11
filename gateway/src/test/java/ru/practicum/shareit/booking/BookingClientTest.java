package ru.practicum.shareit.booking;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.client.BaseClient;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BookingClientTest {

    private static final String SERVER_URL = "http://localhost:9090";

    private BookingClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() throws Exception {
        client = new BookingClient(SERVER_URL, new RestTemplateBuilder());

        Field field = BaseClient.class.getDeclaredField("rest");
        field.setAccessible(true);
        RestTemplate restTemplate = (RestTemplate) field.get(client);
        server = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    void bookItem() {
        server.expect(requestTo(SERVER_URL + "/bookings"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header("X-Sharer-User-Id", "2"))
                .andRespond(withSuccess("{\"id\":1,\"status\":\"WAITING\"}", MediaType.APPLICATION_JSON));

        BookItemRequestDto dto = new BookItemRequestDto(1L,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2));
        ResponseEntity<Object> response = client.bookItem(2L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void approve() {
        server.expect(requestTo(SERVER_URL + "/bookings/10?approved=true"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header("X-Sharer-User-Id", "1"))
                .andRespond(withSuccess("{\"id\":10,\"status\":\"APPROVED\"}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.approve(1L, 10L, true);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getById() {
        server.expect(requestTo(SERVER_URL + "/bookings/10"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("X-Sharer-User-Id", "2"))
                .andRespond(withSuccess("{\"id\":10}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.getById(2L, 10L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAllByBooker() {
        server.expect(requestTo(SERVER_URL + "/bookings?state=ALL"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.getAllByBooker(2L, BookingState.ALL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAllByOwner() {
        server.expect(requestTo(SERVER_URL + "/bookings/owner?state=ALL"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.getAllByOwner(1L, BookingState.ALL);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getByIdPropagatesServerError() {
        server.expect(requestTo(SERVER_URL + "/bookings/99"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Бронирование не найдено\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.getById(2L, 99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}