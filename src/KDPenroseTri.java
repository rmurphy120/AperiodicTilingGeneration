import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

/**
 * Class defines Triangles for the Kites and Darts version of Penrose Tiling. Provides methods to aid in constructing
 * a tiling. These triangles will always be isosceles, and there are only two shapes possible
 */
public class KDPenroseTri {
    public interface CheckTriangle {
        KDPenroseTri set(KDPenroseTri tri);
    }

    // The golden ratio. Used to relate edge widths to each other
    public static final double PHI = (1 + Math.sqrt(5)) / 2;

    // Gives some leeway for when checking for overlapping to account for rounding errors
    private static final int TOLERANCE = 3;

    // Width of lines drawn
    private static final int LINE_WIDTH = 1;

    // Primary color
    private static final Color KITE_COLOR = Color.FORESTGREEN;

    // Secondary color
    private static final Color DART_COLOR = Color.LIGHTGREEN;

    private static final Color LINE_COLOR = Color.BLACK;

    private static double cartesianLength(double[] p1, double[] p2) {
        return Math.sqrt(Math.pow(p1[0] - p2[0], 2) + Math.pow(p1[1] - p2[1], 2));
    }

    // Can either be 0, 1, 2, or 3. Represents different triangles used to construct Kites and Darts
    private final short type;
    private boolean ghostDrawn = false;

    // The three points which define the triangle
    private final double[] p1;
    private final double[] p2;
    private final double[] p3;

    private KDPenroseTri[] children;

    public KDPenroseTri(short type, double[] p1, double[] p2, double[] p3) {
        if (type < 0 || type > 3)
            throw new IllegalArgumentException("Invalid type");
        if (p1.length != 2 || p2.length != 2 || p3.length != 2)
            throw new IllegalArgumentException("One of the points are not 2-D");

        this.type = type;
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
    }

    public void setGhostDrawn(boolean ghostDrawn) {
        this.ghostDrawn = ghostDrawn;
    }

    public boolean isGhostDrawn() {
        return ghostDrawn;
    }

    /**
     * Normal draw method which draws the triangle at the coordinates with the correct color and lines to combine two
     * triangles into either a kite or dart
     *
     * @param root the pane that will be drawn on
     */
    public void draw(Pane root) {
        drawHelper(root, p1, p2, p3);
    }

    /**
     * Draws the rectangle as would the regular draw method but scales with respect to a rectangle to the screen
     *
     * @param root              root the pane that will be drawn on
     * @param rectX             the rectangle's left x coordinate
     * @param rectY             the rectangle's upper y coordinate
     * @param ratioRectToScreen (screen size) / (rectangle size)
     */
    public void drawScaled(Pane root, int rectX, int rectY, double ratioRectToScreen) {
        double[] scaledP1 = new double[]{ratioRectToScreen * (p1[0] - rectX),
                ratioRectToScreen * (p1[1] - rectY)};
        double[] scaledP2 = new double[]{ratioRectToScreen * (p2[0] - rectX),
                ratioRectToScreen * (p2[1] - rectY)};
        double[] scaledP3 = new double[]{ratioRectToScreen * (p3[0] - rectX),
                ratioRectToScreen * (p3[1] - rectY)};

        drawHelper(root, scaledP1, scaledP2, scaledP3);
    }

    /**
     * Helper function to reduce copy code in draw and drawScaled
     *
     * @param root root the pane that will be drawn on
     * @param d1   akin to p1
     * @param d2   akin to p2
     * @param d3   akin to p3
     */
    private void drawHelper(Pane root, double[] d1, double[] d2, double[] d3) {
        Line l2 = new Line(d1[0], d1[1], d3[0], d3[1]);
        l2.setStrokeWidth(LINE_WIDTH);

        Line l1 = new Line(d1[0], d1[1], d2[0], d2[1]);
        l1.setStrokeWidth(LINE_WIDTH);

        switch (type) {
            case 0 -> {
                l2.setStroke(LINE_COLOR);
                l1.setStroke(KITE_COLOR);
            }
            case 1 -> {
                l1.setStroke(LINE_COLOR);
                l2.setStroke(KITE_COLOR);
            }
            case 2 -> {
                l2.setStroke(LINE_COLOR);
                l1.setStroke(DART_COLOR);
            }
            case 3 -> {
                l1.setStroke(LINE_COLOR);
                l2.setStroke(DART_COLOR);
            }
        }

        root.getChildren().add(l2);
        root.getChildren().add(l1);

        Line l3 = new Line(d2[0], d2[1], d3[0], d3[1]);
        l3.setStroke(LINE_COLOR);
        l3.setStrokeWidth(LINE_WIDTH);
        root.getChildren().add(l3);

        Polygon fill = new Polygon(d1[0], d1[1], d2[0], d2[1], d3[0], d3[1]);
        fill.setStroke(Color.rgb(0, 0, 0, 0));
        fill.setFill((type == 0 || type == 1) ? KITE_COLOR : DART_COLOR);
        root.getChildren().add(fill);

        // For formatting
        fill.toBack();

        if (type == 0 || type == 2)
            l2.toFront();
        else
            l1.toFront();
    }

    /**
     * "Ghost" draws a triangle, which means it draws the 3 sides of the triangle in gray with no fill-in color
     *
     * @param root root the pane that will be drawn on
     */
    public void ghostDraw(Pane root) {
        Line l1 = new Line(p1[0], p1[1], p2[0], p2[1]);
        l1.setStrokeWidth(LINE_WIDTH);
        l1.setStroke(Color.GRAY);
        root.getChildren().add(l1);

        Line l2 = new Line(p1[0], p1[1], p3[0], p3[1]);
        l2.setStrokeWidth(LINE_WIDTH);
        l2.setStroke(Color.GRAY);
        root.getChildren().add(l2);

        Line l3 = new Line(p2[0], p2[1], p3[0], p3[1]);
        l3.setStrokeWidth(LINE_WIDTH);
        l3.setStroke(Color.GRAY);
        root.getChildren().add(l3);
    }

    /**
     * Builds the appropriate children for the triangle
     *
     * @param setTri lambda function that lets this method work with and without a rectangle constraint
     * @return the children of the triangle
     */
    public KDPenroseTri[] buildChildren(CheckTriangle setTri) {
        double baseLength = cartesianLength(p2, p3);
        double sideLength = baseLength * (type == 0 || type == 1 ? PHI : 1 / PHI);

        double[] newPt1;
        double[] newPt2;

        // Handles each of the 4 cases appropriately. Note 0 and 1 are mirrors of each other and so are 2 and 3
        switch (type) {
            case 0 -> {
                children = new KDPenroseTri[3];
                newPt1 = new double[]{(p1[0] + (baseLength / sideLength) * (p2[0] - p1[0])),
                        (p1[1] + (baseLength / sideLength) * (p2[1] - p1[1]))};
                newPt2 = new double[]{(p3[0] + (baseLength / sideLength) * (p1[0] - p3[0])),
                        (p3[1] + (baseLength / sideLength) * (p1[1] - p3[1]))};
                children[0] = setTri.set(new KDPenroseTri((short) 2, newPt2, p1, newPt1));
                children[1] = setTri.set(new KDPenroseTri((short) 1, p3, newPt2, newPt1));
                children[2] = setTri.set(new KDPenroseTri((short) 0, p3, newPt1, p2));
            }
            case 1 -> {
                children = new KDPenroseTri[3];
                newPt1 = new double[]{(p2[0] + (baseLength / sideLength) * (p1[0] - p2[0])),
                        (p2[1] + (baseLength / sideLength) * (p1[1] - p2[1]))};
                newPt2 = new double[]{(p1[0] + (baseLength / sideLength) * (p3[0] - p1[0])),
                        (p1[1] + (baseLength / sideLength) * (p3[1] - p1[1]))};
                children[0] = setTri.set(new KDPenroseTri((short) 3, newPt1, newPt2, p1));
                children[1] = setTri.set(new KDPenroseTri((short) 0, p2, newPt2, newPt1));
                children[2] = setTri.set(new KDPenroseTri((short) 1, p2, p3, newPt2));
            }
            case 2 -> {
                children = new KDPenroseTri[2];
                newPt1 = new double[]{(p2[0] + (sideLength / baseLength) * (p3[0] - p2[0])),
                        (p2[1] + (sideLength / baseLength) * (p3[1] - p2[1]))};
                children[0] = setTri.set(new KDPenroseTri((short) 2, newPt1, p3, p1));
                children[1] = setTri.set(new KDPenroseTri((short) 1, p2, newPt1, p1));
            }
            case 3 -> {
                children = new KDPenroseTri[2];
                newPt1 = new double[]{(p2[0] + (sideLength / (PHI * baseLength)) * (p3[0] - p2[0])),
                        (p2[1] + (sideLength / (PHI * baseLength)) * (p3[1] - p2[1]))};
                children[0] = setTri.set(new KDPenroseTri((short) 3, newPt1, p1, p2));
                children[1] = setTri.set(new KDPenroseTri((short) 0, p3, p1, newPt1));
            }
        }

        return children;
    }

    /**
     * Checks if a rectangle and triangle are overlapping or one contains another
     *
     * @param rectX      the left x coordinate of the rectangle
     * @param rectY      the top y coordinate of the rectangle
     * @param rectLength the width and height of the rectangle
     * @return true if they do overlap
     */
    public boolean overlapping(int rectX, int rectY, int rectLength) {
        double[] triPts = new double[]{p1[0], p1[1], p2[0], p2[1], p3[0], p3[1]};

        // Case 1, a triangle point is inside the rectangle
        for (int i = 0; i < triPts.length; i += 2) {
            if (triPts[i] + TOLERANCE >= rectX && triPts[i] <= rectX + rectLength + TOLERANCE &&
                    triPts[i + 1] + TOLERANCE >= rectY && triPts[i + 1] <= rectY + rectLength + TOLERANCE)
                return true;
        }

        // Case 2, a rectangle point is in the triangle
        int[] rectPts = new int[]{rectX, rectY, rectX, rectY + rectLength, rectX + rectLength, rectY,
                rectX + rectLength, rectY + rectLength};

        double m1 = (p3[1] - p2[1]) / (p3[0] - p2[0]);
        double b1 = -m1 * p2[0] + p2[1];

        for (int i = 0; i < rectPts.length; i += 2) {
            double m2 = (rectPts[i + 1] - p1[1]) / (rectPts[i] - p1[0]);
            double b2 = -m2 * p1[0] + p1[1];

            int intersectX;
            int intersectY;

            // Deals with the edge case where line 1 or line 2 are vertical
            if (Math.abs(m2) == Double.POSITIVE_INFINITY) {
                intersectX = rectPts[i];
                intersectY = (int) p2[1];
            } else if (Math.abs(m1) == Double.POSITIVE_INFINITY) {
                intersectX = (int) p2[0];
                intersectY = rectPts[i + 1];
            } else {
                intersectX = (int) ((b1 - b2) / (m2 - m1));
                intersectY = (int) (m1 * intersectX + b1);
            }

            // Skips if the rectangle point is past the intercept
            if (cartesianLength(new double[]{rectPts[i], rectPts[i + 1]}, new double[]{p1[0], p1[1]}) >
                    cartesianLength(new double[]{intersectX, intersectY}, new double[]{p1[0], p1[1]}))
                continue;

            // Skips if the rectangle is before p1
            int checkLeft = Math.min(rectPts[i], intersectX);
            int checkRight = Math.max(rectPts[i], intersectX);

            if ((int) p1[0] > checkLeft && (int) p1[0] < checkRight)
                continue;

            // Checks if the intercept is between p2 and p3
            int triPtLeft = (int) Math.min(triPts[2], triPts[4]);
            int triPtRight = (int) Math.max(triPts[2], triPts[4]);
            int triPtUp = (int) Math.min(triPts[3], triPts[5]);
            int triPtDown = (int) Math.max(triPts[3], triPts[5]);

            if (intersectX + TOLERANCE >= triPtLeft && intersectX <= triPtRight + TOLERANCE &&
                    intersectY + TOLERANCE >= triPtUp && intersectY <= triPtDown + TOLERANCE)
                return true;
        }

        // Case 3, no rectangle point is in triangle, no triangle point is in rectangle, but edges intersect

        // triLines represents 2 lines that form two edges of the triangle in y-intercept form. In form {m1,b1,...}
        double[] triLines = new double[4];
        for (int i = 0; i < triLines.length; i += 2) {
            triLines[i] = (triPts[i + 3] - triPts[i + 1]) / (triPts[i + 2] - triPts[i]);
            triLines[i + 1] = -triLines[i] * triPts[i] + triPts[i + 1];
        }

        for (int i = 0; i < triLines.length; i += 2) {
            // Check verticals
            int checkLeftX = rectX;
            int checkLeftY = (int) (triLines[i] * checkLeftX + triLines[i + 1]);
            int checkRightX = rectX + rectLength;
            int checkRightY = (int) (triLines[i] * checkRightX + triLines[i + 1]);

            int triPtLeft = (int) Math.min(triPts[i], triPts[i + 2]);
            int triPtRight = (int) Math.max(triPts[i], triPts[i + 2]);

            if (checkLeftX + TOLERANCE >= triPtLeft && checkLeftX <= triPtRight + TOLERANCE &&
                    checkLeftY + TOLERANCE >= rectY && checkLeftY <= rectY + rectLength + TOLERANCE)
                return true;

            if (checkRightX + TOLERANCE >= triPtLeft && checkRightX <= triPtRight + TOLERANCE &&
                    checkRightY + TOLERANCE >= rectY && checkRightY <= rectY + rectLength + TOLERANCE)
                return true;

            // Check a horizontal
            int checkTopY = rectY;
            int checkTopX = (int) ((checkTopY - triLines[i + 1]) / triLines[i]);

            int triPtUp = (int) Math.min(triPts[i + 1], triPts[i + 3]);
            int triPtDown = (int) Math.max(triPts[i + 1], triPts[i + 3]);

            if (checkTopX + TOLERANCE >= rectX && checkTopX <= rectX + rectLength + TOLERANCE &&
                    checkTopY + TOLERANCE >= triPtUp && checkTopY <= triPtDown + TOLERANCE)
                return true;
        }

        return false;
    }
}
