package com.summary.eSummarizer;

import com.summary.eSummarizer.Summarizer.SummaryInfo;
import com.summary.eSummarizer.Summarizer.TextRankSummarizer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class eSummarizerApplicationTests {

	@Autowired
	private TextRankSummarizer summarizer;

	@Test
	void contextLoads() {
		// This test ensures that the Spring context loads successfully
	}

	@Test
	void testSummarize() {
		String testText = "This is the first sentence of the test. This is the second sentence. " +
				"Here's a third one to make it longer. And a fourth one for good measure. " +
				"Fifth sentence adds more content. Sixth sentence is getting verbose.";

		SummaryInfo result = summarizer.summarize(testText);

		assertNotNull(result, "Summary result should not be null");
		assertFalse(result.getSummarizedText().isEmpty(), "Summarized text should not be empty");
		assertTrue(result.getSummarizedSentenceCount() < result.getOriginalSentenceCount(),
				"Summarized sentence count should be less than original");
		assertTrue(result.getSummarizedWordCount() < result.getOriginalWordCount(),
				"Summarized word count should be less than original");
		assertTrue(result.getReductionRate() > 0 && result.getReductionRate() <= 1,
				"Reduction rate should be between 0 and 1");

		// Print summary details for manual verification
		System.out.println("Original Sentence Count: " + result.getOriginalSentenceCount());
		System.out.println("Summarized Sentence Count: " + result.getSummarizedSentenceCount());
		System.out.println("Original Word Count: " + result.getOriginalWordCount());
		System.out.println("Summarized Word Count: " + result.getSummarizedWordCount());
		System.out.println("Reduction Rate: " + result.getReductionRate());
		System.out.println("Summarized Text: " + String.join(" ", result.getSummarizedText()));
	}
}