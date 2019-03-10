package dragon.bakuman.iu.buzztasker.Fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.ahmadrosid.lib.drawroutemap.DrawMarker;
import com.ahmadrosid.lib.drawroutemap.DrawRouteMaps;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import dragon.bakuman.iu.buzztasker.Adapters.TrayAdapter;
import dragon.bakuman.iu.buzztasker.Objects.Tray;
import dragon.bakuman.iu.buzztasker.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class OrderFragment extends Fragment implements OnMapReadyCallback {


    private ArrayList<Tray> trayList;
    private TrayAdapter adapter;
    private Button statusView;

    private GoogleMap mMap;

    public OrderFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_order, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        trayList = new ArrayList<Tray>();

        adapter = new TrayAdapter(this.getActivity(), trayList);


        ListView listView = getActivity().findViewById(R.id.tray_list);

        listView.setAdapter(adapter);

        statusView = getActivity().findViewById(R.id.status);

        getLatestOrder();


        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.order_map);
        mapFragment.getMapAsync(this);
    }

    private void getLatestOrder() {

        SharedPreferences sharedPref = getActivity().getSharedPreferences("MY_KEY", Context.MODE_PRIVATE);

        String url = getString(R.string.API_URL) + "/customer/order/latest/?access_token=" + sharedPref.getString("token", "");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("LATEST ORDER", response.toString());

                        JSONArray orderDetailsArray = null;
                        String status = "";

                        try {

                            orderDetailsArray = response.getJSONObject("order").getJSONArray("order_details");

                            status = response.getJSONObject("order").getString("status");

                        } catch (JSONException e) {

                            e.printStackTrace();
                        }


                        if (orderDetailsArray == null || orderDetailsArray.length() == 0) {


                            TextView alertText = new TextView(getActivity());
                            alertText.setText("Your have no order");
                            alertText.setTextSize(17);
                            alertText.setGravity(Gravity.CENTER);
                            alertText.setLayoutParams(
                                    new TableLayout.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT,
                                            1));

                            LinearLayout linearLayout = getActivity().findViewById(R.id.order_layout);
                            linearLayout.removeAllViews();
                            linearLayout.addView(alertText);


                        }

                        for (int i = 0; i < orderDetailsArray.length(); i++) {

                            Tray tray = new Tray();
                            try {

                                JSONObject orderDetail = orderDetailsArray.getJSONObject(i);
                                tray.setMealName(orderDetail.getJSONObject("meal").getString("name"));
                                tray.setMealPrice(orderDetail.getJSONObject("meal").getInt("price"));
                                tray.setMealQuantity(orderDetail.getInt("quantity"));

                            } catch (JSONException e) {

                                e.printStackTrace();
                            }

                            trayList.add(tray);
                        }

                        adapter.notifyDataSetChanged();

                        statusView.setText(status);

                        drawRouteOnMap(response);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                }
        );
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        queue.add(jsonObjectRequest);


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

    }


    private void drawRouteOnMap(JSONObject response) {

        try {

            String restaurantAddress = response.getJSONObject("order").getJSONObject("restaurant").getString("address");
            String orderAddress = response.getJSONObject("order").getString("address");

            Geocoder coder  = new Geocoder(getActivity());
            ArrayList<Address>  resAddresses = (ArrayList<Address>) coder.getFromLocationName(restaurantAddress, 1);
            ArrayList<Address>  ordAddresses = (ArrayList<Address>) coder.getFromLocationName(orderAddress, 1);

            if (!resAddresses.isEmpty() && !ordAddresses.isEmpty() ){

                LatLng restaurantPos = new LatLng(resAddresses.get(0).getLatitude(), resAddresses.get(0).getLongitude());
                LatLng orderPos = new LatLng(ordAddresses.get(0).getLatitude(), ordAddresses.get(0).getLongitude());

                DrawRouteMaps.getInstance(getActivity())
                        .draw(restaurantPos, orderPos, mMap);
                DrawMarker.getInstance(getActivity()).draw(mMap, restaurantPos, R.drawable.pin_restaurant, "Restaurant Location");
                DrawMarker.getInstance(getActivity()).draw(mMap, orderPos, R.drawable.pin_customer, "Customer Location");

                LatLngBounds bounds = new LatLngBounds.Builder()
                        .include(restaurantPos)
                        .include(orderPos).build();
                Point displaySize = new Point();
               getActivity().getWindowManager().getDefaultDisplay().getSize(displaySize);
                mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, displaySize.x, 250, 30));


            }


        } catch (JSONException | IOException e) {

            e.printStackTrace();
        }

    }
}
