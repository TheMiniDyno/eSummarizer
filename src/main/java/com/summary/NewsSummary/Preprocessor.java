package com.summary.NewsSummary;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

@Service
public class Preprocessor {

    // Split text into sentences
    public List<String> tokenizeSentences(String text) {
        // Split text into sentences using regular expression
        String[] sentences = text.split("(?<!Mr|Mrs|Ms|Dr|\\b[A-Z])\\.\\s+");
        List<String> result = new ArrayList<>();
        for (String sentence : sentences) {
            result.add(sentence.trim());
        }
        return result;
    }

    // Split sentence into words
    public List<String> tokenizeWords(String sentence) {
        StringTokenizer tokenizer = new StringTokenizer(sentence, " .,;:!?()[]\"");
        List<String> words = new ArrayList<>();
        while (tokenizer.hasMoreTokens()) {
            words.add(tokenizer.nextToken().toLowerCase());
        }
        return words;
    }

    // Preprocess the text
    public List<String> preprocess(String text) {
        return tokenizeSentences(text);
    }
}