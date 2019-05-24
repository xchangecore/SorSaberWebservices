package com.spotonresponse.saber.webservices.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class SorTools2 {

    static Logger logger = LoggerFactory.getLogger(SorTools.class);

    private static Map<String, String> iconmap = new HashMap<String, String>();

    public static String determineIcon(String status, String client) {

        // Add icons to the icon map
        iconmap.put("walmart", "WalmartIcon.png");
        iconmap.put("7-eleven", "7-Eleven.png");
        iconmap.put("bp oil", "bp-logo.png");
        iconmap.put("k-mart", "K-mart.png");
        iconmap.put("best buy", "Best Buy.png");
        iconmap.put("dennys", "Dennys.png");
        iconmap.put("kfc", "KFC.png");
        iconmap.put("waffle house", "waffle house.png");
        iconmap.put("pizza hut", "Pizza Hut.png");
        iconmap.put("sonic", "Sonic.png");
        iconmap.put("starbucks", "Starbucks.png");
        iconmap.put("taco bell", "Taco Bell.png");
        iconmap.put("walls fargo", "Wells Fargo.png");
        iconmap.put("dicks ", "DicksSportingGoods.png");
        iconmap.put("dick's ", "DicksSportingGoods.png");
        iconmap.put("dicks ", "DicksSportingGoods.png");
        iconmap.put("macys", "MacysIcon.png");
        iconmap.put("macy's", "MacysIcon.png");
        iconmap.put("sears", "SearsIcon.png");

        String icon = "";


        // Add an icon to display with the GeoJSON file
        // Use GrayScale icons for closed status
        try {
            String icondir = "";
            if (!status.toLowerCase().equals("open")) {
                icondir = "GS/";
            }

            String mapMarkerDirectory = "https://app.spotonresponse.com/MapMarkers/" + icondir;


            // TODO: Enhance this bit to use a lookup table for customers
            //  and the associated icons
            logger.debug("Searching icons for string: " + client.toLowerCase());

            for (Map.Entry<String, String> iconEntry : iconmap.entrySet()) {
                if (client.toLowerCase().contains(iconEntry.getKey())) {
                    icon = mapMarkerDirectory + iconEntry.getValue();
                    break;
                }
            }

        } catch (org.json.JSONException jex) {
            logger.warn("Entity does not contain status or icon");
        }

        return icon;
    }
}