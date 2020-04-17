package com.spotonresponse.saber.webservices.controller;

import static com.spotonresponse.saber.webservices.utils.Util.isValidCoordinate;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.spotonresponse.saber.webservices.service.FilterService;
import com.spotonresponse.saber.webservices.service.IconService;
import com.spotonresponse.saber.webservices.utils.CreateBrandData;
import com.spotonresponse.saber.webservices.utils.CreateMapData;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spotonresponse.saber.webservices.model.Entity;
import com.spotonresponse.saber.webservices.model.EntityRepository;
import com.spotonresponse.saber.webservices.utils.CreateGeoJSON;
import com.spotonresponse.saber.webservices.utils.GeometryBuilder;


@RestController
public class WebserviceController {

    @Autowired
    private EntityRepository repo;

    @Autowired
    private FilterService filterService;

    @Autowired
    private IconService iconService;

    public static int totalCount = 0;

    // Caching parameters
    private long CacheTimeoutSeconds = 300; // 5 minutes

    // The number of seconds to wait to update cache even if force is specified
    private int CacheTimeoutForceSeconds = 10;

    private Instant DbLastQueryTime = Instant.now();
    private boolean firstRun = true;
    private JSONArray resultArray = null;
    private static final Logger logger = Logger.getLogger(WebserviceController.class.getName());
    private String output = "No Data";

    // This will hold the icon map across queries (like a cache) - they rarely get updated
    public static Map<String, String> iconmap = new HashMap<String, String>();
    // The time to wait before querying for icon changes
    public long IconsTimeoutSeconds = 3600;  // 1 hour
    // The Instant the icon map was last updated
    public static Instant IconsLastQueryTime = Instant.now();
    // The time to wait between icon refreshes even when forced
    public static long IconsTimeoutForceSeconds = 300;  // 5 minutes


    // Request to update icon map
    @RequestMapping(value = "/updateicons", produces = {"application/json"})
    @CrossOrigin
    public String updateIcons(@RequestParam Map<String,String> allParams) {
        iconService.updateIcons();
        JSONObject jo = new JSONObject();
        jo.put("status", "success");
        return jo.toString();
    }


    // Request to update cache
    @RequestMapping(value = "/updatecache", produces = {"application/json"})
    @CrossOrigin
    public String updateCache(@RequestParam Map<String,String> allParams) {
        JSONObject jo = new JSONObject();
        String message = "";

        if (DbLastQueryTime.plusSeconds(CacheTimeoutForceSeconds).isBefore(Instant.now()) ) {
            // Reset DBLastQuery time to current instant
            DbLastQueryTime = Instant.now();

            // Get all results in the Database
            resultArray = new JSONArray();
            for (Entity e : repo.findAll()) {
                resultArray.put(e.getEntityJson());
            }
            // Get a total count of items in the database
            totalCount = resultArray.length();
            message = resultArray.length() + " records have been updated";

        } else {
            message = "Unable to force cache update, timer has not expired";
        }



        logger.info(message);
        jo.put("status", message);
        return jo.toString();
    }


    @RequestMapping(value = "/saberdata", produces = {"application/json"})
    @CrossOrigin
    public String query(@RequestParam Map<String,String> allParams) {

        // Extract the non-filter parameters, and use default values if the parameters are not specified.
        String nocache = allParams.getOrDefault("nocache", "");
        String updateicons = allParams.getOrDefault("updateicons", "");
        String outputFormat = allParams.getOrDefault("outputFormat", "raw");
        String arcgis = allParams.getOrDefault("arcgis", "false");
        String topLeft = allParams.getOrDefault("topLeft", "");
        String bottomRight = allParams.getOrDefault("bottomRight", "");

        // Remove the non-filter parameters from the map, and let the rest be
        // treated as filter parameters hence-forth.
        Arrays.asList("nocache", "updateicons", "outputFormat", "arcgis", "topLeft", "bottomRight").forEach(allParams::remove);


        // Get the current time
        Instant now = Instant.now();
        Instant scanStart = Instant.now();

        // Determine if we should just used cached data instead of
        // re-querying the database
        // Query the database if:
        // 1. This is the first run
        // 2. The last database query is within CacheTimeoutSeconds
        // 3. nocache=true unless it is within CacheTimeoutForceSeconds - we will force cache if within just a few seconds to reduce load on DB
        logger.info("firstRun is" + firstRun);
        logger.info("DbLastQueryTime + CacheTimeout: " + DbLastQueryTime.plusSeconds(CacheTimeoutSeconds));
        logger.info("Now:             " + now);
        logger.info("NoCache Specified? " + nocache);

        if ( (firstRun)
                || (DbLastQueryTime.plusSeconds(CacheTimeoutSeconds).isBefore(now))
                || ( (nocache.toLowerCase().equals("true"))) &&
                (DbLastQueryTime.plusSeconds(CacheTimeoutForceSeconds).isBefore(now)) ) {
            logger.info("Querying database");

            DbLastQueryTime = Instant.now();

            // UPdate the iconDatabase on the first run
            iconService.updateIcons();

            // Get all results in the Database
            resultArray = new JSONArray();
            for (Entity e : repo.findAll()) {
                resultArray.put(e.getEntityJson());
            }

            // Get a total count of items in the database
            totalCount = resultArray.length();
            logger.info("Record count: " + totalCount);

            firstRun = false;
        } else {
            logger.info("Using Cached data");
        }

        Instant scanEnd = Instant.now();


        // Determine if the icon hashmap needs to be update
        if ( (IconsLastQueryTime.plusSeconds(IconsTimeoutSeconds).isBefore(now))
                || ( (updateicons.toLowerCase().equals("true")) && (IconsLastQueryTime.plusSeconds(IconsTimeoutForceSeconds ).isBefore(now))) )
        {
            logger.info("Updating icon database");
            iconService.updateIcons();
        }


        // We are now either using cached data, or the database query has completed
        // Determine if we need to filter items before returning to client
        JSONArray jsonFiltered = resultArray;



        if(!allParams.isEmpty()){
            jsonFiltered = filterService.filter(resultArray, allParams);
        }


        // jsonBounded is a new JSONArray to store the items after the bound box (geofence) is applied
        // if those parameters were specified.
        JSONArray jsonBounded = new JSONArray();
        String error = "";
        // Check to see if we were given bounding box coordinates
        if (topLeft.contains(",") && bottomRight.contains(",")) {
            // We have strings for top left and bottom right and could look like coordinates
            // Test to make sure they are a valid coordinate pair
            logger.info("We have bounding coordinates");
            try {
                if (isValidCoordinate(topLeft.split(",")[0], topLeft.split(",")[1]) &&
                    (isValidCoordinate(bottomRight.split(",")[0], bottomRight.split(",")[1])) ) {
                        logger.info("We have valid bounding coordinates");
                        // OK - Should be valid coordinates
                        // Calculate the Bounding Box
                        Double lat1 = Double.parseDouble(topLeft.split(",")[0]);
                        Double lon1 = Double.parseDouble(topLeft.split(",")[1]);
                        Double lat2 = Double.parseDouble(bottomRight.split(",")[0]);
                        Double lon2 = Double.parseDouble(bottomRight.split(",")[1]);

                        GeometryBuilder gb = new GeometryBuilder();
                        Polygon bb = gb.box(lon1, lat1, lon2, lat2);

                        // Loop over the filtered JSONArray and get the coordinates
                        String lat = "";
                        String lon = "";
                        boolean gotlatlng = false;
                        for (int c = 0; c < jsonFiltered.length(); c++) {
                            JSONObject jo = jsonFiltered.getJSONObject(c);
                            JSONObject jo_item = jo.getJSONObject("item");
                            if (jo_item.has("where")) {
                                JSONObject where = jo_item.getJSONObject("where");
                                if (where.has("Point")) {
                                    JSONObject point = where.getJSONObject("Point");
                                    if (point.has("pos")) {
                                        String pos = point.getString("pos");
                                        if (pos.contains(" ")) {
                                            String pos_array[] = point.getString("pos").split(" ");
                                            if (pos_array.length > 0) {
                                                lat = pos_array[0];
                                                lon = pos_array[1];
                                                if ((lat != null) && (lon != null)) {
                                                    try {
                                                        Point gbPoint = gb.point(Double.parseDouble(lon), Double.parseDouble(lat));
                                                        if (gbPoint.within(bb)) {
                                                            jsonBounded.put(jo);
                                                            gotlatlng = true;
                                                        } else {
                                                            gotlatlng = false;
                                                        }

                                                    } catch (java.lang.NumberFormatException nfe) {
                                                        logger.info("ERROR in bounding box: POS was: " + pos);
                                                    }
                                                } else {
                                                    logger.info("POS was: " + pos);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // If we didn't have a proper WHERE attribute, see if we have latlng attributes
                            if (!gotlatlng) {
                                if (jo_item.has("latitude") && jo_item.has("longitude")) {
                                    lat = jo_item.getString("latitude");
                                    lon = jo_item.getString(("longitude"));
                                    Point gbPoint = gb.point(Double.parseDouble(lon), Double.parseDouble(lat));
                                    if (gbPoint.within(bb)) {
                                        jsonBounded.put(jo);
                                    } else {
                                    }
                                }
                            }
                        }

                } else {
                    error = "{'error': 'Invalid bounding coordinates provided' }";
                    jsonBounded = jsonFiltered;
                }
            } catch (org.json.JSONException jex) {
                logger.info("unable to use bounding box: " + jex.getMessage());
                error = "{'error': 'Invalid bounding coordinates provided' }";
            }
        } else {
            // No bounding box, pass the filtered array
            jsonBounded = jsonFiltered;
        }


        // Determine what output is needed, and format the data as necessary
        if (error.isEmpty()) {
            Instant jsonStart;
            Instant jsonEnd;
            JSONObject perf = null;
            JSONObject jo = null;
            switch (outputFormat) {
                case "maponly":
                    jsonStart = Instant.now();
                    jo = CreateMapData.build(jsonBounded, false);
                    jsonEnd = Instant.now();
                    perf = new JSONObject();
                    perf.put("DB Scan/Transfer Time", Duration.between(scanStart, scanEnd));
                    perf.put("JSON Create Time", Duration.between(jsonStart, jsonEnd));
                    jo.put("Statistics", perf);
                    output = jo.toString();
                    break;
                case "brandonly":
                    jsonStart = Instant.now();
                    jo = CreateBrandData.build(jsonBounded, false);
                    jsonEnd = Instant.now();
                    perf = new JSONObject();
                    perf.put("DB Scan/Transfer Time", Duration.between(scanStart, scanEnd));
                    perf.put("JSON Create Time", Duration.between(jsonStart, jsonEnd));
                    jo.put("Statistics", perf);
                    output = jo.toString();
                    break;
                case "sor":
                    jsonStart = Instant.now();
                    jo = CreateGeoJSON.build(jsonBounded, false);
                    jsonEnd = Instant.now();
                    perf = new JSONObject();
                    perf.put("DB Scan/Transfer Time", Duration.between(scanStart, scanEnd));
                    perf.put("JSON Create Time", Duration.between(jsonStart, jsonEnd));
                    jo.put("Statistics", perf);
                    output = jo.toString();
                    break;
                case "geojson":
                    jsonStart = Instant.now();
                    if (!arcgis.isEmpty()) {
                        jo = CreateGeoJSON.build(jsonBounded, true, arcgis);
                    } else {
                        jo = CreateGeoJSON.build(jsonBounded, true);
                    }

                    jsonEnd = Instant.now();
                    perf = new JSONObject();
                    perf.put("DB Scan/Transfer Time", Duration.between(scanStart, scanEnd));
                    perf.put("JSON Create Time", Duration.between(jsonStart, jsonEnd));
                    jo.put("Statistics", perf);
                    output = jo.toString();
                    break;
                case "xml":
                    output = XML.toString(jsonBounded);
                    break;

                default:
                    output = jsonBounded.toString();
            }
        } else {
            output = error;
        }


        return output;
    }
    

}
