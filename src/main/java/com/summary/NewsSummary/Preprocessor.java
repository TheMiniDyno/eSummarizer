package com.summary.NewsSummary;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class Preprocessor {

    public List<String> tokenizeSentences(String text) {
        // Split text into sentences using regular expression
        String[] sentences = text.split("(?<!Mr|Mrs|Ms|Dr|\\b[A-Z])\\.\\s+");
        List<String> result = new ArrayList<>();
        for (String sentence : sentences) {
            result.add(sentence.trim());
        }
        return result;
    }

    public List<String> tokenizeWords(String sentence) {
        // Split sentence into words
        StringTokenizer tokenizer = new StringTokenizer(sentence, " .,;:!?()[]\"");
        List<String> words = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            words.add(tokenizer.nextToken().toLowerCase());
        }
        return words;
    }

    public List<String> preprocess(String text) {
        return tokenizeSentences(text);
    }
}
