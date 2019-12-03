package com.spotonresponse.saber.webservices.service;

import com.spotonresponse.saber.webservices.controller.WebserviceController;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

@Service
public class FilterService {

    private static final Logger logger = Logger.getLogger(WebserviceController.class.getName());

    public JSONArray filter(Map<String, String> filters){
        JSONArray resultArray = new JSONArray();

        for (int c = 0; c < resultArray.length(); c++) {
            JSONObject jo = resultArray.getJSONObject(c);
            JSONObject joItem = jo.getJSONObject("item");

            if(hasKeysFilter(joItem, filters) && allValuesMatchFilter(joItem, filters)){
                resultArray.put(joItem);
            }
        }

        return resultArray;
    }

    // Check whether the JSON object has all the
    // keys specified in the params.
    private boolean hasKeysFilter(JSONObject jsonObject, Map<String, String> filters){
        return filters.keySet().stream().allMatch(key -> {
            return jsonObject.has(key) || jsonObject.has(key.toLowerCase());
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
    private String getValueFromJsonObject(JSONObject jsonObject, String filterKey){
        // assume the obtained value is null.
        String value = null;

        // attempt getting the value with the given key.
        try {
            value = jsonObject.getString(filterKey);
        }catch (JSONException jex){
            logger.info("Entity does not contain status: " + jex.getMessage());
        }

        // if value is still null, re-attempt to get the value by using the filter key
        // in lowercase.
        if(value == null){
            try {
                value = jsonObject.getString(filterKey.toLowerCase());
            }catch (JSONException jex){
                logger.info("Entity does not contain status: " + jex.getMessage());
            }
        }

        // return whatever was found, or null.
        return value;

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

            String jsonObjectValue = getValueFromJsonObject(jsonObject, key);

            // the object value is null. We do not expect this since the hasKeysFilter is to be
            // called first.
            if(jsonObjectValue == null){
                return false;
            }

            boolean jsonObjectValueMatches = jsonObjectValueMatchesAnyFilterValue(jsonObjectValue, filterValues);

            if(!jsonObjectValueMatches){
                // The current JSON object value not match. Quit any further processing on the supplied JSON object.
                return false;
            }
        }

        return true;

    }

}
