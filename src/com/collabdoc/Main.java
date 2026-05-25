package com.collabdoc;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private static Stage primaryStage;
    public static final String FIREBASE_BASE_URL =
            "https://collabdoc-ee1dd-default-rtdb.asia-southeast1.firebasedatabase.app"; // change if needed
    private static firebasehelper fb;

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        fb = new firebasehelper(FIREBASE_BASE_URL); // firebase helper requires base URL
        showLoginPage();
        stage.setTitle("CollabDoc");
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.setFullScreenExitHint("");
        stage.show();
    }

    public static firebasehelper getFirebase() { return fb; }

    public static void showLoginPage() {
        loginpage login = new loginpage(fb);
        Scene scene = new Scene(login.getView(), 1200, 800);
        scene.getStylesheets().add(Main.class.getResource("styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.centerOnScreen();
    }

    public static void showSignUpPage() {
        signuppage signup = new signuppage(fb);
        Scene scene = new Scene(signup.getView(), 1200, 800);
        scene.getStylesheets().add(Main.class.getResource("styles.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.centerOnScreen();
    }

    public static void showDocumentView(String email) {

        Documentview doc = new Documentview(email, fb);

        Scene scene = new Scene(doc.getView(primaryStage, email), 1000, 700);

        primaryStage.setScene(scene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

