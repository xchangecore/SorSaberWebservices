package com.spotonresponse.saber.webservices.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class HtmlOutputService {
    public String toHTML(JSONArray jsonArray){
        StringBuilder builder = new StringBuilder();

        // append the HTML header
        builder.append("<!DOCTYPE html>\n" +
                "<html lang='en'>\n" +
                "<head>\n" +
                "    <meta charset='UTF-8'>\n" +
                "    <meta name='viewport' content='width=device-width, initial-scale=1.0'>\n" +
                "    <title>HTML Output</title>\n" +
                "</head>\n" +
                "<body>");

        // append the HTML body to the HTML builder.
        for (int c = 0; c < jsonArray.length(); c++) {
            // get the JSON object to be converted to HTML
            JSONObject jo = jsonArray.getJSONObject(c);

            // pull the "item" property from the JSON object
            JSONObject joItem = jo.getJSONObject("item");

            // convert the JSON item to HTML format, append it to builder.
            builder.append(jsonObjectToHTML(joItem));
        }

        // append the HTML ending to the builder
        builder.append("</body>\n" +
                "</html>");

        // convert builder to string and return it.
        return builder.toString();
    }

    private String jsonObjectToHTML(JSONObject jsonObject){
        StringBuilder propertyBuilder = new StringBuilder();
        for (String key : jsonObject.keySet()) {
            if(key.trim().equals("where")){
                continue;
            }
            Object value = jsonObject.get(key);
            String valueStr = value == null ? "null" : value.toString();
            String keyValuePropertyHTML = keyValueToHTML(key, valueStr);
            propertyBuilder.append(keyValuePropertyHTML);
        }
        propertyBuilder.append("<br />");

        return propertyBuilder.toString();
    }

    private String keyValueToHTML(String key, String value){
        return "<div><span style='font-weight: bold'>" + key
                + " :</span>&nbsp;<span>" + value +
                "</span></div>";
    }
}
