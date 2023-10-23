package webry.pickerman.redder.adapter;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.List;

import webry.pickerman.redder.R;
import webry.pickerman.redder.model.Item;
import webry.pickerman.redder.util.Helper;

public class StreamListAdapter extends RecyclerView.Adapter<StreamListAdapter.MyViewHolder> {

    private Context mContext;
    private List<Item> itemList;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public TextView title, price;
        public ImageView thumbnail;

        public ProgressBar mProgressBar;

        public MyViewHolder(View view) {

            super(view);

            title = (TextView) view.findViewById(R.id.title);
            price = (TextView) view.findViewById(R.id.price);
            thumbnail = (ImageView) view.findViewById(R.id.thumbnail);

            mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar);
        }
    }


    public StreamListAdapter(Context mContext, List<Item> itemList) {

        this.mContext = mContext;
        this.itemList = itemList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_list_row, parent, false);


        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {

        Item item = itemList.get(position);

        holder.title.setText(item.getTitle());

        Helper helper = new Helper();

        holder.price.setText(helper.getCurrency(mContext, item.getCurrency(), item.getPrice()));

        holder.mProgressBar.setVisibility(View.VISIBLE);
        holder.thumbnail.setVisibility(View.VISIBLE);

        final ImageView img = holder.thumbnail;
        final ProgressBar progressBar = holder.mProgressBar;

        Glide.with(mContext)
                .load(item.getImgUrl())
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

        // loading album cover using Glide library
//        Glide.with(mContext).load(item.getImgUrl()).into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {

        return itemList.size();
    }
}