package com.majorProject.app.salesman;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;

import static com.majorProject.app.salesman.Route.cross;
import static com.majorProject.app.salesman.Route.formatDistance;
import static java.lang.Double.compare;
import static java.util.Collections.shuffle;

public class Solver implements Callable<Route> {
    private static final Logger LOG = LoggerFactory.getLogger(Solver.class);
    public static final int GENERATION_SIZE = 100;
    private final List<City> cities;
    private RouteUpdate update;

    static Random RANDOM = new Random();

    public Solver() {
        cities = new ArrayList<>();
        update = (g, r, l) -> System.out.println(g + ": " + r);
    }

    public Solver add(final City city) {
        cities.add(city);
        return this;
    }

    public void add(final List<City> cities) {
        this.cities.addAll(cities);
    }

    public static List<Route> createGenerationZero(final List<City> cities) {
        final List<Route> routes = new ArrayList<>();
        final List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < cities.size() - 1; i++) {
            indices.add(i + 1);
        }

        for (int i = 0; i < GENERATION_SIZE; i++) {
            final Route r = new Route(cities);
            shuffle(indices);
            r.add(0);
            for (final int index : indices) {
                r.add(index);
            }
            r.add(0);

            routes.add(r);
        }

        return routes;
    }

    public static List<Route> selectParents(final List<Route> parents) {
        Collections.sort(parents, (o1, o2) -> {
            return compare(o1.getDistance(), o2.getDistance()); //Lower is better
        });

        final List<Route> selected = new ArrayList<>();
        for (int i = 0; i < GENERATION_SIZE; i++) {
            selected.add(parents.get(i));
        }

        return selected;
    }

    public static void crossAndMutate(final List<Route> routes) {
        final int size = routes.size();
        for (int i1 = 0; i1 < size; i1++) {
            for (int i2 = 0; i2 < size; i2++) {
                if (i1 != i2) {
                    final Route child = cross(routes.get(i1), routes.get(i2));
                    child.mutate();
                    routes.add(child);
                }
            }
        }
    }

    @Override
    public Route call() throws Exception {
        List<Route> routes = createGenerationZero(cities);
        double dist = 0;
        int sameCount = 0;
        for (int gen = 1; gen <= 100; gen++) {
            routes = selectParents(routes);
            crossAndMutate(routes);
            final double curDist = routes.get(0).getDistance();
            if (curDist != dist) {
                update.update(gen, routes.get(0), false);
                dist = curDist;
                sameCount = 0;
            } else {
                sameCount++;
            }
            if (sameCount > 10) {
                return routes.get(0);
            }
        }
        return null;
    }

    public static List<Route> solve(List<Route> routes, int maxDuration, int maxSame, int maxGenerations) {
        if (maxSame <= 0 && maxDuration <= 0 && maxGenerations <= 0) {
            throw new IllegalArgumentException("You need to specify a positive max duration, same or generations");
        }
        double dist = 0;
        int sameCount = 0;
        long start = System.currentTimeMillis();
        for (int generation = 1; ; generation++) {
            routes = selectParents(routes);
            crossAndMutate(routes);
            final double curDist = routes.get(0).getDistance();
            if (curDist != dist) {
                dist = curDist;
                sameCount = 0;
                LOG.debug("{}: {}", generation, formatDistance(routes.get(0).getDistance()));
            } else {
                sameCount++;
            }
            if (maxSame > 0 && sameCount > maxSame) {
                return routes;
            } else if (maxDuration > 0 && (System.currentTimeMillis() - start) > maxDuration) {
                return routes;
            } else if (maxGenerations > 0 && generation > maxGenerations) {
                return routes;
            }
        }
    }
}
