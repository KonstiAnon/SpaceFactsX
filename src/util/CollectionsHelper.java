package util;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CollectionsHelper {

    /**
     * This function sorts a Map in descending order
     * @param map   The map to be sorted
     * @return      A new map which is a sorted version of the parameter map
     */
    public static Map<String, Integer> sortDescending(Map<String, Integer> map){
        List<Map.Entry<String, Integer>> list = new LinkedList<>(map.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()) == 0
        ? o2.getKey().compareTo(o1.getKey())
        : o2.getValue().compareTo(o1.getValue()));
        return list.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
    }

    /**
     * This function sorts a Map in descending order
     * @param map   The map to be sorted
     * @return      A new map which is a sorted version of the parameter map
     */
    public static Map<String, Long> sortDescendingLong(Map<String, Long> map){
        List<Map.Entry<String, Long>> list = new LinkedList<>(map.entrySet());
        list.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()) == 0
                ? o2.getKey().compareTo(o1.getKey())
                : o2.getValue().compareTo(o1.getValue()));
        return list.stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b, LinkedHashMap::new));
    }
}
