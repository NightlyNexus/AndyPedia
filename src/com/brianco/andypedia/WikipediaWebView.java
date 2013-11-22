package com.brianco.andypedia;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

class WikipediaWebView extends WebViewClient{
	private ActionBarActivity mActivity;
	private View loadingView;
	private boolean shouldStartNewActivity;
	private boolean didError = false;
	
	public WikipediaWebView(ActionBarActivity activity, View loadingView){
		mActivity = activity;
		this.loadingView = loadingView;
		this.shouldStartNewActivity = false;
	}
	
	@Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
		didError = true;
        try {
			//TODO:view.loadData(Api.getContent(getResources().openRawResource(R.raw.noresult)), "text/html", "utf-8");
        	//TODO:view.loadUrl("file:///android_asset/myerrorpage.html");
        	mActivity.findViewById(R.id.refresh_page).setVisibility(View.VISIBLE);
       		loadingView.setVisibility(View.INVISIBLE);
		} catch (Exception e) {
			view.loadData("Error !", "text/html", "utf-8");
		}
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
    	if(didError){return;}
    	view.setVisibility(View.INVISIBLE);
    	loadingView.setVisibility(View.VISIBLE);
        mActivity.getSupportActionBar().setTitle(mActivity.getString(R.string.app_name));
        //this.progressDialog = ProgressDialog.show(MainActivity.this, "", getResources().getText(R.string.loading), true);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
    	if(didError){return;}
        mActivity.getSupportActionBar().setTitle(view.getTitle());
      	shouldStartNewActivity = true;
        /*view.loadUrl("javascript:" +
            "$('#searchbox').remove();" +
            "$('#nav').remove();" +
            "$('#footmenu').remove();"
        );*/
        /*view.loadUrl("javascript:(function() { " +
                "document.getElementsByTagName('content')[0].style.display=\"none\"; " +
                "})()");*/
        /*view.loadUrl("javascript:" +
        		"$(\"div:not(.mainpage)\").remove();"
            );*/
        /*view.loadUrl("javascript:"
        		+ "$('.header').hide();"
        		+ "$('#section_0').hide();"
        		+ "$('#page_actions').hide();"
        		+ "$('.languageSelector').hide();"
        		+ "$('#mw-mf-last-modified').hide();"
        		+ "$('#footer').hide();");*///works, too
        view.loadUrl("javascript:"
        		+ "var FunctionOne = function () {"
        		+ "  var r = $.Deferred();"
        		+ "  try{document.getElementById('siteNotice').style.display='none';}catch(e){}"
        		+ "  try{document.getElementsByClassName('header')[0].style.display='none';}catch(e){}"
        		+ "  try{document.getElementById('section_0').style.display='none';}catch(e){}"
        		+ "  try{document.getElementById('page-actions').style.display='none';}catch(e){}"
        		+ "  try{document.getElementsByClassName('languageSelector')[0].style.display='none';}catch(e){}"
        		+ "  try{document.getElementById('mw-mf-last-modified').style.display='none';}catch(e){}"
        		+ "  try{document.getElementById('footer').style.display='none';}catch(e){}"
        		+ "  setTimeout(function () {"
        		+ "    r.resolve();"
        		+ "  }, 2500);"
        		+ "  return r;"
        		+ "};"
        		+ "var FunctionTwo = function () {"
        		+ "  window.CallToAnAndroidFunction.setVisible();"
        		+ "};"
        		+ "FunctionOne().done(FunctionTwo);");
        /*view.loadUrl("javascript:"
        		+ "document.getElementsByClassName('header')[0].style.display='none';"
        		+ "document.getElementById('section_0').style.display='none';"
        		+ "document.getElementById('page-actions').style.display='none';"
        		+ "document.getElementsByClassName('languageSelector')[0].style.display='none';"
        		+ "document.getElementById('mw-mf-last-modified').style.display='none';"
        		+ "document.getElementById('footer').style.display='none';"
        		+ "window.CallToAnAndroidFunction.setVisible();");*/
        //this.progressDialog.dismiss();
    	//loadingView.setVisibility(View.INVISIBLE);
    	//view.setVisibility(View.VISIBLE);
    }
    
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if(!url.contains(".wikipedia.org")){
        	Intent i = new Intent(Intent.ACTION_VIEW);
        	i.setData(Uri.parse(url));
            mActivity.startActivity(i);
            view.reload();
            return true;
        } else{
        	if(shouldStartNewActivity){
	        	//just make new instance of activity because you clicked on a link
	        	Intent intent = new Intent(Intent.ACTION_VIEW);
	        	intent.setData(Uri.parse(url));
	        	intent.setClass(mActivity, MainActivity.class);
	        	mActivity.startActivity(intent);
        	} else{
        		//if it was just initialized
	          	view.loadUrl(url);
        	}
     	}
    	return true;
	}
}
