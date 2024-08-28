package de.kaiserdragon.callforwardingstatus.sms;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

import de.kaiserdragon.callforwardingstatus.R;

public class ForwardingConfig {
    final private Context context;

    private static final String KEY_URL = "url";
    private static final String KEY_TEMPLATE = "template";
    private static final String KEY_HEADERS = "headers";
    private static final String KEY_IGNORE_SSL = "ignore_ssl";

    private String sender;
    private String url;
    private String template;
    private String headers;
    private boolean ignoreSsl = false;

    public ForwardingConfig(Context context) {
        this.context = context;
    }

    public String getSender() {
        return this.sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTemplate() {
        return this.template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public String getHeaders() {
        return this.headers;
    }

    public void setHeaders(String headers) {
        this.headers = headers;
    }

    public boolean getIgnoreSsl() {
        return this.ignoreSsl;
    }

    public void setIgnoreSsl(boolean ignoreSsl) {
        this.ignoreSsl = ignoreSsl;
    }

    public void save() {
        try {
            JSONObject json = new JSONObject();
            json.put(KEY_URL, this.url);
            json.put(KEY_TEMPLATE, this.template);
            json.put(KEY_HEADERS, this.headers);
            json.put(KEY_IGNORE_SSL, this.ignoreSsl);

            SharedPreferences.Editor editor = getEditor(context);
            editor.putString(this.sender, json.toString());

            editor.commit();
        } catch (Exception e) {
            Log.e("ForwardingConfig", e.getMessage());
        }
    }

    public static String getDefaultJsonTemplate() {
        return "{\n  \"from\":\"%from%\",\n  \"to\":\"%to%\",\n  \"text\":\"%text%\",\n  \"sentStamp\":%sentStamp%,\n  \"receivedStamp\":%receivedStamp%,\n  \"sim\":\"%sim%\"\n}";
    }

    public static String getDefaultJsonHeaders() {
        return "{\"User-agent\":\"SMS Forwarder App\"}";
    }

    public static ForwardingConfig getConfig(Context context) {
        ForwardingConfig config = new ForwardingConfig(context);
        config.setSender("*");
        config.setUrl("http://192.168.1.155:8080/message/receive");
        config.setIgnoreSsl(true);
        config.setTemplate(ForwardingConfig.getDefaultJsonTemplate());
        config.setHeaders(ForwardingConfig.getDefaultJsonHeaders());
        return config;
    }

    public void remove() {
        SharedPreferences.Editor editor = getEditor(context);
        editor.remove(this.getSender());
        editor.commit();
    }

    private static SharedPreferences getPreference(Context context) {
        return context.getSharedPreferences(
                context.getString(R.string.key_phones_preference),
                Context.MODE_PRIVATE
        );
    }

    private static SharedPreferences.Editor getEditor(Context context) {
        SharedPreferences sharedPref = getPreference(context);
        return sharedPref.edit();
    }
}
