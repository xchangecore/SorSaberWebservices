package com.spotonresponse.saber.webservices.service;

import com.spotonresponse.saber.webservices.controller.WebserviceController;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.logging.Logger;

@Service
public class FilterService {

    private static final Logger logger = Logger.getLogger(WebserviceController.class.getName());

    public JSONArray filter(JSONArray unfilteredData, Map<String, String> filters){
        JSONArray resultArray = new JSONArray();

        for (int c = 0; c < unfilteredData.length(); c++) {
            // get the JSON object to be filtered.
            JSONObject jo = unfilteredData.getJSONObject(c);

            // get the "item" part of the JSON object.
            JSONObject joItem = jo.getJSONObject("item");

            if(hasKeysFilter(joItem, filters) && allValuesMatchFilter(joItem, filters)){
                // The "item" part of the JSON object passed through the filter. That
                // qualifies the entire JSON object to be placed in the result array.
                resultArray.put(jo);
            }
        }

        return resultArray;
    }


    // Given a filter key, find out the corresponding JSON key.
    // A filter key like "status" can be mapped to a JSON key
    // "STATUS", "Status", "status" e.t.c
    private Optional<String> filterKeyToJsonObjectKey(String filterKey, JSONObject jsonObject){
        for (Iterator<String> it = jsonObject.keys(); it.hasNext(); ) {
            String jsonKey = it.next();
            if(jsonKey.equalsIgnoreCase(filterKey)){
                return Optional.of(jsonKey);
            }
        }
        return Optional.empty();
    }

    // Check whether the JSON object has all the
    // keys specified in the params.
    private boolean hasKeysFilter(JSONObject jsonObject, Map<String, String> filters){
        return filters.keySet().stream().allMatch(filterKey -> {
            // get the JSON object key. If the key is found, then the JSON object contains
            // the key in the filter, regardless of whether uppercase, lowercase or a combination.
            return filterKeyToJsonObjectKey(filterKey, jsonObject).isPresent();
        });
    }

    // given a parameter key, parse the values into a list.
    // If a single value has been specified, then we will get a singleton list.
    private List<String> parseParamValues(String value){
        if(value.startsWith("(") && value.endsWith(")")){
            return Arrays.asList(value.replace("(", "")
                    .replace(")", "")
                    .split("\\|"));
        } else {
            return Collections.singletonList(value);
        }
    }

    // attempt getting a String value from a JSON object given a filter key.
    private Optional<String> getValueFromJsonObject(JSONObject jsonObject, String filterKey){
        // assume the found value is null.
        String value = null;

        // get the JSON object key from the given filter key.
        Optional<String> jsonObjectKeyOptional = filterKeyToJsonObjectKey(filterKey, jsonObject);

        if(jsonObjectKeyOptional.isPresent()){
            // get the JSON key.
            String jsonKey = jsonObjectKeyOptional.get();

            try {
                value = jsonObject.getString(jsonKey);
            }catch (JSONException jex){
                logger.info("Entity does not contain status: " + jex.getMessage());
            }
        }

        // no value found for the given key.
        if(value == null){
            return Optional.empty();
        }

        return Optional.of(value);
    }

    // Given the json object value and a collection of filter values, and that all of them
    // were obtained from the same filter key.. find out whether the json object value
    // matches any of the filter values.
    private boolean jsonObjectValueMatchesAnyFilterValue(String jsonObjectValue, List<String> filterValues){
        return filterValues.stream().anyMatch(filterValue -> {
            if(filterValue.startsWith("!")){
                return !jsonObjectValue.equalsIgnoreCase(filterValue.substring(1));
            } else {
                return jsonObjectValue.equalsIgnoreCase(filterValue);
            }
        });
    }

    // Check whether the JSON object has all the values as specified by the keys.
    // Multiple values for a single key are expressed as a single expression
    // as "(value1|value2)".
    private boolean allValuesMatchFilter(JSONObject jsonObject, Map<String, String> filters){
        for(String key : filters.keySet()){

            // get the list of filter values for the current filter key.
            List<String> filterValues = parseParamValues(filters.get(key));


            Optional<String> jsonObjectValueOptional = getValueFromJsonObject(jsonObject, key);
            if(!jsonObjectValueOptional.isPresent()){
                // the object value was not found.
                return false;
            }

            String jsonObjectValue = jsonObjectValueOptional.get();

            boolean jsonObjectValueMatches = jsonObjectValueMatchesAnyFilterValue(jsonObjectValue, filterValues);

            if(!jsonObjectValueMatches){
                // The current JSON object value not match. Quit any further processing on the supplied JSON object.
                return false;
            }
        }

        return true;

    }

}
