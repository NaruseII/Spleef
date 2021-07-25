package fr.naruse.spleef.database;

import fr.naruse.dbapi.api.DatabaseAPI;
import fr.naruse.dbapi.database.Database;
import fr.naruse.dbapi.sql.SQLHelper;
import fr.naruse.dbapi.sql.SQLRequest;
import fr.naruse.dbapi.sql.SQLResponse;
import fr.naruse.spleef.main.SpleefPlugin;
import fr.naruse.spleef.player.SpleefPlayer;
import fr.naruse.spleef.player.statistic.StatisticBuilder;
import fr.naruse.spleef.player.statistic.StatisticType;
import org.bukkit.OfflinePlayer;

import java.util.Map;

public class DatabaseSQLManager implements IDatabaseManager {

    private final SpleefPlugin pl;
    private final Database database;
    private final String TABLE_NAME;

    public DatabaseSQLManager(SpleefPlugin spleefPlugin) {
        this.pl = spleefPlugin;
        this.TABLE_NAME = pl.getConfig().getString("sql.tableName");

        DatabaseAPI.createNewDatabase(database = new Database("Spleef", TABLE_NAME) {
            @Override
            public String getQuery() {
                return "CREATE TABLE `" + TABLE_NAME + "` ("
                        + "`uuid` varchar(64) COLLATE utf8_unicode_ci NOT NULL,"
                        + "`properties` varchar(8000) NOT NULL)";
            }
        });
    }

    @Override
    public void isRegistered(String uuid, fr.naruse.spleef.database.SpleefSQLResponse sqlResponse, boolean inMainThread){
        SQLRequest sqlRequest = new SQLRequest(SQLHelper.getSelectRequest(TABLE_NAME, "properties", "uuid"), uuid);
        if(inMainThread){
            boolean exists = database.hasDirectAccount(sqlRequest);
            sqlResponse.handleResponse(exists);
        }else{
            database.hasAccount(sqlRequest, new SQLResponse() {
                @Override
                public void handleResponse(Object response) {
                    super.handleResponse(response);
                    if (response == null) {
                        return;
                    }
                    boolean exists = (boolean) response;
                    sqlResponse.handleResponse(exists);
                }
            });
        }
    }

    @Override
    public void register(String uuid, Map<StatisticType, Integer> map) {
        SQLRequest sqlRequest = new SQLRequest(SQLHelper.getInsertRequest(TABLE_NAME, new String[]{"uuid", "properties"}), uuid, StatisticBuilder.toJson(map));
        database.prepareStatement(sqlRequest);
    }

    @Override
    public void getProperties(String uuid, SpleefSQLResponse sqlResponse) {
        SQLRequest.GetObject sqlRequest = new SQLRequest.GetObject(SQLHelper.getSelectRequest(TABLE_NAME, "properties", "uuid"), "properties", uuid);
        database.getObject(sqlRequest, new SQLResponse() {
            @Override
            public void handleResponse(Object response) {
                super.handleResponse(response);
                if (response == null) {
                    return;
                }
                sqlResponse.handleResponse(response);
            }
        });
    }

    @Override
    public void save(String uuid, String toJson, boolean mainThread) {
        SQLRequest sqlRequest = new SQLRequest(SQLHelper.getUpdateRequest(TABLE_NAME, "properties", "uuid"), toJson, uuid);
        if(mainThread){
            database.prepareDirectStatement(sqlRequest);
        }else{
            database.prepareStatement(sqlRequest);
        }
    }

    @Override
    public void clearAll() {
        SQLRequest sqlRequest = new SQLRequest(SQLHelper.getTruncateRequest(TABLE_NAME));
        database.prepareStatement(sqlRequest);
        for (Map.Entry<OfflinePlayer, SpleefPlayer> entry : pl.getSpleefPlayerRegistry().getSpleefPlayerHashMap()) {
            SpleefPlayer spleefPlayer = entry.getValue();
            for (StatisticType value : StatisticType.values()) {
                spleefPlayer.setStatistic(value, 0);
            }
        }
    }
}
