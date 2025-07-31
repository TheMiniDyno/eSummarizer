package com.summary.eSummarizer.Summarizer;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class TFIDFCalculator {

    public Map<String, Map<String, Double>> calculateTFIDFVectors(List<String> sentences) {
        Set<String> vocabulary = buildVocabulary(sentences);
        Map<String, Map<String, Integer>> tf = calculateTermFrequency(sentences);
        Map<String, Double> idf = calculateInverseDocumentFrequency(sentences, vocabulary);

        return buildTFIDFVectors(sentences, vocabulary, tf, idf);
    }

    private Set<String> buildVocabulary(List<String> sentences) {
        Set<String> vocabulary = new HashSet<>();
        for (String sentence : sentences) {
            String[] words = sentence.toLowerCase().split("\\s+");
            Collections.addAll(vocabulary, words);
        }
        return vocabulary;
    }

    private Map<String, Map<String, Integer>> calculateTermFrequency(List<String> sentences) {
        Map<String, Map<String, Integer>> tf = new HashMap<>();
        for (String sentence : sentences) {
            tf.put(sentence, new HashMap<>());
            String[] words = sentence.toLowerCase().split("\\s+");
            for (String word : words) {
                tf.get(sentence).put(word, tf.get(sentence).getOrDefault(word, 0) + 1);
            }
        }
        return tf;
    }

    private Map<String, Double> calculateInverseDocumentFrequency(List<String> sentences, Set<String> vocabulary) {
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
        return idf;
    }

    private Map<String, Map<String, Double>> buildTFIDFVectors(List<String> sentences, Set<String> vocabulary,
                                                               Map<String, Map<String, Integer>> tf, Map<String, Double> idf) {
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
}