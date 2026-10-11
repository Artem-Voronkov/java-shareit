package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.dto.UserDto;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class UserClientTest {

    private static final String SERVER_URL = "http://localhost:9090";

    private UserClient client;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() throws Exception {
        client = new UserClient(SERVER_URL, new RestTemplateBuilder());

        Field field = BaseClient.class.getDeclaredField("rest");
        field.setAccessible(true);
        RestTemplate restTemplate = (RestTemplate) field.get(client);
        server = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    void create() {
        server.expect(requestTo(SERVER_URL + "/users"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.create(new UserDto(null, "Name", "name@test.com"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void update() {
        server.expect(requestTo(SERVER_URL + "/users/1"))
                .andExpect(method(HttpMethod.PATCH))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.update(1L, new UserDto(1L, "Name", "name@test.com"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getById() {
        server.expect(requestTo(SERVER_URL + "/users/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.getById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getAll() {
        server.expect(requestTo(SERVER_URL + "/users"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.getAll();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void delete() {
        server.expect(requestTo(SERVER_URL + "/users/1"))
                .andExpect(method(HttpMethod.DELETE))
                .andRespond(withSuccess());

        ResponseEntity<Object> response = client.delete(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getByIdPropagatesServerError() {
        server.expect(requestTo(SERVER_URL + "/users/99"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"Пользователь не найден\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.getById(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}