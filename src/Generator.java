import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Random;

/**
 * Driver class. Theoretically set up so that it can easily run different aperiodic tilings
 */
public class Generator extends Application {
    // Base length of rootTri. Also is the length of the scene
    private static final int BASE_LENGTH = 900;

    // Change to increase or decrease the density of the triangles in the rectangle
    private static final double DENSITY_CONSTANT = 1500;
    // Makes it to where the bounding box won't be at one of the extremes. Also controls minimum width
    private static final int BUFFER = 60;


    @Override
    public void start(Stage window) {
        Pane center = new Pane();

        // Used to determine the size of the window
        int height = (int) (BASE_LENGTH / 2 * Math.tan(Math.PI / 5)); // For baseLength=1000, height=363

        Label depthLabel = new Label("Enter the depth of the algorithm");
        depthLabel.setBackground(Background.fill(Color.WHITESMOKE));

        TextField depthTextBox = new TextField();

        Label errorLabel = new Label("Invalid input. Only input an integer 0+");
        errorLabel.setBackground(Background.fill(Color.WHITESMOKE));
        errorLabel.setVisible(false);

        depthTextBox.setOnAction(event -> {
            String textInput = depthTextBox.getText();

            depthTextBox.clear();

            // Root triangle is always the same
            KDPenroseTri rootTri = new KDPenroseTri((short) 2, new double[]{BASE_LENGTH / 2., 0}, new double[]{0, height},
                    new double[]{BASE_LENGTH, height});

            ArrayList<KDPenroseTri> tiles;

            try {
                int depth = Integer.parseInt(textInput);
                // Past 12 and start to run into floating point errors
                if (depth > 12)
                    throw new IllegalArgumentException("Depth too large");

                // Clears the previous generation
                center.getChildren().clear();

                KDPenroseTri.CheckTriangle setTri = (tri) -> tri;
                // Gets a list of the tiles at the specified depth (None of the parents are in the list)
                tiles = buildPenroseTiles(rootTri, depth, setTri);

                errorLabel.setVisible(false);
            } catch (Exception e) {
                System.out.println(e.getMessage());
                errorLabel.setVisible(true);

                return;
            }

            // Draws the new tiles next
            for (KDPenroseTri each : tiles)
                if (!each.isGhostDrawn())
                    each.draw(center);
        });

        HBox depthInputBox = new HBox(depthLabel, depthTextBox, errorLabel);
        depthInputBox.setSpacing(10);
        depthInputBox.setAlignment(Pos.BOTTOM_LEFT);

        Button randomSection = new Button("Want to find a random subsection?");

        CheckBox fullscreenSelection = new CheckBox("Fullscreen?");
        fullscreenSelection.setBackground(Background.fill(Color.WHITESMOKE));

        randomSection.setOnAction(event -> {
            // Root triangle is always the same
            KDPenroseTri rootTri = new KDPenroseTri((short) 2, new double[]{BASE_LENGTH / 2., 0},
                    new double[]{0, height}, new double[]{BASE_LENGTH, height});

            Random r = new Random();

            // Initializes the coordinates of the rectangle within rootTri (which is a square). This assumes start
            // triangle's base is horizontal

            // Initializes the upper y coordinate of the rectangle. Cannot be within buffer of the top or bottom
            int rectY = r.nextInt(height - 2 * BUFFER) + BUFFER;

            // Length of line in the triangle that parallel to the baseline and passes. Useful in calculating rectX and
            // rectLength
            double y1BaseLength = Math.abs(2 * rectY / Math.tan(Math.PI / 5));

            // Initializes a random x coordinate used for either the left or right x coordinate of the rectangle
            int rectX = BASE_LENGTH / 2 + (r.nextBoolean() ? 1 : -1) * r.nextInt((int) (y1BaseLength / 2));

            // Initializes the length of the rectangle. The upper limit is calculated to make sure it isn't forced to go
            // outside the bounds of the triangle. Also has a minimum length of buffer/2
            int rectLength = r.nextInt((int) Math.min(height - rectY,
                    Math.abs(BASE_LENGTH / 2 - rectX) + y1BaseLength / 2) - BUFFER) + BUFFER / 2;

            // Ensures rectX into the left point of the rectangle
            rectX = rectX < BASE_LENGTH / 2 ? rectX : rectX - rectLength;

            // Identifies where in space the rectangle is
            Rectangle bound = new Rectangle(rectX, rectY, rectLength,
                    rectLength);
            bound.setFill(Color.rgb(0, 0, 0, 0));
            bound.setStroke(Color.BLUE);
            bound.setStrokeWidth(2);


            // Calculates the needed density to be roughly the same density given varying rectangle areas

            // Number of type A and type B triangles. Type A triangles have 2 type A children and 1 type B children and
            // type B triangles have 1 of each
            int typeATris = 0;
            int typeBTris = 1;

            // Algorithm goes down in depth until the density aligns with densityConstant
            int depth = 0;
            while (DENSITY_CONSTANT / Math.pow(rectLength, 2) > (2. * (typeATris + typeBTris)) /
                    (BASE_LENGTH * height)) {
                int prevTypeATris = typeATris;
                int prevTypeBTris = typeBTris;
                typeATris = 2 * prevTypeATris + prevTypeBTris;
                typeBTris = prevTypeATris + prevTypeBTris;
                depth++;
            }

            // Clears the previous generation
            center.getChildren().clear();

            // Lambda function that checks if a triangle overlaps with the rectangle

            // Avoids error with lambda expression
            int finalRectX = rectX;
            KDPenroseTri.CheckTriangle setTri = (tri) ->
                    tri.overlapping(finalRectX, rectY, rectLength) ? tri : null;

            ArrayList<KDPenroseTri> tiles = buildPenroseTiles(rootTri, depth, setTri);

            if (fullscreenSelection.isSelected()) {
                // Draws the new tiles to the scale of the screen
                for (KDPenroseTri each : tiles)
                    if (!each.isGhostDrawn())
                        each.drawScaled(center, rectX, rectY, ((double) (BASE_LENGTH) / rectLength));
            } else {
                // Draws ghost tiles first
                for (KDPenroseTri each : tiles)
                    if (each.isGhostDrawn())
                        each.ghostDraw(center);

                // Draws the new tiles next
                for (KDPenroseTri each : tiles)
                    if (!each.isGhostDrawn())
                        each.draw(center);

                center.getChildren().add(bound);
            }
        });

        HBox randomSubsectionBox = new HBox(fullscreenSelection, randomSection);
        randomSubsectionBox.setSpacing(10);
        randomSubsectionBox.setAlignment(Pos.BOTTOM_RIGHT);

        // Formatting for the bottom
        HBox bottomBox = new HBox();
        bottomBox.setSpacing(10);

        HBox.setHgrow(depthInputBox, javafx.scene.layout.Priority.ALWAYS);
        HBox.setHgrow(randomSubsectionBox, javafx.scene.layout.Priority.ALWAYS);
        bottomBox.getChildren().addAll(depthInputBox, new javafx.scene.layout.Region(), randomSubsectionBox);

        StackPane root = new StackPane(center, bottomBox);

        Scene scene = new Scene(root, BASE_LENGTH, BASE_LENGTH);

        window.setTitle("Aperiodic Tiling Generator");
        window.setScene(scene);
        window.show();
    }

    /**
     * Recursive function that builds the penrose tiling from the root triangle down
     *
     * @param rootTri the triangle which the function will build tiling in
     * @param depth   how many more generations this method will go down
     * @param setTri  lambda function that lets this method work with and without a rectangle constraint
     * @return a list of the base tiles within rootTri
     */
    private ArrayList<KDPenroseTri> buildPenroseTiles(KDPenroseTri rootTri, int depth,
                                                      KDPenroseTri.CheckTriangle setTri) {
        if (depth < 0)
            throw new IllegalArgumentException("Depth less than 0");

        // Base case
        if (depth == 0) {
            ArrayList<KDPenroseTri> tile = new ArrayList<>();
            tile.add(rootTri);
            return tile;
        }

        // Recursive case
        KDPenroseTri[] children = rootTri.buildChildren(setTri);
        ArrayList<KDPenroseTri> tiles = new ArrayList<>();
        rootTri.setGhostDrawn(true);
        tiles.add(rootTri);

        for (KDPenroseTri each : children)
            if (each != null)
                tiles.addAll(buildPenroseTiles(each, depth - 1, setTri));


        return tiles;
    }
}
