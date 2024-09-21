package com.summary.eSummarizer.Controller;

import com.summary.eSummarizer.Summarizer.SummaryInfo;
import com.summary.eSummarizer.Summarizer.TextRankSummarizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class SummarizationController {

    @Autowired
    private TextRankSummarizer summarizer;

    // Handle POST requests to /summarize
    @PostMapping("/summarize")
    public SummaryInfo summarize(@RequestBody String text) {
        return summarizer.summarize(text);
    }
}