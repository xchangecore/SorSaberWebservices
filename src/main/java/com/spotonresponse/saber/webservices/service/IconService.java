package com.spotonresponse.saber.webservices.service;


import com.google.cloud.datastore.*;
import com.spotonresponse.saber.webservices.controller.WebserviceController;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.logging.Logger;
import static java.time.Instant.*;


@Service
public class IconService {
    private static final Logger logger = Logger.getLogger(IconService.class.getName());

    public void updateIcons() {

        logger.info("Update icons called");
        // Only do this if the timeout for updating has expired
        // We are doing this to prevent DOS type attacks agains this service
            logger.info("Will update icons");
            WebserviceController.IconsLastQueryTime = now();

            // Add icons from Google DataStore
            WebserviceController.iconmap = new HashMap<String, String>();

            Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
            Query<Entity> query = Query.newEntityQueryBuilder()
                    .setKind("icons")
                    .build();
            QueryResults<Entity> results = datastore.run(query);
            logger.info("Fetching icon database");
            while (results.hasNext()) {
                Entity entity = results.next();
                WebserviceController.iconmap.put(entity.getString("name"), entity.getString("icon"));
            }
    }
};


