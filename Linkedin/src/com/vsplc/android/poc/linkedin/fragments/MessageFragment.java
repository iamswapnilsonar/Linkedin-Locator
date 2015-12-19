package com.vsplc.android.poc.linkedin.fragments;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.tokenautocomplete.FilteredArrayAdapter;
import com.tokenautocomplete.TokenCompleteTextView;
import com.vsplc.android.poc.linkedin.BaseActivity;
import com.vsplc.android.poc.linkedin.R;
import com.vsplc.android.poc.linkedin.linkedin_api.model.EasyLinkedIn;
import com.vsplc.android.poc.linkedin.logger.Logger;
import com.vsplc.android.poc.linkedin.model.LinkedinUser;
import com.vsplc.android.poc.linkedin.utils.DataWrapper;
import com.vsplc.android.poc.linkedin.utils.FontUtils;
import com.vsplc.android.poc.linkedin.utils.LinkedinApplication;
import com.vsplc.android.poc.linkedin.utils.MethodUtils;
import com.vsplc.android.poc.linkedin.views.ContactsCompletionView;

public class MessageFragment extends Fragment implements OnClickListener, TokenCompleteTextView.TokenListener{

	private FragmentActivity mFragActivityContext;
	
	private ArrayList<LinkedinUser> listLinkedinUsers;
	
	private Button btnSend, btnCancel, btnLeft;
	private EditText edtSubject, edtMessage;
	
	private ContactsCompletionView edtRecipients;
	
	private ProgressDialog pDialog;
	private Typeface typeface;
	
	private String callingFrom;
	
	private LinkedinUser[] users;
	private ArrayAdapter<LinkedinUser> adapter;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {

		mFragActivityContext = getActivity();

		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.message_fragment, container, false);
		
		btnLeft = (Button) view.findViewById(R.id.btn_left);
		btnSend = (Button) view.findViewById(R.id.btn_send);
		btnCancel = (Button) view.findViewById(R.id.btn_cancel);
		
		edtSubject = (EditText) view.findViewById(R.id.edit_message_subject);
		edtMessage = (EditText) view.findViewById(R.id.edit_message_body);
		
		edtRecipients = (ContactsCompletionView) view.findViewById(R.id.edit_message_receipents); 		
		
		btnLeft.setOnClickListener(this);
		btnSend.setOnClickListener(this);
		btnCancel.setOnClickListener(this);
				
		return view;
	}

	@Override
	public void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		// showing progress dialog while performing heavy tasks..
		pDialog = new ProgressDialog(MethodUtils.getContextWrapper(mFragActivityContext));	
		pDialog.setCancelable(false);
		pDialog.setCancelable(false);
		pDialog.setCanceledOnTouchOutside(false);
		
		listLinkedinUsers = new ArrayList<LinkedinUser>();
		
		Bundle args = getArguments(); 

		if (args != null) { // load web link received through bundle
			//NOP
			
			try{
				
				DataWrapper dataWrapper = (DataWrapper) args.getSerializable("connection_list");
				ArrayList<LinkedinUser> mConnections = dataWrapper.getList();
				
				listLinkedinUsers = mConnections;
				
				Logger.vLog("MessageFragment", "Receipents : "+listLinkedinUsers.size());				
				// add the recipents to message..
				for(LinkedinUser user : listLinkedinUsers){
					edtRecipients.addObject(user);
				}
				
			}catch(Exception ex){
				
			}
			
			callingFrom = args.getString("callingFrom");
		}
		
		if (callingFrom.equals("NavigationDrawer")) {
			btnLeft.setBackgroundResource(R.drawable.btn_list_tap_effect);
		}else{
			btnLeft.setBackgroundResource(R.drawable.btn_back_tap_effect);
		}
		
		typeface = FontUtils.getLatoRegularTypeface(mFragActivityContext);
		
		edtRecipients.setTypeface(typeface);
		edtSubject.setTypeface(typeface);
		edtMessage.setTypeface(typeface);		
		
		users = (LinkedinUser[]) LinkedinApplication.listGlobalConnections.toArray(
					new LinkedinUser[LinkedinApplication.listGlobalConnections.size()]);
		
		adapter = new FilteredArrayAdapter<LinkedinUser>(mFragActivityContext, R.layout.person_layout, users) {
            
        	@Override
            public View getView(int position, View convertView, ViewGroup parent) {
                
        		if (convertView == null) {

                    LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
                    convertView = (View)inflater.inflate(R.layout.person_layout, parent, false);
                }

                LinkedinUser user = getItem(position);
               
                TextView userName = (TextView) convertView.findViewById(R.id.name);
                TextView userEmail = (TextView) convertView.findViewById(R.id.email);
                
                userName.setTypeface(typeface);
                userEmail.setTypeface(typeface);
                
                userName.setText(user.fname+" "+user.lname);
                userEmail.setText(user.industry);
                         
                return convertView;
            }

            @SuppressLint("DefaultLocale")
			@Override
            protected boolean keepObject(LinkedinUser user, String mask) {
                
//            	mask = mask.toLowerCase();
//                return obj.getName().toLowerCase().startsWith(mask) || obj.getEmail().toLowerCase().startsWith(mask);
                
            	mask = mask.toLowerCase();
            	return user.fname.toLowerCase().startsWith(mask) || user.lname.toLowerCase().startsWith(mask);

            }
            
        };
		
		edtRecipients.setAdapter(adapter);
		edtRecipients.setTokenListener(this);
		
	}

	public void hideKeyboardAndClearSearchText() {   
	    
		edtRecipients.clearFocus();
		edtRecipients.setText("");
		
		edtSubject.clearFocus();
		edtSubject.setText("");
		
		edtMessage.clearFocus();
		edtMessage.setText("");
		
		// Check if no view has focus:			
	    View view = mFragActivityContext.getCurrentFocus();
	    if (view != null) {
	        InputMethodManager inputManager = (InputMethodManager) mFragActivityContext.getSystemService(Context.INPUT_METHOD_SERVICE);
	        inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	    }
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		int key = v.getId();
		
		switch (key) {

		case R.id.btn_left:
			
			hideKeyboardAndClearSearchText();
			
			if (callingFrom.equals("NavigationDrawer")) {
				((BaseActivity) getActivity()).showHideNevigationDrawer();
			}else{
				getActivity().onBackPressed();
			}
			
			
			break;

		case R.id.btn_send:
			
			if (!isEmpty(edtSubject) && !isEmpty(edtMessage)) {
				
				listLinkedinUsers.clear();
				
				List<Object> list = edtRecipients.getObjects();							
				
				for (int i = 0; i < list.size(); i++) {
					LinkedinUser user = (LinkedinUser) list.get(i);
					Logger.vLog("Message Fragment", "User : "+user.fname +" "+user.lname);
					listLinkedinUsers.add(user);
				}
				
				new LongOperationForSendMessage().execute(listLinkedinUsers);
			}else{
				Toast.makeText(mFragActivityContext, "Please enter the details..", Toast.LENGTH_SHORT).show();
			}
			
			break;
			
		case R.id.btn_cancel:

			hideKeyboardAndClearSearchText();
			getActivity().onBackPressed();
						
			break;
			
		}
	}
	
	private boolean isEmpty(EditText etText) {
		return etText.getText().toString().trim().length() == 0;
	}
	
	private class LongOperationForSendMessage extends AsyncTask<Object, Void, String> {

		@Override
		protected String doInBackground(Object... params) {

			HttpResponse response = null;
			String result;
			
			String strSubject = edtSubject.getText().toString();
			String strMessage = edtMessage.getText().toString();
			
			@SuppressWarnings("unchecked")
			List<LinkedinUser> listRecipients = (ArrayList<LinkedinUser>) params[0];
			
			Logger.vLog("LongOperationForSendMessage", "Size of Recipients : "+listRecipients.size());
			
			if (listRecipients.size() < 50) {

				String prepared_url = "https://api.linkedin.com/v1/people/~/mailbox?oauth2_access_token="
						+ EasyLinkedIn.getAccessToken();

				DefaultHttpClient httpclient = new DefaultHttpClient();
				HttpPost post = new HttpPost(prepared_url);

				post.setHeader("content-type", "text/XML");

				StringBuilder builder = new StringBuilder();

				builder.append("<mailbox-item>");
				builder.append("<recipients>");
				
				for (LinkedinUser user : listRecipients) {
					builder.append("<recipient><person path='/people/"+user.id+"' /></recipient>");
				}
				
//				builder.append("<recipient><person path='/people/abcdefg' /></recipient>");
				builder.append("</recipients>");
				
				String mSubject = "<subject>"+strSubject+"</subject>";
				String mMessage = "<body>"+strMessage+"</body>";
								
				builder.append(mSubject);
				builder.append(mMessage);
				
				builder.append("</mailbox-item>");

				String myEntity = builder.toString();

				try {

					StringEntity str_entity = new StringEntity(myEntity);
					post.setEntity(str_entity);

					response = httpclient.execute(post);
					Log.i("Send Message : ", ""+response.toString());

					result = "Success";

				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					result = "Error.. Message not sent.";

				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					result = "Error.. Message not sent.";

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					result = "Error.. Message not sent.";

				}
				
			}else{
				result = "Too many recipients in the message. Max allowed to be 50 recipients.";
			}
			
			return result;
		}

		@Override
		protected void onPostExecute(String result) {
			
			if (pDialog.isShowing()) {
				pDialog.dismiss();
			}
			
			if (result.equals("Success")) {
				Toast.makeText(mFragActivityContext, "Message Sent Successfully..", Toast.LENGTH_SHORT).show();
				hideKeyboardAndClearSearchText();
				getActivity().onBackPressed();
				
			}else{
				Toast.makeText(mFragActivityContext, result, Toast.LENGTH_SHORT).show();
			}
						
		}

		@Override
		protected void onPreExecute() {
			pDialog.setMessage("Sending Message..");
			pDialog.show();
		}

		@Override
		protected void onProgressUpdate(Void... values) {}
	}

	@SuppressWarnings("unused")
	private void updateTokenConfirmation() {
        
//    	StringBuilder sb = new StringBuilder("Current tokens:\n");
//        
//    	for (Object token: completionView.getObjects()) {
//            sb.append(token.toString());
//            sb.append("\n");
//        }
//
//        ((TextView)findViewById(R.id.tokens)).setText(sb);
    }
	
	@Override
	public void onTokenAdded(Object object) {
		// TODO Auto-generated method stub
		
		if(edtRecipients.getObjects().size() <= 50){
			
		}else{
			//NOP
		}
		
	}

	@Override
	public void onTokenRemoved(Object object) {
		// TODO Auto-generated method stub
		
	}
}
