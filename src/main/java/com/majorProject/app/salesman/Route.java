package com.majorProject.app.salesman;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Route implements Serializable{
    private final List<City> cities;
    private final List<Integer> route;
    private double dist = -1;

    public Route(final List<City> cities) {
        this.cities = cities;
        route = new ArrayList<>();
    }

    public Route(final Route route) {
        cities = route.cities;
        this.route = new ArrayList<>(route.route);
    }

    public void add(final int cityIndex) {
        route.add(cityIndex);
    }

    public double getDistance() {
        if (dist == -1) {
            dist = 0;
            for (int i = 0; i < route.size() - 1; i++) {
                dist += cities.get(route.get(i)).distanceTo(cities.get(route.get(i + 1)));
            }
        }
        return dist;
    }

    public void mutate() {
        int i1 = 0;
        int i2 = 0;

        while (i1 == i2) {
            i1 = Solver.RANDOM.nextInt(route.size() - 2) + 1;
            i2 = Solver.RANDOM.nextInt(route.size() - 2) + 1;
        }

        final int temp = route.get(i1);
        route.set(i1, route.get(i2));
        route.set(i2, temp);
    }

    public static Route mutate(final Route route) {
        final Route newRoute = new Route(route);
        newRoute.mutate();
        return newRoute;
    }

    public static Route cross(final Route a, final Route b) {
        final Route child = new Route(a.cities);
        child.add(a.route.get(0));
        final int indexCount = a.route.size();
        for (int i = 1; i < indexCount / 2; i++) {
            child.add(a.route.get(i));
        }
        for (int i = indexCount / 2; i < indexCount - 1; i++) {
            final int index = b.route.get(i);
            if (!child.route.contains(index)) {
                child.route.add(index);
            }
        }
        for (int i = 1; i < indexCount / 2; i++) {
            final int index = b.route.get(i);
            if (!child.route.contains(index)) {
                child.route.add(index);
            }
        }
        child.add(a.route.get(0));
        return child;
    }

    public static String formatDistance(double distance) {
        return String.format(Locale.ROOT, "%.2f km", distance);
    }

    @Override
    public String toString() {
        final StringBuilder build = new StringBuilder();

        for (int i = 0; i < route.size(); i++) {
            if (i != 0) {
                build.append(" > ");
            }
            build.append(cities.get(route.get(i)).getName());
        }

        return String.format(Locale.ROOT, "%f: %s", getDistance(), build.toString());
    }
}
