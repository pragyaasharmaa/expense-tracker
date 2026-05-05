package com.pragya.expensetracker.ai;

/**
 * Holds the result of intent detection: the matched intent and a confidence score.
 * Confidence is normalized to 0.0–1.0 range.
 */
public class IntentResult {

    private final Intent intent;
    private final double confidence;

    public IntentResult(Intent intent, double confidence) {
        this.intent = intent;
        this.confidence = confidence;
    }

    public Intent getIntent() {
        return intent;
    }

    public double getConfidence() {
        return confidence;
    }

    @Override
    public String toString() {
        return String.format("IntentResult{intent=%s, confidence=%.2f}", intent, confidence);
    }
}
