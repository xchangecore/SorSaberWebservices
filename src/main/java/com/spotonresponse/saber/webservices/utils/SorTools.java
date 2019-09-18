package com.spotonresponse.saber.webservices.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class SorTools {

    static Logger logger = LoggerFactory.getLogger(SorTools.class);

    private static Map<String, String> iconmap = new HashMap<String, String>();

    public static String determineIcon(String status, ArrayList<String> fields) {
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
        iconmap.put("wells fargo", "Wells Fargo.png");
        iconmap.put("dicks ", "DicksSportingGoods.png");
        iconmap.put("dick's ", "DicksSportingGoods.png");
        iconmap.put("dicks ", "DicksSportingGoods.png");
        iconmap.put("macys", "MacysIcon.png");
        iconmap.put("macy's", "MacysIcon.png");
        iconmap.put("sears", "SearsIcon.png");
        iconmap.put("lowes", "LowesIcon.png");
        iconmap.put("cvs", "cvs.png");
        iconmap.put("costco", "CostcoIcon.png");
        iconmap.put("target", "target.png");
        iconmap.put("walgreens", "walgreens.png");
        iconmap.put("conoco", "conoco.png");
        iconmap.put("wyndham", "wyndham.png");
        iconmap.put("food-grocery", "food-grocery.png");
        iconmap.put("associated grocers", "associatedgrocers.png");
        iconmap.put("price chopper", "pricechopper.png");
        String icon = "";


        // Add an icon to display with the GeoJSON file
        // Use GrayScale icons for closed status
        try {
            String icondir = "";
            if (status.toLowerCase().equals("closed")) {
                icondir = "GS/";
            } else {
                if (status.toLowerCase().equals("limited")) {
                    icondir = "RED/";
                }
            }

            String mapMarkerDirectory = "https://app.spotonresponse.com/MapMarkers/" + icondir;


            // TODO: Enhance this bit to use a lookup table for customers
            //  and the associated icons

            // Loop over the field that were sent
            for (String field : fields) {
                logger.debug("Searching icons for string: " + field.toLowerCase());

                // Now loop through all the configured icons and try to find a match
                // Once hit  stop
                for (Map.Entry<String, String> iconEntry : iconmap.entrySet()) {
                    if (field.toLowerCase().contains(iconEntry.getKey())) {
                        icon = mapMarkerDirectory + iconEntry.getValue();
                        break;
                    }
                }

                // Stop as soon as we find a match
                if (!icon.isEmpty()) {
                    break;
                }
            }

        } catch (org.json.JSONException jex) {
            logger.warn("Entity does not contain status or icon");
        }

        return icon;
    }
}
