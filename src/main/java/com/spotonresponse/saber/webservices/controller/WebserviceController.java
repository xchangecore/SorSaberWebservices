package com.spotonresponse.saber.webservices.controller;

import com.spotonresponse.saber.webservices.model.Entity;
import com.spotonresponse.saber.webservices.model.EntityRepository;
import com.spotonresponse.saber.webservices.service.FilterService;
import com.spotonresponse.saber.webservices.service.HtmlOutputService;
import com.spotonresponse.saber.webservices.service.IconService;
import com.spotonresponse.saber.webservices.service.field_mapping.FieldMappingService;
import com.spotonresponse.saber.webservices.utils.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.XML;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.Polygon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.logging.Logger;

import static com.spotonresponse.saber.webservices.utils.Util.isValidCoordinate;

@RestController
public class WebserviceController {

    @Autowired
    private EntityRepository repo;

    @Autowired
    private FilterService filterService;

    @Autowired
    private IconService iconService;

    @Autowired
    private FieldMappingService fieldMappingService;

    @Autowired
    private HtmlOutputService htmlOutputService;

    @Value("${kml.service.url}")
    private String kmlServiceUrl;

    public static int totalCount = 0;

    // Caching parameters
    @Value("${cache_minutes}")
    private int cache_minutes;
    private long CacheTimeoutSeconds; // minutes * 60 (convert to seconds)

    // The number of seconds to wait to update cache even if force is specified
    private final int CacheTimeoutForceSeconds = 10;

    private Instant DbLastQueryTime = Instant.now();
    private JSONArray resultArray = null;
    private static final Logger logger = Logger.getLogger(WebserviceController.class.getName());
    private String output = "No Data";

    // This will hold the icon map across queries (like a cache) - they rarely get updated
    private static Map<String, String> iconmap = new HashMap<String, String>();
    // The time to wait before querying for icon changes
    public long IconsTimeoutSeconds = 3600;  // 1 hour
    // The Instant the icon map was last updated
    public static Instant IconsLastQueryTime = Instant.now();
    // The time to wait between icon refreshes even when forced
    public static long IconsTimeoutForceSeconds = 300;  // 5 minutes


    private boolean dbFetchRunning = false;

    private boolean timerstate = false;
    private Timer timer;

    public static Map<String, String> getIconMap() {
        return iconmap;
    }

    // Initialize data at startup
    @PostConstruct
    private void init() {
        logger.info("AppInitializator initialization logic ...");

        CacheTimeoutSeconds = cache_minutes * 60;
        logger.info("**********************");
        logger.info("Cache will be refreshed every: " + cache_minutes + " minutes");
        logger.info("Cache will be refreshed every: " + CacheTimeoutSeconds + " seconds");
        logger.info("**********************");



        TimerTask reloadDataTask = new TimerTask() {
            public void run() {
                timerstate=true;
                getDataForCache();
                Map<String, String> t_iconmap = iconService.updateIcons();
                iconmap = t_iconmap;
            }
        };


        // If this is the first run - get data
        if (!timerstate) {
            // Update the iconDatabase on the first run
            Map<String, String> t_iconmap = iconService.updateIcons();
            iconmap = t_iconmap;

            logger.info("Iconmap size:" +  String.valueOf(iconmap.size()));

            // Get data from the database
            getDataForCache();
        }

        if (timerstate) {
            timerstate = false;
            timer.purge();
            timer.cancel();
        }

        // Start thread to update data as specified by the cacheTimeout
        timer = new Timer();
        timer.scheduleAtFixedRate(reloadDataTask, 0, CacheTimeoutSeconds*1000);
    }



    private void getDataForCache() {
        if (dbFetchRunning) {
            logger.info("Tried to run DynamoDB fetch, it was already running");
        } else {
            dbFetchRunning = true;
            logger.info("Fetching data from DynamoDB...");

            DbLastQueryTime = Instant.now();

            // Get all results in the Database
            JSONArray ra = new JSONArray();
            for (Entity e : repo.findAll()) {
                ra.put(e.getEntityJson());
            }

            Instant finishQuery = Instant.now();

            logger.info("Done in " + Duration.between(DbLastQueryTime, finishQuery).getSeconds() + " seconds.");
            if (resultArray != null) {
                resultArray.clear();
            }
            resultArray = ra;
            ra = null;

            logger.info("Total memory: " + Runtime.getRuntime().totalMemory());
            logger.info("Meg used=" + (Runtime.getRuntime().totalMemory() -
                    Runtime.getRuntime().freeMemory()) / (1000 * 1000) + "M");


            // Get a total count of items in the database
            totalCount = resultArray.length();
            logger.info("Record count: " + totalCount);

            dbFetchRunning = false;
        }
    }


    // Request to update icon map
    @RequestMapping(value = "/updateicons", produces = {"application/json"})
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public String updateIcons(@RequestParam Map<String, String> allParams) {
        Map<String, String> t_iconmap = iconService.updateIcons();
        iconmap = t_iconmap;

        JSONObject jo = new JSONObject();
        jo.put("status", "success");
        return jo.toString();
    }


    // For monitoring the service
    @RequestMapping(value = "/alive", produces = {"application/json"})
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public String alive(@RequestParam Map<String, String> allParams) {
        logger.info("Alive check returned response");
        JSONObject jo = new JSONObject();
        jo.put("status", "success");
        return jo.toString();
    }

    // For monitoring the service
    @RequestMapping(value = "/status", produces = {"application/json"})
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public String status(@RequestParam Map<String, String> allParams) {
        logger.info("Status check returned response");
        JSONObject jo = new JSONObject();
        jo.put("status", "success");
        jo.put("iconCount", iconmap.size());
        jo.put("incidentSize", resultArray.length());
        return jo.toString();
    }

    // Request to update cache
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    @RequestMapping(value = "/cacheupdate", produces = {"application/json"})
    public String updateCache(@RequestParam Map<String, String> allParams) {

        logger.info("Request recieved to update cache");

        String message = "";
        JSONObject jo = new JSONObject();

            if (DbLastQueryTime.plusSeconds(CacheTimeoutForceSeconds).isBefore(Instant.now()) ) {
                message = "Cache will be updated in the background, allow a few minutes to complete";
                init();
            } else {
                message = "Unable to force cache update, timer has not expired";
            }

            logger.info(message);
            jo.put("status", message);

            return jo.toString();

    }


    @RequestMapping(value = "/saberdata")
    @CrossOrigin(origins = "*", allowedHeaders = "*")
    public ResponseEntity<String> query(@RequestParam Map<String,String> allParams, HttpServletRequest request) {


        // Extract the non-filter parameters, and use default values if the parameters are not specified.
        String nocache = allParams.getOrDefault("nnocache", "");
        String updateicons = allParams.getOrDefault("updateicons", "");
        String outputFormat = allParams.getOrDefault("outputFormat", "raw");
        String arcgis = allParams.getOrDefault("arcgis", "false");
        String topLeft = allParams.getOrDefault("topLeft", "");
        String bottomRight = allParams.getOrDefault("bottomRight", "");
        String fieldMap = allParams.getOrDefault("fieldMap", "");

        // Log the reqest
        StringBuilder result = new StringBuilder();
        for (Map.Entry<String, String> entry : allParams.entrySet()) {
            result.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }
        String finalString = result.toString();
        logger.info("Request recieved from: " + request.getRemoteAddr() + " with params: " + finalString + " - producing output");

        // Remove the non-filter parameters from the map, and let the rest be
        // treated as filter parameters hence-forth.
        Arrays.asList("fieldMap", "nnocache", "nocache", "updateicons", "outputFormat", "arcgis", "topLeft", "bottomRight").forEach(allParams::remove);


        // Get the current time
        Instant now = Instant.now();
        Instant scanStart = Instant.now();
        Instant scanEnd = Instant.now();

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
                                            String[] pos_array = point.getString("pos").split(" ");
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
                    jo.clear();
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
                    jo.clear();
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
                    jo.clear();
                    break;
                case "geojson":
                case "kml":
                    jsonStart = Instant.now();

                    // do field mapping
                    List<String> mandatoryProperties = Collections.singletonList("where");
                    jsonBounded = fieldMappingService.doFieldMapping(jsonBounded, fieldMap, mandatoryProperties);

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
                    if(outputFormat.equals("kml")){
                        output = CreateKML.build(output, kmlServiceUrl);
                    }
                    jo.clear();
                    break;
                case "html":
                    // do field mapping before rendering in HTML
                    jsonBounded = fieldMappingService.doFieldMapping(jsonBounded, fieldMap);
                    output =  htmlOutputService.toHTML(jsonBounded);
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

        String contentType = "application/json";
        if(outputFormat.equals("html")){
            contentType = "text/html";
        } else if(outputFormat.equals("kml") || outputFormat.equals("xml")){
            contentType = "text/xml";
        }


        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", contentType);
        return ResponseEntity.ok()
                .headers(headers)
                .body(output);
    }
    

}
