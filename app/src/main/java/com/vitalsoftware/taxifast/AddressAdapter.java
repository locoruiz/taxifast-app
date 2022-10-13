package com.vitalsoftware.taxifast;

import android.content.Context;
import android.location.Address;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by titin on 7/28/16.
 */
public class AddressAdapter extends ArrayAdapter<Address> {
    private List<Address> lista;
    private Context context;

    public AddressAdapter(List<Address> addresses, Context c){
        super(c, 0, addresses);
        lista = addresses;
        context = c;
    }

    @Override
    public int getCount() {
        return lista.size();
    }

    public String direccion(int pos){
        Address address = lista.get(pos);
        String result;
        String ciudad = address.getAdminArea();
        String pais = address.getCountryName();
        String numero = address.getSubThoroughfare();
        String calle = address.getThoroughfare();
        if (calle == null)
            calle = "Calle Desconocida";
        if (numero != null)
            result = calle + " #" + address.getSubThoroughfare() + ", " + ciudad + ", " + pais;
        else
            result = calle + ", " + ciudad + ", " + pais;

        return result;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        TextView v = (TextView) view;

        if (v == null){
            LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            v = (TextView) mInflater.inflate(R.layout.dropdown_item1, null);
        }
        Address address = lista.get(i);
        String result;
        String ciudad = address.getAdminArea();
        String pais = address.getCountryName();
        String numero = address.getSubThoroughfare();
        String calle = address.getThoroughfare();
        if (calle == null)
            calle = "Calle Desconocida";
        if (numero != null)
            result = calle + " #" + address.getSubThoroughfare() + ", " + ciudad + ", " + pais;
        else
            result = calle + ", " + ciudad + ", " + pais;

        v.setText(result);

        return v;
    }
}
