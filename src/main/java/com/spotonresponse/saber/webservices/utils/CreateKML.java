package com.spotonresponse.saber.webservices.utils;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;


public class CreateKML {
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final HttpHeaders headers = getHeaders();

    public static String build(String inputObjectAsJsonString, String kmlServiceUrl) {
        HttpEntity<String> entity = new HttpEntity<>(inputObjectAsJsonString, headers);
        return restTemplate.postForObject(kmlServiceUrl, entity, String.class);
    }

    private static HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return headers;
    }
}
