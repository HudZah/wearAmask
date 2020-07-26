package com.hudzah.wearamask;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import org.w3c.dom.Text;

public class SliderAdapter extends PagerAdapter {

    Context context;
    LayoutInflater layoutInflater;

    public int[] slideImages = {R.drawable.ic_gps_icon, R.drawable.ic_man_mask_icon, R.drawable.ic_heart_icon};
    public int[] slideHeadings = {R.string.onboarding_heading_1, R.string.onboarding_heading_2, R.string.onboarding_heading_3};
    public int[] slideParas = {R.string.onboarding_desc_1, R.string.onboarding_desc_2, R.string.onboarding_desc_3};

    public SliderAdapter(Context context){

        this.context = context;
    }



    @Override
    public int getCount() {
        return slideHeadings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return  view == (RelativeLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {

        layoutInflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

        ImageView slideImageView = (ImageView) view.findViewById(R.id.slideImageView);
        TextView slideHeading = (TextView) view.findViewById(R.id.slideHeading);
        TextView slideDesc = (TextView) view.findViewById(R.id.slideParagraph);

        slideImageView.setBackground(null);
        slideImageView.setImageResource(slideImages[position]);
        slideHeading.setText(slideHeadings[position]);
        slideDesc.setText(slideParas[position]);

        container.addView(view);

        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {

        container.removeView((RelativeLayout) object);
    }
}
