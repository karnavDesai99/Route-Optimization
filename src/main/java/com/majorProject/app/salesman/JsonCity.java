package com.majorProject.app.salesman;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JsonCity {
    public String city;
    public String ll;

    public City toCity() {
        String[] latLon = ll.split(",");
        String[] cityParts = city.split(",");

        double lat = Double.parseDouble(latLon[0]);
        double lon = Double.parseDouble(latLon[1]);

        return new City(cityParts[0], lat, lon);
    }

    public static void main(String... argv) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        JsonCity[] cities = mapper.readValue(JsonCity.class.getResourceAsStream("/europe.json"), JsonCity[].class);
    
        List<City> cityList = Arrays.asList(cities).stream().map(JsonCity::toCity).collect(Collectors.toList());
    
        mapper.writeValue(System.out, cityList);
    }
}






