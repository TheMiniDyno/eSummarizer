package com.summary.NewsSummary;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/summarize")
public class SummarizationController {

    @PostMapping
    public List<String> summarize(@RequestBody String text) {
        TextRankSummarizer summarizer = new TextRankSummarizer();
        return summarizer.summarize(text);
    }
}
