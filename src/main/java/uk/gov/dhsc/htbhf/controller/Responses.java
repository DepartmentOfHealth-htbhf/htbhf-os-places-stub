package uk.gov.dhsc.htbhf.controller;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;

import static org.springframework.util.StringUtils.isEmpty;

@Slf4j
@Component
public class Responses {

    private static final Pattern POSTCODE_REGEX = Pattern.compile("^([A-Za-z]{2}[0-9]{1,2}[A-Za-z]{0,1}[0-9]{1,2}[A-Za-z]{2}).*");

    private final Map<String, ResponseEntity<String>> responses = readResponses();
    private final ResponseEntity<String> defaultResponse = readResponseFile("_default-response.json", HttpStatus.OK);

    @Getter
    private final ResponseEntity<String> missingKeyResponse = readResponseFile("_missing-api-key-returns-401.json", HttpStatus.UNAUTHORIZED);

    @PostConstruct
    public void logPostcodes() {
        responses.keySet().stream().forEach(postcode -> {
            log.info("Cached response for {}", postcode);
        });
    }

    public ResponseEntity<String> getResponseForPostcode(String postcode) {
        return responses.getOrDefault(postcode, defaultResponse);

    }

    private Map<String, ResponseEntity<String>> readResponses() {
        try {
            ClassLoader cl = Responses.class.getClassLoader();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
            Resource[] resources = resolver.getResources("classpath:/responses/*.json");
            return readPostcodeResponses(resources);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Map<String, ResponseEntity<String>> readPostcodeResponses(Resource[] resources) {
        Map<String, ResponseEntity<String>> responses = new ConcurrentHashMap<>();
        for (Resource resource : resources) {
            Matcher matcher = POSTCODE_REGEX.matcher(resource.getFilename());
            if (matcher.matches()) {
                String postcode = matcher.group(1);
                try (InputStream stream = resource.getInputStream()) {
                    String result = IOUtils.toString(stream, StandardCharsets.UTF_8);
                    responses.put(postcode, new ResponseEntity<>(result, HttpStatus.OK));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return responses;
    }

    private ResponseEntity<String> readResponseFile(String filename, HttpStatus status) {
        try {
            ClassLoader cl = Responses.class.getClassLoader();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
            Resource resource = resolver.getResource("/responses/" + filename);
            try (InputStream stream = resource.getInputStream()) {
                String result = IOUtils.toString(stream, StandardCharsets.UTF_8);
                return new ResponseEntity<>(result, status);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}