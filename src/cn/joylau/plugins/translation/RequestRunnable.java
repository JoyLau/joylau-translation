package cn.joylau.plugins.translation;

import com.google.gson.Gson;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.JBColor;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Created by sky on 16/5/18.
 */
public class RequestRunnable implements Runnable {
    private static final String HOST = "openapi.youdao.com";
    private static final String PATH = "/api";
    private static final String APP_KEY = "01491290b069140b";
    private static final String APP_SECRET = "Tvh5WadpVrUbdyBReAGjszu9X961yU8G";
    private Editor mEditor;
    private String mQuery;

    public RequestRunnable(Editor editor, String query) {
        this.mEditor = editor;
        this.mQuery = query;
    }

    public void run() {
        try {
            URI uri = createTranslationURI(mQuery);
            RequestConfig requestConfig = RequestConfig.custom()
                    .setSocketTimeout(5000)
                    .setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000)
                    .build();
            HttpGet httpGet = new HttpGet(uri);
            System.out.println(uri.toString());
            httpGet.setConfig(requestConfig);
            httpGet.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8");
            HttpClient client = HttpClients.createDefault();
            HttpResponse response = client.execute(httpGet);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status < 300) {
                HttpEntity resEntity = response.getEntity();
                String json = EntityUtils.toString(resEntity, "UTF-8");
                System.out.println(json);
                Gson gson = new Gson();
                Translation translation = gson.fromJson(json, Translation.class);
                //show result
                showPopupBalloon(translation.toString());
                Logger.info(translation.toString());
            } else {
                showPopupBalloon(response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException e) {
            showPopupBalloon(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showPopupBalloon(final String result) {
        ApplicationManager.getApplication().invokeLater(() -> {
            JBPopupFactory factory = JBPopupFactory.getInstance();
            factory.createHtmlTextBalloonBuilder(result, null, new JBColor(new Color(186, 238, 186), new Color(73, 117, 73)), null)
                    .setFadeoutTime(5000)
                    .createBalloon()
                    .show(factory.guessBestPopupLocation(mEditor) , Balloon.Position.below);
        });
    }


    private URI createTranslationURI(String query) throws URISyntaxException, NoSuchAlgorithmException {
        String salt = UUID.randomUUID().toString();
        String curtime = String.valueOf(System.currentTimeMillis() / 1000);
        String sign = AuthV3Util.calculateSign(APP_KEY, APP_SECRET, query, salt, curtime);

        URIBuilder builder = new URIBuilder();
        builder.setScheme("https")
                .setHost(HOST)
                .setPath(PATH)
                .addParameter("q", query)
                .addParameter("from", "auto")
                .addParameter("to", "auto")
                .addParameter("appKey", APP_KEY)
                .addParameter("salt", salt)
                .addParameter("curtime", curtime)
                .addParameter("signType", "v3")
                .addParameter("sign", sign);
        return builder.build();
    }
}
