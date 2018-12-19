package com.tlaskie.groupgift;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

public class AdapterWishlist extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    Context context;
    List<String> details;

    public AdapterWishlist(Context context, List<String> details){
        this.context = context;
        this.details = details;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View row = inflater.inflate(R.layout.custom_row_group, viewGroup, false);
        AdapterWishlist.Item item = new AdapterWishlist.Item(row);
        return item;
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
        int temp = i+1;
        String currentItem = details.get(i);
        String arrayOfDetails[] = currentItem.split("~");
        ((AdapterWishlist.Item)viewHolder).textItemNum.setText("Item Number " + temp);
        ((AdapterWishlist.Item)viewHolder).textName.setText(arrayOfDetails[0]);
        ((AdapterWishlist.Item)viewHolder).textDescription.setText(arrayOfDetails[1]);
        ((AdapterWishlist.Item)viewHolder).textPrice.setText(arrayOfDetails[2]);
        ((AdapterWishlist.Item)viewHolder).textLocaiton.setText(arrayOfDetails[3]);
    }

    @Override
    public int getItemCount() {
        return details.size();
    }

    public class Item extends RecyclerView.ViewHolder{
        TextView textItemNum;
        TextView textName;
        TextView textDescription;
        TextView textPrice;
        TextView textLocaiton;

        public Item(@NonNull View itemView) {
            super(itemView);
            textItemNum = itemView.findViewById(R.id.textItemNum);
            textName = itemView.findViewById(R.id.textWishlistName);
            textDescription = itemView.findViewById(R.id.textWishlistDescription);
            textPrice = itemView.findViewById(R.id.textWishlistPrice);
            textLocaiton = itemView.findViewById(R.id.textWishlistLocation);
        }
    }
}
