package com.comp2042;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Screen;
import javafx.geometry.Rectangle2D;

import java.net.URL;
import java.util.ResourceBundle;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        URL location = getClass().getClassLoader().getResource("gameLayout.fxml");
        ResourceBundle resources = null;
        FXMLLoader fxmlLoader = new FXMLLoader(location, resources);
        Parent root = fxmlLoader.load();
        GuiController c = fxmlLoader.getController();

        primaryStage.setTitle("TetrisJFX");
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        Scene scene = new Scene(root, bounds.getWidth(), bounds.getHeight());
        primaryStage.setScene(scene);
        primaryStage.setX(bounds.getMinX());
        primaryStage.setY(bounds.getMinY());
        primaryStage.setWidth(bounds.getWidth());
        primaryStage.setHeight(bounds.getHeight());
        primaryStage.setMaximized(true);
        primaryStage.show();
        
        // Setup video background after scene is shown
        c.setupVideoBackgroundAfterSceneReady(scene);
        // Setup background music after scene is shown
        c.setupBackgroundMusicAfterSceneReady(scene);
        
        new GameController(c);
    }


    public static void main(String[] args) {
        launch(args);
    }
}
