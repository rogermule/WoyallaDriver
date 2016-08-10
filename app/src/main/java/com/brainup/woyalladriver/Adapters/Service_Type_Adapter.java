package com.brainup.woyalladriver.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.brainup.woyalladriver.R;

import java.util.ArrayList;

/**
 * Created by Rog on 6/10/16.
 */
public class Service_Type_Adapter extends ArrayAdapter<String> {
    private Context myContext;
    private ArrayList<String> object;
    public Service_Type_Adapter(Context context, int txtViewResourceId, ArrayList<String> objects) {
        super(context, txtViewResourceId, objects);
        myContext = context;
        this.object = objects;
    }

    @Override
    public View getDropDownView(int position, View cnvtView, ViewGroup prnt) {
        return getCustomView(position, cnvtView, prnt);
    }
    @Override
    public View getView(int pos, View cnvtView, ViewGroup prnt) {
        return getCustomView(pos, cnvtView, prnt);
    }
    public View getCustomView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View mySpinner = inflater.inflate(R.layout.spinner_service_type, parent, false);
        TextView main_text = (TextView) mySpinner.findViewById(R.id.spinner_text);
        main_text.setText(object.get(position).toString());
        return mySpinner;
    }

}
