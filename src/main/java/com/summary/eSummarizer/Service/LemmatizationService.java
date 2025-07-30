package com.summary.eSummarizer.Service;

import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@Service
public class LemmatizationService {

    private static final Logger logger = LoggerFactory.getLogger(LemmatizationService.class);
    private final Map<String, String> lemmatizationDict;
    private final CSVLoaderService csvLoaderService;

    public LemmatizationService(CSVLoaderService csvLoaderService) {
        this.csvLoaderService = csvLoaderService;
        this.lemmatizationDict = loadDictionary();
    }

    private Map<String, String> loadDictionary() {
        return csvLoaderService.loadAsKeyValue("/CSV/lemmatization_dictionary.csv");
    }

    public String lemmatize(String word) {
        String lemma = lemmatizationDict.getOrDefault(word.toLowerCase(), word);
//        logger.info("Lemmatized '{}' to '{}'", word, lemma);

        return lemma;
    }
}