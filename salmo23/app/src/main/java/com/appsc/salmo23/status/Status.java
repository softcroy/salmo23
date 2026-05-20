package com.appsc.salmo23.status;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class Status extends Fragment {

    SwipeRefreshLayout swipeRefreshLayout;
    List<GetDataAdapter_status> GetDataAdapter1;
    RecyclerView recyclerView;
    RecyclerView.Adapter recyclerViewadapter;
    RequestQueue requestQueue;

    String STATUS;


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View myFragmentView = inflater.inflate(R.layout.status, container, false);

        swipeRefreshLayout = myFragmentView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.white);
        swipeRefreshLayout.setProgressBackgroundColor(R.color.colorPrimary);

        GetDataAdapter1 = new ArrayList<>();
        recyclerView = myFragmentView.findViewById(R.id.recyclerView1);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));

        if (isEnglish()) {
            STATUS = "https://softcroy.com/app_santos_v2/api3/status/api_status_v2_en.php";
        } else {
            STATUS = "https://softcroy.com/app_santos_v2/api3/status/api_status_v2_br.php";
        }
        JSON_DATA_WEB_CALL();

        swipeRefreshLayout.setOnRefreshListener(() -> {
            GetDataAdapter1.clear();
            JSON_DATA_WEB_CALL();
        });

        return myFragmentView;
    }
    private boolean isEnglish() {
        Locale locale = getResources().getConfiguration().locale;
        return locale.getLanguage().equals(Locale.ENGLISH.getLanguage());
    }
    public void JSON_DATA_WEB_CALL() {
        swipeRefreshLayout.setRefreshing(true);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(STATUS, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {

                            populateGroups(response);
                            populateImages(response.getJSONArray("Todos"));

                        } catch (JSONException e) {
                            e.printStackTrace();
                        } finally {
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

        requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(jsonObjectRequest);
    }

    private void populateGroups(JSONObject jsonResponse) throws JSONException {
        View view = getView();
        if (view != null) {
            LinearLayout groupContainer = view.findViewById(R.id.group_container);
            groupContainer.removeAllViews();

            int[] backgrounds = {
                    R.drawable.bg_background_green,
                    R.drawable.bg_background_orange,
                    R.drawable.bg_background_pink,
                    R.drawable.bg_background_purple,
                    R.drawable.bg_background_red,
                    R.drawable.bg_background_yellow,
                    R.drawable.group_button_background
            };
            int backgroundIndex = 0;

            Iterator<String> keys = jsonResponse.keys();
            while (keys.hasNext()) {
                String groupName = keys.next();

                if (!groupName.isEmpty()) {
                    Button groupButton = new Button(getActivity());
                    groupButton.setText(groupName);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            (int) TypedValue.applyDimension(
                                    TypedValue.COMPLEX_UNIT_SP, 35, getResources().getDisplayMetrics())
                    );
                    params.setMargins(5, 5, 5, 5);
                    groupButton.setLayoutParams(params);

                    int padding = (int) TypedValue.applyDimension(
                            TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());
                    groupButton.setPadding(padding, padding, padding, padding);

                    groupButton.setBackgroundResource(backgrounds[backgroundIndex]);
                    groupButton.setTextColor(getResources().getColor(R.color.white));
                    groupButton.setTextSize(12);

                    groupButton.setOnClickListener(v -> {
                        try {
                            JSONArray groupImages = jsonResponse.getJSONArray(groupName);
                            populateImages(groupImages);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    });

                    groupContainer.addView(groupButton);
                    backgroundIndex = (backgroundIndex + 1) % backgrounds.length;
                }
            }
        }
    }

    private void populateImages(JSONArray imagesArray) {
        GetDataAdapter1.clear();
        for (int i = 0; i < imagesArray.length(); i++) {
            try {
                JSONObject imageObject = imagesArray.getJSONObject(i);
                GetDataAdapter_status item = new GetDataAdapter_status();
                item.setId(imageObject.getInt("id"));
                item.setimage(imageObject.getString("image"));
                item.setVisualizacao(imageObject.getInt("visualizacao"));
                GetDataAdapter1.add(item);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        recyclerViewadapter = new RecyclerViewAdapter_status(GetDataAdapter1, getActivity());
        recyclerView.setAdapter(recyclerViewadapter);
    }
}
