package com.video.evolution.application.updater;



import org.json.JSONObject;

public interface UpdateListener {
    void onJsonDataReceived(UpdateModel updateModel, JSONObject jsonObject);
    void onError(String error);
}
