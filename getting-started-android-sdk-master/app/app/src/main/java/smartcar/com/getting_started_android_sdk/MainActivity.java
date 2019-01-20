package smartcar.com.getting_started_android_sdk;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

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
    final OkHttpClient client = new OkHttpClient().newBuilder().followRedirects(true).followSslRedirects(true).build();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        appContext = getApplicationContext();
        final TextView authText = (TextView) findViewById(R.id.testText);

        CLIENT_ID = getString(R.string.client_id);
        REDIRECT_URI = "sc" + getString(R.string.client_id) + "://exchange";
        SCOPE = new String[]{"read_vehicle_info", "read_location", "control_security", "control_security:unlock", "control_security:lock", "read_odometer"};

        smartcarAuth = new SmartcarAuth(
                CLIENT_ID,
                REDIRECT_URI,
                SCOPE,
                false,//TODO was true before
                new SmartcarCallback() {
                    @Override
                    public void handleResponse(final SmartcarResponse smartcarResponse) {
                        Log.d("SmartCode", smartcarResponse.getCode() + " -- " + smartcarResponse.getState());
                        // send request to exchange the auth code for the access token
                        Request exchangeRequest = new Request.Builder()
                                // Android emulator runs in a VM, therefore localhost will be the
                                // emulator's own loopback address
                                .url(getString(R.string.app_server) + "/exchange?code=" + smartcarResponse.getCode())
                                .build();

                        client.newCall(exchangeRequest).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                e.printStackTrace();
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                Log.d("RES", "Code:" + response.code());
                                Log.d("RES", response.body().string());
                                if (response.code() == 500) { //TODO should be 200 when live
                                    unlock();
                                } else {
                                    Log.e("Unlock", "Unable to unlock, bad status code");

                                }
                            }
                        });
                        authText.setText(smartcarResponse.getCode());
                    }
                });

        final Button connectButton = (Button) findViewById(R.id.connect_button);
        smartcarAuth.addClickHandler(appContext, connectButton, true);
    }

    private void unlock() {
        //send request to retrieve the vehicle info
        Request infoRequest = new Request.Builder()
                .url(getString(R.string.app_server) + "/vehicle")
                .build();

        client.newCall(infoRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                int jsonBody = response.code();

                if (jsonBody == 200) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(), "Vehicle Unlocked", Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }
}
