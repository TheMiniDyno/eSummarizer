package com.summary.eSummarizer.Controller;

import com.summary.eSummarizer.Summarizer.SummaryInfo;
import com.summary.eSummarizer.Summarizer.TextRankSummarizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class SummarizationController {

    @Autowired
    private TextRankSummarizer summarizer;

    // Handle POST requests to /summarize
    @PostMapping("/summarize")
    public SummaryInfo summarize(@RequestBody String text) {
        // Get authentication details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if user is anonymous
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            // Word count limit for anonymous users
            int wordCount = text.trim().split("\\s+").length;
            if (wordCount > 200) {
                throw new IllegalArgumentException("Login to summarize more than 200 words sentences.");
            }
        }


        return summarizer.summarize(text);
    }
}
