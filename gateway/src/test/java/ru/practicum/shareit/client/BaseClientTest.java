package ru.practicum.shareit.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PATCH;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BaseClientTest {

    private static final String SERVER_URL = "http://localhost:9090";

    private MockRestServiceServer server;
    private BaseClient client;

    @BeforeEach
    void setUp() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setUriTemplateHandler(new DefaultUriBuilderFactory(SERVER_URL));
        server = MockRestServiceServer.bindTo(restTemplate).build();
        client = new BaseClient(restTemplate);
    }

    @Test
    void getWithoutUserId() {
        server.expect(requestTo(SERVER_URL + "/ping"))
                .andExpect(method(GET))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.get("/ping");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getWithUserId() {
        server.expect(requestTo(SERVER_URL + "/items"))
                .andExpect(method(GET))
                .andExpect(header("X-Sharer-User-Id", "5"))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.get("/items", 5L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getWithParameters() {
        server.expect(requestTo(SERVER_URL + "/search?text=abc"))
                .andExpect(method(GET))
                .andRespond(withSuccess("[]", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.get("/search?text={text}", 5L, Map.of("text", "abc"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void postWithoutUserId() {
        server.expect(requestTo(SERVER_URL + "/users"))
                .andExpect(method(POST))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.post("/users", Map.of("name", "test"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void postWithUserId() {
        server.expect(requestTo(SERVER_URL + "/items"))
                .andExpect(method(POST))
                .andExpect(header("X-Sharer-User-Id", "3"))
                .andRespond(withSuccess("{\"id\":1}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.post("/items", 3L, Map.of("name", "test"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void postWithUserIdAndParameters() {
        server.expect(requestTo(SERVER_URL + "/items?text=abc"))
                .andExpect(method(POST))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.post("/items?text={text}", 3L, Map.of("text", "abc"), Map.of("name", "test"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void putWithUserId() {
        server.expect(requestTo(SERVER_URL + "/items/1"))
                .andExpect(method(PUT))
                .andRespond(withSuccess());

        ResponseEntity<Object> response = client.put("/items/1", 3L, Map.of("name", "test"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void putWithUserIdAndParameters() {
        server.expect(requestTo(SERVER_URL + "/items/1?text=abc"))
                .andExpect(method(PUT))
                .andRespond(withSuccess());

        ResponseEntity<Object> response = client.put("/items/1?text={text}", 3L, Map.of("text", "abc"), Map.of("name", "test"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void patchWithoutUserId() {
        server.expect(requestTo(SERVER_URL + "/items/1"))
                .andExpect(method(PATCH))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.patch("/items/1", Map.of("name", "test"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void patchWithUserIdNoBody() {
        server.expect(requestTo(SERVER_URL + "/items/1"))
                .andExpect(method(PATCH))
                .andExpect(header("X-Sharer-User-Id", "3"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.patch("/items/1", 3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void patchWithUserIdAndBody() {
        server.expect(requestTo(SERVER_URL + "/items/1"))
                .andExpect(method(PATCH))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.patch("/items/1", 3L, Map.of("name", "test"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void patchWithUserIdParametersAndBody() {
        server.expect(requestTo(SERVER_URL + "/bookings/1?approved=true"))
                .andExpect(method(PATCH))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.patch("/bookings/1?approved={approved}", 3L, Map.of("approved", true), null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteWithoutUserId() {
        server.expect(requestTo(SERVER_URL + "/users/1"))
                .andExpect(method(DELETE))
                .andRespond(withSuccess());

        ResponseEntity<Object> response = client.delete("/users/1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteWithUserId() {
        server.expect(requestTo(SERVER_URL + "/items/1"))
                .andExpect(method(DELETE))
                .andExpect(header("X-Sharer-User-Id", "3"))
                .andRespond(withSuccess());

        ResponseEntity<Object> response = client.delete("/items/1", 3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void deleteWithUserIdAndParameters() {
        server.expect(requestTo(SERVER_URL + "/items?text=abc"))
                .andExpect(method(DELETE))
                .andRespond(withSuccess());

        ResponseEntity<Object> response = client.delete("/items?text={text}", 3L, Map.of("text", "abc"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void serverErrorIsPropagatedWithBody() {
        server.expect(requestTo(SERVER_URL + "/items/99"))
                .andExpect(method(GET))
                .andRespond(withStatus(HttpStatus.NOT_FOUND)
                        .body("{\"error\":\"not found\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        ResponseEntity<Object> response = client.get("/items/99", 3L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}