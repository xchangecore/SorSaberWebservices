package com.spotonresponse.saber.webservices.utils;
import com.spotonresponse.saber.webservices.controller.WebserviceController;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;


public class CreateGeoJSON {

    private static boolean filterforArcGis = false;

    static org.slf4j.Logger logger = LoggerFactory.getLogger(CreateGeoJSON.class);


    public static JSONObject build(JSONArray jArray, boolean fulloutput, String arcgis) {
        if (arcgis.toLowerCase().equals("true")) {
            filterforArcGis = true;
        }
        JSONObject jo = build(jArray, fulloutput);
        return jo;
    }



    public static JSONObject build(JSONArray jArray, boolean fulloutput) {

        JSONArray featuresArray = new JSONArray();

        int items = jArray.length();

        for (int i = 0; i < jArray.length(); i++) {
            JSONObject itemJson = new JSONObject();
            try {
                JSONObject properties = jArray.getJSONObject(i);

                itemJson = properties.getJSONObject("item");
                JSONObject where = itemJson.getJSONObject("where");
                JSONObject point = where.getJSONObject("Point");
                String pos = point.getString("pos");
                String[] loc = pos.split(" ");

                if (loc.length < 1) {
                    logger.error("No location - disrgard entry: {}", itemJson);

                } else {

                    // If we we not given a status, assume "Open" for the icon color
                    String useStatus = "Open";
                    if (itemJson.has("status")) {
                        useStatus = itemJson.getString("status");
                    } else {
                        if (itemJson.has("Status")) {
                            useStatus = itemJson.getString("Status");
                        } else {
                            logger.debug("JsonItem has no key Status or status");
                        }
                    }

                    ArrayList<String> iconquery = new ArrayList<String>();
                    String field = "Data Source URL";
                    String fielddata = checkIconKey(itemJson, field);
                    if (fielddata.length() > 1) {
                        iconquery.add(fielddata);
                    }

                    boolean haveIcon = false;
                    if (itemJson.has("icon")) {
                        if (itemJson.getString("icon").equals("")) {
                            haveIcon = false;
                        } else {
                            haveIcon = true;
                        }
                    }

                    String icon = "";
                    if (haveIcon) {
                        // Determine if this is a SOR map marker
                        if (itemJson.getString("icon").startsWith("/MapMarkers")) {
                            icon = "https://app.spotonresponse.com" + itemJson.getString("icon");
                        } else {
                            icon = itemJson.getString("icon");
                        }

                    } else {
                        field = "Name";
                        fielddata = checkIconKey(itemJson, field);
                        if (fielddata.length() > 1) {
                            iconquery.add(fielddata);
                        }

                        field = "title";
                        fielddata = checkIconKey(itemJson, field);
                        if (fielddata.length() > 1) {
                            iconquery.add(fielddata);
                        }

                        field = "What";
                        fielddata = checkIconKey(itemJson, field);
                        if (fielddata.length() > 1) {
                            iconquery.add(fielddata);
                        }

                        field = "category";
                        fielddata = checkIconKey(itemJson, field);
                        if (fielddata.length() > 1) {
                            iconquery.add(fielddata);
                        }

                        field = "Description";
                        fielddata = checkIconKey(itemJson, field);
                        if (fielddata.length() > 1) {
                            iconquery.add(fielddata);
                        }

                        icon = SorTools.determineIcon(useStatus, iconquery, WebserviceController.getIconMap());
                    }
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

                    JSONObject props = new JSONObject();

                    // If fulloutput is set to true, give everything
                    // Otherwise only output the title property
                    if (fulloutput) {
                        Iterator<String> keys = itemJson.keys();
                        while (keys.hasNext()) {
                            String key = keys.next();
                            if (filterforArcGis == true) {
                                if (!key.toLowerCase().equals("where")) {
                                    props.put(key, itemJson.get(key));
                                }
                            } else {
                                props.put(key, itemJson.get(key));
                            }
                        }

                    } else {
                        if (itemJson.has("title")) {
                            props.put("title", itemJson.get("title"));
                        } else {
                            if (itemJson.has("name")) {
                                props.put("title", itemJson.get("name"));
                            }
                        }
                        props.put("icon", itemJson.get("icon"));
                        props.put("sorFetchData", "true");
                    }


                    feature.put("properties", props);
                    featuresArray.put(feature);
                }

            } catch (Exception ex) {

                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));
                String stacktrace = sw.toString();

                logger.warn("Unable to add item: {} to geoJSON {}",itemJson, ex);
                logger.warn(stacktrace);
                logger.error(ex.getMessage());
            }

        }

        // Set filterforArcGis back to false for the next run...
        filterforArcGis = false;


        JSONObject fc = new JSONObject();
        fc.put("type", "FeatureCollection");
        fc.put("itemCount", items);
        fc.put("features", featuresArray);

        return fc;
    }

    private static String checkIconKey(JSONObject itemJson, String key) {
        if (itemJson.has(key)) {
                return itemJson.getString(key);
        }
        return "";

    }
}
