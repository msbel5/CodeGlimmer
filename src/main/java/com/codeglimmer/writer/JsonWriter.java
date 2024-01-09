package com.codeglimmer.writer;

import com.codeglimmer.model.ParsedCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

public class JsonWriter implements CodeWriter {
    private static final Logger logger = LoggerFactory.getLogger(JsonWriter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] write(ParsedCode parsedCode) {
        logger.info("Writing parsed code information into JSON format");

        try {
            String jsonString = objectMapper.writeValueAsString(parsedCode);
            return jsonString.getBytes(StandardCharsets.UTF_8);
        } catch (JsonProcessingException e) {
            logger.error("Error occurred while converting parsed code to JSON", e);
            return null;
        }
    }
}
