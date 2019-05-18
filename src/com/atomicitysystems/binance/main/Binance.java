package com.atomicitysystems.binance.main;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import com.atomicitysystems.binance.util.Constants;

public class Binance {
	public static void main(String args[]) {
		try {
			// String stringUrl = "https://api.binance.com/api/v1/exchangeInfo";
			String epochMilli = String.valueOf(Instant.now().toEpochMilli());
			HashMap<String, String> payLoad = new HashMap<String, String>();
			payLoad.put("symbol", "LTCBTC");
			payLoad.put("side", "BUY");
			payLoad.put("type", "LIMIT");
			payLoad.put("timeInForce", "GTC");
			payLoad.put("quantity", "1");
			payLoad.put("price", "0.1");
			payLoad.put("recvWindow", "5000");
			payLoad.put("timestamp", epochMilli);
			String payLoadStr = concatPayLoad(payLoad);
			String signature = hmacSHA256(Constants.secret, payLoadStr);
			payLoad.put("signature", signature);
			String output;
			/*
			 * output=doPost(Constants.baseUrl,Constants.action_order,payLoad);
			 * System.out.println("output is"+output);
			 */
			output = doGet(Constants.baseUrlUnsigned, "exchangeInfo");
			System.out.println("output is:" + output);
			JSONObject jsonObj = new JSONObject(output);
			JSONArray symbols = (JSONArray) jsonObj.get("symbols");
			for (int i = 0; i < symbols.length(); i++) {
				try {
					JSONObject s = (JSONObject) symbols.get(i);
					String s1 = (String) s.get("symbol");
					if (s1.substring(3, 6).equals("BTC"))
						System.out.println(s1.substring(0, 3) + " to " + s1.substring(3, 6));
				} catch (Exception e) {
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String hmacSHA256(String secret, String payLoadStr) throws Exception {
		Mac sha256_HMAC = Mac.getInstance(Constants.HmacSHA256);
		SecretKeySpec secret_key = new SecretKeySpec(Constants.secret.getBytes(Constants.UTF_8), Constants.HmacSHA256);
		sha256_HMAC.init(secret_key);
		return Hex.encodeHexString(sha256_HMAC.doFinal(payLoadStr.getBytes(Constants.UTF_8)));
	}

	public static String doPost(String baseUrl, String action) throws IOException {
		return doPost(baseUrl, action, null);
	}

	public static String doPost(String baseUrl, String action, HashMap<String, String> payLoad) throws IOException {
		HttpClient httpclient = HttpClients.createDefault();
		HttpPost httppost = new HttpPost(baseUrl + action);
		httppost.setHeader(Constants.X_MBX_APIKEY, Constants.id);
		// Request parameters and other properties.
		if (payLoad != null) {
			List<NameValuePair> params = new ArrayList<NameValuePair>(payLoad.size());
			for (String s : payLoad.keySet())
				params.add(new BasicNameValuePair(s, payLoad.get(s)));
			httppost.setEntity(new UrlEncodedFormEntity(params, Constants.UTF_8));
		}
		// Execute and get the response.
		HttpResponse response = httpclient.execute(httppost);
		HttpEntity entity = response.getEntity();
		InputStream instream = entity.getContent();
		String theString = IOUtils.toString(instream, Constants.UTF_8);
		instream.close();
		return theString;
	}

	public static String doGet(String baseUrl, String action) throws IOException {
		return doGet(baseUrl, action, null);
	}

	public static String doGet(String baseUrl, String action, HashMap<String, String> payLoad) throws IOException {
		HttpClient httpclient = HttpClients.createDefault();
		HttpGet httpget = new HttpGet(baseUrl + action);
		// /httpget.setHeader(Constants.X_MBX_APIKEY, Constants.id);
		// Request parameters and other properties.
		if (payLoad != null) {
			List<NameValuePair> params = new ArrayList<NameValuePair>(payLoad.size());
			for (String s : payLoad.keySet())
				params.add(new BasicNameValuePair(s, payLoad.get(s)));
		}
		// Execute and get the response.
		HttpResponse response = httpclient.execute(httpget);
		HttpEntity entity = response.getEntity();
		InputStream instream = entity.getContent();
		String theString = IOUtils.toString(instream, Constants.UTF_8);
		instream.close();
		return theString;
	}

	public static String concatPayLoad(HashMap<String, String> payLoad) {
		String payLoadStr = "";
		int i = 0;
		for (String s : payLoad.keySet()) {
			payLoadStr += s + "=" + payLoad.get(s);
			if (i++ < payLoad.size() - 1)
				payLoadStr += "&";
		}
		return payLoadStr;
	}
}