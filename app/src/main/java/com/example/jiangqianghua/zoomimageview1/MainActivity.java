package com.example.jiangqianghua.zoomimageview1;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jqh.imageview.ZoomImageView;

public class MainActivity extends AppCompatActivity {

    private ViewPager mViewPager ;
    private int mImgs[] = new int[]{R.drawable.a,R.drawable.b,R.drawable.c};
    private ImageView[] mImageViews = new ImageView[mImgs.length];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vp);
        System.out.print("imageview111： onCreate");
        Log.d("imageview111", "imageview111： onCreate");

        mViewPager = (ViewPager)findViewById(R.id.id_viewpager);
        mViewPager.setAdapter(new PagerAdapter() {

            @Override
            public Object instantiateItem(ViewGroup container, int position) {

                // 这个需要做一个判断 mImageViews是否存在imageView
                ZoomImageView imageView = new ZoomImageView(getApplicationContext());
                imageView.setImageResource(mImgs[position]);
                container.addView(imageView);
                mImageViews[position] = imageView ;
                return imageView ;
            }

            @Override
            public int getCount() {
                return mImageViews.length;
            }

            @Override
            public boolean isViewFromObject(View view, Object object) {
                return view == object ;
            }

            @Override
            public void destroyItem(ViewGroup container, int position, Object object) {
                container.removeView(mImageViews[position]);
            }

        });
    }
}
