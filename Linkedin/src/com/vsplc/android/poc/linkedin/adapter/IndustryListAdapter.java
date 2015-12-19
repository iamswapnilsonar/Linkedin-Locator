package com.vsplc.android.poc.linkedin.adapter;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.vsplc.android.poc.linkedin.R;

public class IndustryListAdapter extends BaseAdapter implements Filterable{
    
    @SuppressWarnings("unused")
	private Activity activity;
    private static LayoutInflater inflater=null; 
    public List<String> listIndustries;
    private List<String> originalListIndustries;
    
    private Filter industryFilter;
    
    public IndustryListAdapter(Activity activity, List<String> data) {
        this.activity = activity;
        this.listIndustries = data;
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.originalListIndustries = data;
    }

    public int getCount() {
        return listIndustries.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }
    
    @SuppressLint("InflateParams")
	public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        
        if(convertView == null)
            vi = inflater.inflate(R.layout.list_industries_row, null);

        TextView name = (TextView)vi.findViewById(R.id.ind_name);        

        // Setting all values in listview
        name.setText(listIndustries.get(position));
        
        return vi;
    }

    @Override
	public Filter getFilter() {
		if (industryFilter == null)
			industryFilter = new ConnectionFilter();
		
		return industryFilter;
	}
    
    public void resetData() {
		listIndustries = originalListIndustries;
	}
	
	@SuppressLint("DefaultLocale")
	public class ConnectionFilter extends Filter {
		
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			FilterResults results = new FilterResults();
			
			// We implement here the filter logic
			if (constraint == null || constraint.length() == 0) {
				// No filter implemented we return all the list
				results.values = originalListIndustries;
				results.count = originalListIndustries.size();
				
			}
			else {
				
				// We perform filtering operation
				List<String> nPlanetList = new ArrayList<String>();
				
				for (String str : listIndustries) {
										
					if (str.toUpperCase().startsWith(constraint.toString().toUpperCase()) || 
							str.toUpperCase().contains(constraint.toString().toUpperCase()))
						nPlanetList.add(str);
				}
				
				results.values = nPlanetList;
				results.count = nPlanetList.size();

			}
			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(CharSequence constraint,
				FilterResults results) {
			
			// Now we have to inform the adapter about the new list filtered
			if (results.count == 0)
				notifyDataSetInvalidated();
			else {
				listIndustries = (List<String>) results.values;
				notifyDataSetChanged();
			}
			
		}
		
	}
}