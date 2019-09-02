package com.spotonresponse.saber.webservices.utils;

import com.vividsolutions.jts.geom.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.util.*;

public class Util {

    public static JSONObject nullToNA(JSONObject json) {

        Set<String> keySet = json.keySet();
        for (String key : keySet) {
            Object value = json.get(key);
            if (value instanceof String) {
                if (((String) value).length() == 0) {
                    json.put(key, "N/A");
                }
            }
        }
        return json;
    }

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {

        Map<String, Object> retMap = new HashMap<String, Object>();

        if (json != JSONObject.NULL) {
            retMap = toMap(json);
        }
        return retMap;
    }

    public static Map<String, String> convertKeyValue(Map<String, Object> map) {

        Map<String, String> keyValueMap = new HashMap<String, String>();

        Set<String> keys = map.keySet();
        try {
            for (String key : keys) {
                keyValueMap.put(key, String.valueOf(map.get(key)).trim());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return keyValueMap;
    }

    public static Map<String, Object> toMap(JSONObject object) throws JSONException {

        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {

        List<Object> list = new ArrayList<Object>();
        for (int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if (value instanceof JSONArray) {
                value = toList((JSONArray) value);
            } else if (value instanceof JSONObject) {
                value = toMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }



    public static boolean IsInsideBoundingBox(Double[][] boundingBox, String lat, String lon) {

        final Coordinate coords = new Coordinate(Double.parseDouble(lon), Double.parseDouble(lat));
        final Point point = new GeometryFactory().createPoint(coords);

        return contains(boundingBox, point);
    }

    public static String ToHash(String key) {

        byte[] bytes = key.getBytes();

        MessageDigest md5hash = null;
        try {
            md5hash = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            return null;
        }
        md5hash.update(bytes);
        byte[] digest = md5hash.digest();
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }

    private static boolean contains(Double[][] bb, Point point) {

        final LinearRing bbLinerRing = new GeometryFactory().createLinearRing(getCoordinateArray(bb));
        final Polygon bbPloygon = new GeometryFactory().createPolygon(bbLinerRing, null);
        return point.within(bbPloygon);
    }

    private static Coordinate[] getCoordinateArray(Double[][] coords) {

        final List<Coordinate> coordianteList = new ArrayList<Coordinate>();
        for (final Double[] coord : coords)
            coordianteList.add(new Coordinate(coord[0], coord[1]));
        return coordianteList.toArray(new Coordinate[coordianteList.size()]);
    }

    public static boolean isValidCoordinate(String lat, String lon) {
        boolean isCoordinate = true;
        try {
            Coordinate coords = new Coordinate(Double.parseDouble(lat), Double.parseDouble(lon));
        } catch (Exception ex) {
            isCoordinate = false;
        }

        return isCoordinate;
    }

    public static boolean isValidCoordinate(Double lat, Double lon) {
        boolean isCoordinate = true;
        try {
            Coordinate coords = new Coordinate(lat, lon);
        } catch (Exception ex) {
            isCoordinate = false;
        }

        return isCoordinate;
    }


}
