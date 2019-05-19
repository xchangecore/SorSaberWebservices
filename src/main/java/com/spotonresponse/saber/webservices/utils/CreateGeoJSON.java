package com.spotonresponse.saber.webservices.utils;


import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;


public class CreateGeoJSON {


    static org.slf4j.Logger logger = LoggerFactory.getLogger(CreateGeoJSON.class);

    public static JSONObject build(JSONArray jArray) {

        JSONArray featuresArray = new JSONArray();
        int items = jArray.length();


        for (int i = 0; i < jArray.length(); i++) {
            try {
                JSONObject properties = jArray.getJSONObject(i);

                JSONObject itemJson = properties.getJSONObject("item");
                JSONObject where = itemJson.getJSONObject("where");
                JSONObject point = where.getJSONObject("Point");
                String pos = point.getString("pos");
                String[] loc = pos.split(" ");

                String icon = SorTools.determineIcon(itemJson.getString("status"), itemJson.getString("Data Source URL") + " " + itemJson.getString("title"));
                itemJson.put("icon", icon);

                double latitude = Double.valueOf(loc[0]);
                double longitude = Double.valueOf(loc[1]);

                JSONArray coords = new JSONArray();
                coords.put(longitude);
                coords.put(latitude);

                JSONObject geometry = new JSONObject();
                geometry.put("type", "Point");
                geometry.put("coordinates", coords);

                JSONObject feature = new JSONObject();
                feature.put("type", "Feature");
                feature.put("geometry", geometry);
                feature.put("properties", itemJson);

                featuresArray.put(feature);

            } catch (Exception ex) {
                logger.warn("Unable to add item to GeoJSON");
            }

        }


        JSONObject fc = new JSONObject();
        fc.put("type", "FeatureCollection");
        fc.put("itemCount", items);
        fc.put("features", featuresArray);

        return fc;
    }
}
