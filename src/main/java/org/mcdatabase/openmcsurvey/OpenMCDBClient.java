package org.mcdatabase.openmcsurvey;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public final class OpenMCDBClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final URI submitUri;
    private final HttpClient httpClient;

    public OpenMCDBClient(String baseUrl) {
        String normalized = baseUrl.endsWith("/")
                ? baseUrl.substring(0, baseUrl.length() - 1)
                : baseUrl;
        this.submitUri = URI.create(normalized + "/v1/submit");
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
    }

    public boolean send(Snapshot snapshot) throws IOException, InterruptedException {
        if (OpenMCSurvey.noUserConsent()) return false; // should never trigger here, consent was already checked, but just to make sure
        HttpRequest request = HttpRequest.newBuilder(submitUri)
                .timeout(TIMEOUT)
                .header("Content-Type", "application/json; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(snapshot.toJson(), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        int status = response.statusCode();
        if (status == 429) {
            OpenMCSurvey.LOGGER.warn("openMCDB sent 429, retry after {}. not retrying this session",
                    response.headers()
                            .firstValue("Retry-After")
                            .map(value -> value + "s")
                            .orElse("unknown delay"));
            return false;
        }
        if (status < 200 || status >= 300) {
            throw new IOException("openMCDB submit failed: HTTP " + status + " " + response.body());
        }
        OpenMCSurvey.LOGGER.info("Sent snapshot to openMCDB");
        return true;
    }
}