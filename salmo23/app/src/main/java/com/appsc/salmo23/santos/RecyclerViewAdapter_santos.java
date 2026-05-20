package com.appsc.salmo23.santos;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.appsc.salmo23.R;
import com.squareup.picasso.Picasso;
import java.util.List;

public class RecyclerViewAdapter_santos extends RecyclerView.Adapter<RecyclerViewAdapter_santos.ViewHolder> {
    private int mImgResource;
    Context context;
    List<GetDataAdapter_santos> getDataAdapter;
    private static String TAG = RecyclerViewAdapter_santos.class.getSimpleName();
    public RecyclerViewAdapter_santos(List<GetDataAdapter_santos> getDataAdapter, Context context){

        super();

        this.getDataAdapter = getDataAdapter;
        this.context = context;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.recyclerview_items_santos, parent, false);

        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public int getItemCount() {
        return getDataAdapter.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        public TextView text_id,text_identificador,text_image,text_cont;
        public CardView text_cardview1;
        public ImageView text_imagefeed;

        public ViewHolder(View itemView) {
            super(itemView);
            text_id            = (TextView) itemView.findViewById(R.id.text_id);
            text_identificador = (TextView) itemView.findViewById(R.id.text_identificador);
            text_image         = (TextView) itemView.findViewById(R.id.text_image);
            text_cont          = (TextView) itemView.findViewById(R.id.text_cont);
            text_imagefeed     = (ImageView) itemView.findViewById(R.id.text_imagefeed);
            text_cardview1     = (CardView) itemView.findViewById(R.id.text_cardview1);

            text_cardview1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    GetDataAdapter_santos item = getDataAdapter.get(position);
                    Intent intent = new Intent(context, Santos.class);
                    intent.putExtra("Image",         item.getimage());
                    intent.putExtra("Identificador", item.getidentificador());
                    context.startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        GetDataAdapter_santos getDataAdapter1 =  getDataAdapter.get(position);
        Picasso.with(context).load(getDataAdapter1.getimage()).into(holder.text_imagefeed);
        holder.text_identificador.setText(getDataAdapter1.getidentificador());
        holder.text_cont.setText(getDataAdapter1.getcont());
    }
}