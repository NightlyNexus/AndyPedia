package com.brianco.andypedia;

import android.annotation.SuppressLint;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter.CreateNdefMessageCallback;
import android.nfc.NfcAdapter.OnNdefPushCompleteCallback;
import android.nfc.NfcEvent;

@SuppressLint("NewApi")
public class NfcHelper implements CreateNdefMessageCallback, OnNdefPushCompleteCallback {
	private String properUrl;
	
	public NfcHelper(String properUrl){
		this.properUrl = properUrl;
	}
	
	@Override
	public void onNdefPushComplete(NfcEvent event) {
	}

	@Override
	public NdefMessage createNdefMessage(NfcEvent event) {
		return new NdefMessage(new NdefRecord[] {
			    NdefRecord.createUri(properUrl)
		});
	}

}
