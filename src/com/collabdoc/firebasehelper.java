package com.collabdoc;

import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class firebasehelper {
    private final String baseUrl;
    private final Gson gson = new Gson();

    public firebasehelper(String baseUrl) {
        if (baseUrl.endsWith("/"))
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);

        this.baseUrl = baseUrl;
    }

    //  IMPORTANT: SANITIZE EMAIL
    public String sanitize(String email) {
        return email.replace(".", ",");
    }

    // GET METHOD
    public String get(String path) throws IOException {

        String full = baseUrl + "/" + path + ".json";
        URL url = new URL(full);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setConnectTimeout(8000);
        con.setReadTimeout(8000);

        int code = con.getResponseCode();

        InputStream is = (code >= 200 && code < 300)
                ? con.getInputStream()
                : con.getErrorStream();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null)
                sb.append(line);

            return sb.toString();
        }
    }

    // PUT METHOD

    public String put(String path, Object obj) throws IOException {
        return sendWithBody("PUT", path, obj);
    }

    // POST METHOD
    public String post(String path, Object obj) throws IOException {
        return sendWithBody("POST", path, obj);
    }

    // INTERNAL HTTP METHOD
    private String sendWithBody(String method, String path, Object obj) throws IOException {

        String full = baseUrl + "/" + path + ".json";
        URL url = new URL(full);

        HttpURLConnection con = (HttpURLConnection) url.openConnection();

        con.setRequestMethod(method);
        con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        con.setDoOutput(true);

        String json = gson.toJson(obj);

        try (OutputStream os = con.getOutputStream()) {
            os.write(json.getBytes(StandardCharsets.UTF_8));
        }

        int code = con.getResponseCode();

        InputStream is = (code >= 200 && code < 300)
                ? con.getInputStream()
                : con.getErrorStream();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(is, StandardCharsets.UTF_8))) {

            StringBuilder sb = new StringBuilder();
            String line;

            while ((line = br.readLine()) != null)
                sb.append(line);

            return sb.toString();
        }
    }

    //  SAVE DOCUMENT

    public void saveDocument(String email, String docId, String content) {

        try {
            String key = sanitize(email);

            Map<String, Object> map = new java.util.HashMap<>();
            map.put("content", content);
            map.put("lastUpdated", System.currentTimeMillis());

            put("documents/" + key + "/" + docId, map);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //  LOAD DOCUMENT
    public String loadDocument(String email, String docId) {

        try {
            String key = sanitize(email);

            String res = get("documents/" + key + "/" + docId + "/content");

            if (res == null || res.equals("null") || res.contains("error"))
                return "";

            return res.replace("\"", "");

        } catch (Exception e) {
            return "";
        }
    }

    //  SAVE TITLE
    public void saveTitle(String email, String docId, String title) {
        try {
            String key = sanitize(email);
            put("documents/" + key + "/" + docId + "/title", title);
        } catch (Exception ignored) {}
    }

    // LOAD TITLE
    public String loadTitle(String email, String docId) {

        try {
            String key = sanitize(email);

            String res = get("documents/" + key + "/" + docId + "/title");

            if (res == null || res.equals("null"))
                return "Untitled document";

            return res.replace("\"", "");

        } catch (Exception e) {
            return "Untitled document";
        }
    }

    //  SHARE DOCUMENT
    public void shareDocument(String docId, String ownerEmail, String targetEmail) {

        try {
            String owner = sanitize(ownerEmail);
            String target = sanitize(targetEmail);

            put("shared/" + docId + "/owner", owner);
            put("shared/" + docId + "/collaborators/" + target, true);

        } catch (Exception ignored) {}
    }
    // ACTIVE USERS

    public void addActiveUser(String docId, String email) {
        try {
            String key = sanitize(email);
            put("activeUsers/" + docId + "/" + key, true);
        } catch (Exception ignored) {}
    }

    public void removeActiveUser(String docId, String email) {
        try {
            String key = sanitize(email);
            put("activeUsers/" + docId + "/" + key, null);
        } catch (Exception ignored) {}
    }
    // presence
    public void setPresence(String email, boolean isOnline, String name) {

        try {
            String key = sanitize(email);

            Map<String, Object> map = new java.util.HashMap<>();
            map.put("name", name);
            map.put("online", isOnline);
            map.put("lastSeen", System.currentTimeMillis());

            put("presence/" + key, map);

        } catch (Exception ignored) {}
    }
}