package com.DKB.shutupdrive.tutorial;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.DKB.shutupdrive.R;

/**
 * Created by kyle on 9/6/15.
 */
public class AutoReplyContent extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.layout_tutorial_content, container, false);

        TextView title = (TextView) rootView.findViewById(R.id.feature_name);
        TextView content = (TextView) rootView.findViewById(R.id.description);
        ImageView pic = (ImageView) rootView.findViewById(R.id.photo);

        title.setText(getString(R.string.feature_auto_reply));
        content.setText(getString(R.string.description_auto_reply));
        pic.setImageResource(R.drawable.auto_reply_opt);

        return rootView;
    }
}