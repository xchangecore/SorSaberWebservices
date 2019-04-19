package com.spotonresponse.saber.webservices.utils;

import java.util.logging.Logger;

public class SorTools {

    private static final Logger logger = Logger.getLogger(SorTools.class.getName());

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
            if (client.toLowerCase().contains("walmart")) {
                icon = "https://app.spotonresponse.com/MapMarkers/" + icondir + "WalmartIcon.png";
            }


        } catch (org.json.JSONException jex) {
            logger.finer("Entity does not contain status or icon");
        }

        return icon;
    }
}
