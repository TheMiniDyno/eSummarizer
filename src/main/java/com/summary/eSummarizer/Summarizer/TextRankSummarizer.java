package com.summary.eSummarizer.Summarizer;

import com.summary.eSummarizer.Preprocessor.Preprocessor;
import com.summary.eSummarizer.Service.POSService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@Service
public class TextRankSummarizer {
    private static final Logger logger = LoggerFactory.getLogger(TextRankSummarizer.class);

    @Autowired
    private Preprocessor preprocessor;

    @Autowired
    private POSService posService;

    private static final double DAMPING_FACTOR = 0.85;
    private static final int MAX_ITERATIONS = 100;
    private static final double MIN_DIFF = 0.001;

    /**
     * Summarizes the given text using the TextRank algorithm.
     *
     * @param text The input text to be summarized.
     * @return SummaryInfo containing the summarized text and related metrics.
     */
    public SummaryInfo summarize(String text) {
        logger.info("Starting summarization process for text of length: {}", text.length());

        // Tokenize and preprocess the text
        List<String> originalSentences = preprocessor.tokenizeSentences(text);
        logger.info("Tokenized {} sentences", originalSentences.size());

        List<String> processedSentences = preprocessor.removeStopwordsAndLemmatize(originalSentences);

        List<List<String>> taggedSentences = preprocessor.tagPartsOfSpeech(processedSentences);

        int originalSentenceCount = originalSentences.size();
        int originalWordCount = countWords(text);

        // Determine the number of sentences for the summary
        int numSentences = determineSummaryLength(originalSentences.size());

        // Calculate TF-IDF vectors and build the similarity graph
        Map<String, Map<String, Double>> tfidfVectors = calculateTFIDFVectors(processedSentences);
        double similarityThreshold = determineDynamicThreshold(processedSentences, tfidfVectors);
        Map<String, Set<String>> graph = buildGraph(processedSentences, similarityThreshold, tfidfVectors);

        // Rank sentences using the graph and POS information
        Map<String, Double> scores = rankSentences(processedSentences, graph, taggedSentences);

        // Apply position and length biases to scores
        scores = incorporatePositionBias(scores, processedSentences);
        scores = adjustForSentenceLength(scores, processedSentences);

        // Normalize the scores
        scores = normalizeScores(scores);

        // Select the top sentences for the summary
        List<String> summarizedSentences = selectTopSentences(scores, numSentences, originalSentences, processedSentences);

        // Prepare the ranking and summary information
        List<SentenceRank> sentenceRanks = new ArrayList<>();
        for (int i = 0; i < originalSentences.size(); i++) {
            String originalSentence = originalSentences.get(i);
            double rank = scores.getOrDefault(processedSentences.get(i), 0.0);
            sentenceRanks.add(new SentenceRank(originalSentence, rank));
        }

        // Sort sentences by rank in descending order
        sentenceRanks.sort((a, b) -> Double.compare(b.getRank(), a.getRank()));

        // Generate summarized text
        String summarizedText = String.join(" ", summarizedSentences).replaceAll("\\s+", " ")
                .replaceAll("\\.\\s*\\.", ".").trim();

        // Count summarized words and calculate reduction rate
        int summarizedWordCount = countWords(summarizedText);
        double reductionRate = 1 - ((double) summarizedWordCount / originalWordCount);

        // Return the summary information
        SummaryInfo summaryInfo = new SummaryInfo(
                summarizedText,
                originalSentenceCount,
                summarizedSentences.size(),
                originalWordCount,
                summarizedWordCount,
                reductionRate
        );
        summaryInfo.setSentenceRanks(sentenceRanks);

        // Log completion of summarization
        logger.info("Summarization complete. Original sentences: {}, Summarized sentences: {}",
                originalSentenceCount, summarizedSentences.size());

        // Graph data processing
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
        summaryInfo.setGraphData(graphNodes, graphLinks);

        return summaryInfo;
    }

    /**
     * Counts the number of words in the given text.
     *
     * @param text The input text.
     * @return The word count.
     */
    private int countWords(String text) {
        return text.split("\\s+").length;
    }

    /**
     * Determines the number of sentences to include in the summary.
     *
     * @param numSentencesInText The total number of sentences in the input text.
     * @return The number of sentences for the summary.
     */
    private int determineSummaryLength(int numSentencesInText) {
        return Math.max(1, (int) Math.ceil(numSentencesInText * 0.6));
    }

    /**
     * Calculates the TF-IDF vectors for each sentence in the input list.
     *
     * @param sentences The list of sentences.
     * @return A map containing TF-IDF vectors for each sentence.
     */
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

    /**
     * Determines a dynamic similarity threshold based on sentence similarities.
     *
     * @param sentences      The list of sentences.
     * @param tfidfVectors   The TF-IDF vectors for the sentences.
     * @return The calculated similarity threshold.
     */
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
        double percentile = 0.50; // 50th percentile
        int percentileIndex = (int) (similarities.size() * percentile);
        return similarities.get(percentileIndex);
    }

    /**
     * Builds a similarity graph based on TF-IDF similarity.
     *
     * @param sentences            The list of sentences.
     * @param similarityThreshold   The threshold for considering sentences similar.
     * @param tfidfVectors        The TF-IDF vectors for the sentences.
     * @return A map representing the similarity graph.
     */
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

    /**
     * Calculates the cosine similarity between two vectors.
     *
     * @param vector1 The first vector.
     * @param vector2 The second vector.
     * @return The cosine similarity between the two vectors.
     */
    private double cosineSimilarity(Map<String, Double> vector1, Map<String, Double> vector2) {
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

    /**
     * Ranks sentences based on the similarity graph and POS information.
     *
     * @param sentences      The list of processed sentences.
     * @param graph         The similarity graph.
     * @param taggedSentences The POS-tagged sentences.
     * @return A map containing the scores for each sentence.
     */
    private Map<String, Double> rankSentences(List<String> sentences, Map<String, Set<String>> graph, List<List<String>> taggedSentences) {
        Map<String, Double> scores = new HashMap<>();
        for (String sentence : sentences) {
            scores.put(sentence, 1.0); // Initialize scores
        }

        for (int iteration = 0; iteration < MAX_ITERATIONS; iteration++) {
            Map<String, Double> newScores = new HashMap<>();
            for (String sentence : sentences) {
                double score = (1 - DAMPING_FACTOR);
                for (String neighbor : graph.get(sentence)) {
                    score += DAMPING_FACTOR * (scores.get(neighbor) / graph.get(neighbor).size());
                }
                newScores.put(sentence, score);
            }

            double maxDiff = 0.0;
            for (String sentence : sentences) {
                maxDiff = Math.max(maxDiff, Math.abs(newScores.get(sentence) - scores.get(sentence)));
            }
            scores = newScores;

            if (maxDiff < MIN_DIFF) {
                break;
            }
        }
        return scores;
    }

    /**
     * Incorporates position bias into the sentence scores.
     *
     * @param scores       The current scores of the sentences.
     * @param sentences    The list of processed sentences.
     * @return The adjusted scores with position bias.
     */
    private Map<String, Double> incorporatePositionBias(Map<String, Double> scores, List<String> sentences) {
        for (int i = 0; i < sentences.size(); i++) {
            String sentence = sentences.get(i);
            double positionBias = 1.0 - ((double) i / sentences.size()); // Higher score for earlier sentences
            scores.put(sentence, scores.get(sentence) * positionBias);
        }
        return scores;
    }

    /**
     * Adjusts scores based on sentence length.
     *
     * @param scores       The current scores of the sentences.
     * @param sentences    The list of processed sentences.
     * @return The adjusted scores with length bias.
     */
    private Map<String, Double> adjustForSentenceLength(Map<String, Double> scores, List<String> sentences) {
        for (String sentence : sentences) {
            double lengthBias = 1.0 - ((double) sentence.split("\\s+").length / 30); // Penalty for longer sentences
            scores.put(sentence, scores.get(sentence) * lengthBias);
        }
        return scores;
    }

    /**
     * Normalizes the sentence scores to ensure they sum to 1.
     *
     * @param scores The current scores of the sentences.
     * @return The normalized scores.
     */
    private Map<String, Double> normalizeScores(Map<String, Double> scores) {
        double totalScore = scores.values().stream().mapToDouble(Double::doubleValue).sum();
        for (String sentence : scores.keySet()) {
            scores.put(sentence, scores.get(sentence) / totalScore);
        }
        return scores;
    }

    /**
     * Selects the top sentences for the summary based on their scores.
     *
     * @param scores             The scores of the sentences.
     * @param numSentences       The number of sentences to include in the summary.
     * @param originalSentences  The list of original sentences.
     * @param processedSentences The list of processed sentences.
     * @return The selected top sentences.
     */
    private List<String> selectTopSentences(Map<String, Double> scores, int numSentences, List<String> originalSentences, List<String> processedSentences) {
        List<Map.Entry<String, Double>> sortedScores = new ArrayList<>(scores.entrySet());
        sortedScores.sort((a, b) -> Double.compare(b.getValue(), a.getValue())); // Sort by score

        List<String> summarizedSentences = new ArrayList<>();
        Set<String> selectedSentences = new HashSet<>();

        for (int i = 0; i < Math.min(numSentences, sortedScores.size()); i++) {
            String sentence = sortedScores.get(i).getKey();
            summarizedSentences.add(originalSentences.get(processedSentences.indexOf(sentence))); // Retrieve the original sentence
            selectedSentences.add(sentence);
        }
        return summarizedSentences;
    }
}
