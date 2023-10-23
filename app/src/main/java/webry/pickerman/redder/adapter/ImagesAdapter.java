package webry.pickerman.redder.adapter;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.balysv.materialripple.MaterialRippleLayout;
import com.bumptech.glide.Glide;

import java.util.List;

import webry.pickerman.redder.R;
import webry.pickerman.redder.model.ImageItem;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ImagesAdapter extends PagerAdapter {

    private final Activity act;
    private List<ImageItem> items;

    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {

        void onItemClick(View view, ImageItem obj, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {

        this.onItemClickListener = onItemClickListener;
    }

    // constructor
    public ImagesAdapter(Activity activity, List<ImageItem> items) {

        this.act = activity;
        this.items = items;
    }

    @Override
    public int getCount() {

        return this.items.size();
    }

    public ImageItem getItem(int pos) {

        return items.get(pos);
    }

    public void setItems(List<ImageItem> items) {

        this.items = items;
        notifyDataSetChanged();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {

        return view == ((RelativeLayout) object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, final int position) {

        final ImageItem item = items.get(position);

        LayoutInflater inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View v = inflater.inflate(R.layout.addon_item_image, container, false);

        ImageView mImg = (ImageView) v.findViewById(R.id.img);
        MaterialRippleLayout mParent = (MaterialRippleLayout) v.findViewById(R.id.parent);

        try {

            Glide.with(act).load(item.getImageUrl())
                    .transition(withCrossFade())
                    .into(mImg);

        } catch (Exception e) {

            Log.e("ImagesAdapter", e.toString());
        }

        mParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                if (onItemClickListener != null) {

                    onItemClickListener.onItemClick(v, item, position);
                }
            }
        });

        ((ViewPager) container).addView(v);

        return v;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        ((ViewPager) container).removeView((RelativeLayout) object);

    }

}
