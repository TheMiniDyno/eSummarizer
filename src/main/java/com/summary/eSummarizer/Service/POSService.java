package com.summary.eSummarizer.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
public class POSService {

    private static final Logger logger = LoggerFactory.getLogger(POSService.class);
    private final Map<String, String> posDict = new HashMap<>();

    public POSService() {
        try {
            loadPOSDictionary();
        } catch (IOException e) {
            e.printStackTrace();
            // Handle exceptions properly
        }
    }

    private void loadPOSDictionary() throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ClassPathResource("pos_dictionary.csv").getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    posDict.put(parts[0].trim().toLowerCase(), parts[1].trim());
                }
            }
        }
    }

    public String getPartOfSpeech(String word) {
        String pos = posDict.getOrDefault(word.toLowerCase(), "UNK");
//      logger to show the POS tagging in my LOG................................
//        logger.debug("POS for '{}': {}", word, pos); // Log the POS for the given word
        return pos;
    }

    public double getScoreForPOS(String pos) {
        switch (pos) {
            case "NN":
            case "NNS":
            case "NNP":
            case "NNPS":
                return 1.0; // Nouns
            case "VB":
            case "VBD":
            case "VBG":
            case "VBN":
            case "VBP":
            case "VBZ":
                return 0.8; // Verbs
            case "JJ":
            case "JJR":
            case "JJS":
                return 0.7; // Adjectives
            case "RB":
            case "RBR":
            case "RBS":
                return 0.5; // Adverbs
            case "DT":
                return 0.5; // Determiners
            case "IN":
                return 0.3; // Prepositions
            case "MD":
                return 0.4; // Modal verbs
            case "CD":
                return 0.4; // Cardinal numbers
            case "FW":
                return 0.3; // Foreign words
            case "PRP":
            case "PRP$":
                return 0.2; // Pronouns
            case "CC":
            case "WDT":
            case "WRB":
                return 0.3; // Conjunctions and wh-determiners
            case "TO":
                return 0.1; // To
            case "SYM":
                return 0.1; // Symbols
            case "WP":
            case "WP$":
                return 0.2; // Wh-pronouns
            default:
                return 0.0; // Unrecognized POS
        }
    }
}
