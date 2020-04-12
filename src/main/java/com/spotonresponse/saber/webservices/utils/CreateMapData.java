package com.spotonresponse.saber.webservices.utils;


import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;


public class CreateMapData {

    private static boolean filterforArcGis = false;

    static org.slf4j.Logger logger = LoggerFactory.getLogger(CreateMapData.class);


    public static JSONObject build(JSONArray jArray, boolean fulloutput, String arcgis) {
        if (arcgis.toLowerCase().equals("true")) {
            filterforArcGis = true;
        }
        JSONObject jo = build(jArray, fulloutput);
        return jo;
    }



    public static JSONObject build(JSONArray jArray, boolean fulloutput) {

        JSONArray outputArray = new JSONArray();
        int items = jArray.length();

        for (int i = 0; i < jArray.length(); i++) {
            JSONObject itemJson = new JSONObject();
            try {
                JSONObject properties = jArray.getJSONObject(i);

                String pk = properties.getString("title");

                itemJson = properties.getJSONObject("item");
                JSONObject where = itemJson.getJSONObject("where");
                JSONObject point = where.getJSONObject("Point");
                String pos = point.getString("pos");
                String[] loc = pos.split(" ");
                double latitude = Double.valueOf(loc[0]);
                double longitude = Double.valueOf(loc[1]);


                JSONObject jo = new JSONObject();
                jo.put("latitude", latitude);
                jo.put("longitude", longitude);
                jo.put("pk", pk);

                outputArray.put(jo);

            } catch (Exception ex) {
                logger.warn("Unable to add item to mapData output: " + ex);
                logger.error(ex.getMessage());
            }

        }

        JSONObject out = new JSONObject();
        out.put("itemCount", items);
        out.put("data", outputArray);

        return out;
    }

}
