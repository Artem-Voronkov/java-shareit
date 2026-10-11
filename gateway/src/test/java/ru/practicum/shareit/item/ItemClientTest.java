package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import ru.practicum.shareit.client.BaseClient;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ItemClientTest {

    private static final String SERVER_URL = "http://localhost:9090";
    private static final String USER_ID_HEADER = "X-Sharer-User-Id";

    private ItemClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() throws Exception {
        client = new ItemClient(SERVER_URL, new RestTemplateBuilder());

        Field field = BaseClient.class.getDeclaredField("rest");
        field.setAccessible(true);
        RestTemplate restTemplate = (RestTemplate) field.get(client);
        server = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    void create() {
        server.expect(requestTo(SERVER_URL + "/items"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(USER_ID_HEADER, "2"))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ItemDto dto = new ItemDto(null, "Дрель", "Описание", true, null);
        ResponseEntity<Object> response = client.create(2L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void update() {
        server.expect(requestTo(SERVER_URL + "/items/5"))
                .andExpect(method(HttpMethod.PATCH))
                .andExpect(header(USER_ID_HEADER, "2"))
                .andRespond(withSuccess("{\"id\":5}", MediaType.APPLICATION_JSON));

        ItemDto dto = new ItemDto(5L, "Дрель", "Описание", false, null);
        ResponseEntity<Object> response = client.update(2L, 5L, dto);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getById() {
        server.expect(requestTo(SERVER_URL + "/items/5"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(USER_ID_HEADER, "2"))
                .andRespond(withSuccess("{\"id\":5}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.getById(2L, 5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAllByOwner() {
        server.expect(requestTo(SERVER_URL + "/items"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(USER_ID_HEADER, "2"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.getAllByOwner(2L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void search() {
        server.expect(requestTo(SERVER_URL + "/items/search?text=drill"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.search(2L, "drill");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void addComment() {
        server.expect(requestTo(SERVER_URL + "/items/5/comment"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(USER_ID_HEADER, "2"))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.addComment(2L, 5L, new CreateCommentDto("Отзыв"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getByIdPropagatesServerError() {
        server.expect(requestTo(SERVER_URL + "/items/99"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Вещь не найдена\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.getById(2L, 99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}