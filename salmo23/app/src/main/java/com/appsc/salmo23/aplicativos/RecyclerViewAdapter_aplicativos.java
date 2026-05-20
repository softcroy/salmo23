package com.appsc.salmo23.aplicativos;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.appsc.salmo23.R;
import com.bumptech.glide.Glide;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by JUNED on 6/16/2016.
 */
public class RecyclerViewAdapter_aplicativos extends RecyclerView.Adapter<RecyclerViewAdapter_aplicativos.ViewHolder> {
    private int mImgResource;
    Context context;
    List<GetDataAdapter_aplicativos> getDataAdapter;
    private String modificar = "https://softcroy.com/app_santos_v2/api3/config/somar_downloads_aplicativos2.php?id=";

    private static String TAG = RecyclerViewAdapter_aplicativos.class.getSimpleName();
    public RecyclerViewAdapter_aplicativos(List<GetDataAdapter_aplicativos> getDataAdapter, Context context){

        super();

        this.getDataAdapter = getDataAdapter;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_items_aplicativos, parent, false);

        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }



    @Override
    public int getItemCount() {

        return getDataAdapter.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{

        public TextView Text_link;
        public TextView text_link_play;
        public TextView Text_status;
        public TextView text_nome;
        public TextView idddd;
        public ImageView image;
        public CardView abrir;


        public ViewHolder(View itemView) {

            super(itemView);

            Text_link = (TextView) itemView.findViewById(R.id.text_link) ;
            text_link_play = (TextView) itemView.findViewById(R.id.text_nome) ;
            idddd = (TextView) itemView.findViewById(R.id.idddd);
            Text_status = (TextView) itemView.findViewById(R.id.text_status) ;
            text_nome = (TextView) itemView.findViewById(R.id.text_link_play) ;
            image = (ImageView) itemView.findViewById(R.id.imagefeed);
            abrir = (CardView) itemView.findViewById(R.id.cardview1);

            abrir.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(text_link_play.getText().toString()));
                    context.startActivity(i);

                    /////////////////////////////////////////////////////alterar////////////////////////////////////
                    com.android.volley.RequestQueue mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
                    JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                            modificar + idddd.getText().toString(), null, new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {


                        }
                    }, new Response.ErrorListener() {

                        @Override
                        public void onErrorResponse(VolleyError error) {
                            VolleyLog.d(TAG, "Error: " + error.getMessage());
                        }
                    });

                    int socketTimeout = 30000;//30 seconds - change to what you want
                    RetryPolicy policy = new DefaultRetryPolicy(socketTimeout, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                    jsonObjReq.setRetryPolicy(policy);
                    mRequestQueue.add(jsonObjReq);
                    /////////////////////////////////////////////////////alterar////////////////////////////////////
                }
            });


        }

    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

        GetDataAdapter_aplicativos getDataAdapter1 =  getDataAdapter.get(position);

            Glide.with(context).load(getDataAdapter1.getImageUrl()).into(holder.image);

            holder.Text_link.setText(getDataAdapter1.getLink());

            holder.idddd.setText(getDataAdapter1.getId());

            holder.text_link_play.setText(getDataAdapter1.gethashtags());

            holder.Text_status.setText(getDataAdapter1.getmodelo());

            holder.text_nome.setText(getDataAdapter1.getVisualizacao());





    }

}




