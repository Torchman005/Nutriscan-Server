package com.luminous.nutriscan.controller;

import com.luminous.nutriscan.model.UserStatistics;
import com.luminous.nutriscan.service.UserStatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/statistics")
public class UserStatisticsController {

    @Autowired
    private UserStatisticsService userStatisticsService;

    /**
     * 获取今日统计数据
     */
    @GetMapping("/today")
    public ResponseEntity<UserStatistics> getTodayStatistics(@RequestParam("userId") String userId) {
        UserStatistics stats = userStatisticsService.getTodayStatistics(userId);
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取指定日期统计数据
     */
    @GetMapping("/date")
    public ResponseEntity<UserStatistics> getStatisticsForDate(
            @RequestParam("userId") String userId,
            @RequestParam("date") String dateStr) {
        LocalDate date = LocalDate.parse(dateStr);
        UserStatistics stats = userStatisticsService.getStatisticsForDate(userId, date);
        return ResponseEntity.ok(stats);
    }

    /**
     * 获取周统计数据
     */
    @GetMapping("/week")
    public ResponseEntity<List<UserStatistics>> getWeekStatistics(@RequestParam("userId") String userId) {
        List<UserStatistics> stats = userStatisticsService.getWeekStatistics(userId);
        return ResponseEntity.ok(stats);
    }
}
