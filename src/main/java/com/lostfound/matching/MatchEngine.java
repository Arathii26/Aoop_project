package com.lostfound.matching;

import com.lostfound.model.FoundItem;
import com.lostfound.model.LostItem;

import java.util.*;
import java.util.stream.Collectors;

public class MatchEngine {
    private static final int CATEGORY_MAX = 40;
    private static final int LOCATION_MAX = 25;
    private static final int TIME_MAX = 20;
    private static final int KEYWORD_MAX = 15;
    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 100;
    private final int minimumScore;

    public MatchEngine() {
        this(50);
    }

    public MatchEngine(int minimumScore) {
        if (minimumScore < MIN_SCORE || minimumScore > MAX_SCORE) {
            throw new IllegalArgumentException("Minimum score must be between 0 and 100.");
        }
        this.minimumScore = minimumScore;
    }

    public int calculateScore(LostItem lost, FoundItem found) {
        validateInputs(lost, found);

        int categoryScore = calculateCategoryScore(lost, found);
        int locationScore = calculateLocationScore(lost, found);
        int timeScore = calculateTimeScore(lost, found);
        int keywordScore = calculateKeywordScore(lost, found);

        int total = categoryScore + locationScore + timeScore + keywordScore;
        return Math.max(MIN_SCORE, Math.min(MAX_SCORE, total));
    }

    public List<MatchResult> findMatches(LostItem lost, List<FoundItem> foundItems) {
        if (lost == null) {
            throw new IllegalArgumentException("Lost item cannot be null.");
        }
        if (foundItems == null) {
            return new ArrayList<>();
        }

        List<MatchResult> results = foundItems.stream()
                .filter(Objects::nonNull)
                .map(found -> new MatchResult(lost, found, calculateScore(lost, found)))
                .filter(result -> result.getScore() >= minimumScore)
                .sorted(MatchResult.byScoreDescending())
                .limit(5)
                .collect(Collectors.toList());

        return results;
    }

    private void validateInputs(LostItem lost, FoundItem found) {
        if (lost == null) {
            throw new IllegalArgumentException("Lost item cannot be null.");
        }
        if (found == null) {
            throw new IllegalArgumentException("Found item cannot be null.");
        }
        if (lost.getCategory() == null || found.getCategory() == null) {
            throw new IllegalArgumentException("Item category cannot be null.");
        }
        if (lost.getLocation() == null || found.getLocation() == null) {
            throw new IllegalArgumentException("Location cannot be null.");
        }
    }

    private int calculateCategoryScore(LostItem lost, FoundItem found) {
        return lost.getCategory() == found.getCategory() ? CATEGORY_MAX : 0;
    }

    private int calculateLocationScore(LostItem lost, FoundItem found) {
        String left = normalizeText(lost.getLocation());
        String right = normalizeText(found.getLocation());
        return left.equals(right) ? LOCATION_MAX : 0;
    }

    private int calculateTimeScore(LostItem lost, FoundItem found) {
        if (lost.getDateTime() == null || found.getDateTime() == null) {
            return 0;
        }
        long hours = Math.abs(java.time.Duration.between(lost.getDateTime(), found.getDateTime()).toHours());
        if (hours <= 2) {
            return TIME_MAX;
        }
        if (hours <= 6) {
            return (int) (TIME_MAX * 0.75);
        }
        if (hours <= 12) {
            return (int) (TIME_MAX * 0.5);
        }
        if (hours <= 24) {
            return (int) (TIME_MAX * 0.25);
        }
        return 0;
    }

    private int calculateKeywordScore(LostItem lost, FoundItem found) {
        Set<String> lostWords = extractKeywords(lost.getName(), lost.getDescription());
        Set<String> foundWords = extractKeywords(found.getName(), found.getDescription());

        if (lostWords.isEmpty() || foundWords.isEmpty()) {
            return 0;
        }

        Set<String> intersection = new HashSet<>(lostWords);
        intersection.retainAll(foundWords);
        double overlapRatio = intersection.size() / (double) Math.max(1, Math.min(lostWords.size(), foundWords.size()));
        return (int) Math.round(overlapRatio * KEYWORD_MAX);
    }

    private Set<String> extractKeywords(String... values) {
        Set<String> keywords = new HashSet<>();
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            String normalized = normalizeText(value);
            if (normalized.isBlank()) {
                continue;
            }
            keywords.addAll(Arrays.stream(normalized.split("\\s+"))
                    .map(word -> word.replaceAll("[^a-z0-9]", ""))
                    .filter(word -> !word.isBlank())
                    .collect(Collectors.toSet()));
        }
        return keywords;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
