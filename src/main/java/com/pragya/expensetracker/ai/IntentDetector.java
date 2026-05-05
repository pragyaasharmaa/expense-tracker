package com.pragya.expensetracker.ai;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Rule-based intent detection engine.
 *
 * Uses a two-pass scoring approach:
 *   1. Keyword matching — each keyword hit adds a weight-based score
 *   2. Regex pattern matching — structural patterns add bonus score
 *
 * The intent with the highest combined score wins. A minimum threshold
 * prevents weak matches from triggering a real intent (falls back to UNKNOWN).
 */
@Component
public class IntentDetector {

    private static final double MIN_SCORE_THRESHOLD = 1.0;
    private static final double MAX_SCORE_FOR_NORMALIZATION = 10.0;

    // --- Keyword groups: (weight, [keywords]) ---
    // Higher weight = stronger signal. Multi-word phrases get higher weight
    // because they're more specific.
    private final Map<Intent, List<WeightedKeywords>> intentKeywords = new LinkedHashMap<>();

    // --- Regex patterns for structural matching ---
    private final Map<Intent, List<Pattern>> intentPatterns = new LinkedHashMap<>();

    public IntentDetector() {
        registerKeywords();
        registerPatterns();
    }

    /**
     * Detect the most likely intent from a user message.
     *
     * @param message raw user message
     * @return IntentResult with the best intent and confidence (0.0–1.0)
     */
    public IntentResult detect(String message) {
        if (message == null || message.trim().isEmpty()) {
            return new IntentResult(Intent.UNKNOWN, 0.0);
        }

        // Normalize: lowercase, strip punctuation, collapse whitespace
        String normalized = message.toLowerCase().trim()
                .replaceAll("[^a-z0-9\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();

        if (normalized.isEmpty()) {
            return new IntentResult(Intent.UNKNOWN, 0.0);
        }

        Map<Intent, Double> scores = new EnumMap<>(Intent.class);

        // Pass 1: Keyword scoring
        for (Map.Entry<Intent, List<WeightedKeywords>> entry : intentKeywords.entrySet()) {
            double score = 0.0;
            for (WeightedKeywords wk : entry.getValue()) {
                for (String keyword : wk.keywords) {
                    if (normalized.contains(keyword)) {
                        score += wk.weight;
                    }
                }
            }
            if (score > 0) {
                scores.put(entry.getKey(), score);
            }
        }

        // Pass 2: Regex pattern bonus
        for (Map.Entry<Intent, List<Pattern>> entry : intentPatterns.entrySet()) {
            for (Pattern pattern : entry.getValue()) {
                if (pattern.matcher(normalized).find()) {
                    scores.merge(entry.getKey(), 2.0, Double::sum);
                }
            }
        }

        // Find the intent with the highest score
        Intent bestIntent = Intent.UNKNOWN;
        double bestScore = 0.0;

        for (Map.Entry<Intent, Double> entry : scores.entrySet()) {
            if (entry.getValue() > bestScore) {
                bestScore = entry.getValue();
                bestIntent = entry.getKey();
            }
        }

        // Apply minimum threshold
        if (bestScore < MIN_SCORE_THRESHOLD) {
            return new IntentResult(Intent.UNKNOWN, 0.0);
        }

        // Normalize confidence to 0.0–1.0
        double confidence = Math.min(bestScore / MAX_SCORE_FOR_NORMALIZATION, 1.0);

        return new IntentResult(bestIntent, confidence);
    }

    // ────────────────────────────────────────────────────────
    //  Keyword Registration
    // ────────────────────────────────────────────────────────

    private void registerKeywords() {

        // GREETING
        intentKeywords.put(Intent.GREETING, List.of(
                new WeightedKeywords(2.0, "good morning", "good evening", "good afternoon", "good night"),
                new WeightedKeywords(1.5, "hello", "hi there", "hey there"),
                new WeightedKeywords(1.0, "hi", "hey", "greetings", "howdy", "yo")
        ));

        // MONTHLY_TOTAL
        intentKeywords.put(Intent.MONTHLY_TOTAL, List.of(
                new WeightedKeywords(3.0, "total spent this month", "how much spent this month",
                        "monthly spending total", "this month total"),
                new WeightedKeywords(2.0, "how much", "total spent", "monthly spending",
                        "spent this month", "month total", "total expenses this month"),
                new WeightedKeywords(1.0, "total", "spent", "spending", "this month", "month")
        ));

        // CATEGORY_ANALYSIS
        intentKeywords.put(Intent.CATEGORY_ANALYSIS, List.of(
                new WeightedKeywords(3.0, "category breakdown", "spending by category",
                        "category wise spending", "which category do i spend"),
                new WeightedKeywords(2.0, "category wise", "which category", "most on",
                        "spend most on", "category analysis", "by category"),
                new WeightedKeywords(1.0, "category", "categories", "breakdown")
        ));

        // MONTH_COMPARISON
        intentKeywords.put(Intent.MONTH_COMPARISON, List.of(
                new WeightedKeywords(3.0, "compare this month with last month",
                        "this month vs last month", "month over month",
                        "compare months", "last month vs this month"),
                new WeightedKeywords(2.0, "compare", "comparison", "last month",
                        "previous month", "month vs", "versus last"),
                new WeightedKeywords(1.0, "vs", "versus", "compared", "difference")
        ));

        // SAVING_SUGGESTION
        intentKeywords.put(Intent.SAVING_SUGGESTION, List.of(
                new WeightedKeywords(3.0, "where can i save", "how to save money",
                        "saving suggestions", "reduce spending", "cut expenses"),
                new WeightedKeywords(2.0, "save money", "saving tips", "reduce expenses",
                        "cut costs", "budget tips", "spend less"),
                new WeightedKeywords(1.0, "save", "saving", "reduce", "cut", "budget",
                        "tips", "suggest", "suggestion", "advice")
        ));

        // TOP_EXPENSES
        intentKeywords.put(Intent.TOP_EXPENSES, List.of(
                new WeightedKeywords(3.0, "top expenses", "highest expenses",
                        "biggest expenses", "most expensive", "largest expenses"),
                new WeightedKeywords(2.0, "top spending", "highest spending",
                        "biggest spending", "show top", "show highest"),
                new WeightedKeywords(1.0, "top", "highest", "biggest", "largest", "max", "maximum")
        ));

        // DAILY_TREND
        intentKeywords.put(Intent.DAILY_TREND, List.of(
                new WeightedKeywords(3.0, "daily spending trend", "day by day spending",
                        "daily expense trend", "day wise spending"),
                new WeightedKeywords(2.0, "daily trend", "daily spending", "day by day",
                        "day wise", "per day", "each day"),
                new WeightedKeywords(1.0, "daily", "trend", "pattern", "day")
        ));

        // SPENDING_SUMMARY
        intentKeywords.put(Intent.SPENDING_SUMMARY, List.of(
                new WeightedKeywords(3.0, "spending summary", "expense summary",
                        "full summary", "complete summary", "give me a summary"),
                new WeightedKeywords(2.0, "summary", "overview", "report",
                        "overall spending", "full report"),
                new WeightedKeywords(1.0, "overall", "everything", "all", "full")
        ));

        // INCOME_STATUS
        intentKeywords.put(Intent.INCOME_STATUS, List.of(
                new WeightedKeywords(3.0, "income status", "savings rate",
                        "income vs spending", "how much left"),
                new WeightedKeywords(2.0, "income", "salary", "earnings",
                        "savings", "left over", "remaining"),
                new WeightedKeywords(1.0, "earn", "earning", "left", "balance")
        ));

        // HELP
        intentKeywords.put(Intent.HELP, List.of(
                new WeightedKeywords(3.0, "what can you do", "what can i ask",
                        "show commands", "available commands"),
                new WeightedKeywords(2.0, "help me", "help", "options",
                        "commands", "features", "what do you do")
        ));
    }

    // ────────────────────────────────────────────────────────
    //  Pattern Registration (regex bonus scoring)
    // ────────────────────────────────────────────────────────

    private void registerPatterns() {

        intentPatterns.put(Intent.MONTHLY_TOTAL, List.of(
                Pattern.compile("how much.*(spend|spent|spending)"),
                Pattern.compile("total.*(month|spend|expense)"),
                Pattern.compile("(spend|spent).*(month|total)")
        ));

        intentPatterns.put(Intent.CATEGORY_ANALYSIS, List.of(
                Pattern.compile("(which|what).*(category|categories)"),
                Pattern.compile("category.*(breakdown|analysis|wise|split)"),
                Pattern.compile("(most|highest).*(category|categories)")
        ));

        intentPatterns.put(Intent.MONTH_COMPARISON, List.of(
                Pattern.compile("compare.*(month|last|previous)"),
                Pattern.compile("(this|current).*(vs|versus|compared|against).*(last|previous)"),
                Pattern.compile("(last|previous) month.*(vs|versus|compared|against)")
        ));

        intentPatterns.put(Intent.SAVING_SUGGESTION, List.of(
                Pattern.compile("(where|how).*(save|reduce|cut)"),
                Pattern.compile("(save|reduce|cut).*(money|expense|spending|cost)")
        ));

        intentPatterns.put(Intent.TOP_EXPENSES, List.of(
                Pattern.compile("(top|highest|biggest|largest).*(expense|spending|transaction)"),
                Pattern.compile("show.*(top|highest|biggest)")
        ));

        intentPatterns.put(Intent.DAILY_TREND, List.of(
                Pattern.compile("(daily|day).*(trend|pattern|spending|expense)"),
                Pattern.compile("(trend|pattern).*(daily|day)")
        ));

        intentPatterns.put(Intent.SPENDING_SUMMARY, List.of(
                Pattern.compile("(give|show|get).*(summary|overview|report)"),
                Pattern.compile("(spending|expense).*(summary|overview|report)")
        ));

        intentPatterns.put(Intent.INCOME_STATUS, List.of(
                Pattern.compile("(income|salary).*(status|vs|versus|compared)"),
                Pattern.compile("how much.*(left|remaining|saved|balance)")
        ));
    }

    // ────────────────────────────────────────────────────────
    //  Inner helper class
    // ────────────────────────────────────────────────────────

    /**
     * Groups keywords under a common weight.
     * Higher weight = stronger signal that this keyword indicates the intent.
     */
    private static class WeightedKeywords {
        final double weight;
        final List<String> keywords;

        WeightedKeywords(double weight, String... keywords) {
            this.weight = weight;
            this.keywords = List.of(keywords);
        }
    }
}
