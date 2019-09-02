package com.spotonresponse.saber.webservices.controller;


import com.spotonresponse.saber.webservices.model.Entity;
import com.spotonresponse.saber.webservices.model.EntityKey;
import com.spotonresponse.saber.webservices.model.EntityRepository;
import com.spotonresponse.saber.webservices.utils.CreateGeoJSON;
import com.spotonresponse.saber.webservices.utils.GeometryBuilder;
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

import java.time.Duration;
import java.time.Instant;
import java.util.logging.Logger;

import static com.spotonresponse.saber.webservices.utils.Util.isValidCoordinate;


@RestController
public class WebserviceController {

    @Autowired
    private EntityRepository repo;

    // Caching parameters
    private int CacheTimeoutSeconds = 7200;
    private int CacheTimeoutForceSeconds = 10;
    private Instant DbLastQueryTime = Instant.now();
    private boolean firstRun = true;
    private JSONArray resultArray = null;
    private static final Logger logger = Logger.getLogger(WebserviceController.class.getName());
    private String output = "No Data";

    @RequestMapping(value = "/saberdata", produces = {"application/json"})
    @CrossOrigin

    public String query(@RequestParam(value = "nocache", defaultValue = "") String nocache,
                        @RequestParam(value = "outputFormat", defaultValue = "raw") String outputFormat,
                        @RequestParam(value = "arcgis", defaultValue = "false") String arcgis,
                        @RequestParam(value = "title", defaultValue = "") String title,
                        @RequestParam(value = "md5hash", defaultValue = "") String md5hash,
                        @RequestParam(value = "filter", defaultValue = "") String filter,
                        @RequestParam(value = "topLeft", defaultValue = "") String topLeft,
                        @RequestParam(value = "bottomRight", defaultValue = "") String bottomRight) {

        // Get the current time
        Instant now = Instant.now();
        Instant scanStart = Instant.now();

        if ((md5hash.length() > 1) && (title.length() > 1)) {
            resultArray = new JSONArray();
            EntityKey ek = new EntityKey();
            ek.setMd5hash(md5hash);
            ek.setTitle(title);
            Entity e = repo.findByKey(ek);
            if (e != null) {
                resultArray.put(e.getEntityJson());
            }

        } else {
            // Get all results in the Database
            resultArray = new JSONArray();
            for (Entity e : repo.findAll()) {
                resultArray.put(e.getEntityJson());
            }
        }

        Instant scanEnd = Instant.now();


        // We are now either using cached data, or the database query has completed
        // Determine if we need to filter items before returning to client
        JSONArray jsonFiltered = new JSONArray();
        if (filter.length() > 2) {
            try {

                logger.info("Checking for STATUS filter, filter Length: " + filter.length() + " Filter is: " + filter);
                for (int c = 0; c < resultArray.length(); c++) {
                    JSONObject jo = resultArray.getJSONObject(c);
                    JSONObject jo_item = jo.getJSONObject("item");
                    //logger.info("jo: " + jo.toString(2));
                    try {

                        if (jo_item.has("Status")) {
                            if (jo_item.getString("Status").equalsIgnoreCase(filter.toLowerCase())) {
                                jsonFiltered.put(jo);
                            }
                        } else {
                            if (jo_item.has("status")) {
                                if (jo_item.getString("status").equalsIgnoreCase(filter)) {
                                    jsonFiltered.put(jo);
                                }
                            }
                        }
                    } catch (org.json.JSONException jex) {
                        logger.info("Entity does not contain status: " + jex.getMessage());
                    }
                }
            } catch (org.json.JSONException jex) {
                logger.info("Entity does not contain status: " + jex.getMessage());
            }
        } else {
            jsonFiltered = resultArray;
        }


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
                            JSONObject jo = resultArray.getJSONObject(c);
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
            Instant jsonStart = null;
            Instant jsonEnd = null;
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
