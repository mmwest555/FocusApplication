package org.example.focusapplication;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class ApiClientTest {
    @Test
    void parsesSuccessfulResponseMessage() throws Exception {
        ApiClient.ApiResult result = invokeFromResponse(200, "{\"message\":\"Login successful\"}");

        assertTrue(result.success());
        assertEquals("Login successful", result.message());
    }

    @Test
    void fallsBackWhenResponseHasNoMessage() throws Exception {
        ApiClient.ApiResult result = invokeFromResponse(500, "{}");

        assertFalse(result.success());
        assertEquals("Request failed.", result.message());
    }

    private static ApiClient.ApiResult invokeFromResponse(int statusCode, String body) throws Exception {
        Method method = ApiClient.class.getDeclaredMethod("fromResponse", java.net.http.HttpResponse.class);
        method.setAccessible(true);
        return (ApiClient.ApiResult) method.invoke(null, new StubHttpResponse(statusCode, body));
    }

    private static final class StubHttpResponse implements java.net.http.HttpResponse<String> {
        private final int statusCode;
        private final String body;

        private StubHttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        @Override
        public int statusCode() {
            return statusCode;
        }

        @Override
        public String body() {
            return body;
        }

        @Override
        public java.net.http.HttpRequest request() {
            return null;
        }

        @Override
        public java.util.Optional<java.net.http.HttpResponse<String>> previousResponse() {
            return java.util.Optional.empty();
        }

        @Override
        public java.net.http.HttpHeaders headers() {
            return java.net.http.HttpHeaders.of(java.util.Map.of(), (a, b) -> true);
        }

        @Override
        public java.net.URI uri() {
            return java.net.URI.create("http://localhost");
        }

        @Override
        public java.net.http.HttpClient.Version version() {
            return java.net.http.HttpClient.Version.HTTP_1_1;
        }

        @Override
        public java.util.Optional<javax.net.ssl.SSLSession> sslSession() {
            return java.util.Optional.empty();
        }
    }
}
