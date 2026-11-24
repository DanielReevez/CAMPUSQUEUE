package com.campusqueue.app.service;

import com.campusqueue.app.model.ServiceOffice;

import java.util.List;

public class WaitTimeCalculator {

    /**
     * Calculates dynamic average service duration in minutes based on recent actual completions.
     */
    public static double calculateDynamicAvgDuration(ServiceOffice office, List<Integer> recentDurations) {
        if (office == null) return 5.0;

        int defaultAvg = office.getAvgServiceTimeMins() > 0 ? office.getAvgServiceTimeMins() : 5;
        if (recentDurations == null || recentDurations.isEmpty()) {
            return defaultAvg;
        }

        double sum = 0;
        for (int duration : recentDurations) {
            sum += duration;
        }
        double actualAvg = sum / recentDurations.size();

        // Weighted combination: 70% actual recent performance, 30% baseline office average
        return Math.round((0.7 * actualAvg + 0.3 * defaultAvg) * 10.0) / 10.0;
    }

    /**
     * Calculates total estimated wait time in minutes for a student given their position ahead in line.
     */
    public static int calculateEstimatedWaitTime(ServiceOffice office, List<Integer> recentDurations, int positionAhead) {
        if (positionAhead <= 0) {
            return 0;
        }
        double dynamicAvg = calculateDynamicAvgDuration(office, recentDurations);
        return (int) Math.ceil(positionAhead * dynamicAvg);
    }
}
