package com.pragya.expensetracker.repository;

import com.pragya.expensetracker.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Dedicated repository for chatbot analytics queries.
 * Kept separate from ExpenseRepository to maintain clean separation of concerns.
 * All queries are scoped by username for data isolation.
 */
public interface ChatExpenseRepository extends JpaRepository<Expense, Long> {

    /**
     * Total spending for a date range.
     */
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.username = :username AND e.date BETWEEN :startDate AND :endDate")
    Double getTotalSpending(@Param("username") String username,
                            @Param("startDate") LocalDate startDate,
                            @Param("endDate") LocalDate endDate);

    /**
     * Number of expenses in a date range.
     */
    @Query("SELECT COUNT(e) FROM Expense e " +
           "WHERE e.username = :username AND e.date BETWEEN :startDate AND :endDate")
    Long getExpenseCount(@Param("username") String username,
                         @Param("startDate") LocalDate startDate,
                         @Param("endDate") LocalDate endDate);

    /**
     * Category-wise spending breakdown, ordered by highest spend first.
     * Returns List of [category (String), totalAmount (Double)].
     */
    @Query("SELECT e.category, SUM(e.amount) FROM Expense e " +
           "WHERE e.username = :username AND e.date BETWEEN :startDate AND :endDate " +
           "GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> getCategoryWiseSpending(@Param("username") String username,
                                           @Param("startDate") LocalDate startDate,
                                           @Param("endDate") LocalDate endDate);

    /**
     * All expenses in a date range, sorted by amount descending (for top-N).
     */
    @Query("SELECT e FROM Expense e " +
           "WHERE e.username = :username AND e.date BETWEEN :startDate AND :endDate " +
           "ORDER BY e.amount DESC")
    List<Expense> getExpensesSortedByAmount(@Param("username") String username,
                                            @Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    /**
     * Daily spending aggregation for a date range.
     * Returns List of [date (LocalDate), totalAmount (Double)].
     */
    @Query("SELECT e.date, SUM(e.amount) FROM Expense e " +
           "WHERE e.username = :username AND e.date BETWEEN :startDate AND :endDate " +
           "GROUP BY e.date ORDER BY e.date")
    List<Object[]> getDailySpending(@Param("username") String username,
                                     @Param("startDate") LocalDate startDate,
                                     @Param("endDate") LocalDate endDate);
}
