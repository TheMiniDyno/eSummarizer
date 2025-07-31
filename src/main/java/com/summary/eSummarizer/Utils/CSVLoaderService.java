package com.summary.eSummarizer.Utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

@Service
public class CSVLoaderService {

    private static final Logger logger = LoggerFactory.getLogger(CSVLoaderService.class);

    public Set<String> loadAsSet(String filename) {
        return loadAsSet(filename, ",");
    }

    //Method to load CSV as Sets.
    public Set<String> loadAsSet(String filename, String delimiter) {
        Set<String> result = new HashSet<>();
        try (BufferedReader reader = createReader(filename)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] values = line.split(delimiter);
                for (String value : values) {
                    String trimmed = value.trim();
                    if (!trimmed.isEmpty()) {
                        result.add(trimmed);
                    }
                }
            }
            logger.info("Loaded {} items from {}", result.size(), filename);
        } catch (IOException e) {
            logger.error("Error loading CSV file {}: {}", filename, e.getMessage());
            throw new RuntimeException("Failed to load CSV file: " + filename, e);
        }
        return result;
    }

    private BufferedReader createReader(String filename) throws IOException {
        return new BufferedReader(
                new InputStreamReader(new ClassPathResource(filename).getInputStream()));
    }
}