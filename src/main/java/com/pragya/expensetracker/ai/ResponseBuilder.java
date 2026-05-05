package com.pragya.expensetracker.ai;

import com.pragya.expensetracker.entity.Expense;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Template-based response generator.
 *
 * Builds natural-language, human-like responses for each intent using real data.
 * Uses randomized template variants so repeated queries don't feel robotic.
 * Includes contextual tips, comparisons, and actionable suggestions.
 */
@Component
public class ResponseBuilder {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private final Random random = new Random();

    // ────────────────────────────────────────────────────────
    //  MONTHLY TOTAL
    // ────────────────────────────────────────────────────────

    public String buildMonthlyTotal(double total, long count, String monthName, int year, Double income) {
        if (count == 0) {
            return String.format("📭 You haven't recorded any expenses for %s %d yet. " +
                    "Start adding expenses to get personalized insights!", monthName, year);
        }

        double avg = total / count;
        StringBuilder sb = new StringBuilder();

        String[] openers = {
                String.format("💰 In %s %d, you spent a total of ₹%.2f across %d transactions.", monthName, year, total, count),
                String.format("📊 Your %s %d spending: ₹%.2f total from %d expenses.", monthName, year, total, count),
                String.format("💸 You've recorded %d expenses in %s %d, totaling ₹%.2f.", count, monthName, year, total)
        };
        sb.append(pickRandom(openers));
        sb.append(String.format("\n📌 Average per expense: ₹%.2f", avg));

        if (income != null && income > 0) {
            double pct = (total / income) * 100.0;
            sb.append(String.format("\n💵 That's %.1f%% of your monthly income (₹%.2f).", pct, income));

            if (pct > 90) {
                sb.append("\n🚨 Warning: You're spending over 90% of your income! Urgent review needed.");
            } else if (pct > 70) {
                sb.append("\n⚠️ You're spending a large chunk of your income. Consider reviewing non-essential expenses.");
            } else if (pct < 40) {
                sb.append("\n✅ Excellent! You're keeping spending well under control.");
            } else {
                sb.append("\n👍 Your spending-to-income ratio is within a healthy range.");
            }
        }

        return sb.toString();
    }

    // ────────────────────────────────────────────────────────
    //  CATEGORY ANALYSIS
    // ────────────────────────────────────────────────────────

    public String buildCategoryAnalysis(List<Object[]> categories, double total, String monthName, int year) {
        if (categories == null || categories.isEmpty()) {
            return String.format("📭 No category data available for %s %d. Add some expenses first!", monthName, year);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📊 Category breakdown for %s %d:\n", monthName, year));
        sb.append("─────────────────────────────\n");

        for (int i = 0; i < categories.size(); i++) {
            String category = (String) categories.get(i)[0];
            double amount = ((Number) categories.get(i)[1]).doubleValue();
            double pct = total > 0 ? (amount / total) * 100.0 : 0;
            String bar = generateBar(pct);

            sb.append(String.format("%s %s: ₹%.2f (%.1f%%)\n", getmedal(i), category, amount, pct));
            sb.append(String.format("   %s\n", bar));
        }

        sb.append("─────────────────────────────");

        // Highlight top category
        String topCategory = (String) categories.get(0)[0];
        double topAmount = ((Number) categories.get(0)[1]).doubleValue();
        double topPct = total > 0 ? (topAmount / total) * 100.0 : 0;

        if (topPct > 50) {
            sb.append(String.format("\n\n⚠️ '%s' dominates your spending at %.1f%%. " +
                    "Consider if all those expenses are necessary.", topCategory, topPct));
        } else {
            sb.append(String.format("\n\n📌 Your highest category is '%s' at %.1f%% of total spending.", topCategory, topPct));
        }

        return sb.toString();
    }

    // ────────────────────────────────────────────────────────
    //  MONTH COMPARISON
    // ────────────────────────────────────────────────────────

    public String buildMonthComparison(double currentTotal, long currentCount,
                                        double lastTotal, long lastCount,
                                        String currentMonth, String lastMonth, int year) {
        StringBuilder sb = new StringBuilder();
        sb.append("📈 Month-over-Month Comparison:\n");
        sb.append("─────────────────────────────\n");
        sb.append(String.format("📅 %s: ₹%.2f (%d expenses)\n", lastMonth, lastTotal, lastCount));
        sb.append(String.format("📅 %s: ₹%.2f (%d expenses)\n", currentMonth, currentTotal, currentCount));
        sb.append("─────────────────────────────\n");

        if (lastTotal == 0 && currentTotal == 0) {
            sb.append("📭 No expenses recorded in either month.");
            return sb.toString();
        }

        if (lastTotal == 0) {
            sb.append(String.format("📌 No data for %s, so comparison isn't possible. " +
                    "You spent ₹%.2f this month.", lastMonth, currentTotal));
            return sb.toString();
        }

        double change = currentTotal - lastTotal;
        double pctChange = (change / lastTotal) * 100.0;

        if (change > 0) {
            sb.append(String.format("📈 Spending INCREASED by ₹%.2f (%.1f%%).\n", change, pctChange));
            if (pctChange > 30) {
                sb.append("🚨 That's a significant jump! Review your recent expenses.");
            } else if (pctChange > 10) {
                sb.append("⚠️ A noticeable increase. Keep an eye on non-essential spending.");
            } else {
                sb.append("👍 A slight increase, but nothing alarming.");
            }
        } else if (change < 0) {
            sb.append(String.format("📉 Spending DECREASED by ₹%.2f (%.1f%%).\n", Math.abs(change), Math.abs(pctChange)));
            if (Math.abs(pctChange) > 20) {
                sb.append("🎉 Great job! You've significantly reduced your spending!");
            } else {
                sb.append("✅ Nice! You're spending a bit less than last month.");
            }
        } else {
            sb.append("➡️ Spending is exactly the same as last month.");
        }

        return sb.toString();
    }

    // ────────────────────────────────────────────────────────
    //  SAVING SUGGESTIONS
    // ────────────────────────────────────────────────────────

    public String buildSavingSuggestions(List<Object[]> categories, double total,
                                         Double income, String monthName) {
        StringBuilder sb = new StringBuilder();
        sb.append("💡 Here are personalized saving suggestions based on your data:\n\n");

        if (categories == null || categories.isEmpty()) {
            sb.append("📭 I don't have enough expense data to make suggestions. " +
                    "Keep tracking your expenses and ask me again later!");
            return sb.toString();
        }

        int tipCount = 1;

        // Tip 1: Analyze top spending category
        String topCategory = (String) categories.get(0)[0];
        double topAmount = ((Number) categories.get(0)[1]).doubleValue();
        double topPct = total > 0 ? (topAmount / total) * 100.0 : 0;

        if (topPct > 40) {
            sb.append(String.format("%d. 🎯 Your highest category '%s' accounts for %.1f%% of spending (₹%.2f). " +
                    "Try setting a budget cap for this category.\n\n", tipCount++, topCategory, topPct, topAmount));
        } else {
            sb.append(String.format("%d. 📊 '%s' is your top category at ₹%.2f (%.1f%%). " +
                    "This is fairly balanced.\n\n", tipCount++, topCategory, topAmount, topPct));
        }

        // Tip 2: Multiple small categories
        if (categories.size() > 4) {
            sb.append(String.format("%d. 🔍 You're spending across %d categories. " +
                    "Consolidating subscriptions or eliminating minor recurring costs can add up.\n\n",
                    tipCount++, categories.size()));
        }

        // Tip 3: Income-based tip
        if (income != null && income > 0) {
            double savingsRate = ((income - total) / income) * 100.0;
            if (savingsRate < 10) {
                sb.append(String.format("%d. 🚨 Your savings rate is only %.1f%%. " +
                        "Financial experts recommend saving at least 20%% of income. " +
                        "Try the 50/30/20 rule: 50%% needs, 30%% wants, 20%% savings.\n\n",
                        tipCount++, savingsRate));
            } else if (savingsRate < 20) {
                sb.append(String.format("%d. ⚠️ Your savings rate is %.1f%%. " +
                        "You're close to the recommended 20%% — a few small cuts could get you there!\n\n",
                        tipCount++, savingsRate));
            } else {
                sb.append(String.format("%d. ✅ Your savings rate is %.1f%% — " +
                        "you're doing well! Consider investing the surplus.\n\n", tipCount++, savingsRate));
            }
        }

        // Tip 4: General advice
        sb.append(String.format("%d. 📋 Track daily to catch spending leaks early. " +
                "Small daily savings compound into big monthly differences!", tipCount));

        return sb.toString();
    }

    // ────────────────────────────────────────────────────────
    //  TOP EXPENSES
    // ────────────────────────────────────────────────────────

    public String buildTopExpenses(List<Expense> expenses, String monthName, int year) {
        if (expenses == null || expenses.isEmpty()) {
            return String.format("📭 No expenses found for %s %d.", monthName, year);
        }

        int limit = Math.min(expenses.size(), 5);
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("🏆 Top %d expenses for %s %d:\n", limit, monthName, year));
        sb.append("─────────────────────────────\n");

        double topTotal = 0;
        for (int i = 0; i < limit; i++) {
            Expense e = expenses.get(i);
            topTotal += e.getAmount();
            sb.append(String.format("%s #%d  ₹%.2f — %s [%s] (%s)\n",
                    getmedal(i), i + 1, e.getAmount(), e.getTitle(),
                    e.getCategory(), e.getDate().format(DATE_FMT)));
        }

        sb.append("─────────────────────────────\n");
        sb.append(String.format("💰 These top %d expenses total ₹%.2f.", limit, topTotal));

        if (limit >= 3) {
            Expense biggest = expenses.get(0);
            sb.append(String.format("\n📌 Your single biggest expense: '%s' at ₹%.2f on %s.",
                    biggest.getTitle(), biggest.getAmount(), biggest.getDate().format(DATE_FMT)));
        }

        return sb.toString();
    }

    // ────────────────────────────────────────────────────────
    //  DAILY TREND
    // ────────────────────────────────────────────────────────

    public String buildDailyTrend(List<Object[]> dailyData, String monthName, int year) {
        if (dailyData == null || dailyData.isEmpty()) {
            return String.format("📭 No daily spending data for %s %d.", monthName, year);
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📆 Daily spending trend for %s %d:\n", monthName, year));
        sb.append("─────────────────────────────\n");

        double maxDay = 0;
        LocalDate maxDate = null;
        double total = 0;

        for (Object[] row : dailyData) {
            LocalDate date = (LocalDate) row[0];
            double amount = ((Number) row[1]).doubleValue();
            total += amount;

            String miniBar = generateMiniBar(amount, getMaxAmount(dailyData));
            sb.append(String.format("  %s  ₹%8.2f  %s\n", date.format(DATE_FMT), amount, miniBar));

            if (amount > maxDay) {
                maxDay = amount;
                maxDate = date;
            }
        }

        sb.append("─────────────────────────────\n");

        double avg = total / dailyData.size();
        sb.append(String.format("📊 Average daily spending: ₹%.2f\n", avg));

        if (maxDate != null) {
            sb.append(String.format("📈 Peak spending day: %s with ₹%.2f\n", maxDate.format(DATE_FMT), maxDay));

            if (maxDay > avg * 3) {
                sb.append("⚠️ Your peak day is over 3x the average — was this a one-time large purchase?");
            }
        }

        return sb.toString();
    }

    // ────────────────────────────────────────────────────────
    //  SPENDING SUMMARY
    // ────────────────────────────────────────────────────────

    public String buildSpendingSummary(double total, long count, List<Object[]> categories,
                                       double lastMonthTotal, Double income,
                                       String monthName, int year) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("📋 Complete Spending Summary — %s %d\n", monthName, year));
        sb.append("═══════════════════════════════\n\n");

        // Overall
        if (count == 0) {
            sb.append("📭 No expenses recorded this month yet.\n");
            return sb.toString();
        }

        double avg = total / count;
        sb.append(String.format("💰 Total Spent: ₹%.2f\n", total));
        sb.append(String.format("📝 Transactions: %d\n", count));
        sb.append(String.format("📌 Average: ₹%.2f per expense\n\n", avg));

        // vs Last Month
        if (lastMonthTotal > 0) {
            double change = ((total - lastMonthTotal) / lastMonthTotal) * 100.0;
            String direction = change >= 0 ? "📈 Up" : "📉 Down";
            sb.append(String.format("🔄 vs Last Month: %s %.1f%% (₹%.2f → ₹%.2f)\n\n",
                    direction, Math.abs(change), lastMonthTotal, total));
        }

        // Income
        if (income != null && income > 0) {
            double savingsAmt = income - total;
            double savingsRate = (savingsAmt / income) * 100.0;
            sb.append(String.format("💵 Income: ₹%.2f\n", income));
            sb.append(String.format("🏦 Savings: ₹%.2f (%.1f%%)\n\n", savingsAmt, savingsRate));
        }

        // Categories
        if (categories != null && !categories.isEmpty()) {
            sb.append("📊 By Category:\n");
            for (Object[] cat : categories) {
                String name = (String) cat[0];
                double amt = ((Number) cat[1]).doubleValue();
                double pct = (amt / total) * 100.0;
                sb.append(String.format("   • %s: ₹%.2f (%.1f%%)\n", name, amt, pct));
            }
        }

        sb.append("\n═══════════════════════════════");
        return sb.toString();
    }

    // ────────────────────────────────────────────────────────
    //  INCOME STATUS
    // ────────────────────────────────────────────────────────

    public String buildIncomeStatus(Double income, double totalSpent, String monthName, int year) {
        StringBuilder sb = new StringBuilder();

        if (income == null || income <= 0) {
            sb.append(String.format("📭 No income data recorded for %s %d.\n", monthName, year));
            sb.append("💡 Set your monthly income using the income feature to get savings insights!");
            return sb.toString();
        }

        double savings = income - totalSpent;
        double savingsRate = (savings / income) * 100.0;
        double spendingRate = (totalSpent / income) * 100.0;

        sb.append(String.format("💵 Income & Savings Status — %s %d\n", monthName, year));
        sb.append("─────────────────────────────\n");
        sb.append(String.format("💰 Monthly Income:  ₹%.2f\n", income));
        sb.append(String.format("💸 Total Spent:     ₹%.2f (%.1f%%)\n", totalSpent, spendingRate));
        sb.append(String.format("🏦 Remaining:       ₹%.2f (%.1f%%)\n", savings, savingsRate));
        sb.append("─────────────────────────────\n");

        if (savings < 0) {
            sb.append(String.format("🚨 You've OVERSPENT by ₹%.2f! You're spending more than you earn.", Math.abs(savings)));
        } else if (savingsRate < 10) {
            sb.append("⚠️ Your savings rate is below 10%. Try to reduce discretionary spending.");
        } else if (savingsRate < 20) {
            sb.append("👍 Decent savings rate! Aim for the 20% benchmark to build a strong safety net.");
        } else if (savingsRate < 40) {
            sb.append("✅ Great savings rate! You're managing your money well.");
        } else {
            sb.append("🌟 Exceptional! You're saving over 40% of your income. Consider investing the surplus!");
        }

        return sb.toString();
    }

    // ────────────────────────────────────────────────────────
    //  GREETING
    // ────────────────────────────────────────────────────────

    public String buildGreeting(String username, double monthlySpent, long expenseCount) {
        String[] greetings = {
                String.format("👋 Hello, %s! Welcome to your financial assistant.", username),
                String.format("😊 Hi %s! Ready to explore your finances?", username),
                String.format("🙌 Hey %s! Great to see you. Let's dive into your expenses.", username)
        };

        StringBuilder sb = new StringBuilder(pickRandom(greetings));

        if (expenseCount > 0) {
            sb.append(String.format("\n\n📊 Quick snapshot: You've spent ₹%.2f across %d expenses this month.",
                    monthlySpent, expenseCount));
        } else {
            sb.append("\n\n📭 You haven't added any expenses this month yet. Start tracking to unlock insights!");
        }

        sb.append("\n\n💡 Try asking me things like:");
        sb.append("\n   • \"How much did I spend this month?\"");
        sb.append("\n   • \"Show my category breakdown\"");
        sb.append("\n   • \"Where can I save money?\"");

        return sb.toString();
    }

    // ────────────────────────────────────────────────────────
    //  HELP
    // ────────────────────────────────────────────────────────

    public String buildHelp() {
        return """
                🤖 Here's what I can help you with:
                ─────────────────────────────
                💰 Monthly Spending
                   "How much did I spend this month?"
                   "Total expenses this month"

                📊 Category Analysis
                   "Show my category breakdown"
                   "Which category do I spend most on?"

                📈 Month Comparison
                   "Compare this month with last month"
                   "This month vs last month"

                💡 Saving Suggestions
                   "Where can I save money?"
                   "Give me saving tips"

                🏆 Top Expenses
                   "Show my highest expenses"
                   "What are my biggest spending items?"

                📆 Daily Trends
                   "Show my daily spending trend"
                   "Day by day spending"

                📋 Full Summary
                   "Give me a spending summary"
                   "Show me everything"

                💵 Income Status
                   "What's my income status?"
                   "How much do I have left?"
                ─────────────────────────────
                Just type your question naturally — I'll understand! 😊""";
    }

    // ────────────────────────────────────────────────────────
    //  UNKNOWN / FALLBACK
    // ────────────────────────────────────────────────────────

    public String buildUnknown() {
        String[] responses = {
                "🤔 I'm not sure I understood that. Could you rephrase?",
                "😅 I didn't quite catch that. Try asking about your spending, categories, or savings!",
                "🤷 Hmm, I don't know how to answer that one yet."
        };

        return pickRandom(responses) +
                "\n\n💡 Try asking:\n" +
                "   • \"How much did I spend this month?\"\n" +
                "   • \"Show my category breakdown\"\n" +
                "   • \"Compare this month with last month\"\n" +
                "   • Type \"help\" for all available commands";
    }

    // ────────────────────────────────────────────────────────
    //  SUGGESTIONS (follow-up questions)
    // ────────────────────────────────────────────────────────

    public List<String> getSuggestions(Intent intent) {
        return switch (intent) {
            case MONTHLY_TOTAL -> List.of("Show my category breakdown", "Compare with last month", "Where can I save?");
            case CATEGORY_ANALYSIS -> List.of("Show my top expenses", "Where can I save money?", "Give me a summary");
            case MONTH_COMPARISON -> List.of("Show daily trends", "Where can I save?", "Show category breakdown");
            case SAVING_SUGGESTION -> List.of("Show my top expenses", "What's my income status?", "Give me a summary");
            case TOP_EXPENSES -> List.of("Show category breakdown", "Where can I save?", "Compare months");
            case DAILY_TREND -> List.of("Show top expenses", "Give me a summary", "Compare with last month");
            case SPENDING_SUMMARY -> List.of("Where can I save?", "Show daily trends", "What's my income status?");
            case INCOME_STATUS -> List.of("Where can I save?", "Show category breakdown", "Give me a summary");
            case GREETING, HELP -> List.of("How much did I spend?", "Show category breakdown", "Give me a summary");
            case UNKNOWN -> List.of("How much did I spend this month?", "Show categories", "Help");
        };
    }

    // ────────────────────────────────────────────────────────
    //  Helpers
    // ────────────────────────────────────────────────────────

    private String pickRandom(String[] options) {
        return options[random.nextInt(options.length)];
    }

    private String getmedal(int index) {
        return switch (index) {
            case 0 -> "🥇";
            case 1 -> "🥈";
            case 2 -> "🥉";
            default -> "  ";
        };
    }

    private String generateBar(double percentage) {
        int filled = (int) Math.round(percentage / 5.0);
        return "█".repeat(Math.max(filled, 1)) + "░".repeat(Math.max(20 - filled, 0));
    }

    private String generateMiniBar(double value, double maxValue) {
        if (maxValue <= 0) return "";
        int filled = (int) Math.round((value / maxValue) * 10);
        return "▓".repeat(Math.max(filled, 1));
    }

    private double getMaxAmount(List<Object[]> dailyData) {
        double max = 0;
        for (Object[] row : dailyData) {
            double amount = ((Number) row[1]).doubleValue();
            if (amount > max) max = amount;
        }
        return max;
    }
}
