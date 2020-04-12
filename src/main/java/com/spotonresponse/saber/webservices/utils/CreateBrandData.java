package com.spotonresponse.saber.webservices.utils;


import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;


public class CreateBrandData {

    private static boolean filterforArcGis = false;

    static org.slf4j.Logger logger = LoggerFactory.getLogger(CreateBrandData.class);


    public static JSONObject build(JSONArray jArray, boolean fulloutput, String arcgis) {
        if (arcgis.toLowerCase().equals("true")) {
            filterforArcGis = true;
        }
        JSONObject jo = build(jArray, fulloutput);
        return jo;
    }



    public static JSONObject build(JSONArray jArray, boolean fulloutput) {

        JSONArray outputArray = new JSONArray();

        ArrayList<String> brandNames = new ArrayList<String>();

        for (int i = 0; i < jArray.length(); i++) {
            JSONObject itemJson = new JSONObject();
            try {
                JSONObject properties = jArray.getJSONObject(i);
                itemJson = properties.getJSONObject("item");

                brandNames.add(itemJson.getString("Brand Name"));

            } catch (Exception ex) {
                logger.warn("Unable to add item to brand name output: " + ex);
                logger.error(ex.getMessage());
            }

        }

        HashSet<String> hashBrandNames = new HashSet(brandNames);
        for(String bn : hashBrandNames) {
            JSONObject jo = new JSONObject();
            jo.put("brand", bn);
            outputArray.put(jo);
        }

        int items = hashBrandNames.size();
        JSONObject out = new JSONObject();
        out.put("itemCount", items);
        out.put("data", outputArray);

        return out;
    }

}
