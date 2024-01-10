package com.andrew.tgbot.service;

import com.andrew.tgbot.api.TelegramServerApi;
import com.andrew.tgbot.entities.RequestEntity;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BotService {
    private final TelegramServerApi telegramServerApi = new TelegramServerApi(new RestTemplate());

    public String categoryMessage(){
        String message = "";
        try {
            List<String> list = telegramServerApi.getCategory();
            for (int i = 0; i < list.size(); i++) {
                if (i != list.size() - 1)
                    message += list.get(i) + "\n";
            }
            return message;
        } catch (Exception e){
            return "Не найдены категории!";
        }
    }

    public List<InputStream> getPhotoStreams(RequestEntity request){
        List<String> urls = telegramServerApi.getUrls(request);
        if(urls.get(0).equals("Rate Limit Exceeded")){
            return null;
        }
        List<InputStream> inputStreams = new ArrayList<>();
        urls.forEach(url -> {
            try {
                inputStreams.add(new ByteArrayInputStream(downloadPhoto(url)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return inputStreams;
    }
    private byte[] downloadPhoto(String photoUrl) throws IOException {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(photoUrl);

        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();

        if (entity != null) {
            return EntityUtils.toByteArray(entity);
        } else {
            throw new IOException("Failed to download photo. Empty response.");
        }
    }
}
