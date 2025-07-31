package com.summary.eSummarizer.Summarizer;

import com.summary.eSummarizer.DTO.GraphLink;
import com.summary.eSummarizer.DTO.GraphNode;
import com.summary.eSummarizer.DTO.SentenceRank;
import com.summary.eSummarizer.DTO.SummaryInfo;
import com.summary.eSummarizer.Preprocessor.Preprocessor;
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
    private TFIDFCalculator tfidfCalculator;

    @Autowired
    private SimilarityGraphBuilder graphBuilder;

    @Autowired
    private TextRankAlgorithm textRankAlgorithm;

    @Autowired
    private SentenceScorer sentenceScorer;

    @Autowired
    private SummarySelector summarySelector;

    @Autowired
    private GraphDataProcessor graphDataProcessor;

    public SummaryInfo summarize(String text) {
        logger.info("Starting summarization process for text of length: {}", text.length());

        // Tokenize text
        List<String> originalSentences = preprocessor.tokenizeSentences(text);
        logger.info("Tokenized {} sentences", originalSentences.size());

        List<String> processedSentences = preprocessor.removeStopwordsAndLemmatize(originalSentences);

        List<List<String>> taggedSentences = preprocessor.tagPartsOfSpeech(processedSentences);

        int originalSentenceCount = originalSentences.size();
        int originalWordCount = countWords(text);

        // Number of sentences for the summary
        int numSentences = summarySelector.determineSummaryLength(originalSentences.size());

        // Calculate TF-IDF vectors and build the similarity graph
        Map<String, Map<String, Double>> tfidfVectors = tfidfCalculator.calculateTFIDFVectors(processedSentences);
        double similarityThreshold = graphBuilder.determineDynamicThreshold(processedSentences, tfidfVectors);
        Map<String, Set<String>> graph = graphBuilder.buildGraph(processedSentences, similarityThreshold, tfidfVectors);

        // Rank sentences using the graph
        Map<String, Double> scores = textRankAlgorithm.rankSentences(processedSentences, graph);

        // Apply position and length biases to scores
        scores = sentenceScorer.incorporatePositionBias(scores, processedSentences);
        scores = sentenceScorer.adjustForSentenceLength(scores, processedSentences);

        // POS-based scoring
        scores = sentenceScorer.incorporatePOSBias(scores, processedSentences, taggedSentences);

        // Normalize the scores
        scores = sentenceScorer.normalizeScores(scores);

        // Select the top sentences for the summary
        List<String> summarizedSentences = summarySelector.selectTopSentences(scores, numSentences, originalSentences,
                processedSentences);

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
                reductionRate);
        summaryInfo.setSentenceRanks(sentenceRanks);

        // Log completion of summarization
        logger.info("Summarization complete. Original sentences: {}, Summarized sentences: {}",
                originalSentenceCount, summarizedSentences.size());

        List<GraphNode> graphNodes = graphDataProcessor.createGraphNodes(processedSentences, scores);
        List<GraphLink> graphLinks = graphDataProcessor.createGraphLinks(graph, processedSentences);
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
}
