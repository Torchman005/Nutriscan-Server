package com.luminous.nutriscan.service;

import com.luminous.nutriscan.model.User;
import com.luminous.nutriscan.model.UserCalorieLog;
import com.luminous.nutriscan.model.UserStatistics;
import com.luminous.nutriscan.model.UserWeightLog;
import com.luminous.nutriscan.repository.UserRepository;
import com.luminous.nutriscan.repository.UserCalorieLogRepository;
import com.luminous.nutriscan.repository.UserWeightLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserStatisticsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserCalorieLogRepository calorieLogRepository;

    @Autowired
    private UserWeightLogRepository weightLogRepository;

    public UserStatistics getTodayStatistics(String userId) {
        return getStatisticsForDate(userId, LocalDate.now());
    }

    public UserStatistics getStatisticsForDate(String userId, LocalDate date) {
        Optional<User> userOpt = userRepository.findByUserUid(userId);
        if (!userOpt.isPresent()) {
            return new UserStatistics();
        }

        User user = userOpt.get();
        UserStatistics stats = new UserStatistics();
        stats.setDate(date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        List<UserCalorieLog> dayRecords = calorieLogRepository
                .findByUserUidAndRecordedAtBetweenOrderByRecordedAtAsc(userId, startOfDay, endOfDay);

        double totalCalories = dayRecords.stream()
                .map(UserCalorieLog::getCalories)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        stats.setTotalCalories(totalCalories);
        stats.setCalorieGoal(2000.0);
        stats.setCalorieProgress(totalCalories / 2000.0 * 100);

        stats.setProtein(0.0);
        stats.setCarbohydrates(0.0);
        stats.setFat(0.0);
        stats.setFiber(0.0);

        Double resolvedWeight = resolveWeightForDate(userId, date, user.getWeight());
        stats.setWeight(resolvedWeight);
        Double targetWeight = user.getTargetWeight();
        if (targetWeight == null && resolvedWeight != null) {
            targetWeight = resolvedWeight - 5;
        }
        stats.setTargetWeight(targetWeight != null ? targetWeight : 60.0);

        if (user.getHeight() != null && resolvedWeight != null) {
            double heightInMeters = user.getHeight() / 100.0;
            stats.setBmi(resolvedWeight / (heightInMeters * heightInMeters));
        }
        stats.setBmr(user.getBmr() != null ? user.getBmr().doubleValue() : 1500.0);

        if (stats.getCalorieGoal() > 0) {
            int achievementRate = (int) Math.min(100, stats.getCalorieProgress());
            stats.setAchievementRate(achievementRate);
        }

        stats.setFoodCount(dayRecords.size());
        stats.setDayStreak(calculateDayStreak(userId));

        return stats;
    }

    public List<UserStatistics> getWeekStatistics(String userId) {
        User user = userRepository.findByUserUid(userId).orElse(null);
        if (user == null) return Collections.emptyList();
        List<UserStatistics> weekStats = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Double logWeight = resolveWeightFromLogOnly(userId, date);
            UserStatistics stats = getStatisticsForDate(userId, date);
            stats.setWeight(logWeight);
            weekStats.add(stats);
        }
        return weekStats;
    }

    private int calculateDayStreak(String userId) {
        int streak = 0;
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(60);

        List<UserCalorieLog> recentLogs = calorieLogRepository
                .findByUserUidAndRecordedAtBetweenOrderByRecordedAtAsc(
                        userId,
                        startDate.atStartOfDay(),
                        today.atTime(23, 59, 59)
                );

        Set<LocalDate> daysWithRecords = new HashSet<>();
        for (UserCalorieLog log : recentLogs) {
            if (log.getRecordedAt() != null) {
                daysWithRecords.add(log.getRecordedAt().toLocalDate());
            }
        }

        LocalDate currentDate = today;
        while (daysWithRecords.contains(currentDate)) {
            streak++;
            currentDate = currentDate.minusDays(1);
        }

        return streak;
    }

    private Double resolveWeightForDate(String userId, LocalDate date, Double fallbackWeight) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        List<UserWeightLog> logs = weightLogRepository
                .findByUserUidAndRecordedAtBetweenOrderByRecordedAtAsc(userId, startOfDay, endOfDay);
        if (logs.isEmpty()) {
            return fallbackWeight;
        }
        for (int i = logs.size() - 1; i >= 0; i--) {
            Double weight = logs.get(i).getWeight();
            if (weight != null) {
                return weight;
            }
        }
        return fallbackWeight;
    }

    private Double resolveWeightFromLogOnly(String userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        List<UserWeightLog> logs = weightLogRepository
                .findByUserUidAndRecordedAtBetweenOrderByRecordedAtAsc(userId, startOfDay, endOfDay);
        if (logs.isEmpty()) {
            return null;
        }
        for (int i = logs.size() - 1; i >= 0; i--) {
            Double weight = logs.get(i).getWeight();
            if (weight != null) {
                return weight;
            }
        }
        return null;
    }
}
