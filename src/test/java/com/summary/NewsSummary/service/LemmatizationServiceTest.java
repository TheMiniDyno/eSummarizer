package com.summary.NewsSummary.service;
import com.summary.NewsSummary.LemmatizationService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LemmatizationServiceTest {

    private LemmatizationService lemmatizationService;

    @BeforeEach
    void setUp() {
        lemmatizationService = new LemmatizationService();
    }

    @Test
    void testLemmatizeKnownWord() {
        String lemma = lemmatizationService.lemmatize("running");
        assertEquals("run", lemma, "Lemmatization failed for known word 'running'");
    }

    @Test
    void testLemmatizeUnknownWord() {
        String lemma = lemmatizationService.lemmatize("kChaHajur");
        assertEquals("kChaHajur", lemma, "Lemmatization failed for unknown word 'unknowword'");
    }

    @Test
    void testLemmatizePluralWord() {
        String lemma = lemmatizationService.lemmatize("running");
        assertEquals("run", lemma, "Lemmatization failed for plural word 'apples'");
    }

    @Test
    void testLemmatizeStopword() {
        String lemma = lemmatizationService.lemmatize("was");
        assertEquals("be", lemma, "Lemmatization failed for stopword 'the'");
    }
}
