package smartcar.com.getting_started_android_sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.util.Log;

import com.smartcar.sdk.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

import java.lang.Boolean;


public class MainActivity extends AppCompatActivity {

    private static String CLIENT_ID;
    private static String REDIRECT_URI;
    private static String[] SCOPE;
    private Context appContext;
    private SmartcarAuth smartcarAuth;
    private Boolean faceVerify = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appContext = getApplicationContext();
        CLIENT_ID = getString(R.string.client_id);
        REDIRECT_URI = "http://b9de84cb.ngrok.io/exchange";
        SCOPE = new String[]{"read_vehicle_info"};

        smartcarAuth = new SmartcarAuth(
                CLIENT_ID,
                REDIRECT_URI,
                SCOPE,
                false,//TODO was true before
                new SmartcarCallback() {
            @Override
            public void handleResponse(final SmartcarResponse smartcarResponse) {

                final OkHttpClient client = new OkHttpClient();

                // Request can not run on the Main Thread
                // Main Thread is used for UI and therefore can not be blocked
                new Thread(new Runnable() {
                    @Override
                    public void run() {

                        // send request to exchange the auth code for the access token
                        Request exchangeRequest = new Request.Builder()
                            // Android emulator runs in a VM, therefore localhost will be the
                            // emulator's own loopback address
                            .url(getString(R.string.app_server) + "/exchange?code=" + smartcarResponse.getCode())
                            .build();

                        try {
                            client.newCall(exchangeRequest).execute();
                        } catch (IOException e) {}

                        // send request to retrieve the vehicle info
                        Request infoRequest = new Request.Builder()
                            .url(getString(R.string.app_server) + "/vehicle")
                            .build();

                        try {
                            Response response = client.newCall(infoRequest).execute();

                            String jsonBody = response.body().string();
                            JSONObject JObject = new JSONObject(jsonBody);

                            String make = JObject.getString("make");
                            String model = JObject.getString("model");
                            String year = JObject.getString("year");

                            Intent intent = new Intent(appContext, DisplayInfoActivity.class);
                            intent.putExtra("INFO", make + " " + model + " " + year);
                            startActivity(intent);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();


            }
        });

        final Button connectButton = (Button) findViewById(R.id.connect_button);
        smartcarAuth.addClickHandler(appContext, connectButton);
        connectButton.setVisibility(View.GONE);

        // Face verification
        final Button faceVeriButton = (Button) findViewById(R.id.verification_button);
        faceVeriButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //Runs face verification module and returns bool into faceVerify
                faceVerify = true;
                //If face is verified, display connect button and hide face verify button
                if (faceVerify){
                    connectButton.setVisibility(View.VISIBLE);
                    faceVeriButton.setVisibility(View.GONE);
                } else {
                    connectButton.setVisibility(View.GONE);
                    faceVeriButton.setVisibility(View.VISIBLE);
                }
            }
        });




    }
}
