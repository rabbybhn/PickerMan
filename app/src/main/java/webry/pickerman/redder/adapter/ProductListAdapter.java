package webry.pickerman.redder.adapter;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import webry.pickerman.redder.R;
import webry.pickerman.redder.model.Item;
import webry.pickerman.redder.util.Helper;

public class ProductListAdapter extends RecyclerView.Adapter<ProductListAdapter.MyViewHolder> {

    private Context mContext;
    private List<Item> itemList;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {

        void onItemClick(View view, Item item, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {

        this.mOnItemClickListener = mItemClickListener;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView title, price, mAlert, mLocation;
        public ImageView thumbnail;
        public MaterialRippleLayout mParent;
        public LinearLayout mAlertContainer, mLocationContainer;

        public ProgressBar mProgressBar;

        public MyViewHolder(View view) {

            super(view);

            mParent = (MaterialRippleLayout) view.findViewById(R.id.parent);

            mAlertContainer = (LinearLayout) view.findViewById(R.id.alert_container);
            mAlert = (TextView) view.findViewById(R.id.alert);

            mLocationContainer = (LinearLayout) view.findViewById(R.id.location_container);
            mLocation = (TextView) view.findViewById(R.id.location);

            title = (TextView) view.findViewById(R.id.title);
            price = (TextView) view.findViewById(R.id.price);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);

            mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        }
    }


    public ProductListAdapter(Context mContext, List<Item> itemList) {

        this.mContext = mContext;
        this.itemList = itemList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_product, parent, false);


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        Item item = itemList.get(position);

        holder.mAlertContainer.setVisibility(View.GONE);

        if (item.getInactiveAt() != 0) {

            holder.mAlertContainer.setVisibility(View.VISIBLE);
            holder.mAlert.setText(R.string.label_item_inactive);
        }

        holder.mLocationContainer.setVisibility(View.GONE);

        if (item.getLocation().length() != 0) {

            holder.mLocationContainer.setVisibility(View.VISIBLE);
            holder.mLocation.setText(item.getLocation());
        }

        holder.title.setText(item.getTitle());

        Helper helper = new Helper();

        holder.price.setText(helper.getCurrency(mContext, item.getCurrency(), item.getPrice()));

        holder.thumbnail.setVisibility(View.VISIBLE);
        holder.mProgressBar.setVisibility(View.GONE);

        holder.mProgressBar.setVisibility(View.VISIBLE);

        final ImageView img = holder.thumbnail;
        final ProgressBar progressBar = holder.mProgressBar;

        String url = "https://snb.today/items/t_3968_949_2800b1de5fa2b6a17d9e2eee8ec23c47393e6726.jpg";

        Glide.with(mContext)
                .load(item.getPreviewImgUrl())
                .thumbnail(0.5f)
                .listener(new RequestListener<Drawable>() {

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {

                        progressBar.setVisibility(View.GONE);
                        img.setVisibility(View.VISIBLE);

                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {

                        progressBar.setVisibility(View.GONE);
                        img.setVisibility(View.VISIBLE);

                        return false;
                    }

                })
                .into(holder.thumbnail);

        holder.mParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mOnItemClickListener != null) {

                    mOnItemClickListener.onItemClick(view, itemList.get(position), position);
                }
            }
        });

        // loading album cover using Glide library
//        Glide.with(mContext).load(item.getImgUrl()).into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {

        return itemList.size();
    }
}