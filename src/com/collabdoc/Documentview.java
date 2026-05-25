package com.collabdoc;

import com.google.gson.Gson;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.web.HTMLEditor;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

public class Documentview {

    private String email;
    private firebasehelper fb;
    private String currentDoc = "doc1";

    public Documentview(String email, firebasehelper fb) {
        this.email = email;
        this.fb = fb;
    }

    public BorderPane getView(Stage stage, String email) {

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color:#f1f3f4;");

        /* ===================== EDITOR ===================== */

        HTMLEditor editor = new HTMLEditor();
        editor.setPrefHeight(900);

        String content = fb.loadDocument(email, currentDoc);
        if (content != null && !content.equals("null")) {
            editor.setHtmlText(content);
        }

        VBox page = new VBox(editor);
        page.setMaxWidth(850);
        page.setPadding(new Insets(30));
        page.setStyle("-fx-background-color:white; -fx-effect:dropshadow(gaussian, rgba(0,0,0,0.15),10,0,0,4);");

        StackPane workspace = new StackPane(page);
        workspace.setPadding(new Insets(30));

        ScrollPane scroll = new ScrollPane(workspace);
        scroll.setFitToWidth(true);

        root.setCenter(scroll);

        /* ===================== HEADER ===================== */

        TextField title = new TextField(fb.loadTitle(email, currentDoc));
        title.setStyle("-fx-font-size:20px; -fx-font-weight:bold; -fx-background-color:transparent;");
        title.textProperty().addListener((obs, o, n) ->
                fb.saveTitle(email, currentDoc, n)
        );

        Button save = new Button("💾 Save");
        Button download = new Button("⬇ Download");
        Button addPage = new Button("📄 Add Page");
        Button share = new Button("🔗 Share");
        Button logout = new Button("🚪 Logout");

        logout.setStyle("-fx-background-color:#ff4b4b; -fx-text-fill:white;");

        HBox header = new HBox(15, title, save, download, addPage, share, logout);
        header.setPadding(new Insets(10));
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color:white;");

        /* ===================== MENU BAR ===================== */

        MenuBar menuBar = new MenuBar();

        Menu fileMenu = new Menu("File");
        MenuItem newFile = new MenuItem("New");
        MenuItem saveFile = new MenuItem("Save");
        MenuItem downloadFile = new MenuItem("Download");

        fileMenu.getItems().addAll(newFile, saveFile, downloadFile);

        Menu editMenu = new Menu("Edit");
        MenuItem copy = new MenuItem("Copy");
        MenuItem paste = new MenuItem("Paste");
        MenuItem clear = new MenuItem("Clear");

        editMenu.getItems().addAll(copy, paste, clear);

        Menu insertMenu = new Menu("Insert");
        MenuItem addPageMenu = new MenuItem("Add Page");
        insertMenu.getItems().add(addPageMenu);

        Menu formatMenu = new Menu("Format");
        MenuItem boldMenu = new MenuItem("Bold");
        MenuItem italicMenu = new MenuItem("Italic");
        MenuItem underlineMenu = new MenuItem("Underline");

        MenuItem leftAlign = new MenuItem("Align Left");
        MenuItem centerAlign = new MenuItem("Align Center");
        MenuItem rightAlign = new MenuItem("Align Right");

        formatMenu.getItems().addAll(
                boldMenu, italicMenu, underlineMenu,
                new SeparatorMenuItem(),
                leftAlign, centerAlign, rightAlign
        );

        menuBar.getMenus().addAll(fileMenu, editMenu, insertMenu, formatMenu);

        VBox topContainer = new VBox(header, menuBar);
        root.setTop(topContainer);

        /* ===================== LEFT PANEL ===================== */

        ListView<String> docs = new ListView<>();
        docs.getItems().add("doc1");

        docs.setOnMouseClicked(e -> {
            currentDoc = docs.getSelectionModel().getSelectedItem();
            editor.setHtmlText(fb.loadDocument(email, currentDoc));
            title.setText(fb.loadTitle(email, currentDoc));
        });

        Button newDocBtn = new Button("➕ New Document");
        newDocBtn.setOnAction(e -> {
            String newId = "doc" + (docs.getItems().size() + 1);
            docs.getItems().add(newId);
        });

        VBox left = new VBox(10, new Label("📄 Documents"), docs, newDocBtn);
        left.setPadding(new Insets(10));
        left.setPrefWidth(200);
        left.setStyle("-fx-background-color:white;");
        root.setLeft(left);

        /* ===================== RIGHT PANEL (ACTIVE USERS) ===================== */

        ListView<String> users = new ListView<>();

        fb.addActiveUser(currentDoc, email);

        Timeline userRefresh = new Timeline(
                new KeyFrame(Duration.seconds(3), e -> {
                    try {
                        String data = fb.get("activeUsers/" + currentDoc);
                        users.getItems().clear();

                        if (data != null && !data.equals("null")) {
                            Map<String, Object> map =
                                    new Gson().fromJson(data, Map.class);

                            for (String key : map.keySet()) {
                                users.getItems().add("🟢 " + key.replace(",", "."));
                            }
                        }

                    } catch (Exception ignored) {
                    }
                })
        );

        userRefresh.setCycleCount(Timeline.INDEFINITE);
        userRefresh.play();

        VBox right = new VBox(10, new Label("👥 Active Users"), users);
        right.setPadding(new Insets(10));
        right.setPrefWidth(200);
        right.setStyle("-fx-background-color:white;");
        root.setRight(right);

        /* ===================== STATUS BAR ===================== */

        Label status = new Label("Ready");
        Label words = new Label("Words: 0");

        HBox bottom = new HBox(20, status, words);
        bottom.setPadding(new Insets(10));
        bottom.setStyle("-fx-background-color:white;");
        root.setBottom(bottom);

        /* ===================== LOGIC ===================== */

        // Word count
        editor.setOnKeyReleased(e -> {
            String text = editor.getHtmlText().replaceAll("<[^>]*>", "");
            int count = text.trim().isEmpty() ? 0 : text.trim().split("\\s+").length;
            words.setText("Words: " + count);
        });

        // Save
        save.setOnAction(e -> {
            fb.saveDocument(email, currentDoc, editor.getHtmlText());
            status.setText("Saved");
        });

        saveFile.setOnAction(e -> save.fire());

        // Download (FIXED → TEXT FILE)
        download.setOnAction(e -> {
            try {
                FileChooser fc = new FileChooser();
                fc.setInitialFileName("document.txt");
                File file = fc.showSaveDialog(stage);

                if (file != null) {
                    String text = editor.getHtmlText().replaceAll("<[^>]*>", "");
                    FileWriter writer = new FileWriter(file);
                    writer.write(text);
                    writer.close();
                    status.setText("Downloaded as TXT");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        downloadFile.setOnAction(e -> download.fire());

        // Add Page
        addPage.setOnAction(e -> {
            editor.setHtmlText(editor.getHtmlText() +
                    "<div style='page-break-before:always;'></div><p><br></p>");
        });

        addPageMenu.setOnAction(e -> addPage.fire());

        // New File
        newFile.setOnAction(e -> editor.setHtmlText(""));

        // Edit actions
        copy.setOnAction(e -> applyFormatting(editor, "copy"));
        paste.setOnAction(e -> applyFormatting(editor, "paste"));
        clear.setOnAction(e -> editor.setHtmlText(""));

        // Formatting
        boldMenu.setOnAction(e -> applyFormatting(editor, "bold"));
        italicMenu.setOnAction(e -> applyFormatting(editor, "italic"));
        underlineMenu.setOnAction(e -> applyFormatting(editor, "underline"));

        leftAlign.setOnAction(e -> applyFormatting(editor, "justifyLeft"));
        centerAlign.setOnAction(e -> applyFormatting(editor, "justifyCenter"));
        rightAlign.setOnAction(e -> applyFormatting(editor, "justifyRight"));

        // Auto-save
        Timeline autosave = new Timeline(
                new KeyFrame(Duration.seconds(5), e ->
                        fb.saveDocument(email, currentDoc, editor.getHtmlText()))
        );
        autosave.setCycleCount(Timeline.INDEFINITE);
        autosave.play();

        // Share
        share.setOnAction(e -> {
            TextInputDialog d = new TextInputDialog();
            d.setHeaderText("Enter email to share");
            d.showAndWait().ifPresent(target ->
                    fb.shareDocument(currentDoc, email, target));
        });

        // Logout
        logout.setOnAction(e -> {
            fb.removeActiveUser(currentDoc, email);
            Main.showLoginPage();
        });

        return root;
    }

    // Hidden formatting (alignment + bold works)
    private void applyFormatting(HTMLEditor editor, String command) {
        try {
            WebView webView = (WebView) editor.lookup(".web-view");
            if (webView != null) {
                webView.getEngine()
                        .executeScript("document.execCommand('" + command + "', false, null);");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
