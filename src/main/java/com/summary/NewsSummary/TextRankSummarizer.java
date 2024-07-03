package com.summary.NewsSummary;

import java.util.*;
import java.util.stream.Collectors;

public class TextRankSummarizer {

    private static final double DAMPING_FACTOR = 0.85;
    private static final int MAX_ITERATIONS = 100;
    private static final double MIN_DIFF = 0.001;

    public List<String> summarize(String text) {
        List<String> sentences = tokenizeSentences(text);
        int numSentences = determineSummaryLength(sentences.size());
        Map<String, Set<String>> graph = buildGraph(sentences);
        Map<String, Double> scores = rankSentences(sentences, graph);
        return selectTopSentences(scores, numSentences);
    }

    private List<String> tokenizeSentences(String text) {
        // Simple tokenizer based on period '.' and newline '\n'
        String[] sentencesArray = text.split("[.!?]\\s+");
        List<String> sentences = new ArrayList<>();
        for (String sentence : sentencesArray) {
            String trimmedSentence = sentence.trim();
            if (!trimmedSentence.isEmpty()) {
                sentences.add(trimmedSentence);
            }
        }
        return sentences;
    }

    private int determineSummaryLength(int numSentencesInText) {
        return (int) Math.ceil(numSentencesInText * 0.5); // Adjust this to set the length of the summary
    }

    private Map<String, Set<String>> buildGraph(List<String> sentences) {
        Map<String, Set<String>> graph = new HashMap<>();
        for (String sentence : sentences) {
            graph.put(sentence, new HashSet<>());
        }
        Map<String, Map<String, Double>> tfidfVectors = calculateTFIDFVectors(sentences);
        for (int i = 0; i < sentences.size(); i++) {
            String sentence1 = sentences.get(i);
            for (int j = i + 1; j < sentences.size(); j++) {
                String sentence2 = sentences.get(j);
                double similarity = cosineSimilarity(tfidfVectors.get(sentence1), tfidfVectors.get(sentence2));
                if (similarity > 0.0) {
                    graph.get(sentence1).add(sentence2);
                    graph.get(sentence2).add(sentence1);
                }
            }
        }
        return graph;
    }

    private Map<String, Map<String, Double>> calculateTFIDFVectors(List<String> sentences) {
        Set<String> vocabulary = new HashSet<>();
        for (String sentence : sentences) {
            String[] words = sentence.split("\\s+");
            Collections.addAll(vocabulary, words);
        }
        Map<String, Map<String, Integer>> tf = new HashMap<>();
        for (String sentence : sentences) {
            tf.put(sentence, new HashMap<>());
            String[] words = sentence.split("\\s+");
            for (String word : words) {
                tf.get(sentence).put(word, tf.get(sentence).getOrDefault(word, 0) + 1);
            }
        }
        Map<String, Double> idf = new HashMap<>();
        for (String word : vocabulary) {
            int docCount = 0;
            for (String sentence : sentences) {
                if (sentence.contains(word)) {
                    docCount++;
                }
            }
            idf.put(word, Math.log((double) sentences.size() / (docCount + 1)));
        }
        Map<String, Map<String, Double>> tfidfVectors = new HashMap<>();
        for (String sentence : sentences) {
            tfidfVectors.put(sentence, new HashMap<>());
            for (String word : vocabulary) {
                double tfidf = tf.get(sentence).getOrDefault(word, 0) * idf.get(word);
                tfidfVectors.get(sentence).put(word, tfidf);
            }
        }
        return tfidfVectors;
    }

    private double cosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
        double dotProduct = 0.0;
        for (Map.Entry<String, Double> entry : vector1.entrySet()) {
            dotProduct += entry.getValue() * vector2.getOrDefault(entry.getKey(), 0.0);
        }
        double magnitude1 = calculateMagnitude(vector1);
        double magnitude2 = calculateMagnitude(vector2);
        if (magnitude1 == 0.0 || magnitude2 == 0.0) {
            return 0.0;
        }
        return dotProduct / (magnitude1 * magnitude2);
    }

    private double calculateMagnitude(Map<String, Double> vector) {
        double magnitude = 0.0;
        for (double value : vector.values()) {
            magnitude += Math.pow(value, 2);
        }
        return Math.sqrt(magnitude);
    }

    private Map<String, Double> rankSentences(List<String> sentences, Map<String, Set<String>> graph) {
        Map<String, Double> scores = initializeScores(sentences);
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            Map<String, Double> newScores = new HashMap<>();
            double maxChange = 0;
            for (String sentence : sentences) {
                double rank = (1 - DAMPING_FACTOR);
                for (String neighbor : graph.get(sentence)) {
                    rank += DAMPING_FACTOR * (scores.get(neighbor) / graph.get(neighbor).size());
                }
                newScores.put(sentence, rank);
                maxChange = Math.max(maxChange, Math.abs(rank - scores.get(sentence)));
            }
            scores = newScores;
            if (maxChange < MIN_DIFF) break;
        }
        return scores;
    }

    private Map<String, Double> initializeScores(List<String> sentences) {
        Map<String, Double> scores = new HashMap<>();
        double initialScore = 1.0 / sentences.size();
        for (String sentence : sentences) {
            scores.put(sentence, initialScore);
        }
        return scores;
    }

    private List<String> selectTopSentences(Map<String, Double> scores, int numSentences) {
        List<Map.Entry<String, Double>> sortedEntries = new ArrayList<>(scores.entrySet());
        sortedEntries.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));
        List<String> topSentences = new ArrayList<>();
        for (int i = 0; i < Math.min(numSentences, sortedEntries.size()); i++) {
            topSentences.add(sortedEntries.get(i).getKey());
        }
        return topSentences;
    }
}
