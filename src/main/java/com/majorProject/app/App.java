package com.majorProject.app;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.majorProject.app.salesman.City;
import com.majorProject.app.salesman.JsonCity;
import com.majorProject.app.spark.SparkFacade;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    public static void main(String... argv) throws Exception {
        OptionParser parser = new OptionParser();

        OptionSpec<Integer> citiesOption = parser.accepts("c", "Amount of cities").withRequiredArg().ofType(Integer.class).defaultsTo(100);
        OptionSpec<Integer> iterationsOption = parser.accepts("i", "Spark iterations").withRequiredArg().ofType(Integer.class).defaultsTo(10);
        OptionSpec<Integer> durationOption = parser.accepts("d", "Max iteration duration (seconds)").withRequiredArg().ofType(Integer.class).defaultsTo(8);
        parser.accepts("s", "Shuffle cities");
        OptionSpec<String> masterOption = parser.accepts("m", "Spark Master").withRequiredArg().ofType(String.class).defaultsTo("local[4]");
        OptionSpec<String> appNameOption = parser.accepts("a", "App Name").withRequiredArg().ofType(String.class).defaultsTo("SparkOfLife");

        OptionSet options = parser.parse(argv);

        List<City> cities = loadCities(options.valueOf(citiesOption), options.has("s"));

        SparkFacade facade = new SparkFacade(options.valueOf(appNameOption), options.valueOf(masterOption));
        facade.init();
        facade.solveFor(cities, options.valueOf(iterationsOption), options.valueOf(durationOption) * 1000);
        facade.stop();
    }

    private static List<City> loadCities(int trimTo, boolean shuffle) throws Exception {
        List<JsonCity> cities = new ArrayList<>(Arrays.asList(new ObjectMapper().readValue(App.class.getResourceAsStream("/europe.json"), JsonCity[].class)));
        if (shuffle) {
            Collections.shuffle(cities);
        }
        return cities
                .stream()
                .limit(trimTo)
                .map(JsonCity::toCity)
                .collect(Collectors.toList());
    }
}
