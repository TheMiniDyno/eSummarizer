package com.summary.eSummarizer.Summarizer;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class SummarySelector {

    public List<String> selectTopSentences(Map<String, Double> scores, int numSentences,
            List<String> originalSentences, List<String> processedSentences) {

        // Sort sentences by score (highest first) to get the best ones
        List<Map.Entry<String, Double>> sortedScores = new ArrayList<>(scores.entrySet());
        sortedScores.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        // 2.Top-scored sentences in their original positions
        Set<Integer> selectedIndices = new HashSet<>();
        for (int i = 0; i < Math.min(numSentences, sortedScores.size()); i++) {
            String processedSentence = sortedScores.get(i).getKey();
            int originalIndex = processedSentences.indexOf(processedSentence);
            if (originalIndex != -1) {
                selectedIndices.add(originalIndex);
            }
        }

        // 3.Summary maintaining original document order
        List<String> summarizedSentences = new ArrayList<>();
        for (int i = 0; i < originalSentences.size(); i++) {
            if (selectedIndices.contains(i)) {
                summarizedSentences.add(originalSentences.get(i));
            }
        }

        System.out.println("\n=== SUMMARY SELECTION DEBUG ===");
        System.out.println("Selected indices (in original order): " +
                selectedIndices.stream().sorted().toList());
        System.out.println("Total sentences selected: " + summarizedSentences.size());
        System.out.println("Summary maintains original order: YES");

        return summarizedSentences;
    }

    public int determineSummaryLength(int numSentencesInText) {
        if (numSentencesInText <= 3) {
            return Math.max(1, numSentencesInText - 1);
        } else if (numSentencesInText <= 10) {
            return Math.max(3, numSentencesInText / 2);
        } else {
            return Math.max(5, numSentencesInText / 3);
        }
    }
}