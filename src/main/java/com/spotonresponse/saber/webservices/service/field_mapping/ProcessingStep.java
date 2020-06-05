package com.spotonresponse.saber.webservices.service.field_mapping;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ProcessingStep {
    private final JSONObject inputObject;
    private final JSONObject propertiesToBeIncluded;
    private final List<String> propertiesToBeExcluded;
    private boolean explicitInclusionSpecified;
    private final List<String> mandatoryProperties;

    public ProcessingStep(JSONObject inputObject, List<String> mandatoryProperties) {
        this.inputObject = inputObject;
        propertiesToBeExcluded = new LinkedList<>();
        propertiesToBeIncluded = new JSONObject();
        this.mandatoryProperties = mandatoryProperties;
    }

    public JSONObject getInputObject(){
        return inputObject;
    }

    public void setExplicitInclusionSpecified(boolean explicitInclusionSpecified) {
        this.explicitInclusionSpecified = explicitInclusionSpecified;
    }

    public Iterator<String> getInputObjectKeys(){
        return this.inputObject.keys();
    }

    public boolean isExplicitInclusionSpecified() {
        return explicitInclusionSpecified;
    }


    public void includeProperty(String propertyName){
        if(!inputObject.has(propertyName)){
            return;
        }

        Object value = this.inputObject.get(propertyName);
        this.propertiesToBeIncluded.put(propertyName, value);
    }

    public void includeProperty(String previousProperty, String newProperty){
        if(!inputObject.has(previousProperty)){
            return;
        }

        propertiesToBeExcluded.add(previousProperty);
        Object value = this.inputObject.get(previousProperty);
        this.propertiesToBeIncluded.put(newProperty, value);
    }


    public void createStaticProperty(String key, Object value){
        this.inputObject.put(key, value);
    }

    public void excludeProperty(String key){
        this.propertiesToBeExcluded.add(key);
    }

    public JSONObject getProcessedObject(){
        // all properties to be included must feature in the output.
        JSONObject outputObject = new JSONObject(propertiesToBeIncluded.toMap());


        if(isExplicitInclusionSpecified()){
            mandatoryProperties.forEach(mandatoryProperty -> {
                if(inputObject.has(mandatoryProperty)){
                    // the mandatory properties *must* be included no matter what.
                    outputObject.put(mandatoryProperty, inputObject.get(mandatoryProperty));
                }
            });


            return outputObject;
        }

        for (Iterator<String> it = getInputObjectKeys(); it.hasNext(); ) {
            String key = it.next();

            if(outputObject.has(key) || propertiesToBeExcluded.contains(key)){
                continue;
            }

            outputObject.put(key, inputObject.get(key));
        }

        return outputObject;
    }
}
