# SorSaberWebservices

This project Queries the SpotOnResponse SABER BigData Table

**Info:**</br>
+ This is a Gradle Spring Boot project

**To Create a Runable JAR:**</br>
+ gradle bootJar

**To Run from Gradle:**</br>
+ gradle bootRun
+ The JAR will run Tomcat listening on port 8081

</br></br>
## RAW output
https://sorsaberwebservices.spotonresponse.com/saberdata

</br></br>
## For GeoJSON output
https://sorsaberwebservices.spotonresponse.com/saberdata?outputFormat=geojson

</br></br>
## And you can filter only on Status at the moment:
https://sorsaberwebservices.spotonresponse.com/saberdata?outputFormat=geojson&filter=Closed
