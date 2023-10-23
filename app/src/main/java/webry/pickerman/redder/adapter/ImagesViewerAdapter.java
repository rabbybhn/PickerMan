package webry.pickerman.redder.adapter;

import android.app.Activity;
import android.content.Context;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;

import java.util.List;

import webry.pickerman.redder.R;
import webry.pickerman.redder.model.ImageItem;
import webry.pickerman.redder.view.TouchImageView;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

public class ImagesViewerAdapter extends PagerAdapter {

    private Activity act;
    private List<ImageItem> items;
    private LayoutInflater inflater;

    public ImagesViewerAdapter(Activity activity, List<ImageItem> items) {

        this.act = activity;
        this.items = items;
    }

    @Override
    public int getCount() {

        return this.items.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {

        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {

        final ImageItem item = items.get(position);

        TouchImageView mImage;
        inflater = (LayoutInflater) act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.images_viewer_item, container, false);

        mImage = (TouchImageView) viewLayout.findViewById(R.id.image);

        try {

            Glide.with(act).load(item.getImageUrl())
                    .transition(withCrossFade())
                    .into(mImage);

        } catch (Exception e) {

            Log.e("ImagesViewerAdapter", e.toString());
        }

        ((ViewPager) container).addView(viewLayout);

        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {

        ((ViewPager) container).removeView((RelativeLayout) object);

    }
}
