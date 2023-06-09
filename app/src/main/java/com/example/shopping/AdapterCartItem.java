package com.example.shopping;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopping.activities.ShopDetailsActivity;
import com.example.shopping.models.ModelCartItem;

import java.util.ArrayList;

import p32929.androideasysql_library.Column;
import p32929.androideasysql_library.EasyDB;

public class AdapterCartItem extends RecyclerView.Adapter<AdapterCartItem.HolderCartItem>{

    Context context;
    ArrayList<ModelCartItem> cartItems;

    public AdapterCartItem(Context context, ArrayList<ModelCartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public HolderCartItem onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.row_cart, parent,false);
        return new HolderCartItem(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HolderCartItem holder, int position) {
        ModelCartItem modelCartItem = cartItems.get(position);

        String id = modelCartItem.getId();
        String getPid = modelCartItem.getpId();
        String title = modelCartItem.getName();
        String cost = modelCartItem.getCost();
        String price = modelCartItem.getPrice();
        String quantity = modelCartItem.getQuantity();

    holder.itemTitleTv.setText(""+title);
    holder.itemPriceTv.setText(""+cost);
    holder.itemQuantityTv.setText("["+quantity+"]");
    holder.itemPriceEachTv.setText(""+price);

    holder.itemRemoveTv.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            EasyDB easyDB = EasyDB.init(context,"ITEMS_DB")
                    .setTableName("ITEMS_TABLE")
                    .addColumn(new Column("Item_id",new String[]{"text","unique"}))
                    .addColumn(new Column("Item_Pid",new String[]{"text","not null"}))
                    .addColumn(new Column("Item_Name",new String[]{"text","not null"}))
                    .addColumn(new Column("Item_Price_Each",new String[]{"text","not null"}))
                    .addColumn(new Column("Item_Price",new String[]{"text","not null"}))
                    .addColumn(new Column("Item_Quantity",new String[]{"text","not null"}))
                    .doneTableColumn();
            
            easyDB.deleteRow(1,id);
            Toast.makeText(context, "Removed From Cart", Toast.LENGTH_SHORT).show();


            cartItems.remove(position);
            notifyItemChanged(position);
            notifyDataSetChanged();

            double tx = Double.parseDouble((((ShopDetailsActivity)context)
                    .allTotalPriceTv.getText().toString().trim().replace("$","")));
            double totalPrice = tx - Double.parseDouble(cost.replace("$",""));
            double deliveryFee = Double.parseDouble((((ShopDetailsActivity)context)
                    .deliveryFee.replace("$","")));
            double sTotalPrice = Double.parseDouble(String.format("%.2f",totalPrice)) - Double.parseDouble(String.format("%.2f",deliveryFee));

            ((ShopDetailsActivity)context).allTotalPrice =0.00;
            ((ShopDetailsActivity)context).sTotalTv.setText(String.format("$%.2f", sTotalPrice));
            ((ShopDetailsActivity)context).allTotalPriceTv.setText(String.format("$%.2f", Double.parseDouble(String.format("%.2f",totalPrice))));
            ((ShopDetailsActivity)context).cartCount();
        }
    });

    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class HolderCartItem extends RecyclerView.ViewHolder{

        TextView itemTitleTv, itemPriceTv, itemPriceEachTv, itemQuantityTv, itemRemoveTv;

        public HolderCartItem(@NonNull View itemView) {
            super(itemView);

            itemTitleTv= itemView.findViewById(R.id.itemTitleTv);
            itemPriceTv= itemView.findViewById(R.id.itemPriceTv);
            itemPriceEachTv= itemView.findViewById(R.id.itemPriceEachTv);
            itemQuantityTv= itemView.findViewById(R.id.itemQuantityTv);
            itemRemoveTv= itemView.findViewById(R.id.itemRemoveTv);

        }
    }
}
