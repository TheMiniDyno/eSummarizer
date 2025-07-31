package com.summary.eSummarizer.Summarizer;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class TextRankAlgorithm {

    private static final double DAMPING_FACTOR = 0.85;
    private static final int MAX_ITERATIONS = 100;
    private static final double MIN_DIFF = 0.001;

    public Map<String, Double> rankSentences(List<String> sentences, Map<String, Set<String>> graph) {
        Map<String, Double> scores = initializeScores(sentences);

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            Map<String, Double> newScores = calculateNewScores(sentences, graph, scores);

            double maxDiff = calculateMaxDifference(sentences, scores, newScores);
            scores = newScores;

            if (maxDiff < MIN_DIFF) {
                break;
            }
        }
        return scores;
    }

    private Map<String, Double> initializeScores(List<String> sentences) {
        Map<String, Double> scores = new HashMap<>();
        for (String sentence : sentences) {
            scores.put(sentence, 1.0);
        }
        return scores;
    }

    private Map<String, Double> calculateNewScores(List<String> sentences, Map<String, Set<String>> graph,
                                                   Map<String, Double> currentScores) {
        Map<String, Double> newScores = new HashMap<>();
        for (String sentence : sentences) {
            double score = (1 - DAMPING_FACTOR);
            for (String neighbor : graph.get(sentence)) {
                score += DAMPING_FACTOR * (currentScores.get(neighbor) / graph.get(neighbor).size());
            }
            newScores.put(sentence, score);
        }
        return newScores;
    }

    private double calculateMaxDifference(List<String> sentences, Map<String, Double> oldScores,
                                          Map<String, Double> newScores) {
        double maxDiff = 0.0;
        for (String sentence : sentences) {
            maxDiff = Math.max(maxDiff, Math.abs(newScores.get(sentence) - oldScores.get(sentence)));
        }
        return maxDiff;
    }
}