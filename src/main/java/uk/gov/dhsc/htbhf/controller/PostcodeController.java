package uk.gov.dhsc.htbhf.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;

import static org.springframework.util.StringUtils.isEmpty;

@Slf4j
@Controller
public class PostcodeController {

    private static final Pattern POSTCODE_REGEX = Pattern.compile("^([A-Za-z]{2}[0-9]{1,2}[A-Za-z]{0,1}[0-9]{1,2}[A-Za-z]{2}).*");

    private static final Map<String, ResponseEntity<String>> RESPONSES = readResponses();

    private static final ResponseEntity<String> DEFAULT_RESPONSE = readResponseFile("_default-response.json", HttpStatus.OK);

    private static final ResponseEntity<String> MISSING_KEY_RESPONSE = readResponseFile("_missing-api-key-returns-401.json", HttpStatus.UNAUTHORIZED);

    @PostConstruct
    public void logPostcodes() {
        RESPONSES.keySet().stream().forEach(postcode -> {
            log.info("Cached response for {}", postcode);
        });
    }

    @GetMapping(path = "/places/v1/addresses/postcode")
    public ResponseEntity<String> getAddressesForPostcode(@RequestParam("postcode") String postcode, @RequestParam(name = "key", required = false) String key) {
        if (isEmpty(key)) {
            return MISSING_KEY_RESPONSE;
        }
        return RESPONSES.getOrDefault(postcode, DEFAULT_RESPONSE);
    }

    private static Map<String, ResponseEntity<String>> readResponses() {
        try {
            ClassLoader cl = PostcodeController.class.getClassLoader();
            ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver(cl);
            Resource[] resources = resolver.getResources("classpath:/responses/*.json");
            return readPostcodeResponses(resources);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Map<String, ResponseEntity<String>> readPostcodeResponses(Resource[] resources) {
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

    private static ResponseEntity<String> readResponseFile(String filename, HttpStatus status) {
        try {
            ClassLoader cl = PostcodeController.class.getClassLoader();
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