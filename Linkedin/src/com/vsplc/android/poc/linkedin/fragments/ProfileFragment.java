package com.vsplc.android.poc.linkedin.fragments;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.FragmentManager.OnBackStackChangedListener;
import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.vsplc.android.poc.linkedin.BaseActivity;
import com.vsplc.android.poc.linkedin.R;
import com.vsplc.android.poc.linkedin.logger.Logger;
import com.vsplc.android.poc.linkedin.model.LinkedinUser;
import com.vsplc.android.poc.linkedin.model.SignedLinkedinUser;
import com.vsplc.android.poc.linkedin.utils.CircleTransform;
import com.vsplc.android.poc.linkedin.utils.ConstantUtils;
import com.vsplc.android.poc.linkedin.utils.DataWrapper;
import com.vsplc.android.poc.linkedin.utils.FontUtils;

@SuppressLint("NewApi")
public class ProfileFragment extends Fragment implements OnClickListener, OnBackStackChangedListener{
	
	private TextView tvProfileName, tvProfileHeading, tvProfileLocation;
	
	@SuppressWarnings("unused")
	private TextView tvConnectionCount, tvSkills, tvLanguages, tvIndustry, tvProfileURL, tvProfileSummary;
	private ImageView ivProfileImage;
	private LinearLayout linearLayout;
	private Button btnSeeOnMap, btnViewProfile, btnSendMessage;
	
	@SuppressWarnings("unused")
	private TextView tvSeeOnMap, tvViewProfile, tvSendMessage;
	
	private Button btnLeft;
	private String profileType;
	
//	private FragmentManager fragmentManager;	
	private FragmentActivity mFragActivityContext;
		
	private LinkedinUser user;
	private SignedLinkedinUser signedLinkedinUser;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		mFragActivityContext = getActivity();
		
		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.profile_fragment, container, false);
		
		ivProfileImage = (ImageView) view.findViewById(R.id.iv_profileimage);
		tvProfileName = (TextView) view.findViewById(R.id.tv_profilename);
		tvProfileHeading = (TextView) view.findViewById(R.id.tv_profile_heading);
		tvProfileLocation = (TextView) view.findViewById(R.id.tv_profile_location);
		
		tvConnectionCount = (TextView) view.findViewById(R.id.tv_connection_count);
		tvSkills = (TextView) view.findViewById(R.id.tv_skills);
		tvLanguages = (TextView) view.findViewById(R.id.tv_languages);
		tvIndustry = (TextView) view.findViewById(R.id.tv_industry);
		tvProfileURL = (TextView) view.findViewById(R.id.tv_profile_url);
		tvProfileSummary = (TextView) view.findViewById(R.id.tv_summary);
		
		linearLayout = (LinearLayout) view.findViewById(R.id.lin_options_user_profile);
		
		btnSeeOnMap = (Button) view.findViewById(R.id.btn_seeonmap);
		btnViewProfile = (Button) view.findViewById(R.id.btn_viewprofile);			
		btnSendMessage = (Button) view.findViewById(R.id.btn_sendmessage);
		
		tvSeeOnMap = (TextView) view.findViewById(R.id.tv_seeonmap);
		tvViewProfile = (TextView) view.findViewById(R.id.tv_viewprofile);			
		tvSendMessage = (TextView) view.findViewById(R.id.tv_sendmessage);
		
		btnSeeOnMap.setOnClickListener(this);
		btnViewProfile.setOnClickListener(this);
		btnSendMessage.setOnClickListener(this);		
		
//		tvProfileURL.setSelected(true);
		tvProfileURL.setPaintFlags(tvProfileURL.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
		tvProfileURL.setOnClickListener(this);
		
		btnLeft = (Button) view.findViewById(R.id.btn_left);				
		return view;
	}

	@Override
	public void onStart() {
		super.onStart();
		
		// set font to all texts on Profile Fragment..
		RelativeLayout layout = ((RelativeLayout)getActivity().findViewById(R.id.rel_profile_fragment));
		overrideFonts(getActivity(), layout);	

		// tvProfileHeading.setSelected(true);
		tvProfileName.setTypeface(FontUtils.getLatoBlackTypeface(getActivity()));
		
		Bundle args = getArguments(); 

		if (args != null) { 
			
			profileType = args.getString("profile_type");
			
			if (profileType.equals("AppUser")) {
				
				signedLinkedinUser = (SignedLinkedinUser) args.getSerializable("user");
				Logger.vLog("ProfileFragment", ""+signedLinkedinUser.toString());

				if (signedLinkedinUser != null) {

					tvProfileName.setText(signedLinkedinUser.fname+" "+signedLinkedinUser.lname);
					tvProfileHeading.setText(signedLinkedinUser.headline);
					tvProfileLocation.setText(signedLinkedinUser.location);

					tvIndustry.setText(signedLinkedinUser.industry);
					tvProfileURL.setText(signedLinkedinUser.profileurl);	

					
//					JSONObject json = new JSONObject("");
//					ArrayList items = json.optJSONArray("uniqueArrays");
					
//					String[] stringArray = list.toArray(new String[list.size()]);
					
//					String[] skills = signedLinkedinUser.skills;
					
//					StringBuilder skillBuilder = new StringBuilder();
//					
//					if (skills.length > 0) {
//						
//						for (int i = 0; i <= (skills.length-2); i++) {
//							skillBuilder.append(skills[i]);
//							skillBuilder.append(", ");
//						}
//						
//						skillBuilder.append(skills[skills.length-1]);						
//					}
//					
//					tvSkills.setText(skillBuilder.toString());	
					
//					String[] languages = signedLinkedinUser.languages;
//					
//					StringBuilder languagesBuilder = new StringBuilder();
//					
//					if (languages.length > 0) {
//						
//						for (int i = 0; i <= (languages.length-2); i++) {
//							languagesBuilder.append(languages[i]);
//							languagesBuilder.append(", ");
//						}
//						
//						languagesBuilder.append(languages[languages.length-1]);						
//					}
//					
//					tvLanguages.setText(languagesBuilder.toString());	
					
					tvProfileSummary.setText(signedLinkedinUser.summary);
					tvConnectionCount.setText(signedLinkedinUser.connectionsCount);					
					
					Picasso picasso = Picasso.with(mFragActivityContext);
					RequestCreator creator = picasso.load(signedLinkedinUser.profilepicture);
					creator.resize(80, 80);
					creator.centerCrop();
					creator.placeholder(R.drawable.btn_viewprofile_pressed);
					creator.error(R.drawable.btn_viewprofile_pressed);
					creator.transform(new CircleTransform());
					creator.into(ivProfileImage);
										
					linearLayout.setVisibility(View.GONE);
				}

				btnLeft.setBackgroundResource(R.drawable.btn_list_tap_effect);
				
			}else{
				
				user = (LinkedinUser) args.getSerializable("user");
				Logger.vLog("ProfileFragment", ""+user.toString());
				
				if (user != null) {

					tvProfileName.setText(user.fname+" "+user.lname);
					tvProfileHeading.setText(user.headline);
					tvProfileLocation.setText(user.location);

//					tvConnectionCount.setVisibility(View.GONE);
					tvIndustry.setText(user.industry);
					
					((LinearLayout)mFragActivityContext.findViewById(R.id.lin_skills)).setVisibility(View.GONE);
					((LinearLayout)mFragActivityContext.findViewById(R.id.lin_languages)).setVisibility(View.GONE);
					((LinearLayout)mFragActivityContext.findViewById(R.id.lin_profile_url)).setVisibility(View.GONE);
					((LinearLayout)mFragActivityContext.findViewById(R.id.lin_summary)).setVisibility(View.GONE);

					Picasso picasso = Picasso.with(mFragActivityContext);
					RequestCreator creator = picasso.load(user.profilepicture);
					creator.resize(80, 80);
					creator.centerCrop();
					creator.placeholder(R.drawable.btn_viewprofile_pressed);
					creator.error(R.drawable.btn_viewprofile_pressed);
					creator.transform(new CircleTransform());
					creator.into(ivProfileImage);
					
					linearLayout.setVisibility(View.VISIBLE);
					
				}
				
				btnLeft.setBackgroundResource(R.drawable.btn_back_tap_effect);
				
			}
		}
		
		btnLeft.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				// TODO Auto-generated method stub
				
				if (profileType.equals("AppUser")) {
					((BaseActivity) getActivity()).showHideNevigationDrawer();
				}else{
					getActivity().onBackPressed();
				}
				
			}
		});

	}

	private void overrideFonts(final Context context, final View v) {
		try {
			if (v instanceof ViewGroup) {
				ViewGroup vg = (ViewGroup) v;
				for (int i = 0; i < vg.getChildCount(); i++) {
					View child = vg.getChildAt(i);
					overrideFonts(context, child);
				}
			} else if (v instanceof TextView ) {
				((TextView) v).setTypeface(FontUtils.getLatoRegularTypeface(getActivity()));
			}
		} catch (Exception e) {
		}
	}

	@Override
	public void onClick(View view) {
		// TODO Auto-generated method stub
		
		int key = view.getId();
		
		FragmentTransaction transaction = mFragActivityContext.getSupportFragmentManager().beginTransaction();
		Fragment targetFragment = null;
		String tagFragment = null;
		
		if (key == R.id.btn_seeonmap) {
			
			// Create fragment and give it an argument for the selected article
			targetFragment = (GoogleMapFragment) Fragment.instantiate(mFragActivityContext, 
            						ConstantUtils.GOOGLE_MAP_FRAGMENT);	           

            Bundle bundle = new Bundle();            
            bundle.putString("marker_type", "LocateOnMap");
			bundle.putSerializable("user", user);
			
			bundle.putString("callingFrom","ProfileFragment");
			
			targetFragment.setArguments(bundle);
			tagFragment = "googlemap";
			
		}else if (key == R.id.btn_viewprofile || key == R.id.tv_profile_url){
						
			// Create fragment and give it an argument for the selected article
            targetFragment = (LinkedinProfileFragment) Fragment.instantiate(mFragActivityContext, 
            						ConstantUtils.LINKEDIN_PROFILE_FRAGMENT);	           
            
            Bundle bundle = new Bundle();
            
            if (profileType.equals("AppUser")) {
            	bundle.putString("url", signedLinkedinUser.profileurl);
			}else{
				bundle.putString("url", user.profileurl);
			}                    
            
            targetFragment.setArguments(bundle);
            
            tagFragment = "linkedinprofile";
            
		}else if(key == R.id.btn_sendmessage){
		
			List<LinkedinUser> listRecipients  = new ArrayList<LinkedinUser>();
 			listRecipients.add(user);
 			
			// Create fragment and give it an argument for the selected article
            targetFragment = (MessageFragment) Fragment.instantiate(mFragActivityContext, 
            						ConstantUtils.MESSAGE_FRAGMENT);	           

            Bundle bundle = new Bundle();            
			            
			DataWrapper dataWrapper = new DataWrapper((ArrayList<LinkedinUser>)listRecipients);
			bundle.putSerializable("connection_list", dataWrapper);			
			bundle.putString("callingFrom","ProfileFragment");
			targetFragment.setArguments(bundle);
			
			tagFragment = "message";
			
		}
		
		if (targetFragment != null && tagFragment != null) {
			
			// Replace whatever is in the fragment_container view with this fragment,
			// and add the transaction to the back stack so the user can navigate back
			transaction.replace(R.id.fragment_container, targetFragment, tagFragment);

			transaction.addToBackStack(null);		 
			transaction.commit();
			
		}
		
	}

	@Override
	public void onBackStackChanged() {
		// TODO Auto-generated method stub
		
	}
	
	
	
}