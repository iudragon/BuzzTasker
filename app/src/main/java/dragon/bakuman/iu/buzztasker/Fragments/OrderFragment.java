package dragon.bakuman.iu.buzztasker.Fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
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

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import dragon.bakuman.iu.buzztasker.Adapters.TrayAdapter;
import dragon.bakuman.iu.buzztasker.Objects.Tray;
import dragon.bakuman.iu.buzztasker.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class OrderFragment extends Fragment {


    private ArrayList<Tray> trayList;
    private TrayAdapter adapter;
    private Button buttonStatus;

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

        buttonStatus = getActivity().findViewById(R.id.status);

        getLatestOrder();
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

                        buttonStatus.setText(status);

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

}
