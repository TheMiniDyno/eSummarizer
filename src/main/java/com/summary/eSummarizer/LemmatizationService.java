package com.summary.eSummarizer;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class LemmatizationService {

    private final Map<String, String> lemmatizationDict = new HashMap<>();

    public LemmatizationService() {
        try {
            loadDictionary();
        } catch (IOException | CsvValidationException e) {
            e.printStackTrace();
            // Handle exceptions properly
        }
    }

    private void loadDictionary() throws IOException, CsvValidationException {
        try (CSVReader reader = new CSVReader(
                new FileReader(new ClassPathResource("lemmatization_dictionary.csv").getFile()))) {
            String[] line;
            while ((line = reader.readNext()) != null) {
                if (line.length >= 2) {
                    String word = line[0].trim().toLowerCase();
                    String lemma = line[1].trim().toLowerCase();
                    lemmatizationDict.put(word, lemma);
                }
            }
        }
    }

    public String lemmatize(String word) {
        return lemmatizationDict.getOrDefault(word.toLowerCase(), word);
    }
}