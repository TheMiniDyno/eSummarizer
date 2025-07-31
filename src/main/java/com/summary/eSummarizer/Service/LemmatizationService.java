package com.summary.eSummarizer.Service;

import opennlp.tools.lemmatizer.LemmatizerME;
import opennlp.tools.lemmatizer.LemmatizerModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

@Service
public class LemmatizationService {

    private static final Logger logger = LoggerFactory.getLogger(LemmatizationService.class);
    private final LemmatizerME lemmatizer;
    private final POSService posService;

    @Autowired
    public LemmatizationService(POSService posService) {
        this.posService = posService;

        try {
            // Load lemmatizer model
            InputStream lemmatizerModelStream = getClass().getResourceAsStream("/models/openNLP/en-lemma.bin");
            if (lemmatizerModelStream == null) {
                throw new IOException("Lemmatizer model file not found: /models/openNLP/en-lemma.bin");
            }

            LemmatizerModel lemmatizerModel = new LemmatizerModel(lemmatizerModelStream);
            this.lemmatizer = new LemmatizerME(lemmatizerModel);

            logger.info("OpenNLP Lemmatizer loaded successfully");

        } catch (IOException e) {
            logger.error("Failed to load OpenNLP lemmatizer model", e);
            throw new RuntimeException("Failed to initialize lemmatization service", e);
        }
    }

    public String lemmatize(String word) {
        try {
            // Get POS tag for the word
            String posTag = posService.getPartOfSpeech(word);

            // Lemmatize using POS tag
            String[] tokens = { word };
            String[] posTags = { posTag };
            String[] lemmas = lemmatizer.lemmatize(tokens, posTags);

            String lemma = lemmas[0];
            // OpenNLP returns "O" when it can't lemmatize
            return "O".equals(lemma) ? word : lemma;

        } catch (Exception e) {
            logger.warn("Failed to lemmatize word '{}', returning original", word, e);
            return word;
        }
    }

    // Batch processing method for better performance
    public String[] lemmatize(String[] words) {
        try {
            String[] posTags = posService.getPartsOfSpeech(words);
            return lemmatizer.lemmatize(words, posTags);
        } catch (Exception e) {
            logger.warn("Failed to lemmatize word array, returning original", e);
            return words;
        }
    }

    // Method that takes pre-computed POS tags (for efficiency)
    public String[] lemmatize(String[] words, String[] posTags) {
        try {
            return lemmatizer.lemmatize(words, posTags);
        } catch (Exception e) {
            logger.warn("Failed to lemmatize word array with POS tags, returning original", e);
            return words;
        }
    }
}