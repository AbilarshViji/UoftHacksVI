package com.example.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.Calendar;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

//CAMERA STUFF
//package edu.gvsu.cis.masl.camerademo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity {

    @Override    //MAIN FUNCTION
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//        HttpClient httpclient = HttpClientBuilder.create().build();   ///ADDED
//
//        try
//
//        {
//           // HttpClient httpclient = HttpClientBuilder.create().build();   ///ADDED
//
//            URIBuilder builder = new URIBuilder(uriBase);
//
//             //Request parameters. All of them are optional.
//            builder.setParameter("returnFaceId", "true");
//            builder.setParameter("returnFaceLandmarks", "false");
//            builder.setParameter("returnFaceAttributes", faceAttributes);
//
//            // Prepare the URI for the REST API call.
//           URI uri = builder.build();
//           HttpPost request = new HttpPost(uri);
//
//            // Request headers.
//             request.setHeader("Content-Type", "application/json");
//             request.setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);
//
//           //  Request body.
//            StringEntity reqEntity = new StringEntity(imageWithFaces);
//            request.setEntity(reqEntity);
//
//            // Execute the REST API call and get the response entity.
//            HttpResponse response = httpclient.execute(request);
//            HttpEntity entity = response.getEntity();
//
//            if (entity != null)
//            {
//                // Format and display the JSON response.
//                System.out.println("REST Response:\n");
//
//                String jsonString = EntityUtils.toString(entity).trim();
//                if (jsonString.charAt(0) == '[') {
//                    JSONArray jsonArray = new JSONArray(jsonString);
//                    System.out.println(jsonArray.toString(2));
//                }
//                else if (jsonString.charAt(0) == '{') {
//                    JSONObject jsonObject = new JSONObject(jsonString);
//                    System.out.println(jsonObject.toString(2));
//                } else {
//                    System.out.println(jsonString);
//                }
//            }
//        }
//        catch (Exception e)
//        {
//            // Display error message.
//            System.out.println(e.getMessage());
//        }

    }


    private void makeRequest() {
        OkHttpClient client = new OkHttpClient();

        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host("westcentralus.api.cognitive.microsoft.com")
                .addPathSegments("face/v1.0/detect")
                //Request parameters. All of them are optional.
                //.addQueryParameter("overload", "stream")
                .addQueryParameter("returnFaceId", "true")
                .addQueryParameter("returnFaceLandmarks", "false")
                .addQueryParameter("returnFaceAttributes", faceAttributes)
                .build();

        Log.d("URL", httpUrl.toString());


        // "data:image/png;base64, "
        // RequestBody body = RequestBody.create(MediaType.get("application/json"), imageWithFaces);

        // "data:image/jpg;base64,"+encodeTobase64(photo)
        RequestBody body = RequestBody.create(MediaType.get("application/octet-stream"), encodeToByteArray(photo));

        //RequestBody body = RequestBody.create(MediaType.get("text/plain; charset=ISO-8859-1"), imageWithFaces);

        Request request = new Request.Builder()
                .url(httpUrl)
                .addHeader("Content-Type", "application/json")
                .addHeader("Ocp-Apim-Subscription-Key", subscriptionKey)
                .post(body)
                .build();


        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d("RES", response.body().string());
            }
        });
    }
    static final int REQUEST_IMAGE_CAPTURE = 1;

    public void launchCamera(View view) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }


    private Bitmap photo;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            photo = (Bitmap) extras.get("data");
            ((ImageView) findViewById(R.id.imageView1)).setImageBitmap(photo);

            // Send to Azure
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    makeRequest();
                }
            });
        }
    }

    public static byte[] encodeToByteArray(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }

    public static String encodeTobase64(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return imageEncoded;
    }

    private static final String subscriptionKey = "4f941a3492b442d9991405a98aae402f";

    // NOTE: You must use the same region in your REST call as you used to
// obtain your subscription keys. For example, if you obtained your
// subscription keys from westus, replace "westcentralus" in the URL
// below with "westus".
//
// Free trial subscription keys are generated in the "westus" region. If you
// use a free trial subscription key, you shouldn't need to change this region.
    private static final String uriBase =
            "https://westcentralus.api.cognitive.microsoft.com/face/v1.0/detect";

    private static final String imageWithFaces =
            "{\"url\":\"https://scontent-yyz1-1.xx.fbcdn.net/v/t1.0-1/32130657_2029130694014026_489960609178189824_n.jpg?_nc_cat=107&_nc_ht=scontent-yyz1-1.xx&oh=50820c3f6fbf535115fb77a984ae8d8a&oe=5CBA7123\"}";
    //  "{\"url\":\"https://upload.wikimedia.org/wikipedia/commons/c/c3/RH_Louise_Lillian_Gish.jpg\"}";
    //  "C:\\Users\\idilo\\Pictures\\chenyen.jpg";

    private static final String faceAttributes =
            "age,gender,headPose,smile,facialHair,glasses,emotion,hair,makeup,occlusion,accessories,blur,exposure,noise";
    //ADDED

}


