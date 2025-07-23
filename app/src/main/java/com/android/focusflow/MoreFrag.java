package com.android.focusflow;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Objects;

public class MoreFrag extends Fragment {

    ListView lvTutorialChoices,lvDataChoices;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_more, container, false);

        lvTutorialChoices = view.findViewById(R.id.lvTutorialChoices);
        lvDataChoices = view.findViewById(R.id.lvDataChoices);

        String[] tutorialChoices = {"Tutorial"};
        String[] dataChoices = {"Delete all data from scheduled task", "Delete all data from daily task"};

        CustomFontStyleLV customFontStyleLV = new CustomFontStyleLV(getContext(), tutorialChoices);
        CustomFontStyleLV customFontStyleLV1 = new CustomFontStyleLV(getContext(), dataChoices);

        lvTutorialChoices.setAdapter(customFontStyleLV);
        lvDataChoices.setAdapter(customFontStyleLV1);

        lvTutorialChoices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    Toast.makeText(getContext(), "You've selected tutorial section!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Try again later!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        lvDataChoices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i){
                    case 0:
                        Toast.makeText(getContext(), "Delete all data from scheduled task!", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(getContext(), "Delete all data from daily task", Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        Toast.makeText(getContext(), "Try again later!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        return view;
    }
}