package com.collabdoc;

import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ForgotPasswordDialog {

    private final firebasehelper fb;

    public ForgotPasswordDialog(firebasehelper fb) {
        this.fb = fb;
    }

    public void show() {
        Dialog<Void> d = new Dialog<>();
        d.setTitle("Reset Password");
        VBox box = new VBox(8);
        box.setPadding(new javafx.geometry.Insets(12));

        TextField email = new TextField();
        email.setPromptText("Your registered email");

        PasswordField newPwd = new PasswordField();
        newPwd.setPromptText("New password");

        Button reset = new Button("Reset Password");
        reset.setOnAction(e -> {
            String em = email.getText().trim().toLowerCase();
            String pwd = newPwd.getText();
            if (em.isEmpty() || pwd.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Fill both fields", ButtonType.OK).show();
                return;
            }
            String key = em.replace(".", ",");
            try {
                String json = fb.get("users/" + key);
                if (json == null || json.equals("null")) {
                    new Alert(Alert.AlertType.ERROR, "Email not registered", ButtonType.OK).show();
                    return;
                }
                String hashed = HashUtil.sha256(pwd);
                java.util.Map<String, String> map = new java.util.HashMap<>();
                com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
                map.put("name", obj.get("name").getAsString());
                map.put("email", em);
                map.put("password", hashed);
                fb.put("users/" + key, map);
                new Alert(Alert.AlertType.INFORMATION, "Password reset successful", ButtonType.OK).show();
                d.close();
            } catch (IOException ex) {
                ex.printStackTrace();
                new Alert(Alert.AlertType.ERROR, "Network error", ButtonType.OK).show();
            }
        });

        box.getChildren().addAll(new Label("Enter registered email"), email, new Label("New password"), newPwd, reset);
        d.getDialogPane().setContent(box);
        d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        d.showAndWait();
    }
}

