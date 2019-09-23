package uk.gov.dhsc.htbhf.controller;

import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class PostcodeController {

    private final Responses responses;

    @GetMapping(path = "/places/v1/addresses/postcode")
    public ResponseEntity<String> getAddressesForPostcode(@RequestParam("postcode") String postcode, @RequestParam(name = "key", required = false) String key) {
        if (isEmpty(key)) {
            return responses.getMissingKeyResponse();
        }
        return responses.getResponseForPostcode(postcode);
    }
}