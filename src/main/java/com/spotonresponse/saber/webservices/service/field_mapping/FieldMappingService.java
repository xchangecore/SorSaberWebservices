package com.spotonresponse.saber.webservices.service.field_mapping;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

@Service
public class FieldMappingService {

    private static final Logger logger = Logger.getLogger(FieldMappingService.class.getName());

    /**
     * @param rawDataInput The raw JSON data input to be processed.
     * @return Processed JSON data output based on {@param fieldMappingInfo} used to carry out
     * field mapping.
     */
    public JSONArray doFieldMapping(JSONArray rawDataInput, String fieldMappingInfo, List<String> mandatoryProperties){
        if(fieldMappingInfo.isEmpty()){
            return rawDataInput;
        }

        RuleChain ruleChain = new RuleChain(fieldMappingInfo);
        JSONArray dataOutput = new JSONArray();
        for(int i = 0; i < rawDataInput.length(); i++){
            JSONObject originalObject = rawDataInput.getJSONObject(i);
            JSONObject clonedObject = new JSONObject(originalObject.toMap());

            JSONObject itemToBeProcessed = clonedObject.getJSONObject("item");
            JSONObject processedItem = ruleChain.applyRules(itemToBeProcessed, mandatoryProperties);
            clonedObject.put("item", processedItem);
            dataOutput.put(clonedObject);
        }

        return dataOutput;
    }

    public JSONArray doFieldMapping(JSONArray rawDataInput, String fieldMappingInfo){
        return doFieldMapping(rawDataInput, fieldMappingInfo, Collections.emptyList());
    }
}
