package me.plugin.serene.listeners;

import me.plugin.serene.actions.SleepHandler;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class SleepListenerTest {

    @Test
    void nextDayFullTimeFirstDay() {
        long nextDayFullTime = SleepHandler.nextDayFullTime(12400);
        assertThat(nextDayFullTime).isEqualTo(24000);
    }

    @Test
    void nextDayFullTimeSecondDay() {
        long nextDayFullTime = SleepHandler.nextDayFullTime(44000);
        assertThat(nextDayFullTime).isEqualTo(48000);
    }

    @Test
    void hundredDays() {
        int hundredthNight = 2_412_900;
        long nextDayFullTime = SleepHandler.nextDayFullTime(hundredthNight);
        assertThat(nextDayFullTime).isEqualTo(2_424_000);
    }
}