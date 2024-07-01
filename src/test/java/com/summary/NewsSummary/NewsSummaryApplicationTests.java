package com.summary.NewsSummary;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

@SpringBootTest
class NewsSummaryApplicationTests {

	@Test
	void testSummarization() throws Exception {
		String text = "Your test article text here.";
		TextRankSummarizer summarizer = new TextRankSummarizer();
		List<String> summary = summarizer.summarize(text);
		assertFalse(summary.isEmpty());
	}
}
