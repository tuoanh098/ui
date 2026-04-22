package com.trohub.ui.auth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import org.json.JSONObject;
import org.json.JSONArray;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class SessionManager {
    private static final String PREF_NAME = "trohub_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_ROLES = "roles";

    private final SharedPreferences prefs;

    public SessionManager(Context context) {
        this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public void saveSession(String token, String username, String rolesCsv) {
        prefs.edit()
                .putString(KEY_TOKEN, token)
                .putString(KEY_USERNAME, username)
                .putString(KEY_ROLES, rolesCsv)
                .apply();
    }

    public String getToken() {
        return prefs.getString(KEY_TOKEN, null);
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, "");
    }

    public String getRole() {
        List<String> roles = getRoles();
        return roles.isEmpty() ? "" : roles.get(0);
    }

    public List<String> getRoles() {
        List<String> fromToken = getRolesFromToken();
        if (!fromToken.isEmpty()) return fromToken;

        String raw = prefs.getString(KEY_ROLES, "");
        List<String> out = new ArrayList<>();
        if (raw == null || raw.trim().isEmpty()) return out;
        String[] split = raw.split(",");
        for (String s : split) {
            String role = s.trim();
            if (!role.isEmpty()) out.add(role);
        }
        return out;
    }

    public boolean hasAnyRole(String... roles) {
        List<String> mine = getRoles();
        for (String myRole : mine) {
            String normalizedMine = normalizeRole(myRole);
            for (String role : roles) {
                if (normalizedMine.equals(normalizeRole(role))) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isAdminOrLandlord() {
        return hasAnyRole("ROLE_ADMIN", "ROLE_LANDLORD");
    }

    private String normalizeRole(String role) {
        if (role == null) return "";
        String normalized = role.trim().toUpperCase();
        if (normalized.isEmpty()) return "";
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        return normalized;
    }

    public Long getUserIdFromToken() {
        JSONObject obj = parseTokenPayload();
        if (obj == null || !obj.has("userId")) return null;
        try {
            return obj.getLong("userId");
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<String> getRolesFromToken() {
        JSONObject obj = parseTokenPayload();
        List<String> out = new ArrayList<>();
        if (obj == null || !obj.has("roles")) return out;
        try {
            JSONArray arr = obj.getJSONArray("roles");
            for (int i = 0; i < arr.length(); i++) {
                String role = arr.optString(i, "");
                if (!role.isEmpty()) out.add(role);
            }
        } catch (Exception ignored) {
        }
        return out;
    }

    private JSONObject parseTokenPayload() {
        String token = getToken();
        if (token == null || token.trim().isEmpty()) return null;
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;
            String payload = parts[1];
            int padding = (4 - payload.length() % 4) % 4;
            StringBuilder builder = new StringBuilder(payload);
            for (int i = 0; i < padding; i++) builder.append('=');
            byte[] decoded = Base64.decode(builder.toString(), Base64.URL_SAFE | Base64.NO_WRAP);
            return new JSONObject(new String(decoded, StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isLoggedIn() {
        String token = getToken();
        return token != null && !token.trim().isEmpty();
    }

    public void clear() {
        prefs.edit().clear().apply();
    }
}
