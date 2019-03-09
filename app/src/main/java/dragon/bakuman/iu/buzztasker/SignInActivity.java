package dragon.bakuman.iu.buzztasker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "SignInActivity";

    private Button customerButton, driverButton;
    private CallbackManager callbackManager;

    private SharedPreferences sharedPref;

    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        customerButton = findViewById(R.id.button_customer);
        driverButton = findViewById(R.id.button_driver);

        customerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customerButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                customerButton.setTextColor(getResources().getColor(android.R.color.white));

                driverButton.setBackgroundColor(getResources().getColor(android.R.color.white));
                driverButton.setTextColor(getResources().getColor(R.color.colorAccent));
            }
        });

        driverButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                driverButton.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                driverButton.setTextColor(getResources().getColor(android.R.color.white));

                customerButton.setBackgroundColor(getResources().getColor(android.R.color.white));
                customerButton.setTextColor(getResources().getColor(R.color.colorAccent));
            }
        });

        buttonLogin = findViewById(R.id.button_login_fb);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (AccessToken.getCurrentAccessToken() == null){
                    LoginManager.getInstance().logInWithReadPermissions(SignInActivity.this, Arrays.asList("public_profile", "email"));

                } else {
                    ColorDrawable customerbuttonColor = (ColorDrawable) customerButton.getBackground();
                    if (customerbuttonColor.getColor() == getResources().getColor(R.color.colorAccent)) {

                        loginToServer(AccessToken.getCurrentAccessToken().getToken(), "customer");


                    } else {

                        loginToServer(AccessToken.getCurrentAccessToken().getToken(), "driver");

                    }
                }
            }
        });


        callbackManager = CallbackManager.Factory.create();
        sharedPref = getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);


        final Button buttonLogout = findViewById(R.id.button_logout);

        buttonLogout.setVisibility(View.GONE);


        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        // App code
                        Log.d("FACEBOOK TOKEN", loginResult.getAccessToken().getToken());
                        GraphRequest request = GraphRequest.newMeRequest(
                                loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(
                                            JSONObject object,
                                            GraphResponse response) {
                                        // Application code
                                        Log.d("FACEBOOK DETAILS", object.toString());


                                        SharedPreferences.Editor editor = sharedPref.edit();

                                        try {
                                            editor.putString("name", object.getString("name"));
                                            editor.putString("email", object.getString("email"));
                                            editor.putString("avatar", object.getJSONObject("picture").getJSONObject("data").getString("url"));
                                        } catch (JSONException e){
                                            e.printStackTrace();

                                        }

                                        editor.commit();
                                    }
                                });
                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,picture");
                        request.setParameters(parameters);
                        request.executeAsync();


                        ColorDrawable customerbuttonColor = (ColorDrawable) customerButton.getBackground();
                        if (customerbuttonColor.getColor() == getResources().getColor(R.color.colorAccent)) {

                            loginToServer(AccessToken.getCurrentAccessToken().getToken(), "customer");


                        } else {

                            loginToServer(AccessToken.getCurrentAccessToken().getToken(), "driver");

                        }

                    }

                    @Override
                    public void onCancel() {
                        // App code
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        // App code
                    }
                });

        if (AccessToken.getCurrentAccessToken() != null){

            Log.d("USER", sharedPref.getAll().toString());
            buttonLogin.setText("Continue as " + sharedPref.getString("email", ""));
            buttonLogout.setVisibility(View.VISIBLE);

        }


        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logOut();
                buttonLogin.setText("Login with Facebook");
                buttonLogout.setVisibility(View.GONE);

            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void loginToServer(String facebookAccessToken, final String userType){

        buttonLogin.setText("LOADING...");
        buttonLogin.setClickable(false);
        buttonLogin.setBackgroundColor(getResources().getColor(R.color.colorLightGray));

        String url = "http://192.168.43.25:8000/api/social/convert-token";

        JSONObject jsonBody = new JSONObject();
        try{


            jsonBody.put("grant_type", "convert_token");
            jsonBody.put("client_id", "I1NXWZxhOxk7eIt33bynLEATh2xbvab6ur8mwiBW");
            jsonBody.put("client_secret", "Li2azL9b4pBTdjCi1utdVK7W7AfgGAkCrfmLNAZtUMJr9pFMTfAb9H8MemsWhgADwPTxtX1BprzN1fPn3KAoxFSxg2AcPZJSXl6EI9NFQnChiHG41g5EIFOUv4JDftdH");
            jsonBody.put("backend", "facebook");
            jsonBody.put("token", facebookAccessToken);
            jsonBody.put("user_type", userType);



        } catch (JSONException e){

            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, url, jsonBody, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {


                        Log.d("LOGIN TO SERVER", response.toString());

                        SharedPreferences.Editor editor = sharedPref.edit();

                        try{

                            editor.putString("token", response.getString("access_token"));


                        } catch (JSONException e){

                            e.printStackTrace();

                        }

                        editor.commit();

                        if (userType.equals("customer")){


                            Intent intent = new Intent(getApplicationContext(), CustomerMainActivity.class);
                            startActivity(intent);


                        } else {


                            Intent intent = new Intent(getApplicationContext(), DriverMainActivity.class);
                            startActivity(intent);

                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error

                    }
                });

        RequestQueue queue = Volley.newRequestQueue(this);
        queue.add(jsonObjectRequest);

    }
}
