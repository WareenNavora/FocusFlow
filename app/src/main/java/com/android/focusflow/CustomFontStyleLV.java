package com.android.focusflow;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomFontStyleLV extends ArrayAdapter<String> {
    private Context context;
    private String[] items;

    public CustomFontStyleLV(Context context, String[] items) {
        super(context, R.layout.lv_custom_font_style, items);

        this.context = context;
        this.items = items;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.lv_custom_font_style, parent, false);

        TextView textView = view.findViewById(R.id.tvCustomFont);
        textView.setText(items[position]);

        // Set custom font
        Typeface customFont = Typeface.createFromAsset(context.getAssets(), "font/main_font.ttf");
        textView.setTypeface(customFont);

        return view;
    }
}
