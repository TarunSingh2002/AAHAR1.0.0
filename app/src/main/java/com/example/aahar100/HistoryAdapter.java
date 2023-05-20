package com.example.aahar100;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
public class HistoryAdapter extends FirebaseRecyclerAdapter<ReadWriteUserHistory,HistoryAdapter.myViewHolder> {


    public HistoryAdapter(@NonNull FirebaseRecyclerOptions<ReadWriteUserHistory> options) {
        super(options);
    }
    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull ReadWriteUserHistory model) {
       holder.food.setText(model.getFood());
       holder.description.setText(model.getDescription());
    }
    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_item,parent,false);
        return new myViewHolder(view);
    }
    class myViewHolder extends  RecyclerView.ViewHolder{
        TextView food , description;
        public myViewHolder(@NonNull View itemView)
        {
            super(itemView);
            food=(TextView)itemView.findViewById(R.id.rcfood);
            description=(TextView)itemView.findViewById(R.id.rcDescription);
        }
   }
}
