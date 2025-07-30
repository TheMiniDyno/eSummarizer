package com.summary.eSummarizer.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class POSService {

    private static final Logger logger = LoggerFactory.getLogger(POSService.class);
    private final Map<String, String> posDict;
    private final CSVLoaderService csvLoaderService;

    public POSService(CSVLoaderService csvLoaderService) {
        this.csvLoaderService = csvLoaderService;
        this.posDict = loadPOSDictionary();
    }

    private Map<String, String> loadPOSDictionary() {
        return csvLoaderService.loadAsKeyValue("/CSV/pos_dictionary.csv");
    }

    public String getPartOfSpeech(String word) {
        String pos = posDict.getOrDefault(word.toLowerCase(), "UNK");
//        logger.info("POS for '{}': {}", word, pos);
        return pos;
    }
}