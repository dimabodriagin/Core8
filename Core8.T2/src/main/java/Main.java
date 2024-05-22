import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class Main {

    public static final String REMOTE_SERVICE_URI = "https://api.nasa.gov/planetary/apod?api_key=A5Q7BbJPQ0E3gyiNyUybdeAmQ6mHijJIAzfuI7TD";

    public static void main(String[] args) {

        try (CloseableHttpClient httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(
                        RequestConfig.custom()
                                .setConnectTimeout(5000)    // максимальное время ожидание подключения к серверу
                                .setSocketTimeout(30000)    // максимальное время ожидания получения данных
                                .setRedirectsEnabled(false) // возможность следовать редиректу в ответе
                                .build())
                .build()) {


            HttpGet request = new HttpGet(REMOTE_SERVICE_URI);
            request.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            CloseableHttpResponse response = httpClient.execute(request);

            ObjectMapper objectMapper = new ObjectMapper();
            NASAContent content = objectMapper.readValue(
                    response.getEntity().getContent(),
                    NASAContent.class
            );

            HttpGet requestToNASA = new HttpGet(content.getUrl());
            requestToNASA.setHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.getMimeType());
            CloseableHttpResponse responseFromNASA = httpClient.execute(requestToNASA);

            var file = new File(content.getUrl().substring(content.getUrl().lastIndexOf("/") + 1));
            try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
                outputStream.write(responseFromNASA.getEntity().getContent().readAllBytes());
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }

            response.close();
            responseFromNASA.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
