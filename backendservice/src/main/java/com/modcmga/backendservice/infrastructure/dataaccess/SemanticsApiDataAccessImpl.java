package com.modcmga.backendservice.infrastructure.dataaccess;

import com.google.gson.Gson;
import com.modcmga.backendservice.dto.dataccess.EmbeddingsOutput;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Component
public class SemanticsApiDataAccessImpl implements SemanticsApiDataAccess {
    private final static int HTTP_SUCCESS_CODE  = 200;
    @Value("${semanticsapi-baseurl}")
    private String semanticsApiBaseUrl;

    private final HttpClient httpClient;
    private final Gson gson;

    public SemanticsApiDataAccessImpl() {
        this.httpClient = HttpClient.newHttpClient();
        this.gson = new Gson();
    }

    @Override
    public Optional<EmbeddingsOutput> embeddings(final String text) {
        // Create URL
        String encodedQueryParam;
        try {
            final var formattedText = text.replaceAll("[^A-Za-z0-9]", "");
            encodedQueryParam = String.format("words=%s", URLEncoder.encode(formattedText, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        final var semanticsApiUrl = String.format("%s?%s", semanticsApiBaseUrl, encodedQueryParam);

        final var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(String.format(semanticsApiUrl, text)))
                .GET()
                .build();

        String responseBody;
        try {
            // Call embedding endpoint synchronously
            final var httpResponse =
                    httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            if (httpResponse.statusCode() != HTTP_SUCCESS_CODE) {
                throw new RuntimeException(httpResponse.body());
            }

            responseBody = httpResponse.body();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        var output = gson.fromJson(responseBody, EmbeddingsOutput.class);

        return Optional.of(output);
    }
}
