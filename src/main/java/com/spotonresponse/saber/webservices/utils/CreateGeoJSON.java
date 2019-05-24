package com.spotonresponse.saber.webservices.utils;


import org.json.JSONArray;
import org.json.JSONException;
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


                // If we we not given a status, assume "Open" for the icon color
                String useStatus = "Open";
                try {
                    if (itemJson.getString("status") != null) {
                        useStatus = itemJson.getString("status");
                    }
                } catch (JSONException jex) {
                    logger.warn("No Status Provided");
                }

                String iconquery = "";
                try {
                    if (itemJson.getString("Data Source URL") != null) {
                        iconquery += itemJson.getString("Data Source URL");
                    }
                } catch (JSONException jex) {
                    logger.warn("DataSourceUrl was not found");
                }

                try {
                    if (itemJson.getString("title") != null) {
                        iconquery += itemJson.getString("title");
                    }
                } catch (JSONException jex) {
                    logger.warn("Title was not found");
                }


                String icon = SorTools.determineIcon(useStatus, iconquery);
                itemJson.put("icon", icon);

                double latitude = Double.valueOf(loc[0]);
                double longitude = Double.valueOf(loc[1]);

                JSONArray coords = new JSONArray();
                coords.put(longitude);
                coords.put(latitude);

                JSONObject geometry = new JSONObject();
                geometry.put("type", "Point");
                geometry.put("coordinates", coords);

                // Suppress the "Content" field from the JSON and CSV adapters
                itemJson.remove("content");

                JSONObject feature = new JSONObject();
                feature.put("type", "Feature");
                feature.put("geometry", geometry);
                feature.put("properties", itemJson);

                featuresArray.put(feature);

            } catch (Exception ex) {
                logger.warn("Unable to add item to GeoJSON: " + ex);
                logger.error(ex.getMessage());
            }

        }


        JSONObject fc = new JSONObject();
        fc.put("type", "FeatureCollection");
        fc.put("itemCount", items);
        fc.put("features", featuresArray);

        return fc;
    }
}
