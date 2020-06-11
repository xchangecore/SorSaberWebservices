package com.spotonresponse.saber.webservices.utils;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;


public class CreateKML {
    private static RestTemplate restTemplate = new RestTemplate();
    private static HttpHeaders headers = getHeaders();


    static org.slf4j.Logger logger = LoggerFactory.getLogger(CreateKML.class);

    public static String build(String inputObjectAsJsonString, String kmlServiceUrl) {
        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<String> entity = new HttpEntity<>(inputObjectAsJsonString, headers);
        return restTemplate.postForObject(kmlServiceUrl, entity, String.class);
    }

    private static HttpHeaders getHeaders(){
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        return headers;
    }
}
