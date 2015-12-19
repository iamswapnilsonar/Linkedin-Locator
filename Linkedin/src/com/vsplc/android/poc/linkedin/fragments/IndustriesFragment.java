package com.vsplc.android.poc.linkedin.fragments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.vsplc.android.poc.linkedin.BaseActivity;
import com.vsplc.android.poc.linkedin.R;
import com.vsplc.android.poc.linkedin.adapter.IndustryListAdapter;
import com.vsplc.android.poc.linkedin.logger.Logger;
import com.vsplc.android.poc.linkedin.model.LinkedinUser;
import com.vsplc.android.poc.linkedin.utils.ConstantUtils;
import com.vsplc.android.poc.linkedin.utils.DataWrapper;
import com.vsplc.android.poc.linkedin.utils.FontUtils;
import com.vsplc.android.poc.linkedin.utils.LinkedinApplication;
import com.vsplc.android.poc.linkedin.utils.MethodUtils;

public class IndustriesFragment extends Fragment implements OnClickListener{

	private FragmentActivity mFragActivityContext;
	private ListView list;
	
	private Button btnLeft;
	private EditText edtSearch;
	private IndustryListAdapter adapter;
	
	private List<String> industryList;
	private ProgressDialog progressDialog;
	
	private List<LinkedinUser> mConnections = new ArrayList<LinkedinUser>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {

		mFragActivityContext = getActivity();

		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.industries_fragment, container, false);
		
		btnLeft = (Button) view.findViewById(R.id.btn_left);		
		list = (ListView) view.findViewById(R.id.list);
		edtSearch = (EditText) view.findViewById(R.id.edt_search);
		
		return view;
	}

	
	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		industryList = new ArrayList<String>();
		
		// showing progress dialog while performing heavy tasks..
		progressDialog = new ProgressDialog(MethodUtils.getContextWrapper(mFragActivityContext));	
		progressDialog.setCancelable(false);
		progressDialog.setCancelable(false);
		progressDialog.setCanceledOnTouchOutside(false);
		
		Logger.vLog("IndustriesFragment", "Global Length : "+LinkedinApplication.setOfGlobalIndustryNames.size());		
		for (String industry : LinkedinApplication.setOfGlobalIndustryNames) {
			industryList.add(industry);
		}		
		Logger.vLog("IndustriesFragment", "Local Length : "+industryList.size());
				
		Collections.sort(industryList);
		
		adapter = new IndustryListAdapter(mFragActivityContext, industryList);
		list.setAdapter(adapter);
		
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				
				String industry =  adapter.listIndustries.get(position);
				Logger.vLog("industriesList : onItemClick",""+industry);
				
				new GetIndustryWiseConnections().execute(industry);				
			}
			
		});
		
		btnLeft.setOnClickListener(this);			
		
		edtSearch.setTypeface(FontUtils.getLatoRegularTypeface(mFragActivityContext));
		edtSearch.addTextChangedListener(new TextWatcher() {
        
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {
					System.out.println("Text ["+s+"] - Start ["+start+"] - Before ["+before+"] - Count ["+count+"]");
					
					if (count < before) {
						// We're deleting char so we need to reset the adapter data
						adapter.resetData();
					}
						
					adapter.getFilter().filter(s.toString());
					
				}
				
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count,
						int after) {
					
				}
				
				@Override
				public void afterTextChanged(Editable s) {
				}
			});
				
		edtSearch.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				
				if (hasFocus) {
					edtSearch.setHint("");
				}else{
					edtSearch.setHint("Browse");
				}
				
			}
		});
	}


	public void hideKeyboardAndClearSearchText() {   

		edtSearch.clearFocus();
		edtSearch.setText("");

		// Check if no view has focus:			
		View view = mFragActivityContext.getCurrentFocus();
		if (view != null) {
			InputMethodManager inputManager = (InputMethodManager) mFragActivityContext.getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
	
	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		
		int key = view.getId();
		
		switch (key) {
		case R.id.btn_left:
			((BaseActivity) getActivity()).showHideNevigationDrawer();
			break;

		default:
			break;
		}
		
	}
	
	private class GetIndustryWiseConnections extends AsyncTask<String, Void, String> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();

			if (!progressDialog.isShowing()) {
				progressDialog.setMessage("Get Industry wise Connections..");
				progressDialog.show();				
			}

		}

		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub

			String industry = params[0];
//			String country = params[1];
						
			ArrayList<LinkedinUser> temp = MethodUtils.getIndustrywiseConnections(industry, LinkedinApplication.listGlobalConnections); 
			mConnections.clear();
			mConnections.addAll(temp);
			
			Logger.vLog("GetIndustryWiseConnections : doInBackground", "Size of mConnections : "+mConnections.size());
			
			return "Success";
		}

		@Override
		protected void onPostExecute(String result) {

			if (progressDialog.isShowing() && result.equals("Success")) {
				
				progressDialog.dismiss();

				hideKeyboardAndClearSearchText();
				
				FragmentTransaction transaction = mFragActivityContext.getSupportFragmentManager().beginTransaction();
				
				// Create fragment and give it an arguments if any
				ConnectionFragment targetFragment = (ConnectionFragment) Fragment.instantiate(mFragActivityContext, ConstantUtils.CONNECTION_FRAGMENT);
				
				Bundle bundle = new Bundle();
				
				DataWrapper dataWrapper = new DataWrapper((ArrayList<LinkedinUser>)mConnections);
				bundle.putSerializable("connection_list", dataWrapper);
				bundle.putString("callingFrom","IndustriesFragment");
				targetFragment.setArguments(bundle);
				
				// Replace whatever is in the fragment_container view with this fragment,
				// and add the transaction to the back stack so the user can navigate back
				transaction.replace(R.id.fragment_container, targetFragment, "connections");

				transaction.addToBackStack(null);		 
				transaction.commit();
				
			}
		}

	}
}
