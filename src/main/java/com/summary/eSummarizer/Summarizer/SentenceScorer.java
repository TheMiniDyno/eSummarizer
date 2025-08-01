package com.summary.eSummarizer.Summarizer;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class SentenceScorer {

    private static final Map<String, Double> POS_WEIGHTS;

    static {
        Map<String, Double> weights = new HashMap<>();

        // Nouns - very important
        weights.put("NOUN", 1.3); // Replaces NN, NNS
        weights.put("PROPN", 1.6); // Replaces NNP, NNPS

        // Verbs - important
        weights.put("VERB", 1.1); // Replaces VB, VBD, VBG, VBN, VBP, VBZ
        weights.put("AUX", 0.8); // Auxiliary verbs

        // Adjectives - moderate
        weights.put("ADJ", 0.9); // Replaces JJ, JJR, JJS

        // Numbers and quantities - important
        weights.put("NUM", 1.2); // Replaces CD

        // Adverbs - low importance
        weights.put("ADV", 0.7); // Replaces RB, RBR, RBS

        // Function words - low importance
        weights.put("DET", 0.3); // Replaces DT (Determiners)
        weights.put("ADP", 0.4); // Replaces IN (Prepositions)
        weights.put("CCONJ", 0.3); // Replaces CC (Coordinating conjunctions)
        weights.put("SCONJ", 0.3); // Subordinating conjunctions
        weights.put("PART", 0.3); // Replaces TO ("to")

        // Pronouns - low importance
        weights.put("PRON", 0.4); // Pronouns

        // Other tags you might encounter
        weights.put("INTJ", 0.8); // Interjections
        weights.put("SYM", 0.5); // Symbols
        weights.put("X", 0.2); // Other/Unknown

        // Punctuation - no importance
        weights.put("PUNCT", 0.0); // Punctuation

        POS_WEIGHTS = Collections.unmodifiableMap(weights);
    }

    public Map<String, Double> incorporatePositionBias(Map<String, Double> scores, List<String> sentences) {
        Map<String, Double> adjustedScores = new HashMap<>(scores);
        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            double positionBias = 1.0 - ((double) i / sentences.size());
            adjustedScores.put(sentence, adjustedScores.get(sentence) * positionBias);
        }
        return adjustedScores;
    }

    public Map<String, Double> adjustForSentenceLength(Map<String, Double> scores, List<String> sentences) {
        Map<String, Double> adjustedScores = new HashMap<>(scores);
        for (String sentence : sentences) {
            double lengthBias = 1.0 - ((double) sentence.split("\\s+").length / 30);
            adjustedScores.put(sentence, adjustedScores.get(sentence) * lengthBias);
        }
        return adjustedScores;
    }

    // POS-based scoring
    public Map<String, Double> incorporatePOSBias(Map<String, Double> scores, List<String> sentences,
            List<List<String>> taggedSentences) {
        Map<String, Double> adjustedScores = new HashMap<>(scores);

        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            if (i < taggedSentences.size()) {
                List<String> tags = taggedSentences.get(i);
                double posScore = calculatePOSScore(tags);
                adjustedScores.put(sentence, adjustedScores.get(sentence) * posScore);
            }
        }

        return adjustedScores;
    }

    private double calculatePOSScore(List<String> tags) {
        if (tags.isEmpty()) {
            return 1.0; // Neutral score if no tags
        }

        double totalWeight = 0.0;
        int tagCount = 0;

        for (String tag : tags) {
            if (POS_WEIGHTS.containsKey(tag)) {
                totalWeight += POS_WEIGHTS.get(tag);
                tagCount++;
            } else {
                totalWeight += 0.5; // Default weight for unimportant tags
                tagCount++;
            }
        }

        if (tagCount == 0) {
            return 1.0;
        }

        double averageWeight = totalWeight / tagCount;

        // Boost sentences with many important POS tags
        double importantTagRatio = (double) countImportantTags(tags) / tags.size();
        double boost = 1.0 + (importantTagRatio * 0.3); // Up to 30% boost

        return Math.min(1.8, averageWeight * boost); // Cap at 1.8x
    }

    private int countImportantTags(List<String> tags) {
        return (int) tags.stream()
                .filter(tag -> POS_WEIGHTS.containsKey(tag) && POS_WEIGHTS.get(tag) > 1.0)
                .count();
    }

    public Map<String, Double> normalizeScores(Map<String, Double> scores) {
        double totalScore = scores.values().stream().mapToDouble(Double::doubleValue).sum();
        System.out.println("totalScore001: " + totalScore);

        Map<String, Double> normalizedScores = new HashMap<>();
        for (String sentence : scores.keySet()) {
            double normalizedScore = scores.get(sentence) / totalScore;
            normalizedScores.put(sentence, normalizedScore);

            // Print each sentence with its rank
            System.out.println("Sentence: " + sentence.substring(0, Math.min(50, sentence.length())) + "...");
            System.out.println("Raw Score: " + scores.get(sentence));
            System.out.println("Normalized Score: " + normalizedScore);
            System.out.println("Percentage: " + String.format("%.2f%%", normalizedScore * 100));
            System.out.println("---");
        }

        return normalizedScores;
    }
}