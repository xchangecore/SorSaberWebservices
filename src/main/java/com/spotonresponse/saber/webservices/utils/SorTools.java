package com.spotonresponse.saber.webservices.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SorTools {

    static Logger logger = LoggerFactory.getLogger(SorTools.class);

    public static String determineIcon(String status, String client) {

        String icon = "";


        // Add an icon to display with the GeoJSON file
        // Use GrayScale icons for closed status
        try {
            String icondir = "";
            if (!status.toLowerCase().equals("open")) {
                icondir = "GS/";
            }

            // TODO: Enhance this bit to use a lookup table for customers
            //  and the associated icons
            logger.info("Searching icons for string: " + client);
            if (client.toLowerCase().contains("walmart")) {
                icon = "https://app.spotonresponse.com/MapMarkers/" + icondir + "WalmartIcon.png";
            } else {
                if (client.toLowerCase().contains("7-eleven")) {
                    icon = "https://app.spotonresponse.com/MapMarkers/" + icondir + "7-Eleven.png";
                } else {
                    if (client.toLowerCase().contains("bp oil")) {
                        icon = "https://app.spotonresponse.com/MapMarkers/" + icondir + "bp-logo.png";
                    }
                }
            }


        } catch (org.json.JSONException jex) {
            logger.warn("Entity does not contain status or icon");
        }

        return icon;
    }
}
