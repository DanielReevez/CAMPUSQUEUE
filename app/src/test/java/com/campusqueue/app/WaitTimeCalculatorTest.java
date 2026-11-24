package com.campusqueue.app;

import static org.junit.Assert.*;

import com.campusqueue.app.model.ServiceOffice;
import com.campusqueue.app.service.WaitTimeCalculator;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WaitTimeCalculatorTest {

    @Test
    public void testFallbackToDefaultAvgDurationWhenNoDurations() {
        ServiceOffice office = new ServiceOffice("ICT", "Main", "Open", 5, "123", "a@b.com");
        double avg = WaitTimeCalculator.calculateDynamicAvgDuration(office, Collections.emptyList());
        assertEquals(5.0, avg, 0.01);
    }

    @Test
    public void testDynamicAvgDurationWithRecentDurations() {
        ServiceOffice office = new ServiceOffice("ICT", "Main", "Open", 10, "123", "a@b.com");
        List<Integer> recent = Arrays.asList(4, 6, 5); // avg = 5.0
        // Weighted: 0.7 * 5.0 + 0.3 * 10 = 3.5 + 3.0 = 6.5
        double avg = WaitTimeCalculator.calculateDynamicAvgDuration(office, recent);
        assertEquals(6.5, avg, 0.01);
    }

    @Test
    public void testEstimatedWaitTimeZeroPosition() {
        ServiceOffice office = new ServiceOffice("ICT", "Main", "Open", 5, "123", "a@b.com");
        int waitMins = WaitTimeCalculator.calculateEstimatedWaitTime(office, Collections.emptyList(), 0);
        assertEquals(0, waitMins);
    }

    @Test
    public void testEstimatedWaitTimePositivePosition() {
        ServiceOffice office = new ServiceOffice("ICT", "Main", "Open", 5, "123", "a@b.com");
        int waitMins = WaitTimeCalculator.calculateEstimatedWaitTime(office, Collections.emptyList(), 4);
        assertEquals(20, waitMins);
    }
}
