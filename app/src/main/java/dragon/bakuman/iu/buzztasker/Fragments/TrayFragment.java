package dragon.bakuman.iu.buzztasker.Fragments;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import dragon.bakuman.iu.buzztasker.Activities.PaymentActivity;
import dragon.bakuman.iu.buzztasker.Adapters.TrayAdapter;
import dragon.bakuman.iu.buzztasker.AppDatabase;
import dragon.bakuman.iu.buzztasker.Objects.Tray;
import dragon.bakuman.iu.buzztasker.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class TrayFragment extends Fragment {

    private ArrayList<Tray> trayList;
    private TrayAdapter adapter;

    private AppDatabase db;

    public TrayFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_tray, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        db = AppDatabase.getAppDatabase(getContext());
        listTray();


        trayList = new ArrayList<Tray>();
        adapter = new TrayAdapter(this.getActivity(), trayList);


        ListView listView = getActivity().findViewById(R.id.tray_list);
        listView.setAdapter(adapter);


        Button buttonAddPayment = getActivity().findViewById(R.id.button_add_payment);
        buttonAddPayment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), PaymentActivity.class);
                startActivity(intent);
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void listTray() {

        new AsyncTask<Void, Void, List<Tray>>() {

            @Override
            protected List<Tray> doInBackground(Void... voids) {
                return db.trayDao().getAll();
            }

            @Override
            protected void onPostExecute(List<Tray> trays) {
                super.onPostExecute(trays);

                if (!trays.isEmpty()) {

                    trayList.clear();
                    trayList.addAll(trays);
                    adapter.notifyDataSetChanged();

                    float total = 0;
                    for (Tray tray : trays) {
                        total += tray.getMealQuantity() * tray.getMealPrice();

                    }

                    TextView totalView = getActivity().findViewById(R.id.tray_total);
                    totalView.setText("Rs." + total);

                } else {

                    TextView alertText = new TextView(getActivity());
                    alertText.setText("Your tray is empty. Please order a meal");
                    alertText.setTextSize(17);
                    alertText.setGravity(Gravity.CENTER);
                    alertText.setLayoutParams(
                            new TableLayout.LayoutParams(
                                    ActionBar.LayoutParams.WRAP_CONTENT,
                                    ActionBar.LayoutParams.WRAP_CONTENT));

                    LinearLayout linearLayout = getActivity().findViewById(R.id.tray_layout);
                    linearLayout.removeAllViews();
                    linearLayout.addView(alertText);
                }
            }
        }.execute();

    }
}
