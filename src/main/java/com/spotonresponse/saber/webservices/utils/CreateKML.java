package com.spotonresponse.saber.webservices.utils;

import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.util.URLs;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;

import static com.google.common.net.MediaType.KML;


public class CreateKML {


    static org.slf4j.Logger logger = LoggerFactory.getLogger(CreateKML.class);

    public static String build(JSONObject jo) {
        URL url = URLs.fileToUrl(new File("/home/ian/Data/states/states.geojson"));
        HashMap<String, Object> params = new HashMap<>();
        params.put(GeoJSONDataStoreFactory.URLP.key, url);

        DataStore in = DataStoreFinder.getDataStore(params);
        if(in == null) {
            throw new IOException("couldn't open datastore from "+url);
        }
        SimpleFeatureCollection features = in.getFeatureSource(in.getTypeNames()[0]).getFeatures();
        Encoder encoder = new Encoder(new KMLConfiguration());
        encoder.setIndenting(true);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        String out = "";
        try {
            encoder.encode(features, KML.kml, os);
             out = os.toString().replaceAll("kml:", "");
            System.out.println(out);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return out;
    }
}
