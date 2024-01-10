package com.andrew.tgbot.api;

import com.andrew.tgbot.entities.RequestEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TelegramServerApi {
    private final RestTemplate restTemplate;
    public List<String> getUrls(RequestEntity request){
        String url = UriComponentsBuilder.fromHttpUrl("http://193.164.149.86:7077/telegram/picture")
                .queryParam("query", request.getQuery())
                .queryParam("count", request.getCount())
                .encode()
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<String>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<String>>() {});
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                System.out.println("return empty");
                return Collections.emptyList();
            }
        } catch (RestClientException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }

    public List<String> getCategory(){
        String url = UriComponentsBuilder.fromHttpUrl("http://193.164.149.86:7077/telegram/category")
                .encode()
                .toUriString();
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<List<String>> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<String>>() {});
            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                System.out.println("return empty");
                return Collections.emptyList();
            }
        } catch (RestClientException e) {
            e.printStackTrace();
            return Collections.emptyList();
        }
    }
}
