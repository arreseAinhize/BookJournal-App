// StarRatingAdapter.java
package eus.arreseainhize.bookjounal.fragments.adapters;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class StarRatingAdapter extends ArrayAdapter<String> implements Filterable {

    public StarRatingAdapter(@NonNull Context context, @NonNull List<String> objects) {
        super(context, android.R.layout.simple_dropdown_item_1line, objects);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                // Para el AutoCompleteTextView, queremos mostrar todas las opciones siempre
                List<String> allItems = new ArrayList<>();
                for (int i = 0; i < getCount(); i++) {
                    allItems.add(getItem(i));
                }
                results.values = allItems;
                results.count = allItems.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public int getCount() {
        return super.getCount();
    }
}