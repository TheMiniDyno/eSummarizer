package com.summary.eSummarizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class TextRankSummarizer {

    @Autowired
    private Preprocessor preprocessor;

    private static final double DAMPING_FACTOR = 0.85;
    private static final int MAX_ITERATIONS = 100;
    private static final double MIN_DIFF = 0.001;

    public SummaryInfo summarize(String text) {
        List<String> originalSentences = preprocessor.tokenizeSentences(text);
        List<String> processedSentences = preprocessor.removeStopwordsAndLemmatize(originalSentences);

        int originalSentenceCount = originalSentences.size();
        int originalWordCount = countWords(text);

        int numSentences = determineSummaryLength(originalSentences.size());

        Map<String, Map<String, Double>> tfidfVectors = calculateTFIDFVectors(processedSentences);
        double similarityThreshold = determineDynamicThreshold(processedSentences, tfidfVectors);
        Map<String, Set<String>> graph = buildGraph(processedSentences, similarityThreshold, tfidfVectors);

        // Rank sentences using the graph
        Map<String, Double> scores = rankSentences(processedSentences, graph);

        // Apply sentence position bias
        scores = incorporatePositionBias(scores, processedSentences);

        // Adjust scores based on sentence length
        scores = adjustForSentenceLength(scores, processedSentences);

        // Select the top sentences for the summary
        List<String> summarizedSentences = selectTopSentences(scores, numSentences, originalSentences, processedSentences);

        List<SentenceRank> sentenceRanks = new ArrayList<>();
        for (int i = 0; i < originalSentences.size(); i++) {
            String originalSentence = originalSentences.get(i);
            double rank = scores.getOrDefault(processedSentences.get(i), 0.0);
            sentenceRanks.add(new SentenceRank(originalSentence, rank));
        }
        sentenceRanks.sort((a, b) -> Double.compare(b.getRank(), a.getRank())); // Sort by rank descending

        String summarizedText = String.join(" ", summarizedSentences).replaceAll("\\s+", " ")
                .replaceAll("\\.\\s*\\.", ".").trim();

        int summarizedWordCount = countWords(summarizedText);
        double reductionRate = 1 - ((double) summarizedWordCount / originalWordCount);

        List<GraphNode> graphNodes = new ArrayList<>();
        List<GraphLink> graphLinks = new ArrayList<>();

        for (int i = 0; i < processedSentences.size(); i++) {
            graphNodes.add(new GraphNode(i + 1, scores.get(processedSentences.get(i))));
        }

        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            int sourceId = processedSentences.indexOf(entry.getKey()) + 1;
            for (String neighbor : entry.getValue()) {
                int targetId = processedSentences.indexOf(neighbor) + 1;
                graphLinks.add(new GraphLink(sourceId, targetId));
            }
        }

        SummaryInfo summaryInfo = new SummaryInfo(
                summarizedText,
                originalSentenceCount,
                summarizedSentences.size(),
                originalWordCount,
                summarizedWordCount,
                reductionRate);

        summaryInfo.setSentenceRanks(sentenceRanks);
        summaryInfo.setGraphData(graphNodes, graphLinks);

        return summaryInfo;
    }

    private int countWords(String text) {
        return text.split("\\s+").length;
    }

    private int determineSummaryLength(int numSentencesInText) {
        return Math.max(1, (int) Math.ceil(numSentencesInText * 0.5));
    }

    private double determineDynamicThreshold(List<String> sentences, Map<String, Map<String, Double>> tfidfVectors) {
        List<Double> similarities = new ArrayList<>();

        for (int i = 0; i < sentences.size(); i++) {
            String sentence1 = sentences.get(i);
            for (int j = i + 1; j < sentences.size(); j++) {
                String sentence2 = sentences.get(j);
                double similarity = cosineSimilarity(tfidfVectors.get(sentence1), tfidfVectors.get(sentence2));
                similarities.add(similarity);
            }
        }

        Collections.sort(similarities);
        double percentile = 0.50;
        int percentileIndex = (int) (similarities.size() * percentile);
        return similarities.get(percentileIndex);
    }

    private Map<String, Set<String>> buildGraph(List<String> sentences, double similarityThreshold, Map<String, Map<String, Double>> tfidfVectors) {
        Map<String, Set<String>> graph = new HashMap<>();
        for (String sentence : sentences) {
            graph.put(sentence, new HashSet<>());
        }

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

    private Map<String, Map<String, Double>> calculateTFIDFVectors(List<String> sentences) {
        Set<String> vocabulary = new HashSet<>();
        for (String sentence : sentences) {
            String[] words = sentence.toLowerCase().split("\\s+");
            Collections.addAll(vocabulary, words);
        }
        Map<String, Map<String, Integer>> tf = new HashMap<>();
        for (String sentence : sentences) {
            tf.put(sentence, new HashMap<>());
            String[] words = sentence.toLowerCase().split("\\s+");
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
        double norm1 = 0.0;
        double norm2 = 0.0;
        Set<String> allWords = new HashSet<>(vector1.keySet());
        allWords.addAll(vector2.keySet());

        for (String word : allWords) {
            double v1 = vector1.getOrDefault(word, 0.0);
            double v2 = vector2.getOrDefault(word, 0.0);
            dotProduct += v1 * v2;
            norm1 += v1 * v1;
            norm2 += v2 * v2;
        }

        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);

        return (norm1 == 0.0 || norm2 == 0.0) ? 0.0 : dotProduct / (norm1 * norm2);
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
            if (maxChange < MIN_DIFF) {
                break;
            }
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

    private Map<String, Double> incorporatePositionBias(Map<String, Double> scores, List<String> sentences) {
        double totalSentences = sentences.size();
        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            double positionBias = (totalSentences - i) / totalSentences; // Higher bias for earlier sentences
            scores.put(sentence, scores.get(sentence) + positionBias);
        }
        return scores;
    }

    private Map<String, Double> adjustForSentenceLength(Map<String, Double> scores, List<String> sentences) {
        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            double lengthFactor = Math.log(sentence.split("\\s+").length + 1);
            scores.put(sentence, scores.get(sentence) * lengthFactor);
        }
        return scores;
    }

    private List<String> selectTopSentences(Map<String, Double> scores, int numSentences, List<String> originalSentences, List<String> processedSentences) {
        List<Map.Entry<String, Double>> sortedSentences = new ArrayList<>(scores.entrySet());
        sortedSentences.sort((a, b) -> Double.compare(b.getValue(), a.getValue())); // Sort by score descending

        List<String> topSentences = new ArrayList<>();
        Set<String> selectedProcessedSentences = new HashSet<>();

        for (int i = 0; i < Math.min(numSentences, sortedSentences.size()); i++) {
            selectedProcessedSentences.add(sortedSentences.get(i).getKey());
        }

        for (int i = 0; i < originalSentences.size(); i++) {
            if (selectedProcessedSentences.contains(processedSentences.get(i))) {
                topSentences.add(originalSentences.get(i));
            }
        }

        return topSentences;
    }
}
