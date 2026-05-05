package com.pragya.expensetracker.service;

import com.pragya.expensetracker.ai.*;
import com.pragya.expensetracker.dto.ChatResponse;
import com.pragya.expensetracker.entity.Expense;
import com.pragya.expensetracker.repository.ChatExpenseRepository;
import com.pragya.expensetracker.repository.MonthlyIncomeRepository;
import com.pragya.expensetracker.repository.PlannedExpenseRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Core chatbot orchestration service.
 *
 * Flow: User Message → IntentDetector → Data Fetch → ResponseBuilder → ChatResponse
 *
 * Each intent handler:
 *   1. Computes the relevant date range
 *   2. Queries the database via ChatExpenseRepository
 *   3. Delegates to ResponseBuilder for natural-language formatting
 */
@Service
public class ChatService {

    private final IntentDetector intentDetector;
    private final ResponseBuilder responseBuilder;
    private final ChatHistoryManager historyManager;
    private final ChatExpenseRepository chatExpenseRepo;
    private final MonthlyIncomeRepository incomeRepo;

    public ChatService(IntentDetector intentDetector,
                       ResponseBuilder responseBuilder,
                       ChatHistoryManager historyManager,
                       ChatExpenseRepository chatExpenseRepo,
                       MonthlyIncomeRepository incomeRepo) {
        this.intentDetector = intentDetector;
        this.responseBuilder = responseBuilder;
        this.historyManager = historyManager;
        this.chatExpenseRepo = chatExpenseRepo;
        this.incomeRepo = incomeRepo;
    }

    /**
     * Process an incoming chat message and return a structured response.
     */
    public ChatResponse processMessage(String username, String message) {
        // 1. Detect intent
        IntentResult intentResult = intentDetector.detect(message);
        Intent intent = intentResult.getIntent();

        // 2. Route to the appropriate handler
        String reply = switch (intent) {
            case GREETING -> handleGreeting(username);
            case MONTHLY_TOTAL -> handleMonthlyTotal(username);
            case CATEGORY_ANALYSIS -> handleCategoryAnalysis(username);
            case MONTH_COMPARISON -> handleMonthComparison(username);
            case SAVING_SUGGESTION -> handleSavingSuggestion(username);
            case TOP_EXPENSES -> handleTopExpenses(username);
            case DAILY_TREND -> handleDailyTrend(username);
            case SPENDING_SUMMARY -> handleSpendingSummary(username);
            case INCOME_STATUS -> handleIncomeStatus(username);
            case HELP -> responseBuilder.buildHelp();
            case UNKNOWN -> responseBuilder.buildUnknown();
        };

        // 3. Save to chat history
        historyManager.addExchange(username, message, reply);

        // 4. Get follow-up suggestions
        List<String> suggestions = responseBuilder.getSuggestions(intent);

        // 5. Build and return response
        return new ChatResponse(
                reply,
                intent.getDisplayName(),
                intentResult.getConfidence(),
                suggestions
        );
    }

    /**
     * Clear chat history for a user.
     */
    public void clearHistory(String username) {
        historyManager.clearHistory(username);
    }

    /**
     * Get chat history for a user.
     */
    public List<ChatHistoryManager.ChatMessage> getHistory(String username) {
        return historyManager.getHistory(username);
    }

    // ────────────────────────────────────────────────────────
    //  Intent Handlers
    // ────────────────────────────────────────────────────────

    private String handleGreeting(String username) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);

        double spent = safeDouble(chatExpenseRepo.getTotalSpending(username, monthStart, now));
        long count = safeLong(chatExpenseRepo.getExpenseCount(username, monthStart, now));

        return responseBuilder.buildGreeting(username, spent, count);
    }

    private String handleMonthlyTotal(String username) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        String monthName = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int year = now.getYear();

        double total = safeDouble(chatExpenseRepo.getTotalSpending(username, monthStart, now));
        long count = safeLong(chatExpenseRepo.getExpenseCount(username, monthStart, now));
        Double income = getMonthlyIncome(username, year, now.getMonthValue());

        return responseBuilder.buildMonthlyTotal(total, count, monthName, year, income);
    }

    private String handleCategoryAnalysis(String username) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        String monthName = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int year = now.getYear();

        List<Object[]> categories = chatExpenseRepo.getCategoryWiseSpending(username, monthStart, now);
        double total = safeDouble(chatExpenseRepo.getTotalSpending(username, monthStart, now));

        return responseBuilder.buildCategoryAnalysis(categories, total, monthName, year);
    }

    private String handleMonthComparison(String username) {
        LocalDate now = LocalDate.now();

        // Current month range
        LocalDate currentStart = now.withDayOfMonth(1);
        String currentMonthName = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        // Last month range
        LocalDate lastMonthDate = now.minusMonths(1);
        LocalDate lastStart = lastMonthDate.withDayOfMonth(1);
        LocalDate lastEnd = lastMonthDate.withDayOfMonth(lastMonthDate.lengthOfMonth());
        String lastMonthName = lastMonthDate.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        double currentTotal = safeDouble(chatExpenseRepo.getTotalSpending(username, currentStart, now));
        long currentCount = safeLong(chatExpenseRepo.getExpenseCount(username, currentStart, now));
        double lastTotal = safeDouble(chatExpenseRepo.getTotalSpending(username, lastStart, lastEnd));
        long lastCount = safeLong(chatExpenseRepo.getExpenseCount(username, lastStart, lastEnd));

        return responseBuilder.buildMonthComparison(
                currentTotal, currentCount, lastTotal, lastCount,
                currentMonthName, lastMonthName, now.getYear()
        );
    }

    private String handleSavingSuggestion(String username) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        String monthName = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);

        List<Object[]> categories = chatExpenseRepo.getCategoryWiseSpending(username, monthStart, now);
        double total = safeDouble(chatExpenseRepo.getTotalSpending(username, monthStart, now));
        Double income = getMonthlyIncome(username, now.getYear(), now.getMonthValue());

        return responseBuilder.buildSavingSuggestions(categories, total, income, monthName);
    }

    private String handleTopExpenses(String username) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        String monthName = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int year = now.getYear();

        List<Expense> expenses = chatExpenseRepo.getExpensesSortedByAmount(username, monthStart, now);

        return responseBuilder.buildTopExpenses(expenses, monthName, year);
    }

    private String handleDailyTrend(String username) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        String monthName = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int year = now.getYear();

        List<Object[]> dailyData = chatExpenseRepo.getDailySpending(username, monthStart, now);

        return responseBuilder.buildDailyTrend(dailyData, monthName, year);
    }

    private String handleSpendingSummary(String username) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        String monthName = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int year = now.getYear();

        double total = safeDouble(chatExpenseRepo.getTotalSpending(username, monthStart, now));
        long count = safeLong(chatExpenseRepo.getExpenseCount(username, monthStart, now));
        List<Object[]> categories = chatExpenseRepo.getCategoryWiseSpending(username, monthStart, now);

        // Last month
        LocalDate lastMonthDate = now.minusMonths(1);
        LocalDate lastStart = lastMonthDate.withDayOfMonth(1);
        LocalDate lastEnd = lastMonthDate.withDayOfMonth(lastMonthDate.lengthOfMonth());
        double lastTotal = safeDouble(chatExpenseRepo.getTotalSpending(username, lastStart, lastEnd));

        Double income = getMonthlyIncome(username, year, now.getMonthValue());

        return responseBuilder.buildSpendingSummary(total, count, categories, lastTotal, income, monthName, year);
    }

    private String handleIncomeStatus(String username) {
        LocalDate now = LocalDate.now();
        LocalDate monthStart = now.withDayOfMonth(1);
        String monthName = now.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH);
        int year = now.getYear();

        Double income = getMonthlyIncome(username, year, now.getMonthValue());
        double totalSpent = safeDouble(chatExpenseRepo.getTotalSpending(username, monthStart, now));

        return responseBuilder.buildIncomeStatus(income, totalSpent, monthName, year);
    }

    // ────────────────────────────────────────────────────────
    //  Helper methods
    // ────────────────────────────────────────────────────────

    private Double getMonthlyIncome(String username, int year, int month) {
        return incomeRepo.findByUsernameAndYearAndMonth(username, year, month)
                .map(income -> income.getAmount())
                .orElse(null);
    }

    private double safeDouble(Double value) {
        return value != null ? value : 0.0;
    }

    private long safeLong(Long value) {
        return value != null ? value : 0L;
    }
}
