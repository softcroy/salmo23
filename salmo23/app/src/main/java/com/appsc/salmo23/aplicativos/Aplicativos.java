package com.appsc.salmo23.aplicativos;


import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class Aplicativos extends Fragment {

    SwipeRefreshLayout swipeRefreshLayout;

    List<GetDataAdapter_aplicativos> GetDataAdapter1;

    RecyclerView recyclerView;

    RecyclerView.LayoutManager recyclerViewlayoutManager;

    RecyclerView.Adapter recyclerViewadapter;


    String GET_JSON_DATA_HTTP_URL = "https://softcroy.com/app_santos_v2/api3/api_aplicativos2.php";
    String JSON_ID = "id";
    String JSON_link_image = "link_image";
    String JSON_link_play = "link_play";
    String JSON_tipo = "tipo";
    String JSON_nome = "nome";


    JsonArrayRequest jsonArrayRequest ;

    RequestQueue requestQueue ;
    //////////////////////////push/////////////////////////////////



    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        View myFragmentView = inflater.inflate(R.layout.status, container, false);

        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle("\uD83C\uDF43❤Baixe Mais Aplicativos❤\uD83C\uDF43");
        //getSupportActionBar().setTitle(Html.fromHtml("<small>\uD83D\uDD4A\uD83C\uDF33Baixe Mais Aplicativos\uD83C\uDF33\uD83D\uDD4A</small>"));



        swipeRefreshLayout = (SwipeRefreshLayout) myFragmentView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(R.color.white);
        swipeRefreshLayout.setProgressBackgroundColor(R.color.colorPrimary);

        GetDataAdapter1 = new ArrayList<>();

        recyclerView = (RecyclerView)myFragmentView.findViewById(R.id.recyclerView1);



        recyclerView.setHasFixedSize(true);

        recyclerViewlayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(recyclerViewlayoutManager);


        JSON_DATA_WEB_CALL();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                recyclerView.getRecycledViewPool().clear();
                recyclerViewadapter.notifyDataSetChanged();
                GetDataAdapter1.clear();
                JSON_DATA_WEB_CALL();
            }
        });
        swipeRefreshLayout.setColorSchemeResources(R.color.white);
        swipeRefreshLayout.setProgressBackgroundColor(R.color.colorPrimary);

return  myFragmentView;
    }
    public void JSON_DATA_WEB_CALL(){
        swipeRefreshLayout.setRefreshing(true);
        GetDataAdapter1.clear();
        jsonArrayRequest = new JsonArrayRequest(GET_JSON_DATA_HTTP_URL,

                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {


                        JSON_PARSE_DATA_AFTER_WEBCALL(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                    }
                });

        requestQueue = Volley.newRequestQueue(getActivity());

        requestQueue.add(jsonArrayRequest);
    }

    public void JSON_PARSE_DATA_AFTER_WEBCALL(JSONArray array){
        for(int i = 0; i<array.length(); i++) {

            GetDataAdapter_aplicativos GetDataAdapter2 = new GetDataAdapter_aplicativos();

            JSONObject json = null;
            try {
                json = array.getJSONObject(i);

                GetDataAdapter2.setId(json.getString(JSON_ID));
                GetDataAdapter2.setLink(json.getString(JSON_link_image));
                GetDataAdapter2.setImageUrl(json.getString(JSON_link_image));
                GetDataAdapter2.sethashtags(json.getString(JSON_link_play));
                GetDataAdapter2.setmodelo(json.getString(JSON_tipo));
                GetDataAdapter2.setVisualizacao(json.getString(JSON_nome));
                swipeRefreshLayout.setRefreshing(false);
            } catch (JSONException e) {

                e.printStackTrace();
                swipeRefreshLayout.setRefreshing(false);
            }
            try {
                if(json.getString("tipo").equals("santos")) {
                    GetDataAdapter1.add(GetDataAdapter2);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                swipeRefreshLayout.setRefreshing(false);
            }
        }
        // Collection.reverse(array);


        recyclerViewadapter = new RecyclerViewAdapter_aplicativos(GetDataAdapter1, getActivity());

        recyclerView.setAdapter(recyclerViewadapter);


    }

}