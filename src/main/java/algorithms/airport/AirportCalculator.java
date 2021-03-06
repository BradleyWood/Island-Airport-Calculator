package algorithms.airport;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

import static algorithms.airport.Utility.angleBetween;

public class AirportCalculator {

    private final Island poly;

    public AirportCalculator(final Island poly) {
        this.poly = poly;
    }

    /**
     * Finds the longest possible runway on an island
     *
     * @return The runway as a line segment
     */
    public Line2D calculate() {
        double runwayLength = Double.NEGATIVE_INFINITY;
        Line2D runwayLine = null;

        // n choose 2 segments to test
        // If we check pt 1 and pt 4, we will never check pt 4 with pt 1
        // This is the handshake problem
        for (int a = 0; a < poly.npoints; a++) {
            for (int b = a + 1; b < poly.npoints; b++) {
                if (poly.containsLine(a, b)) {
                    Point2D start = new Point2D.Double(poly.xpoints[a], poly.ypoints[a]);
                    Point2D end = new Point2D.Double(poly.xpoints[b], poly.ypoints[b]);

                    start = extend(start, end, true);
                    end = extend(start, end, false);

                    // once we know a line is valid we must attempt to extend it
                    // since the longest line may not end on a vertex

                    final double dist = start.distance(end);

                    if (dist >= runwayLength) {
                        runwayLength = dist;
                        runwayLine = new Line2D.Double(start, end);
                    }
                }
            }
        }

        if (runwayLine == null)
            return null;

        return runwayLine;
    }

    /**
     * Attempts to extend a line segment on one side <br>
     * We will extend the line by a large amount and find the closest
     * intersect to the point we extend from
     *
     * @param a The first point in the line segment
     * @param b The second point in the line segment
     * @param extendA Whether to extend the line from point a or b
     * @return The pt to which the line has been extended to
     */
    private Point2D extend(final Point2D a, final Point2D b, final boolean extendA) {
        // this is arbitrary but guaranteed to extend outside the polygon
        final double extensionAmount = poly.getBounds().getWidth() * poly.getBounds().getHeight();
        double nx, ny;

        if (extendA) {
            final double angle = angleBetween(b, a);
            nx = a.getX() + extensionAmount * Math.cos(Math.toRadians(angle));
            ny = a.getY() + extensionAmount * Math.sin(Math.toRadians(angle));
        } else {
            final double angle = angleBetween(a, b);
            nx = b.getX() + extensionAmount * Math.cos(Math.toRadians(angle));
            ny = b.getY() + extensionAmount * Math.sin(Math.toRadians(angle));
        }

        final Point2D np = getClosestIntersection(new Point2D.Double(nx, ny), extendA ? a : b);

        if (np != null) {
            double m1 = ((extendA ? a.getX() : b.getX()) + np.getX()) / 2;
            double m2 = ((extendA ? a.getY() : b.getY()) + np.getY()) / 2;

            // make sure the new point is also inside the polygon
            if(!poly.contains(m1 , m2)) {
                return extendA ? a : b;
            }

            return np;
        }

        // no intersection, return the original point
        return extendA ? a : b;
    }

    /**
     * Finds the closest intersection to the second point
     * in the provided line segment
     *
     * @param a The first point in the line segment
     * @param b The second line in the segment and the point which to find the closest intersection to
     * @return The closest intersection point
     */
    private Point2D getClosestIntersection(final Point2D a, final Point2D b) {
        final List<Point2D> intersections = poly.getIntersections(a, b);

        double dist = Double.POSITIVE_INFINITY;
        Point2D result = null;

        for (final Point2D intersection : intersections) {
            if (intersection.distance(b) < dist && dist > 0.0000001) {
                result = intersection;
                dist = intersection.distance(b);
            }
        }

        return result;
    }
}