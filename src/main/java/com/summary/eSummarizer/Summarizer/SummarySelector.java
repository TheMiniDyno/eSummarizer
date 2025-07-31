package com.summary.eSummarizer.Summarizer;

import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class SummarySelector {

    public List<String> selectTopSentences(Map<String, Double> scores, int numSentences,
                                           List<String> originalSentences, List<String> processedSentences) {
        List<Map.Entry<String, Double>> sortedScores = new ArrayList<>(scores.entrySet());
        sortedScores.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        List<String> summarizedSentences = new ArrayList<>();
        for (int i = 0; i < Math.min(numSentences, sortedScores.size()); i++) {
            String sentence = sortedScores.get(i).getKey();
            summarizedSentences.add(originalSentences.get(processedSentences.indexOf(sentence)));
        }
        return summarizedSentences;
    }

    public int determineSummaryLength(int numSentencesInText) {
        return Math.max(1, (int) Math.ceil(numSentencesInText * 0.6));
    }
}