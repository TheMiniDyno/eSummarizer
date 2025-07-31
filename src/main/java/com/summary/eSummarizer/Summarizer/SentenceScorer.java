package com.summary.eSummarizer.Summarizer;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class SentenceScorer {

    private static final Map<String, Double> POS_WEIGHTS;

    static {
        Map<String, Double> weights = new HashMap<>();
        weights.put("NN", 1.2); // Noun - high importance
        weights.put("NNS", 1.2); // Plural noun
        weights.put("NNP", 1.5); // Proper noun - highest importance (names, places)
        weights.put("NNPS", 1.5); // Plural proper noun
        weights.put("VB", 1.0); // Verb - medium importance
        weights.put("VBD", 1.0); // Past tense verb
        weights.put("VBG", 1.0); // Gerund
        weights.put("VBN", 1.0); // Past participle
        weights.put("VBP", 1.0); // Present tense verb
        weights.put("VBZ", 1.0); // 3rd person singular present
        weights.put("JJ", 0.8); // Adjective - lower importance
        weights.put("JJR", 0.8); // Comparative adjective
        weights.put("JJS", 0.8); // Superlative adjective
        weights.put("CD", 1.1); // Cardinal number - often important facts
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
        Map<String, Double> normalizedScores = new HashMap<>();
        for (String sentence : scores.keySet()) {
            normalizedScores.put(sentence, scores.get(sentence) / totalScore);
        }
        return normalizedScores;
    }
}