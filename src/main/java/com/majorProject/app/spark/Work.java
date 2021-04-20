package com.majorProject.app.spark;

import com.majorProject.app.salesman.City;
import com.majorProject.app.salesman.Route;
import com.majorProject.app.salesman.Solver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.majorProject.app.salesman.Route.formatDistance;
import static com.majorProject.app.salesman.Solver.createGenerationZero;
import static java.lang.Double.compare;

/**
 * This class represents a unit of 'Work' that is executed by the Spark executors. The solve() method does the actual
 * heavy lifting of using the GP solver to optimize the list of routes. The solver cuts off on the max duration, max
 * iterations without change (maxSame) and max generations, whatever comes first.
 */

public class Work implements Serializable {
    private static final int DEFAULT_MAX_DURATION = 10000;
    private static final Logger LOG = LoggerFactory.getLogger(Work.class);

    private List<Route> routes;
    private int maxDuration;

    public Work() {
        maxDuration = DEFAULT_MAX_DURATION;
    }

    public Work(int maxDuration) {
        this.maxDuration = maxDuration;
    }

    /**
     * The main method used to run a solving session. The session runs for maxDuration milliseconds maximum. This can be
     * used in the .map function in an RDD to perform the calculations.
     *
     * @return this object.
     */
    public Work solve() {
        double start = shortest().getDistance();
        routes = Solver.solve(routes, maxDuration, 10, 100);
        double result = shortest().getDistance();
        String percent = String.format(Locale.ROOT, "%.2f", result / start * 100.0);

        LOG.info("Work package result: {} -> {} ({}%)", formatDistance(start), formatDistance(shortest().getDistance()), percent);

        return this;
    }

    /**
     * Returns the shortest route in the list.
     *
     * @return the shortest route.
     */
    public Route shortest() {
        return routes.get(0);
    }

    /**
     * Forks a single Work package into {slices} identical work packages for parallelisation.
     *
     * @param slices amount of slices
     * @return a list of copies
     */
    public List<Work> fork(int slices) {
        List<Work> list = new ArrayList<>(slices);

        for (int i = 0; i < slices - 1; i++) {
            list.add(clone());
        }
        list.add(this);

        return list;
    }

    /**
     * Constructs an initial Work object from a list of cities.
     *
     * @param cities the cities
     * @return an initial Work object
     */
    public static Work forCities(List<City> cities, int maxDuration) {
        Work work = new Work(maxDuration);
        work.routes = createGenerationZero(cities);

        return work;
    }

    /**
     * Used in the reduce operation to recombine the Work objects into one optimal package
     *
     * @param work1 first object
     * @param work2 second object
     * @return a new Work object containing the shortest routes
     */
    public static Work combine(Work work1, Work work2) {
        Work newWork = new Work(work1.maxDuration);
        List<Route> routes = new ArrayList<>(work1.routes);

        routes.addAll(work2.routes);
        routes.sort((r1, r2) -> compare(r1.getDistance(), r2.getDistance()));
        newWork.routes = new ArrayList<>();
        routes.stream().limit(Solver.GENERATION_SIZE).forEach(newWork.routes::add);

        return newWork;
    }

    /**
     * Creates a clone of this Work object.
     *
     * @return a clone
     */
    public Work clone() {
        Work clone = new Work(maxDuration);
        clone.routes = new ArrayList<>(routes.stream().map(Route::new).collect(Collectors.toList()));

        return clone;
    }
}
