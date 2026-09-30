package com.aether.aegis.data.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * AEGIS Mobile REST Client
 * Communicates with the FastAPI backend server (port 8000).
 * Supports standard Android Emulator host loopback (10.0.2.2) and local Wi-Fi / localhost.
 */
public class AegisApiClient {
    private static final String TAG = "AegisApiClient";
    // 10.0.2.2 is default Android Emulator mapping to development machine localhost
    private static final String DEFAULT_BASE_URL = "http://10.0.2.2:8000";
    private static AegisApiClient instance;

    private String baseUrl = DEFAULT_BASE_URL;
    private final ExecutorService executor = Executors.newFixedThreadPool(2);
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onFailure(Exception error);
    }

    public static class OverviewData {
        public String deviceStatus;
        public int overallRisk;
        public int threatRisk;
        public int privacyRisk;
        public int activeThreatsCount;
        public int monitoredAppsCount;
    }

    private AegisApiClient() {}

    public static synchronized AegisApiClient getInstance() {
        if (instance == null) {
            instance = new AegisApiClient();
        }
        return instance;
    }

    public void setBaseUrl(String url) {
        this.baseUrl = url;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void checkHealth(final ApiCallback<Boolean> callback) {
        executor.execute(() -> {
            try {
                String response = executeGet(baseUrl + "/health", 2500);
                JSONObject json = new JSONObject(response);
                boolean isOk = "ok".equalsIgnoreCase(json.optString("status"));
                mainHandler.post(() -> callback.onSuccess(isOk));
            } catch (Exception e) {
                Log.w(TAG, "Backend health check failed: " + e.getMessage());
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public void fetchOverview(final ApiCallback<OverviewData> callback) {
        executor.execute(() -> {
            try {
                String response = executeGet(baseUrl + "/api/v1/overview", 3000);
                JSONObject json = new JSONObject(response);

                OverviewData data = new OverviewData();
                data.deviceStatus = json.optString("device_status", "MONITORING");
                data.overallRisk = json.optInt("overall_risk", 78);
                data.threatRisk = json.optInt("threat_risk", 84);
                data.privacyRisk = json.optInt("privacy_risk", 62);
                data.activeThreatsCount = json.optInt("active_threats_count", 0);
                data.monitoredAppsCount = json.optInt("monitored_apps_count", 0);

                mainHandler.post(() -> callback.onSuccess(data));
            } catch (Exception e) {
                Log.w(TAG, "Fetch overview failed: " + e.getMessage());
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public void fetchApps(final ApiCallback<JSONArray> callback) {
        executor.execute(() -> {
            try {
                String response = executeGet(baseUrl + "/api/v1/apps", 3000);
                JSONArray array = new JSONArray(response);
                mainHandler.post(() -> callback.onSuccess(array));
            } catch (Exception e) {
                Log.w(TAG, "Fetch apps failed: " + e.getMessage());
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public void fetchAppDetail(String appId, final ApiCallback<JSONObject> callback) {
        executor.execute(() -> {
            try {
                String response = executeGet(baseUrl + "/api/v1/apps/" + appId, 3000);
                JSONObject json = new JSONObject(response);
                mainHandler.post(() -> callback.onSuccess(json));
            } catch (Exception e) {
                Log.w(TAG, "Fetch app detail failed: " + e.getMessage());
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public void fetchAppRisk(String appId, final ApiCallback<JSONObject> callback) {
        executor.execute(() -> {
            try {
                String response = executeGet(baseUrl + "/api/v1/apps/" + appId + "/risk", 3000);
                JSONObject json = new JSONObject(response);
                mainHandler.post(() -> callback.onSuccess(json));
            } catch (Exception e) {
                Log.w(TAG, "Fetch app risk failed: " + e.getMessage());
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public void fetchAppBehaviour(String appId, final ApiCallback<JSONArray> callback) {
        executor.execute(() -> {
            try {
                String response = executeGet(baseUrl + "/api/v1/apps/" + appId + "/behaviour", 3000);
                JSONArray array = new JSONArray(response);
                mainHandler.post(() -> callback.onSuccess(array));
            } catch (Exception e) {
                Log.w(TAG, "Fetch app behaviour failed: " + e.getMessage());
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public void fetchEvidenceGraph(String appId, final ApiCallback<JSONObject> callback) {
        executor.execute(() -> {
            try {
                String response = executeGet(baseUrl + "/api/v1/apps/" + appId + "/evidence-graph", 3000);
                JSONObject json = new JSONObject(response);
                mainHandler.post(() -> callback.onSuccess(json));
            } catch (Exception e) {
                Log.w(TAG, "Fetch evidence graph failed: " + e.getMessage());
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public void fetchThreats(final ApiCallback<JSONArray> callback) {
        executor.execute(() -> {
            try {
                String response = executeGet(baseUrl + "/api/v1/threats", 3000);
                JSONArray array = new JSONArray(response);
                mainHandler.post(() -> callback.onSuccess(array));
            } catch (Exception e) {
                Log.w(TAG, "Fetch threats failed: " + e.getMessage());
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public void fetchActivity(final ApiCallback<JSONArray> callback) {
        executor.execute(() -> {
            try {
                String response = executeGet(baseUrl + "/api/v1/activity", 3000);
                JSONArray array = new JSONArray(response);
                mainHandler.post(() -> callback.onSuccess(array));
            } catch (Exception e) {
                Log.w(TAG, "Fetch activity failed: " + e.getMessage());
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public void fetchRiskHistory(String appId, final ApiCallback<JSONArray> callback) {
        executor.execute(() -> {
            try {
                String response = executeGet(baseUrl + "/api/v1/apps/" + appId + "/risk-history", 3000);
                JSONArray array = new JSONArray(response);
                mainHandler.post(() -> callback.onSuccess(array));
            } catch (Exception e) {
                Log.w(TAG, "Fetch risk history failed: " + e.getMessage());
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public void fetchFindings(String appId, final ApiCallback<JSONArray> callback) {
        executor.execute(() -> {
            try {
                String response = executeGet(baseUrl + "/api/v1/apps/" + appId + "/findings", 3000);
                JSONArray array = new JSONArray(response);
                mainHandler.post(() -> callback.onSuccess(array));
            } catch (Exception e) {
                Log.w(TAG, "Fetch findings failed: " + e.getMessage());
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public void fetchEvidence(String appId, final ApiCallback<JSONArray> callback) {
        executor.execute(() -> {
            try {
                String response = executeGet(baseUrl + "/api/v1/apps/" + appId + "/evidence", 3000);
                JSONArray array = new JSONArray(response);
                mainHandler.post(() -> callback.onSuccess(array));
            } catch (Exception e) {
                Log.w(TAG, "Fetch evidence failed: " + e.getMessage());
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public void startDemo(final ApiCallback<JSONObject> callback) {
        executor.execute(() -> {
            try {
                String response = executePost(baseUrl + "/api/v1/demo/start", 3000);
                JSONObject json = new JSONObject(response);
                mainHandler.post(() -> callback.onSuccess(json));
            } catch (Exception e) {
                Log.w(TAG, "Start demo failed: " + e.getMessage());
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public void advanceDemo(final ApiCallback<JSONObject> callback) {
        executor.execute(() -> {
            try {
                String response = executePost(baseUrl + "/api/v1/demo/step", 3000);
                JSONObject json = new JSONObject(response);
                mainHandler.post(() -> callback.onSuccess(json));
            } catch (Exception e) {
                Log.w(TAG, "Advance demo failed: " + e.getMessage());
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public void resetDemo(final ApiCallback<JSONObject> callback) {
        executor.execute(() -> {
            try {
                String response = executePost(baseUrl + "/api/v1/demo/reset", 3000);
                JSONObject json = new JSONObject(response);
                mainHandler.post(() -> callback.onSuccess(json));
            } catch (Exception e) {
                Log.w(TAG, "Reset demo failed: " + e.getMessage());
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    public void getDemoStatus(final ApiCallback<JSONObject> callback) {
        executor.execute(() -> {
            try {
                String response = executeGet(baseUrl + "/api/v1/demo/status", 3000);
                JSONObject json = new JSONObject(response);
                mainHandler.post(() -> callback.onSuccess(json));
            } catch (Exception e) {
                Log.w(TAG, "Get demo status failed: " + e.getMessage());
                mainHandler.post(() -> callback.onFailure(e));
            }
        });
    }

    private String executeGet(String urlStr, int timeoutMs) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                return sb.toString();
            } else {
                throw new Exception("HTTP " + responseCode + " from " + urlStr);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    private String executePost(String urlStr, int timeoutMs) throws Exception {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Accept", "application/json");
            conn.setConnectTimeout(timeoutMs);
            conn.setReadTimeout(timeoutMs);
            conn.setDoOutput(true);

            int responseCode = conn.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                return sb.toString();
            } else {
                throw new Exception("HTTP " + responseCode + " from " + urlStr);
            }
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }
}
