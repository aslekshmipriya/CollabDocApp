package com.collabdoc;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class loginpage {

    private final firebasehelper fb;

    public loginpage(firebasehelper fb) {
        this.fb = fb;
    }

    public Pane getView() {
        HBox root = new HBox();
        root.setStyle("-fx-background-color: linear-gradient(to right, #e8f3ff, #ffffff);");
        root.setPrefSize(1200, 800);

        // left panel (logo + tag)
        VBox left = new VBox(14);
        left.setPadding(new Insets(80, 40, 80, 80));
        left.setPrefWidth(520);


        Label logo = new Label("CollabDoc");
        logo.setStyle("""
    -fx-font-size:52px;
    -fx-font-weight:900;
    -fx-text-fill: linear-gradient(to right, #1a73e8, #4285f4);
""");

        Label tagline = new Label("Collaborate. Create. Share.");
        tagline.setStyle("""
    -fx-font-size:18px;
    -fx-text-fill:#5f6368;
    -fx-font-weight:500;
""");

        Label tag = new Label("Real-time collaborative document editor. Fast. Simple. Secure.");
        tag.setWrapText(true);
        tag.setStyle("-fx-text-fill:#333; -fx-font-size:16px;");
        left.getChildren().addAll(logo, tagline , tag);
        left.setSpacing(12);
        // right panel - card
        VBox outerRightWrap = new VBox();
        outerRightWrap.setPrefWidth(520);
        outerRightWrap.setAlignment(Pos.CENTER);

        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36));
        card.setMaxWidth(420);

        Label title = new Label("Sign in");
        title.setStyle("-fx-font-size:20px; -fx-font-weight:700;");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("input");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("input");

        Hyperlink forgotPassword = new Hyperlink("Forgot password?");
        forgotPassword.setOnAction(e -> new ForgotPasswordDialog(fb).show());

        Button loginButton = new Button("Log in");
        loginButton.getStyleClass().add("btn-primary");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        Label signup = new Label("Don't have an account? Sign up");
        signup.setStyle("-fx-text-fill:#1976d2; -fx-cursor:hand;");
        signup.setOnMouseClicked(e -> Main.showSignUpPage());

        // Login action
        loginButton.setOnAction(e -> {
            String email = emailField.getText().trim().toLowerCase();
            String pwd = passwordField.getText();
            if (email.isEmpty() || pwd.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please fill all fields", ButtonType.OK).show();
                return;
            }
            String key = email.replace(".", ",");
            try {
                String userJson = fb.get("users/" + key);
                if (userJson == null || userJson.equals("null")) {
                    new Alert(Alert.AlertType.ERROR, "No account found for this email.", ButtonType.OK).show();
                    return;
                }
                com.google.gson.Gson gson = new com.google.gson.Gson();
                java.util.Map user = gson.fromJson(userJson, java.util.Map.class);
                String storedHashedPwd = (String) user.get("password");
                String enteredHashedPwd = HashUtil.sha256(pwd);
                if (storedHashedPwd.equals(enteredHashedPwd)) {
                    String name = (String) user.get("name");
                    // nicer info
                    new Alert(Alert.AlertType.INFORMATION, "Welcome back, " + name + " \uD83D\uDE80", ButtonType.OK).show();
                    // set presence
                    fb.setPresence(key, true, name);
                    // open editor
                    new Alert(Alert.AlertType.INFORMATION,
                            "Login Successful! Editor will open here").show();
                    Main.showDocumentView(email);
                } else {
                    new Alert(Alert.AlertType.ERROR, "Incorrect password.", ButtonType.OK).show();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Network error: " + ex.getMessage(), ButtonType.OK).show();
            }
        });

        card.getChildren().addAll(title, emailField, passwordField, forgotPassword, loginButton, signup);

        outerRightWrap.getChildren().add(card);
        StackPane rightWrap = new StackPane(outerRightWrap);
        rightWrap.setAlignment(Pos.CENTER_RIGHT);
        rightWrap.setPadding(new Insets(80, 80, 80, 40));
        rightWrap.setPrefWidth(520);// compose
        root.getChildren().addAll(left, rightWrap);
        HBox.setHgrow(rightWrap, Priority.ALWAYS);
        return root;
    }
}

