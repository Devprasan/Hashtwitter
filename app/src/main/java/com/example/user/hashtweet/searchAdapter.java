package com.example.user.hashtweet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by user on 12/26/2015.
 */
public class searchAdapter extends ArrayAdapter {
    private  Context context;
    private int resoucre;
    private List<Tweet_model> search;
    private LayoutInflater layoutInflater;
    public searchAdapter(Context context, int resource, List<Tweet_model> objects) {
        super(context, resource, objects);
        this.context =context;
        this.resoucre=resource;
        this.search=objects;
        layoutInflater= (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView==null) {
            convertView = layoutInflater.inflate(resoucre, null);
        }

        TextView textView;
        TextView textView1;
        textView= (TextView) convertView.findViewById(R.id.textView);
        textView1= (TextView) convertView.findViewById(R.id.textView2);
        textView.setText(search.get(position).getUsername());
        textView1.setText(search.get(position).getTweets());




        return convertView;
    }
}
