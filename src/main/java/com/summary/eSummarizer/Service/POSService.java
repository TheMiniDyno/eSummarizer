package com.summary.eSummarizer.Service;

import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class POSService {

    private static final Logger logger = LoggerFactory.getLogger(POSService.class);
    private final POSTaggerME posTagger;

    public POSService() {
        try {
            // Load POS tagger model
            InputStream posModelStream = getClass().getResourceAsStream("/models/openNLP/en-pos.bin");
            if (posModelStream == null) {
                throw new IOException("POS model file not found: /models/openNLP/en-pos.bin");
            }

            POSModel posModel = new POSModel(posModelStream);
            this.posTagger = new POSTaggerME(posModel);

            logger.info("OpenNLP POS Tagger loaded successfully");

        } catch (IOException e) {
            logger.error("Failed to load OpenNLP POS model", e);
            throw new RuntimeException("Failed to initialize POS service", e);
        }
    }

    public String getPartOfSpeech(String word) {
        try {
            String[] tokens = { word };
            String[] posTags = posTagger.tag(tokens);
            return posTags[0];

        } catch (Exception e) {
            logger.warn("Failed to get POS for word '{}', returning UNK", word, e);
            return "UNK";
        }
    }

    // Batch processing method for better performance
    public String[] getPartsOfSpeech(String[] words) {
        try {
            return posTagger.tag(words);
        } catch (Exception e) {
            logger.warn("Failed to get POS for word array, returning UNK array", e);
            String[] unknownTags = new String[words.length];
            for (int i = 0; i < words.length; i++) {
                unknownTags[i] = "UNK";
            }
            return unknownTags;
        }
    }
}