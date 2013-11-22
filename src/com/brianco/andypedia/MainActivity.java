package com.brianco.andypedia;

import com.readystatesoftware.systembartint.SystemBarTintManager;

import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.app.SearchManager.OnDismissListener;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class MainActivity extends ActionBarActivity {
	private Menu mMenu;
	private String query = "";
	private String set;
	private String lang;
	private WebView webView;
	private View loadingView;
	private ActionBarFindHelper mActionBarFindHelper;
	private boolean oldSearchOpened;
	
	@SuppressLint({ "InlinedApi", "NewApi" })
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//getSupportActionBar().setDisplayShowTitleEnabled(false);
		setContentView(R.layout.activity_main);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			View view = findViewById(R.id.relative_layout);
			view.setFitsSystemWindows(true);
			((ViewGroup) view).setClipToPadding(false);
			SystemBarTintManager tintManager = new SystemBarTintManager(this);
		    tintManager.setNavigationBarTintEnabled(true);
		    tintManager.setTintColor(Color.parseColor("#99000000"));
		}
		
        handleIntent(getIntent());
        
        oldSearchOpened = false;
        
        /*WikipediaClient wikipedia = new WikipediaClient(endpoint);
		String htmlPage = wikipedia.getPage(searchTerm);*/
        
        
        
        Api.lang = PreferenceManager.getDefaultSharedPreferences(this)
        		.getString("language", this.getResources().getConfiguration().locale.getLanguage());
        this.lang = Api.lang;
        Api.wiki = "wikipedia.org";
        this.webView = (WebView) findViewById(R.id.webview);
        mActionBarFindHelper = new ActionBarFindHelper(this, webView);
        this.loadingView = (View) findViewById(R.id.loading_view);
        WebSettings webSettings = this.webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setBuiltInZoomControls(false);
        this.webView.setWebViewClient(new WikipediaWebView(this, loadingView));
        this.webView.addJavascriptInterface(new JsObject(webView, loadingView),
        		"CallToAnAndroidFunction");
        String properUrl;
        if(set == null){
	        if(query == null || query.equals("")){
	        	properUrl = "http://" + this.lang + ".m." + Api.wiki + "/wiki/Main_Page";
	            //this.webView.setWebViewClient(new WikipediaWebView(this, loadingView, "Main Page"));
	        } else{
	        	properUrl = "http://" + this.lang + ".m." + Api.wiki + "/wiki?search=" + query;
	            //this.webView.setWebViewClient(new WikipediaWebView(this, loadingView, query));
	        }
        	this.webView.loadUrl(properUrl);
        } else{
        	//from browser or auto-search or NFC
        	if(!set.contains(".m.")){
	        	int index = set.indexOf('.');
	        	if(index == -1){Log.e("-1", "-1"); return;}
	        	set = set.substring(0, index + 1) + "m." + set.substring(index + 1);
        	}
        	this.webView.loadUrl(set);
        	properUrl = set;
        }
        
        setupNfc(properUrl);
        
	}
	
	@SuppressLint("NewApi")
	private void setupNfc(String properUrl){
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
	        NfcAdapter nfc = NfcAdapter.getDefaultAdapter(this);// (only need to do this once)
	        if (nfc != null) { // in case there is no NFC
	        	NfcHelper nfcHelper = new NfcHelper(properUrl);
	        	// Register callback to set NDEF message
	            nfc.setNdefPushMessageCallback(nfcHelper, this);
	            // Register callback to listen for message-sent success
	            nfc.setOnNdefPushCompleteCallback(nfcHelper, this);
	        }
	   	}
	}
	
	public class JsObject {
    	private View loadingView;
    	private View view;
    	JsObject(View view, View loadingView){this.view = view;this.loadingView = loadingView;}
        @JavascriptInterface
        public void setVisible(){
            runOnUiThread(new Runnable() {
            	
               @Override
               public void run() {
                   view.setVisibility(View.VISIBLE);
               		loadingView.setVisibility(View.INVISIBLE);
               }
           });
        }
    }
	
	@Override
    protected void onNewIntent(Intent intent) {
		setIntent(intent);
        handleIntent(intent);
    }
	
	@SuppressLint("NewApi")
	private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //use the query to search your data somehow
            
            this.query = query;
        } else if(Intent.ACTION_VIEW.equals(intent.getAction())
        		|| NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())){
        	String query = intent.getDataString();
        	
        	this.set = query;
        	/*String query = intent.getDataString();
        	//check if it is from the browser
        	if(intent.getCategories() != null){
	        	for (String s : intent.getCategories()) {
	        	    if(s!=null && s.equals(Intent.CATEGORY_BROWSABLE)){
	        	    	this.set = query;
	        	    	return;
	        	    }
	        	}
        	}
        	
        	this.query = query;*/
        }
    }
	
	@SuppressLint("NewApi")
	@Override
	public void onResume(){
		super.onResume();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		    if(mMenu != null){
		    	MenuItem mi = mMenu.findItem(R.id.search);
		    	mi.collapseActionView();
		    }
		} else{
			//I think it is automatically closed.
		}
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		mMenu = menu;
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
	    
	    
	    
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	    	//remove old
	    	menu.removeItem(R.id.search_old);
	        SearchManager searchManager =
	                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	        SearchView searchView =
	                (SearchView) menu.findItem(R.id.search).getActionView();
	        searchView.setSearchableInfo(
	                searchManager.getSearchableInfo(getComponentName()));
	        searchView.setIconifiedByDefault(false);
	    } else{
	    	//remove new
	    	menu.removeItem(R.id.search);
	    	SearchManager searchManager =
	                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    	searchManager.setOnDismissListener(new OnDismissListener() {
	    		@Override
				public void onDismiss() {
					oldSearchOpened = false;
				}
	    	});
	    }
	    
	    
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    switch (item.getItemId()) {
	        case R.id.search_old:
	        	onSearchRequested();
	            return true;
	        case R.id.search:
	        	//close finder
	        	if(mActionBarFindHelper.isFinderOpen()){
	    			mActionBarFindHelper.closeFinder();
	    		}
	        	return true;
	        case R.id.action_find_on_page:
	        	findOnPage();
	        	return true;
	        case R.id.action_share:
	        	shareArticle();
	        	return true;
	        default:
	            return false;
	    }
	}
	
	@SuppressLint("NewApi")
	@Override
	public boolean onSearchRequested() {
		//close finder
		if(mActionBarFindHelper.isFinderOpen()){
			mActionBarFindHelper.closeFinder();
		}
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		    MenuItem mi = mMenu.findItem(R.id.search);
		    if(mi.isActionViewExpanded()){
		    	mi.collapseActionView();
			} else{
				mi.expandActionView();
		  	}
		    return false;
		} else{
			//onOptionsItemSelected(mMenu.findItem(R.id.search));
			oldSearchOpened = true;
		}
	    return super.onSearchRequested();
	}
	
	@SuppressLint("NewApi")
	private void findOnPage(){
		//close searchview or search dialog
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		    if(mMenu != null){
		    	MenuItem mi = mMenu.findItem(R.id.search);
		    	mi.collapseActionView();
		    }
		} else if(oldSearchOpened) {
			onBackPressed();
		}
		mActionBarFindHelper.getView();
	}
	
	private void shareArticle(){
		Intent sendIntent = new Intent();
		sendIntent.setAction(Intent.ACTION_SEND);
		sendIntent.putExtra(Intent.EXTRA_SUBJECT, webView.getTitle());
		sendIntent.putExtra(Intent.EXTRA_TEXT, webView.getUrl());
		sendIntent.setType("text/plain");
		startActivity(Intent.createChooser(sendIntent, getResources().getText(R.string.share_using)));
	}
	
	@Override
	public void onBackPressed(){
		if(mActionBarFindHelper.isFinderOpen()){
			mActionBarFindHelper.closeFinder();
		}else if(webView.canGoBack()){
            webView.goBack();
        }else{
         	super.onBackPressed();
        }
	}
	
	public void refreshPage(View v){
		finish();
		overridePendingTransition(0, 0);
		startActivity(getIntent());
	}
}
