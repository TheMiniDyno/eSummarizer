package com.summary.eSummarizer.Controller;

import com.summary.eSummarizer.Summarizer.SummaryInfo;
import com.summary.eSummarizer.Summarizer.TextRankSummarizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
public class SummarizationController {

    @Autowired
    private TextRankSummarizer summarizer;

    @PostMapping("/summarize")
    public ResponseEntity<?> summarize(@RequestBody String text) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            int wordCount = text.trim().split("\\s+").length;
            if (wordCount > 200) {
                // Send JSON-style error response
                return ResponseEntity
                        .status(401)
                        .body("Login required to summarize more than 200 words.");
            }
        }

        SummaryInfo summary = summarizer.summarize(text);
        return ResponseEntity.ok(summary);
    }

    // Inner class for structured error response
    static class ErrorResponse {
        private String message;

        public ErrorResponse(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
