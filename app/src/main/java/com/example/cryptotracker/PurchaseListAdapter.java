package com.example.cryptotracker;

        import android.view.LayoutInflater;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.ImageView;
        import android.widget.TextView;

        import androidx.recyclerview.widget.RecyclerView;

        import com.example.cryptotracker.model.PurchaseCoinModel;
        import com.squareup.picasso.Picasso;

        import java.util.List;

public class PurchaseListAdapter extends RecyclerView.Adapter<PurchaseListAdapter.ViewHolder> {

    private List<PurchaseCoinModel> mData;

    PurchaseListAdapter(List<PurchaseCoinModel> data) {
        this.mData = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_purchase_list_item, parent, false);
        return new PurchaseListAdapter.ViewHolder(itemLayoutView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        PurchaseCoinModel coin = mData.get(position);
        holder.coinNameTextView.setText(coin.coinName);
        holder.quantityTextView.setText("Quantity: " + coin.quantity);
        holder.dateTimeTextView.setText("Purchased Date: " + coin.dateTime);
        holder.priceUsdTextView.setText("Purchased Price: " + coin.priceUsd);
        holder.purchasedPriceTextView.setText("Current Price: " + coin.purchasedPrice);
        Picasso.get().load(coin.image).into(holder.purchasedCoinImageView);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView coinNameTextView;
        private TextView quantityTextView;
        private TextView priceUsdTextView;
        private TextView purchasedPriceTextView;
        private TextView dateTimeTextView;
        private ImageView purchasedCoinImageView;

        ViewHolder(View itemView) {
            super(itemView);
            coinNameTextView = itemView.findViewById(R.id.purchaseCoinNameTextView);
            quantityTextView = itemView.findViewById(R.id.purchaseQuantityTextView);
            priceUsdTextView = itemView.findViewById(R.id.purchasePriceUsdTextView);
            dateTimeTextView = itemView.findViewById(R.id.purchaseDateTimeTextView);
            purchasedPriceTextView = itemView.findViewById(R.id.purchasedPriceTextView);
            purchasedCoinImageView = itemView.findViewById(R.id.purchaseCoinImageView);
        }
    }
}