package com.spotonresponse.saber.webservices.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;


public class SorTools {
    static Logger logger = LoggerFactory.getLogger(SorTools.class);

    public static String determineIcon(String status, ArrayList<String> fields, Map<String, String> iconmap) {

        String icon = "";

        // Add an icon to display with the GeoJSON file
        // Use GrayScale icons for closed status
        boolean found = false;
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
            found = false;
            for (String field : fields) {
                // TODO: Enhance this bit to use a lookup table for customers
                //  and the associated icons
                //logger.info("Searching icons for string: " + field.toLowerCase());

                for (Map.Entry<String, String> iconEntry : iconmap.entrySet()) {
                    if (field.toLowerCase().contains(iconEntry.getKey())) {
                        icon = mapMarkerDirectory + iconEntry.getValue();
                        found = true;
                        break;
                    }
                }
                if (found) {
                    break;
                }
            }

        } catch (org.json.JSONException jex) {
            logger.warn("Entity does not contain status or icon");
        }

        if (!found) {
            icon = "https://maps.gstatic.com/mapfiles/ms2/micons/red.png";
        }
        return icon;
    }
}
