package fr.naruse.spleef.player.statistic;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class StatisticBuilder {

    public static final Gson GSON = new Gson();
    public static final Type MAP_TYPE = new TypeToken<Map<String, Object>>(){}.getType();

    public static String toJson(Map<StatisticType, Integer> map){
        if(map == null){
            map = Maps.newHashMap();
            for (StatisticType value : StatisticType.values()) {
                map.put(value, 0);
            }
        }
        return GSON.toJson(map);
    }

    public static void fromJson(String json, Map<StatisticType, Integer> statisticMap) {
        Map<String, Object> propertyMap = GSON.fromJson(json, MAP_TYPE);
        try{
            for (String s : propertyMap.keySet()) {
                StatisticType statisticType = StatisticType.valueOf(s);
                int value;
                if(propertyMap.get(s).toString().contains(".")){
                    value = (int) Double.parseDouble(propertyMap.get(s).toString());
                }else{
                    value = Integer.parseInt(propertyMap.get(s).toString());
                }

                statisticMap.put(statisticType, value);
            }
        }catch (Exception e) {
            e.printStackTrace();
        }

        for (StatisticType value : StatisticType.values()) {
            if(!statisticMap.containsKey(value)){
                statisticMap.put(value, 0);
            }
        }
    }
}
