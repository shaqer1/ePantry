package com.jjkaps.epantry.ui.Catalog;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.jjkaps.epantry.R;
import com.jjkaps.epantry.models.BarcodeProduct;
import com.jjkaps.epantry.utils.Utils;


import java.util.ArrayList;

import static com.jjkaps.epantry.ui.Catalog.FilterType.NOTHING;

public class CatalogAdapter extends ArrayAdapter<CatalogAdapterItem> implements Filterable {
    private ArrayList<CatalogAdapterItem> originalItems;
    private Context context;
    private FilterType filter_type = NOTHING; //fave 2, scanned 1, none 0
    private static class ViewHolder {
        TextView itemCat;
    }

    public CatalogAdapter(Context context, ArrayList<CatalogAdapterItem> items) {
        super(context, 0, items);
        this.context = context;
        this.originalItems = items;
    }

    public void filterView(String filter, FilterType filter_type){
        this.filter_type = filter_type;
        this.getFilter().filter(filter);
    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        CatalogAdapterItem item = getItem(position);
        BarcodeProduct bp = null;
        if(item != null){
            bp = item.getBarcodeProduct();
        }

        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.catalogitem, parent, false);
            // Lookup view for data population
            viewHolder.itemCat = convertView.findViewById(R.id.catalog_item_tv);
            convertView.setTag(viewHolder);
        }else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.itemCat.setText(bp != null ? bp.getName() : "");
        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return originalItems.size();
    }

    @Nullable
    @Override
    public CatalogAdapterItem getItem(int position) {
        return originalItems.get(position);
    }

    @NonNull
    public Filter getFilter() {
        if (filter == null)
            filter = new CatalogFilter(originalItems);
        return filter;
    }
    private Filter filter;
    private class CatalogFilter extends Filter {
        private ArrayList<CatalogAdapterItem> sources;

        public CatalogFilter(ArrayList<CatalogAdapterItem> originalItems) {
            this.sources= new ArrayList<>();
            synchronized (this){
                this.sources.addAll(originalItems);
            }
        }

        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            FilterResults filterResults = new FilterResults();
            ArrayList<CatalogAdapterItem> tempList = new ArrayList<>();
            if(charSequence != null && charSequence.toString().length() > 0) {
                for (CatalogAdapterItem item: sources) {
                    if(item != null){
                        BarcodeProduct bp = item.getBarcodeProduct();
                        if(bp!=null && bp.getName().toLowerCase().contains(charSequence.toString().toLowerCase())){
                            tempList.add(new CatalogAdapterItem(bp, item.getDocReference()));
                        }
                    }
                }
                //following two lines is very important
                //as publish result can only take FilterResults objects
                switch (filter_type) {
                    case NOTHING:
                        filterNothing(tempList);
                        break;
                    case SCANNED:
                        filterScannedOnly(tempList);
                        break;
                    case FAVORITES:
                        filterFaveOnly(tempList);
                        break;
                }
                synchronized (this){
                    filterResults.values = tempList;
                    filterResults.count = tempList.size();
                }
            }else {
                tempList = new ArrayList<>(sources);
                switch (filter_type){
                    case NOTHING:
                        tempList = sources;
                        break;
                    case SCANNED:
                        filterScannedOnly(tempList);
                        break;
                    case FAVORITES:
                        filterFaveOnly(tempList);
                        break;
                }
                filterResults.values = tempList;
                filterResults.count = tempList.size();

            }
            return filterResults;
        }
        private ArrayList<CatalogAdapterItem> filterNothing(ArrayList<CatalogAdapterItem> tempItems) {
            return tempItems;
        }
        private ArrayList<CatalogAdapterItem> filterScannedOnly(ArrayList<CatalogAdapterItem> tempItems) {
                for (int i = tempItems.size()-1; i >= 0; i--) {
                    if(!Utils.isNotNullOrEmpty(tempItems.get(i).getBarcodeProduct().getBarcode())){
                        tempItems.remove(i);
                    }
                }
            return tempItems;
        }
        private ArrayList<CatalogAdapterItem> filterFaveOnly(ArrayList<CatalogAdapterItem> tempItems) {
                for (int i = tempItems.size()-1; i >= 0; i--) {
                    if(!(tempItems.get(i).getBarcodeProduct().getFavorite())){
                        tempItems.remove(i);
                    }
                }
            return tempItems;
        }



        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            originalItems = (ArrayList<CatalogAdapterItem>) filterResults.values;
            notifyDataSetChanged();
        }
    }


}
