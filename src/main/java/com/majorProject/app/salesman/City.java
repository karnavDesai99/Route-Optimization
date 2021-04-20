package com.majorProject.app.salesman;

import java.io.Serializable;
import java.util.Locale;

public class City implements Serializable {
    private final String name;
    private final double lat;
    private final double lon;

    public City(final String name, final double lat, final double lon) {
        this.name = name;
        this.lat = lat;
        this.lon = lon;
    }

    public double distanceTo(final City other) {
        return distance(lat, lon, other.lat, other.lon);
    }

    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    private double distance(final double lat1, final double lon1, final double lat2, final double lon2) {
        final double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        dist = dist * 1.609344;
        return (dist);
    }

    @Override
    public String toString() {
        return String.format(Locale.ROOT, "%s (%s, %s)", name, lat, lon);
    }

    private double deg2rad(final double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(final double rad) {
        return (rad * 180 / Math.PI);
    }
}
