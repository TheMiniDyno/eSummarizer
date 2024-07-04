package com.summary.NewsSummary;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.*;

@Service
public class Preprocessor {

    private final Set<String> STOPWORDS;

    @Autowired
    private LemmatizationService lemmatizationService;

    public Preprocessor() {
        STOPWORDS = loadStopwordsFromCsv("stopwords.csv");
    }

    private Set<String> loadStopwordsFromCsv(String filename) {
        Set<String> stopwords = new HashSet<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ClassPathResource(filename).getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] words = line.split(",");
                for (String word : words) {
                    if (!word.isEmpty()) {
                        stopwords.add(word.trim());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading stopwords: " + e.getMessage());
            // You might want to throw a custom exception here
        }
        return stopwords;
    }

    public List<String> tokenizeSentences(String text) {
        return Arrays.asList(text.split("(?<!\\w\\.\\w.)(?<![A-Z][a-z]\\.)(?<=\\.|\\?|\\!)\\s"));
    }

    public List<String> tokenizeWords(String sentence) {
        return Arrays.asList(sentence.toLowerCase().replaceAll("[^a-zA-Z0-9\\s]", "").split("\\s+"));
    }

    public List<String> removeStopwordsAndLemmatize(List<String> sentences) {
        return sentences.stream().map(sentence -> {
            List<String> words = tokenizeWords(sentence);
            return words.stream()
                    .filter(word -> !STOPWORDS.contains(word))
                    .map(lemmatizationService::lemmatize)
                    .collect(Collectors.joining(" "));
        }).collect(Collectors.toList());
    }
}