package com.spotonresponse.adapter.process;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import java.util.ArrayList;
import java.util.List;

public class Util {

    public static boolean insideBoundingBox(Double[][] boundingBox, String lat, String lon) {

        final Coordinate coords = new Coordinate(Double.parseDouble(lon), Double.parseDouble(lat));
        final Point point = new GeometryFactory().createPoint(coords);

        return contains(boundingBox, point);
    }

    public static boolean contains(Double[][] bb, Point point) {

        final LinearRing bbLinerRing = new GeometryFactory().createLinearRing(getCoordinateArray(bb));
        final Polygon bbPloygon = new GeometryFactory().createPolygon(bbLinerRing, null);
        return point.within(bbPloygon);
    }

    private static Coordinate[] getCoordinateArray(Double[][] coords) {

        final List<Coordinate> coordianteList = new ArrayList<Coordinate>();
        for (final Double[] coord : coords) { coordianteList.add(new Coordinate(coord[0], coord[1])); }
        return coordianteList.toArray(new Coordinate[coordianteList.size()]);
    }

}
