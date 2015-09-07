package com.DKB.shutupdrive;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by kyle on 9/6/15.
 */
public class MainTutorialContent  extends Fragment {

    private TextView title, content;
    private ImageView pic;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.layout_tutorial_content, container, false);

        title = (TextView) rootView.findViewById(R.id.feature_name);
        content = (TextView) rootView.findViewById(R.id.description);
        pic = (ImageView) rootView.findViewById(R.id.photo);

        title.setText(getString(R.string.app_name));
        content.setText(getString(R.string.stay_safe));
        pic.setImageResource(R.drawable.icon);

        return rootView;
    }
}