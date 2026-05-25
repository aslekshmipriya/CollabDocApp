package com.collabdoc;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class signuppage {

    private final firebasehelper fb;

    public signuppage(firebasehelper fb) {
        this.fb = fb;
    }

    public Pane getView() {
        HBox root = new HBox();
        root.setStyle("-fx-background-color: linear-gradient(to right, #f7f8fb, #ffffff);");
        root.setPrefSize(1200, 800);

        // left informational panel
        VBox left = new VBox(14);
        left.setPadding(new Insets(80, 40, 80, 80));
        left.setPrefWidth(520);
        Label logo = new Label("CollabDoc");
        logo.setStyle("-fx-font-size:36px; -fx-font-weight:900; -fx-text-fill:#1976d2;");
        Label tag = new Label("Create an account and start collaborating in real time.");
        tag.setWrapText(true);
        left.getChildren().addAll(logo, tag);

        // right panel - card centered vertically
        VBox rightOuter = new VBox();
        rightOuter.setPrefWidth(520);
        rightOuter.setAlignment(Pos.CENTER);

        VBox card = new VBox(12);
        card.getStyleClass().add("card");
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(36));
        card.setMaxWidth(420);

        Label title = new Label("Sign up");
        title.setStyle("-fx-font-size:20px; -fx-font-weight:700;");

        TextField nameField = new TextField();
        nameField.setPromptText("Full name");
        nameField.getStyleClass().add("input");

        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        emailField.getStyleClass().add("input");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("input");

        Button signupBtn = new Button("Create account");
        signupBtn.getStyleClass().add("btn-primary");
        signupBtn.setMaxWidth(Double.MAX_VALUE);

        signupBtn.setOnAction(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim().toLowerCase();
            String pwd = passwordField.getText();
            if (name.isEmpty() || email.isEmpty() || pwd.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please fill all fields", ButtonType.OK).show();
                return;
            }
            String key = email.replace(".", ",");
            try {
                String json = fb.get("users/" + key);
                if (json != null && !json.equals("null")) {
                    new Alert(Alert.AlertType.ERROR, "Account already exists for this email.", ButtonType.OK).show();
                    return;
                }
                java.util.Map<String, Object> map = new java.util.HashMap<>();
                map.put("name", name);
                map.put("email", email);
                map.put("password", HashUtil.sha256(pwd));
                fb.put("users/" + key, map);
                new Alert(Alert.AlertType.INFORMATION, "Account created. Please login.", ButtonType.OK).show();
                Main.showLoginPage();
            } catch (Exception ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Network error", ButtonType.OK).show();
            }
        });

        card.getChildren().addAll(title, nameField, emailField, passwordField, signupBtn);

        rightOuter.getChildren().add(card);
        StackPane rightWrap = new StackPane(rightOuter);
        rightWrap.setAlignment(Pos.CENTER_RIGHT);
        rightWrap.setPadding(new Insets(80, 80, 80, 40));
        rightWrap.setPrefWidth(520);

        root.getChildren().addAll(left, rightWrap);
        HBox.setHgrow(rightWrap, Priority.ALWAYS);

        return root;
    }
}

