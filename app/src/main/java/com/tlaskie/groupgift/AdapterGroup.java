package com.tlaskie.groupgift;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;

public class AdapterGroup extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    Context context;
    List<String> items;

    public AdapterGroup(Context context, List<String> items){
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.customrowgroup, viewGroup, false);
        AdapterGroup.Item item = new AdapterGroup.Item(row);
        return item;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
        ((AdapterGroup.Item)viewHolder).textView.setText(items.get(i));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class Item extends RecyclerView.ViewHolder{
        TextView textView;
        CheckBox checkBox;
        public Item(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.textfriend);
            checkBox = itemView.findViewById(R.id.checkboxadd);
        }
    }
}
