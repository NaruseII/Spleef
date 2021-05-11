package fr.naruse.spleef.database;

import fr.naruse.spleef.player.statistic.StatisticType;

import java.util.Map;

public interface IDatabaseManager {

    default void isRegistered(String uuid, SpleefSQLResponse sqlResponse) {
        this.isRegistered(uuid, sqlResponse, false);
    }

    void isRegistered(String uuid, SpleefSQLResponse sqlResponse, boolean inMainThread) ;

    void register(String uuid, Map<StatisticType, Integer> map) ;

    void getProperties(String uuid, SpleefSQLResponse sqlResponse) ;

    default void save(String uuid, String toJson) {
        this.save(uuid, toJson, false);
    }

    void save(String uuid, String toJson, boolean mainThread) ;

    void clearAll() ;

}
