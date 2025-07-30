package com.summary.eSummarizer.Preprocessor;

import com.summary.eSummarizer.Service.CSVLoaderService;
import com.summary.eSummarizer.Service.LemmatizationService;
import com.summary.eSummarizer.Service.POSService;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class Preprocessor {

    private static final Logger logger = LoggerFactory.getLogger(Preprocessor.class);

    private final Set<String> STOPWORDS;
    private final SentenceDetectorME sentenceDetector;
    private final TokenizerME tokenizer;

    @Autowired
    private LemmatizationService lemmatizationService;

    @Autowired
    private POSService posService;

    @Autowired
    private CSVLoaderService csvLoaderService;

    public Preprocessor(CSVLoaderService csvLoaderService) {
        this.csvLoaderService = csvLoaderService;
        STOPWORDS = loadStopwordsFromCsv("/CSV/stopwords.csv");

        // Debug: Find where models are actually located
        debugModelPaths();

        // OpenNLP models
        this.sentenceDetector = loadSentenceDetector();
        this.tokenizer = loadTokenizer();
    }

    private void debugModelPaths() {
        logger.info("=== Debugging OpenNLP Model Paths ===");
        String[] possiblePaths = {
                "/models/openNLP/en-sent.bin",
                "/models/openNLP/en-token.bin"
        };

        for (String path : possiblePaths) {
            InputStream stream = getClass().getResourceAsStream(path);
            if (stream != null) {
                logger.info("✓ FOUND model at: {}", path);
                try {
                    stream.close();
                } catch (Exception e) {
                }
            } else {
                logger.info("✗ NOT found at: {}", path);
            }
        }
        logger.info("=== End Debug ===");
    }

    private SentenceDetectorME loadSentenceDetector() {
        try (InputStream modelIn = getClass().getResourceAsStream("/models/openNLP/en-sent.bin")) {
            if (modelIn == null) {
                throw new RuntimeException(
                        "Sentence detection model not found at /models/openNLP/en-sent.bin");
            }
            logger.info("✓ Loading sentence detector from: /models/opennlp-en-ud-ewt-sentence-1.3-2.5.4.bin");
            SentenceModel model = new SentenceModel(modelIn);
            return new SentenceDetectorME(model);
        } catch (IOException e) {
            throw new RuntimeException("Error loading sentence detection model: " + e.getMessage(), e);
        }
    }

    private TokenizerME loadTokenizer() {
        try (InputStream modelIn = getClass().getResourceAsStream("/models/openNLP/en-token.bin")) {
            if (modelIn == null) {
                throw new RuntimeException(
                        "Tokenizer model not found at /models/opennlp-en-ud-ewt-tokens-1.3-2.5.4.bin");
            }
            logger.info("✓ Loading tokenizer from: /models/opennlp-en-ud-ewt-tokens-1.3-2.5.4.bin");
            TokenizerModel model = new TokenizerModel(modelIn);
            return new TokenizerME(model);
        } catch (IOException e) {
            throw new RuntimeException("Error loading tokenizer model: " + e.getMessage(), e);
        }
    }

    private Set<String> loadStopwordsFromCsv(String filename) {
        return csvLoaderService.loadAsSet(filename);
    }

    // Enhanced sentence tokenization using OpenNLP
    public List<String> tokenizeSentences(String text) {
        String[] sentences = sentenceDetector.sentDetect(text);
        List<String> sentenceList = Arrays.asList(sentences);

        // logger.info("=== Sentence Tokenization ===");
        // logger.info("Total sentences detected: {}", sentenceList.size());
        // for (int i = 0; i < sentenceList.size(); i++) {
        // logger.info("Sentence {}: {}", i + 1, sentenceList.get(i));
        // }
        // logger.info("=== End Sentence Tokenization ===");

        return sentenceList;
    }

    // Enhanced word tokenization using OpenNLP
    public List<String> tokenizeWords(String sentence) {
        String[] tokens = tokenizer.tokenize(sentence);
        List<String> wordList = Arrays.stream(tokens)
                .map(String::toLowerCase)
                .filter(token -> !token.trim().isEmpty())
                .collect(Collectors.toList());

        // logger.info("=== Word Tokenization ===");
        // logger.info("Input sentence: {}", sentence);
        // logger.info("Total words tokenized: {}", wordList.size());
        // logger.info("Tokenized words: {}", wordList);
        // logger.info("=== End Word Tokenization ===");

        return wordList;
    }

    // Remove stopwords and apply lemmatization on a list of sentences
    public List<String> removeStopwordsAndLemmatize(List<String> sentences) {
        logger.info("=== Starting Stopword Removal and Lemmatization ===");
        logger.info("Input sentences count: {}", sentences.size());

        // Use an array to hold the counts
        final int[] stopwordCount = { 0 }; // Array to hold the number of stopwords removed
        final int[] lemmatizedCount = { 0 }; // Array to hold the total lemmatized words
        final int[] sentenceIndex = { 0 }; // Track sentence index

        List<String> processed = sentences.stream().map(sentence -> {
            boolean isFirstSentence = sentenceIndex[0] == 0;

            if (isFirstSentence) {
                logger.info("Processing first sentence (detailed): {}", sentence);
            }

            List<String> words = tokenizeWords(sentence);

            if (isFirstSentence) {
                logger.info("Words after tokenization: {}", words);
            }

            List<String> filteredWords = words.stream()
                    .filter(word -> {
                        boolean isStopword = STOPWORDS.contains(word);
                        if (isStopword) {
                            stopwordCount[0]++; // Increment stopword count
                            if (isFirstSentence) {
                                logger.debug("Removed stopword: {}", word);
                            }
                        }
                        return !isStopword; // Keep non-stopwords
                    }).collect(Collectors.toList()); // Collect filtered words

            if (isFirstSentence) {
                logger.info("Words after stopword removal: {}", filteredWords);
            }

            List<String> lemmatizedWords = filteredWords.stream()
                    .map(word -> {
                        String lemmatized = lemmatizationService.lemmatize(word);
                        if (!word.equals(lemmatized) && isFirstSentence) {
                            logger.debug("Lemmatized: {} -> {}", word, lemmatized);
                        }
                        return lemmatized;
                    })
                    .collect(Collectors.toList()); // Collect lemmatized words
            lemmatizedCount[0] += lemmatizedWords.size(); // Increment lemmatized words count

            String processedSentence = String.join(" ", lemmatizedWords);

            if (isFirstSentence) {
                logger.info("Processed first sentence: {}", processedSentence);
            }

            sentenceIndex[0]++; // Increment sentence index
            return processedSentence; // Reconstruct the processed sentence
        }).collect(Collectors.toList());

        // Log the summary after processing all sentences
        logger.info("=== Stopword Removal and Lemmatization Summary ===");
        logger.info("Total sentences processed: {}", sentences.size());
        logger.info("Total stopwords removed: {}", stopwordCount[0]);
        logger.info("Total lemmatized words: {}", lemmatizedCount[0]);
        logger.info("=== End Stopword Removal and Lemmatization ===");

        return processed; // Return processed sentences
    }

    // method to tag parts of speech (POS) for each word in the sentences
    public List<List<String>> tagPartsOfSpeech(List<String> sentences) {
        logger.info("=== Starting POS Tagging ===");
        logger.info("Input sentences count: {}", sentences.size());

        final int[] taggedWordCount = { 0 }; // Array to hold the total tagged words

        List<List<String>> taggedSentences = sentences.stream().map(sentence -> {
            logger.info("POS tagging sentence: {}", sentence);

            List<String> words = tokenizeWords(sentence);
            List<String> taggedWords = words.stream()
                    .map(word -> {
                        String pos = posService.getPartOfSpeech(word);
                        String taggedWord = word + "|" + pos; // Tag word with POS
                        taggedWordCount[0]++; // Increment tagged words count
                        logger.debug("POS tagged: {} -> {}", word, taggedWord);
                        return taggedWord;
                    })
                    .collect(Collectors.toList());

            logger.info("Tagged words for sentence: {}", taggedWords);
            return taggedWords;
        }).collect(Collectors.toList());

        // Log the summary for POS tagging
        logger.info("=== POS Tagging Summary ===");
        logger.info("Total POS tagged words: {}", taggedWordCount[0]);
        logger.info("Final tagged sentences: {}", taggedSentences);
        logger.info("=== End POS Tagging ===");

        return taggedSentences;
    }
}