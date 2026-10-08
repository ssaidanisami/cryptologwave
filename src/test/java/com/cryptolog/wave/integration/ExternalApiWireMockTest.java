package com.cryptolog.wave.integration;

import com.github.tomakehurst.wiremock.junit5.WireMockRuntimeInfo;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.assertj.core.api.Assertions.assertThat;

@WireMockTest
class ExternalApiWireMockTest {

    @Test
    @DisplayName("Should successfully stub and verify GET request using WireMock")
    void shouldStubAndVerifyGetRequest(WireMockRuntimeInfo wmRuntimeInfo) {
        // 1. Arrange - Stub the endpoint
        stubFor(get(urlEqualTo("/api/v1/departments/Engineering"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withStatus(200)
                        .withBody("""
                                {
                                    "department": "Engineering",
                                    "activeEmployees": 42,
                                    "budget": 500000.00
                                }
                                """)));

        // 2. Act - Call the stubbed endpoint via RestClient
        RestClient restClient = RestClient.builder()
                .baseUrl(wmRuntimeInfo.getHttpBaseUrl())
                .build();

        String response = restClient.get()
                .uri("/api/v1/departments/Engineering")
                .retrieve()
                .body(String.class);

        // 3. Assert & Verify
        assertThat(response).isNotNull();
        assertThat(response).contains("Engineering");
        assertThat(response).contains("42");

        verify(getRequestedFor(urlEqualTo("/api/v1/departments/Engineering")));
    }

    @Test
    @DisplayName("Should successfully stub and verify POST request using WireMock")
    void shouldStubAndVerifyPostRequest(WireMockRuntimeInfo wmRuntimeInfo) {
        stubFor(post(urlEqualTo("/api/v1/notifications"))
                .withHeader("Content-Type", equalTo(MediaType.APPLICATION_JSON_VALUE))
                .willReturn(aResponse()
                        .withStatus(201)
                        .withHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                        .withBody("""
                                {
                                    "status": "DELIVERED",
                                    "notificationId": "NOTIF-12345"
                                }
                                """)));

        RestClient restClient = RestClient.builder()
                .baseUrl(wmRuntimeInfo.getHttpBaseUrl())
                .build();

        String response = restClient.post()
                .uri("/api/v1/notifications")
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"recipient\":\"john.doe@example.com\",\"message\":\"Welcome!\"}")
                .retrieve()
                .body(String.class);

        assertThat(response).contains("DELIVERED");
        assertThat(response).contains("NOTIF-12345");

        verify(postRequestedFor(urlEqualTo("/api/v1/notifications")));
    }
}
