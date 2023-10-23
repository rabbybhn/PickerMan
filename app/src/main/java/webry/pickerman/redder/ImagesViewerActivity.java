package webry.pickerman.redder;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.viewpager.widget.ViewPager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;

import webry.pickerman.redder.adapter.ImagesViewerAdapter;
import webry.pickerman.redder.common.ActivityBase;
import webry.pickerman.redder.model.ImageItem;

public class ImagesViewerActivity extends ActivityBase {

    private ImagesViewerAdapter adapter;
    private ViewPager mViewPager;
    private TextView mTextView;

    private ArrayList<ImageItem> images;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        Bundle params = new Bundle();
        params.putString("activity", "ImagesViewerActivity");
        mFirebaseAnalytics.logEvent("app_open_activity", params);

        setContentView(R.layout.activity_images_viewer);

        mViewPager = (ViewPager) findViewById(R.id.pager);
        mTextView = (TextView) findViewById(R.id.textView);

        Intent i = getIntent();

        final int position = i.getIntExtra("position", 0);
        images = i.getParcelableArrayListExtra("images");
        adapter = new ImagesViewerAdapter(this, images);
        final int total = adapter.getCount();
        mViewPager.setAdapter(adapter);

        mTextView.setText(String.format(getString(R.string.image_of), (position + 1), total));

        mViewPager.setCurrentItem(position);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int pos, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int pos) {

                mTextView.setText(String.format(getString(R.string.image_of), (pos + 1), total));
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


        ((ImageButton) findViewById(R.id.btnClose)).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                finish();
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(getResources().getColor(R.color.black));
        }
    }


}

