package xyz.tmuapp.utils;

import java.util.*;

public class ArrayUtils {

    public static boolean isNull(Object collectionMapArray) {
        return collectionMapArray == null;
    }

    public static boolean isNotEmpty(Object collectionMapArray) {
        return !isEmpty(collectionMapArray);
    }

    public static boolean isEmpty(Object collectionMapArray, Integer... lengthArr) {
        int length = lengthArr != null && lengthArr.length > 0 ? lengthArr[0] : 1;
        if (collectionMapArray == null) {
            return true;
        } else if (collectionMapArray instanceof Collection) {
            return ((Collection) collectionMapArray).size() < length;
        } else if (collectionMapArray instanceof Map) {
            return ((Map) collectionMapArray).size() < length;
        } else if (collectionMapArray instanceof Object[]) {
            return ((Object[]) collectionMapArray).length < length || ((Object[]) collectionMapArray)[length - 1] == null;
        } else return true;
    }

    public static List<String> splitString(String str, String regex) {
        try {
            return new ArrayList<>(Arrays.asList(str.split(regex)));
        } catch (NullPointerException ex) {
            return new ArrayList<>();
        }
    }
}
