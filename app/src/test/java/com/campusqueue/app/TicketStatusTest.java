package com.campusqueue.app;

import static org.junit.Assert.*;

import com.campusqueue.app.model.TicketStatus;

import org.junit.Test;

public class TicketStatusTest {

    @Test
    public void testValidTransitions() {
        assertTrue(TicketStatus.WAITING.canTransitionTo(TicketStatus.CALLED));
        assertTrue(TicketStatus.WAITING.canTransitionTo(TicketStatus.CANCELLED));

        assertTrue(TicketStatus.CALLED.canTransitionTo(TicketStatus.IN_SERVICE));
        assertTrue(TicketStatus.CALLED.canTransitionTo(TicketStatus.NO_SHOW));

        assertTrue(TicketStatus.IN_SERVICE.canTransitionTo(TicketStatus.COMPLETED));
        assertTrue(TicketStatus.IN_SERVICE.canTransitionTo(TicketStatus.RETURN_REQUIRED));
    }

    @Test
    public void testInvalidTransitions() {
        assertFalse(TicketStatus.WAITING.canTransitionTo(TicketStatus.COMPLETED));
        assertFalse(TicketStatus.COMPLETED.canTransitionTo(TicketStatus.WAITING));
        assertFalse(TicketStatus.CANCELLED.canTransitionTo(TicketStatus.IN_SERVICE));
    }

    @Test
    public void testFromStringParsing() {
        assertEquals(TicketStatus.WAITING, TicketStatus.fromString("Waiting"));
        assertEquals(TicketStatus.IN_SERVICE, TicketStatus.fromString("In Service"));
        assertEquals(TicketStatus.RETURN_REQUIRED, TicketStatus.fromString("RETURN_REQUIRED"));
        assertEquals(TicketStatus.WAITING, TicketStatus.fromString(null));
    }
}
