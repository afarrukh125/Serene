package me.plugin.serene.model;

import me.plugin.serene.statistics.CustomStatistic;
import me.plugin.serene.statistics.DeathStatistic;
import me.plugin.serene.statistics.StatisticsRepository;
import me.plugin.serene.statistics.TimePlayedStatistic;
import org.bukkit.Statistic;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatisticsRepositoryTest {

    @Test
    public void testSimpleTranslation() {
        StatisticsRepository statisticsRepository = new StatisticsRepository()
                .registerStatistic(new DeathStatistic());

        CustomStatistic deaths = statisticsRepository.getStatisticForString(DeathStatistic.MAIN_NAME);
        assertThat(deaths.getStatistic()).isEqualTo(Statistic.DEATHS);
        assertThat(statisticsRepository.getStatisticForString(DeathStatistic.MAIN_NAME).readableValue(5.)).isEqualTo(5.);
    }

    @Test
    public void testTimePlayed() {
        StatisticsRepository statisticsRepository = new StatisticsRepository()
                .registerStatistic(new TimePlayedStatistic());

        CustomStatistic timePlayed = statisticsRepository.getStatisticForString("playtime");
        assertThat(timePlayed.getStatistic()).isEqualTo(Statistic.PLAY_ONE_MINUTE);
        assertThat(timePlayed.readableValue(2376000)).isEqualTo(33);
    }

}
