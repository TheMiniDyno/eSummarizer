package com.summary.eSummarizer.Summarizer;

import com.summary.eSummarizer.DTO.GraphLink;
import com.summary.eSummarizer.DTO.GraphNode;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class GraphDataProcessor {
    public List<GraphNode> createGraphNodes(List<String> processedSentences, Map<String, Double> scores) {
        List<GraphNode> graphNodes = new ArrayList<>();
        for (int i = 0; i < processedSentences.size(); i++) {
            String sentence = processedSentences.get(i);
            double score = scores.getOrDefault(sentence, 0.0);
            graphNodes.add(new GraphNode(i + 1, score));
        }
        return graphNodes;
    }

    public List<GraphLink> createGraphLinks(Map<String, Set<String>> graph, List<String> processedSentences) {
        List<GraphLink> graphLinks = new ArrayList<>();

        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            String sourceSentence = entry.getKey();
            int sourceId = processedSentences.indexOf(sourceSentence) + 1;

            for (String neighbor : entry.getValue()) {
                int targetId = processedSentences.indexOf(neighbor) + 1;
                graphLinks.add(new GraphLink(sourceId, targetId));
            }
        }
        return graphLinks;
    }
}