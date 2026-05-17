package com.maxeriksson.SessionBillingAPI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Unit tests for the legacy command-line runner registration contract. */
class CommandLineRunnerImplTest {

    @Test
    void commandLineRunnerIsAvailableOnlyWhenCliPropertyIsEnabled() {
        Component component = CommandLineRunnerImpl.class.getAnnotation(Component.class);
        ConditionalOnProperty condition =
                CommandLineRunnerImpl.class.getAnnotation(ConditionalOnProperty.class);

        assertNotNull(component);
        assertNotNull(condition);
        assertEquals("session-billing.cli.enabled", condition.name()[0]);
        assertEquals("true", condition.havingValue());
    }
}
