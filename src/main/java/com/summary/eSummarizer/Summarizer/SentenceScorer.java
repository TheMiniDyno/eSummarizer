package com.summary.eSummarizer.Summarizer;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class SentenceScorer {

    // Configuration constants
    private static final double POSITION_BIAS_STRENGTH = 0.3;
    private static final int OPTIMAL_SENTENCE_LENGTH = 20;
    private static final int MAX_SENTENCE_LENGTH = 40;
    private static final double POS_BOOST_FACTOR = 0.4;
    private static final double MAX_SCORE_MULTIPLIER = 2.0;

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

    // Cache for word counts to avoid repeated splits
    private final Map<String, Integer> wordCountCache = new HashMap<>();

    public Map<String, Double> incorporatePositionBias(Map<String, Double> scores, List<String> sentences) {
        Map<String, Double> adjustedScores = new HashMap<>(scores);

        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            // Exponential decay rather than linear
            double positionBias = 1.0 - (POSITION_BIAS_STRENGTH * Math.pow((double) i / sentences.size(), 2));
            adjustedScores.put(sentence, adjustedScores.get(sentence) * positionBias);
        }
        return adjustedScores;
    }

    public Map<String, Double> adjustForSentenceLength(Map<String, Double> scores, List<String> sentences) {
        Map<String, Double> adjustedScores = new HashMap<>(scores);

        for (String sentence : sentences) {
            int wordCount = getWordCount(sentence);
            double lengthBias = calculateLengthBias(wordCount);
            adjustedScores.put(sentence, adjustedScores.get(sentence) * lengthBias);
        }
        return adjustedScores;
    }

    private int getWordCount(String sentence) {
        return wordCountCache.computeIfAbsent(sentence,
                s -> s.trim().split("\\s+").length);
    }

    private double calculateLengthBias(int wordCount) {
        if (wordCount <= OPTIMAL_SENTENCE_LENGTH) {
            return 1.0;
        } else if (wordCount <= MAX_SENTENCE_LENGTH) {
            // Gradual penalty for longer sentences
            return 1.0
                    - (0.5 * (wordCount - OPTIMAL_SENTENCE_LENGTH) / (MAX_SENTENCE_LENGTH - OPTIMAL_SENTENCE_LENGTH));
        } else {
            // Heavy penalty for very long sentences
            return 0.5;
        }
    }

    public Map<String, Double> incorporatePOSBias(Map<String, Double> scores, List<String> sentences,
            List<List<String>> taggedSentences) {
        Map<String, Double> adjustedScores = new HashMap<>(scores);

        for (int i = 0; i < sentences.size() && i < taggedSentences.size(); i++) {
            String sentence = sentences.get(i);
            List<String> tags = taggedSentences.get(i);
            double posScore = calculatePOSScore(tags);
            adjustedScores.put(sentence, adjustedScores.get(sentence) * posScore);
        }

        return adjustedScores;
    }

    private double calculatePOSScore(List<String> tags) {
        if (tags.isEmpty()) {
            return 1.0;
        }

        double totalWeight = 0.0;
        int importantTagCount = 0;

        for (String tag : tags) {
            double weight = POS_WEIGHTS.getOrDefault(tag, 0.6); // Better default
            totalWeight += weight;
            if (weight > 1.0) {
                importantTagCount++;
            }
        }

        double averageWeight = totalWeight / tags.size();

        // More sophisticated boost calculation
        double importantTagRatio = (double) importantTagCount / tags.size();
        double diversityBoost = calculateDiversityBoost(tags);
        double combinedBoost = 1.0 + (importantTagRatio * POS_BOOST_FACTOR) + diversityBoost;

        return Math.min(MAX_SCORE_MULTIPLIER, averageWeight * combinedBoost);
    }

    private double calculateDiversityBoost(List<String> tags) {
        Set<String> uniqueImportantTags = new HashSet<>();
        for (String tag : tags) {
            if (POS_WEIGHTS.getOrDefault(tag, 0.0) > 1.0) {
                uniqueImportantTags.add(tag);
            }
        }
        // Boost for having diverse important POS types
        return Math.min(0.2, uniqueImportantTags.size() * 0.05);
    }

    public Map<String, Double> normalizeScores(Map<String, Double> scores) {
        if (scores.isEmpty()) {
            return new HashMap<>();
        }

        double totalScore = scores.values().stream().mapToDouble(Double::doubleValue).sum();
        if (totalScore == 0.0) {
            return scores; // Avoid division by zero
        }

        Map<String, Double> normalizedScores = new HashMap<>();
        for (Map.Entry<String, Double> entry : scores.entrySet()) {
            normalizedScores.put(entry.getKey(), entry.getValue() / totalScore);
        }
        return normalizedScores;
    }

    // Method to clear cache if needed
    public void clearCache() {
        wordCountCache.clear();
    }
}