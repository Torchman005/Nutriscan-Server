package com.luminous.nutriscan.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

/**
 * 用户统计数据模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics {
    // 日期
    private LocalDate date;

    // 热量相关
    private Double totalCalories;        // 总热量(千卡)
    private Double calorieGoal;          // 热量目标(千卡)
    private Double calorieProgress;      // 热量进度百分比(0-100)

    // 营养数据
    private Double protein;              // 蛋白质(g)
    private Double carbohydrates;        // 碳水化合物(g)
    private Double fat;                  // 脂肪(g)
    private Double fiber;                // 纤维素(g)

    // 权重数据
    private Double weight;               // 当前体重(kg)
    private Double targetWeight;         // 目标体重(kg)
    private Double weightChange;         // 体重变化(kg)

    // BMI和代谢
    private Double bmi;                  // BMI指数
    private Double bmr;                  // 基础代谢(千卡/天)
    private Integer achievementRate;     // 目标达成率(%)

    // 记录数
    private Integer foodCount;           // 当天食物识别数量
    private Integer dayStreak;           // 连续记录天数
}
