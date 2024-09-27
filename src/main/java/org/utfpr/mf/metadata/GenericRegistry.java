package org.utfpr.mf.metadata;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GenericRegistry {

    private HashMap<String, String> values = new HashMap<>();
    private HashMap<String, Function<String, ?>> types = new HashMap<>();

    public Object get(String column) {
        if(types.containsKey(column)){
            return types.get(column).apply(values.get(column));
        }

        return values.get(column);
    }

     public <T> T get(String column, Class<T> clazz, Function<String, T> converter) {
        return clazz.cast( converter.apply( values.get(column) ));
    }

    public void set(String Column, String value) {
        values.put(Column, value);
    }

//    public void mapToColumn(Table table) {
//
//        throw new RuntimeException("Not implemented");
//
//        var columns = table.columns();
//        for(var c : columns){
//            var typeId = c.dataType().getSqlType();
//            if (1 <= typeId && typeId <= 4) {
//                types.put(c.name(), Integer::valueOf);
//                continue;
//            }
//            if (5 <= typeId && typeId <= 9) {
//                types.put(c.name(), Double::valueOf);
//                continue;
//            }
//            if (10 <= typeId && typeId <= 17) {
//                types.put(c.name(), String::valueOf);
//                continue;
//            }
//            if (19 <= typeId && typeId <= 22) {
//                types.put(c.name(), GenericRegistry::convertStringToDate);
//                continue;
//            }
//            if (typeId == 18) {
//                types.put(c.name(), Boolean::valueOf);
//            }
//        }
//    }

    public String generateMD5() {
        var sorted = values.entrySet()
                .stream()
                .sorted((i1, i2)
                        -> i1.getKey().compareTo(
                        i2.getKey())).toList();
        HashMap<String, String> temp = sorted.stream().collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1, LinkedHashMap::new));
        String concat = temp.values().stream().reduce("", String::concat);
        return org.apache.commons.codec.digest.DigestUtils.md5Hex(concat);
    }

    static private Date convertStringToDate(String date){
        var l = LocalDateTime.parse(date).atOffset(ZoneOffset.UTC);
        return Date.from(l.toInstant());
    }
}
