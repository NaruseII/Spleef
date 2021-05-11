package fr.naruse.spleef.database;

import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.player.statistic.StatisticBuilder;
import fr.naruse.spleef.player.statistic.StatisticType;

import java.util.Map;

public class DatabaseYAMLManagerDatabase implements IDatabaseManager {

    private final SpleefPlugin pl;

    public DatabaseYAMLManagerDatabase(SpleefPlugin spleefPlugin) {
        this.pl = spleefPlugin;
    }

    @Override
    public void isRegistered(String uuid, SpleefSQLResponse sqlResponse, boolean inMainThread) {
        sqlResponse.handleResponse(pl.getConfigurations().getStatisticsConfiguration().contains(uuid));
    }

    @Override
    public void register(String uuid, Map<StatisticType, Integer> map) {
        pl.getConfigurations().getStatisticsConfiguration().set(uuid, StatisticBuilder.toJson(map));
        pl.getConfigurations().saveConfigs();
    }

    @Override
    public void getProperties(String uuid, SpleefSQLResponse sqlResponse) {
        sqlResponse.handleResponse(pl.getConfigurations().getStatisticsConfiguration().getString(uuid));
    }

    @Override
    public void save(String uuid, String toJson, boolean mainThread) {
        pl.getConfigurations().getStatisticsConfiguration().set(uuid, toJson);
        pl.getConfigurations().saveConfigs();
    }

    @Override
    public void clearAll() {
        pl.getConfigurations().reset(1);
    }
}
