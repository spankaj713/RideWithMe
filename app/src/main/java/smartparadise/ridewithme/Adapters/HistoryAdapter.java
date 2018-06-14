package smartparadise.ridewithme.Adapters;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;
import java.util.zip.Inflater;

import smartparadise.ridewithme.Activities.HistorySinglePageActivity;
import smartparadise.ridewithme.DataModels.HistoryModels;
import smartparadise.ridewithme.R;

/**
 * Created by HP on 12-05-2018.
 */

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.MyViewHolder> {

    List<HistoryModels> itemList;
    Context context;

    public HistoryAdapter(List<HistoryModels> itemList,Context context){
        this.itemList=itemList;
        this.context=context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view= LayoutInflater.from(parent.getContext())
                .inflate(R.layout.history_recycler_item,parent,false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.rideIdItem.setText(itemList.get(position).getRideId());
        holder.timeView.setText(itemList.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView rideIdItem,timeView;
        CardView historyCard;
        public MyViewHolder(View itemView) {
            super(itemView);
            timeView=(TextView)itemView.findViewById(R.id.timeView);
            rideIdItem=(TextView)itemView.findViewById(R.id.rideIdView);
            historyCard=(CardView)itemView.findViewById(R.id.historyCard);

            historyCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(view.getContext(), HistorySinglePageActivity.class);
                    Bundle b=new Bundle();
                    b.putString("rideId",rideIdItem.getText().toString());
                    intent.putExtras(b);
                    view.getContext().startActivity(intent);
                }
            });
        }
    }
}
