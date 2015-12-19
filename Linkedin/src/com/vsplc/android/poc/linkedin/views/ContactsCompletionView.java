package com.vsplc.android.poc.linkedin.views;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tokenautocomplete.TokenCompleteTextView;
import com.vsplc.android.poc.linkedin.R;
import com.vsplc.android.poc.linkedin.model.LinkedinUser;
import com.vsplc.android.poc.linkedin.utils.FontUtils;

/**
 * Sample token completion view for searching linkedin connections..
 * @Date 12 Jan, 2014
 * @author Swapnil
 */
public class ContactsCompletionView extends TokenCompleteTextView {

	public ContactsCompletionView(Context context) {
		super(context);
		this.allowDuplicates(false);
		this.setTokenClickStyle(TokenClickStyle.SelectDeselect);
	}

	public ContactsCompletionView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.allowDuplicates(false);
		this.setTokenClickStyle(TokenClickStyle.SelectDeselect);
	}

	public ContactsCompletionView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		this.allowDuplicates(false);
		this.setTokenClickStyle(TokenClickStyle.SelectDeselect);
	}

	@Override
	protected View getViewForObject(Object object) {
		
		
		LinkedinUser user = (LinkedinUser) object;
		
//		Person p = (Person) object;

		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(
				Activity.LAYOUT_INFLATER_SERVICE);
		
		LinearLayout view = (LinearLayout) inflater.inflate(R.layout.contact_token,
				(ViewGroup) ContactsCompletionView.this.getParent(), false);
		
		TextView name = (TextView) view.findViewById(R.id.name);
		name.setTypeface(FontUtils.getLatoRegularTypeface(getContext()));
		name.setText(user.fname +" "+user.lname);

		return view;
	}

	@Override
	protected Object defaultObject(String completionText) {
		
		//Stupid simple example of guessing if we have an email or not
//		int index = completionText.indexOf('@');
//		
//		if (index == -1) {
//			return new Person(completionText, completionText.replace(" ", "") + "@example.com");
//		} else {
//			return new Person(completionText.substring(0, index), completionText);
//		}
		
//		return new Person(completionText, completionText.replace(" ", "") + "@example.com");
		
//		return new Person(completionText.substring(0, index), completionText);
		
		return null;
	}

		
		
	@Override
	public void setSelected(boolean selected) {
		super.setSelected(selected);

		if (selected) {
			Toast.makeText(getContext(), "Selected", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getContext(), "Un-Selected", Toast.LENGTH_SHORT).show();
		}
	}
		
	
}
