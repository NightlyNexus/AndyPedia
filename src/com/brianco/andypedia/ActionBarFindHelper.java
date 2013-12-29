package com.brianco.andypedia;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;

class ActionBarFindHelper implements TextWatcher {
	private WebView mWebView;
	private ActionBar mActionBar;
	private MainActivity mActivity;
	private EditText mEditText;
	private boolean finderOpen = false;
	private View v;
	
	private View.OnClickListener mFindNextListener = new View.OnClickListener() {
        public void onClick(View v) {
            mWebView.findNext(true);
        }
    };

    private View.OnClickListener mFindCancelListener  = 
            new View.OnClickListener() {
        public void onClick(View v) {
        	//hide keyboard and close dialog
        	hideKeyboard(true);
        }
    };
    
    public void closeFinder(){
    	mWebView.clearMatches();
    	mActionBar.setDisplayOptions(
    		    ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
    	finderOpen = false;
		mWebView.setFocusable(true);//to prevent awkwardness
    }
    
    private View.OnClickListener mFindPreviousListener  = 
            new View.OnClickListener() {
        public void onClick(View v) {
            if (mWebView == null) {
                throw new AssertionError("No WebView for FindDialog::onClick");
            }
            mWebView.findNext(false);
        }
    };
    
	@Override
	public void afterTextChanged(Editable s) {}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (mWebView == null) {
            throw new AssertionError(
                    "No WebView for FindDialog::onTextChanged");
        }
        CharSequence find = mEditText.getText();
        findAll(find.toString());
	}
    
	public ActionBarFindHelper(MainActivity activity, WebView webView){
		mActivity = activity;
		mWebView = webView;
		mActionBar = activity.getSupportActionBar();
	}
	
	@SuppressLint("NewApi")
	public void getView(){
		mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
		mWebView.setFocusable(false);//to prevent awkwardness
		finderOpen = true;

		LayoutInflater inflator = (LayoutInflater) mActivity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = inflator.inflate(R.layout.find_on_page, null);
		(v.findViewById(R.id.cancel_find)).setOnClickListener(mFindCancelListener);
		(v.findViewById(R.id.find_previous)).setOnClickListener(mFindPreviousListener);
		(v.findViewById(R.id.find_next)).setOnClickListener(mFindNextListener);
		mEditText = (EditText) v.findViewById(R.id.search_in_page);
		mEditText.addTextChangedListener(this);
        mEditText.requestFocus();
        showKeyboard();
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                	//hide keyboard and do not close dialog
                	hideKeyboard(false);
                    return true;
                }
                return false;
            }
        });
		findAll(null);

		mActionBar.setCustomView(v);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mWebView.setFindListener(new WebView.FindListener() {
				@Override
				public void onFindResultReceived(int activeMatchOrdinal,
						int numberOfMatches, boolean isDoneCounting) {
					checkMatchNumber(numberOfMatches);
				}
			});
		}
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void findAll(String str){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			mWebView.findAllAsync(str);
		} else{
			checkMatchNumber(mWebView.findAll(str));
		}
	}

	private void checkMatchNumber(int numberOfMatches) {
		View background = v.findViewById(R.id.background_action_bar);
		if(mEditText.getText().toString().length() > 0 && numberOfMatches == 0) {
			background.setBackgroundColor(
					mActivity.getResources().getColor(R.color.not_found_red));
		} else {
			background.setBackgroundColor(
					mActivity.getResources().getColor(android.R.color.white));
		}
	}

	public boolean isFinderOpen(){
		return finderOpen;
	}

	private void showKeyboard(){
		mEditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager)
                mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(mEditText, 0);
            }
        },200);
	}
	
	private void hideKeyboard(final boolean shouldCloseFinder){
		mEditText.postDelayed(new Runnable() {
            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager)
                mActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.hideSoftInputFromWindow(mEditText.getWindowToken(), 
                        InputMethodManager.RESULT_UNCHANGED_SHOWN);
            	if(shouldCloseFinder){
            		closeFinder();
            	}
            }
        },200);
	}
}
