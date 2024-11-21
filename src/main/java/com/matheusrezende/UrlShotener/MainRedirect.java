package com.matheusrezende.UrlShotener;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MainRedirect implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private final S3Client s3Client = S3Client.builder().build();

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> input, Context context) {
        String pathParameter = (String) input.get("rawPath");
        String shortUrl = pathParameter.replace("/","");

        if(shortUrl == null || shortUrl.isEmpty()){
            throw new IllegalArgumentException("Invalid input: 'shortUrlCode' is required");
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket("url-bucket-storage04")
                .key(shortUrl + ".json")
                .build();

        InputStream s3ObjectStream;

        try {
            s3ObjectStream = s3Client.getObject(getObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get object stream" + e.getMessage(), e);
        }

        OriginalUrlData urlData;

        try {
            urlData = mapper.readValue(s3ObjectStream, OriginalUrlData.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get original url data" + e.getMessage(), e);
        }

        long currentTimeInSeconds = System.currentTimeMillis() / 1000;

        Map<String, Object> response = new HashMap<>();

        if(currentTimeInSeconds > urlData.getExpirationTime()){
            response.put("statusCode", 410);
            response.put("body", "This url has expired");

            return response;
        }
        response.put("statusCode", 302);
        Map<String, String> headers = new HashMap<>();
        headers.put("Location", urlData.getOriginalUrl());
        response.put("headers", headers);

        return response;
    }
}