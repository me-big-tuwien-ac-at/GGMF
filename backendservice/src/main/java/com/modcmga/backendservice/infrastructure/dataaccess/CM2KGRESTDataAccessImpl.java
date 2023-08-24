package com.modcmga.backendservice.infrastructure.dataaccess;

import com.google.gson.GsonBuilder;
import com.modcmga.backendservice.dto.dataccess.CM2KGOutput;
import com.modcmga.backendservice.model.conceptualmodel.MetaModelType;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.channels.Channels;
import java.nio.channels.Pipe;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class CM2KGRESTDataAccessImpl implements CM2KGDataAccess {
    private final static String BASE_URL = "https://me.big.tuwien.ac.at/cm2kg/api/transformation/%s";
    private final static String TARGET_GRAPHML_FILE_NAME = "target.graphml";
    private final static String META_MODEL_FILE_KEY = "metaModelFile";
    private final static String DTD_FILE_KEY = "dtdFile";
    @Override
    public File transformToGraphMLFile(Map<String, File> files, MetaModelType metaModelType) {
        return transform(files, metaModelType);
    }

    // Uses code from https://www.springcloud.io/post/2022-04/httpclient-multipart/#gsc.tab=0
    private File transform(Map<String, File> files, MetaModelType metaModelType) {

        final var url = String.format(BASE_URL, metaModelType.metaModelType);

        final var metaModelFile = files.get(META_MODEL_FILE_KEY);
        final Map<String, String> formData = new LinkedHashMap<>();
        formData.put("file", metaModelFile.toPath().toString());

        String responseBody;
        try {
            HttpEntity httpEntity;
            if (metaModelType != MetaModelType.ADOXX) {
                httpEntity = MultipartEntityBuilder.create()
                        .addBinaryBody("file", new FileInputStream(metaModelFile), ContentType.APPLICATION_XML,
                                metaModelFile.getName())
                        .build();
            } else {
                final var dtdFile = files.get(DTD_FILE_KEY);
                formData.put("dtdfile", dtdFile.toPath().toString());

                httpEntity = MultipartEntityBuilder.create()
                        .addBinaryBody("file", new FileInputStream(metaModelFile), ContentType.APPLICATION_XML,
                                metaModelFile.getName())
                        .addBinaryBody("dtdfile", new FileInputStream(dtdFile), ContentType.APPLICATION_XML,
                                dtdFile.getName())
                        .build();
            }

            final var pipe = Pipe.open();

            new Thread(() -> {
                try (OutputStream outputStream = Channels.newOutputStream(pipe.sink())) {
                    httpEntity.writeTo(outputStream);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }).start();

            final var httpClient = HttpClient.newHttpClient();

            final var request = HttpRequest.newBuilder(new URI(url))
                    .header("Content-Type", httpEntity.getContentType().getValue())
                    .POST(HttpRequest.BodyPublishers.ofInputStream(() -> Channels.newInputStream(pipe.source()))).build();

            var httpResponse = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            responseBody = httpResponse.body();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final var builder = new GsonBuilder();
        final var gson = builder.create();

        final var cm2kgOutputDTO = gson.fromJson(responseBody, CM2KGOutput.class);

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(TARGET_GRAPHML_FILE_NAME), "utf-8"))) {
            writer.write(cm2kgOutputDTO.getTransformedGraph());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new File(TARGET_GRAPHML_FILE_NAME);
    }
}
