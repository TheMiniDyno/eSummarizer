package com.summary.eSummarizer.Summarizer;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class SimilarityGraphBuilder {

    public Map<String, Set<String>> buildGraph(List<String> sentences, double similarityThreshold,
                                               Map<String, Map<String, Double>> tfidfVectors) {
        Map<String, Set<String>> graph = initializeGraph(sentences);

        for (int i = 0; i < sentences.size(); i++) {
            String sentence1 = sentences.get(i);
            for (int j = i + 1; j < sentences.size(); j++) {
                String sentence2 = sentences.get(j);
                double similarity = cosineSimilarity(tfidfVectors.get(sentence1), tfidfVectors.get(sentence2));
                if (similarity > similarityThreshold) {
                    graph.get(sentence1).add(sentence2);
                    graph.get(sentence2).add(sentence1);
                }
            }
        }
        return graph;
    }

    public double determineDynamicThreshold(List<String> sentences, Map<String, Map<String, Double>> tfidfVectors) {
        List<Double> similarities = calculateAllSimilarities(sentences, tfidfVectors);
        Collections.sort(similarities);
        double percentile = 0.50; // 50th percentile
        int percentileIndex = (int) (similarities.size() * percentile);
        return similarities.get(percentileIndex);
    }

    public double cosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;

        for (String key : vector1.keySet()) {
            dotProduct += vector1.get(key) * vector2.getOrDefault(key, 0.0);
            magnitude1 += Math.pow(vector1.get(key), 2);
        }
        for (double value : vector2.values()) {
            magnitude2 += Math.pow(value, 2);
        }

        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);

        if (magnitude1 == 0 || magnitude2 == 0) {
            return 0.0;
        }
        return dotProduct / (magnitude1 * magnitude2);
    }

    private Map<String, Set<String>> initializeGraph(List<String> sentences) {
        Map<String, Set<String>> graph = new HashMap<>();
        for (String sentence : sentences) {
            graph.put(sentence, new HashSet<>());
        }
        return graph;
    }

    private List<Double> calculateAllSimilarities(List<String> sentences, Map<String, Map<String, Double>> tfidfVectors) {
        List<Double> similarities = new ArrayList<>();
        for (int i = 0; i < sentences.size(); i++) {
            String sentence1 = sentences.get(i);
            for (int j = i + 1; j < sentences.size(); j++) {
                String sentence2 = sentences.get(j);
                double similarity = cosineSimilarity(tfidfVectors.get(sentence1), tfidfVectors.get(sentence2));
                similarities.add(similarity);
            }
        }
        return similarities;
    }
}