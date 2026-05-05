package com.pragya.expensetracker.ai;

/**
 * All supported chatbot intents.
 * Each intent maps to a specific type of financial query the user can ask.
 */
public enum Intent {

    GREETING("Greeting"),
    MONTHLY_TOTAL("Monthly Total Spending"),
    CATEGORY_ANALYSIS("Category Analysis"),
    MONTH_COMPARISON("Month-over-Month Comparison"),
    SAVING_SUGGESTION("Saving Suggestions"),
    TOP_EXPENSES("Top Expenses"),
    DAILY_TREND("Daily Spending Trend"),
    SPENDING_SUMMARY("Full Spending Summary"),
    INCOME_STATUS("Income & Savings Status"),
    HELP("Help & Available Commands"),
    UNKNOWN("Unknown Query");

    private final String displayName;

    Intent(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
