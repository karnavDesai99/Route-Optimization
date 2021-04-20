package com.majorProject.app.spark;

import com.majorProject.app.salesman.City;
import com.majorProject.app.salesman.Route;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.majorProject.app.salesman.Route.formatDistance;

public final class SparkFacade {
    private static final Logger LOG = LoggerFactory.getLogger(SparkFacade.class);

    private SparkConf config;
    private JavaSparkContext sc;

    public SparkFacade(String appName, String master) {
        LOG.info("Starting {} on {}", appName, master);
        config = new SparkConf().setAppName(appName).setMaster(master);
    }

    /**
     * Initializes the spark context
     */
    public void init() {
        sc = new JavaSparkContext(config);
    }

    /**
     * Stops the spark context.
     */
    public void stop() {
        sc.close();
    }

    /**
     * Solves the travelling salesman problem using a genetic algorithm that converges on an approximate
     * 'shortest' solution.
     * <p>
     * It does a fork-solve-recombine (parallize-map-reduce) sequence for 'iterations' times. The work
     * is split into a number of Work packages depending on the amount of available workers.
     * <p>
     * If there is no improvement in the best distance between iterations the solver will terminate.
     *
     * @param cities      the list of cities to create a route for.
     * @param iterations  max for-solve-recombine iterations that should be done
     * @param maxDuration max duration for a single iteration
     * @return the shortest route.
     */
    public Route solveFor(List<City> cities, int iterations, int maxDuration) {
        LOG.info("Solving for {} cities in {} iterations ({} ms max iteration duration)", cities.size(), iterations, maxDuration);
        LOG.info("{}", cities.stream().map(City::getName).sorted().collect(Collectors.toList()));
        LOG.info("Parallelism: {}", sc.defaultParallelism());

        Work work = Work.forCities(cities, maxDuration);
        double start = work.shortest().getDistance();

        for (int i = 0; i < iterations; i++) {
            double iterStart = work.shortest().getDistance();
            JavaRDD<Work> dataSet = sc.parallelize(work.fork(sc.defaultParallelism()));

            work = dataSet.map(Work::solve).reduce(Work::combine);

            LOG.info("Iteration {} result: {}", i, formatDistance(work.shortest().getDistance()));
            if (iterStart == work.shortest().getDistance()) {
                LOG.info("No change; terminating.");
                break;
            }
        }

        double result = work.shortest().getDistance();
        String percent = String.format(Locale.ROOT, "%.2f", result / start * 100.0);

        LOG.info("Final result: {} -> {} ({}%)", formatDistance(start), formatDistance(result), percent);

        return work.shortest();
    }
}
