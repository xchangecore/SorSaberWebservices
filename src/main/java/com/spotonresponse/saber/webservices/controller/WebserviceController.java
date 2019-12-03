package com.spotonresponse.saber.webservices.controller;


import static com.spotonresponse.saber.webservices.utils.Util.isValidCoordinate;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

import com.spotonresponse.saber.webservices.service.FilterService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.spotonresponse.saber.webservices.model.Entity;
import com.spotonresponse.saber.webservices.model.EntityKey;
import com.spotonresponse.saber.webservices.model.EntityRepository;
import com.spotonresponse.saber.webservices.utils.CreateGeoJSON;
import com.spotonresponse.saber.webservices.utils.GeometryBuilder;


@RestController
public class WebserviceController {

    @Autowired
    private EntityRepository repo;

    @Autowired
    private FilterService filterService;
    
    // Caching parameters
    private long CacheTimeoutSeconds = 180;
    private int CacheTimeoutForceSeconds = 10;
    private Instant DbLastQueryTime = Instant.now();
    private boolean firstRun = true;
    private JSONArray resultArray = null;
    private static final Logger logger = Logger.getLogger(WebserviceController.class.getName());
    private String output = "No Data";

    @RequestMapping(value = "/saberdata", produces = {"application/json"})
    @CrossOrigin
    public String query(@RequestParam Map<String,String> allParams) {

        // Extract the non-filter parameters, and use default values if the parameters are not specified.
        String nocache = allParams.getOrDefault("nocache", "");
        String outputFormat = allParams.getOrDefault("outputFormat", "raw");
        String arcgis = allParams.getOrDefault("arcgis", "false");
        String topLeft = allParams.getOrDefault("topLeft", "");
        String bottomRight = allParams.getOrDefault("bottomRight", "");

        // Remove the non-filter parameters from the map, and let the rest be
        // treated as filter parameters hence-forth.
        Arrays.asList("nocache", "outputFormat", "arcgis", "topLeft", "bottomRight").forEach(allParams::remove);


        // Get the current time
        Instant now = Instant.now();
        Instant scanStart = Instant.now();

        // Determine if we should just used cached data instead of
        // re-querying the database
        // Query the database if:
        // 1. This is the first run
        // 2. The last database query is within CacheTimeoutSeconds
        // 3. nocache=true unless it is within CacheTimeoutForceSeconds - we will force cache if within just a few seconds to reduce load on DB
        logger.severe("firstRun is" + firstRun);
        logger.severe("DbLastQueryTime: " + DbLastQueryTime.plusSeconds(CacheTimeoutSeconds));
        logger.severe("Now:             " + now);
        logger.severe("NoCache: " + nocache);

        if ( (firstRun)
                || (DbLastQueryTime.plusSeconds(CacheTimeoutSeconds).isBefore(now))
                || ( (nocache.toLowerCase().equals("true"))) &&
                (DbLastQueryTime.plusSeconds(CacheTimeoutForceSeconds).isBefore(now)) ) {
            logger.severe("Querying database");
            firstRun = false;
            DbLastQueryTime = Instant.now();

            // Get all results in the Database
            resultArray = new JSONArray();
            for (Entity e : repo.findAll()) {
                resultArray.put(e.getEntityJson());
            }

        } else {
            logger.severe("Using Cached data");
        }

        Instant scanEnd = Instant.now();


        // We are now either using cached data, or the database query has completed
        // Determine if we need to filter items before returning to client
        JSONArray jsonFiltered = filterService.filter(allParams);

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
                        for (int c = 0; c < jsonFiltered.length(); c++) {
                            JSONObject jo = jsonFiltered.getJSONObject(c);
                            JSONObject jo_item = jo.getJSONObject("item");
                            JSONObject where = jo_item.getJSONObject("where");
                            JSONObject point = where.getJSONObject("Point");
                            String lat = point.getString("pos").split(" ")[0];
                            String lon = point.getString("pos").split(" ")[1];

                            Point gbPoint = gb.point(Double.parseDouble(lon), Double.parseDouble(lat));
                            if (gbPoint.within(bb)) {
                                jsonBounded.put(jo);
                            } else {
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
