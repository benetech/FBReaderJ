package org.bookshare.net;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import org.apache.commons.codec.binary.Base64;
import org.benetech.android.BuildConfig;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

/**
 * Created by animal@martus.org on 3/31/16.
 */
public class BookshareHttpOauth2Client {

    public static final String JSON_CODE_KEY = "key";
    public static final String JSON_CODE_ERROR = "ERROR";
    public static final String CODE_RESPONSE_CODE = "responseCode";
    public static final String CODE_RESPONSE_MESSAGE = "responseMessage";
    public static final String CODE_REQUEST_SUCCESS = "codeRequestSuccess";
    private String userName;
    private String password;

    private static final String HOST_NAME = BuildConfig.BOOKSHARE_API_HOST_NAME;
    private static final String API_KEY = "ytvs9pzsd62bv7rzamwdkthe";
    private static final String MASHERY_API_KEY = API_KEY;
    private static final String COLON = ":";
    private static final String MASHERY_PASSWORD = "";
    private static final String MASHERY_REQUEST_HEADER_LOGIN = MASHERY_API_KEY + COLON + MASHERY_PASSWORD;
    private static final String POST_REQUESTE_METHOD = "POST";
    private static final String GET_REQUEST_METHOD = "GET";
    private static final String DELETE_REQUESTE_METHOD = "DELETE";
    private static String URL_AS_STRING = "https://" + HOST_NAME + "/v2/oauth/token?api_key=" + API_KEY;
    private static final String UTF_8 = "UTF-8";

    private static final String READING_LISTS_LIMIT_PARAM = "limit=100";
    private static final String READING_LISTS_URL = "https://" + HOST_NAME + "/v2/lists?"+READING_LISTS_LIMIT_PARAM+"&api_key=" + API_KEY;
    public static final String ACCESS_TOKEN_CODE = "access_token";

    private static final String READINGLIST_ID_REPLACEMENT_TOKEN = "%s";
    private static final String BOOK_TITLES_FOR_READING_LIST_URL = "https://"  + HOST_NAME + "/v2/lists/" + READINGLIST_ID_REPLACEMENT_TOKEN + "/titles?"+READING_LISTS_LIMIT_PARAM+"&api_key=ytvs9pzsd62bv7rzamwdkthe";

    private static final String JSON_CODE_LISTS = "lists";
    private static final String JSON_CODE_ALLOWS = "allows";
    private static final String JSON_CODE_READING_LIST_ID = "readingListId";
    public static final String JSON_CODE_READING_LIST_NAME = "name";
    public static final String JSON_CODE_READING_LIST_LIST = "lists";
    private static final String JSON_CODE_TITLES = "titles";

    public HttpsURLConnection createBookshareApiUrlConnection(String userNameToUse, String passwordToUse) throws Exception {

        userName = userNameToUse;
        password = passwordToUse;

        HttpsURLConnection urlConnection = createHttpsUrlConnection(URL_AS_STRING, POST_REQUESTE_METHOD);
        setMasheryLoginInHeader(urlConnection);
        writeLoginFormParameters(urlConnection);
        urlConnection.connect();

        return urlConnection;
    }

    public JSONObject getReadingLists(String accessToken) throws Exception {
        HttpsURLConnection urlConnection = createConnection(READING_LISTS_URL, accessToken);
        final String rawResponseWithReadingLists = requestData(urlConnection);

        JSONObject readingListJson = new JSONObject(rawResponseWithReadingLists);
        JSONArray readingListsJsonArray = readingListJson.optJSONArray(JSON_CODE_LISTS);
        JSONArray allowsJsonArray = readingListJson.optJSONArray(JSON_CODE_ALLOWS);
        if (readingListsJsonArray == null)
            return new JSONObject();

        JSONArray readingLists = new JSONArray();
        for (int index = 0; index < readingListsJsonArray.length(); ++index) {
            final JSONObject jsonElement = readingListsJsonArray.getJSONObject(index);
            int bookshareReadingListId = jsonElement.getInt(JSON_CODE_READING_LIST_ID);
            String readingListName = jsonElement.optString(JSON_CODE_READING_LIST_NAME);
            JSONObject readingListDetailsJson = getBooksForReadingList(accessToken, bookshareReadingListId);
            if(isValidReadingList(readingListDetailsJson)) {
                readingListDetailsJson.put(JSON_CODE_READING_LIST_NAME, readingListName);
                readingLists.put(readingListDetailsJson);
            }
        }

        JSONObject ans = new JSONObject();

        ans.put(JSON_CODE_ALLOWS, allowsJsonArray);
        ans.put(JSON_CODE_READING_LIST_LIST, readingLists);

        return ans;
    }

    private boolean isValidReadingList(JSONObject readingListDetailsJson) throws JSONException {
        return !(readingListDetailsJson.has(JSON_CODE_KEY)
                && readingListDetailsJson.getString(JSON_CODE_KEY).contains(JSON_CODE_ERROR));
    }

    public boolean postTitleToReadingList(String accessToken, String readingListId, String titleId) throws Exception {

        String url = String.format("https://%s/v2/lists/%s/titles?api_key=%s",HOST_NAME, readingListId, API_KEY);

        HttpsURLConnection urlConnection = createHttpsUrlConnection(url, POST_REQUESTE_METHOD);
        setAccessToken(accessToken, urlConnection);

        LinkedHashMap<String, String> formParameters = new LinkedHashMap<>();
        formParameters.put("bookshareId", titleId);
        writeFormParameters(formParameters, urlConnection);

        int[] responseCode = new int[1];
        final String rawResponseWithReadingLists = requestData(urlConnection, responseCode);

        return responseCode[0] == HttpURLConnection.HTTP_OK;
    }

    public Bundle postReadingList(String accessToken, String readingListName, String description) throws Exception {

        String url = String.format("https://%s/v2/lists/?api_key=%s",HOST_NAME, API_KEY);

        HttpsURLConnection urlConnection = createHttpsUrlConnection(url, POST_REQUESTE_METHOD);
        setAccessToken(accessToken, urlConnection);

        LinkedHashMap<String, String> formParameters = new LinkedHashMap<>();
        formParameters.put("name", readingListName);
        formParameters.put("description", readingListName);
        formParameters.put("access", "private");
        writeFormParameters(formParameters, urlConnection);

        int[] responseCode = new int[1];
        final String rawResponseWithReadingLists = requestData(urlConnection, responseCode);

        Bundle response = new Bundle();
        response.putInt(CODE_RESPONSE_CODE, responseCode[0]);
        response.putString(CODE_RESPONSE_MESSAGE, rawResponseWithReadingLists);
        response.putBoolean(CODE_REQUEST_SUCCESS, responseCode[0] == HttpURLConnection.HTTP_OK);

        return response;
    }

    public boolean deleteTitleFromReadingList(String accessToken, String readingListId, String titleId) throws Exception {

        String url = String.format("https://%s/v2/lists/%s/titles/%s?api_key=%s",HOST_NAME, readingListId, titleId, API_KEY);

        HttpsURLConnection urlConnection = createHttpsUrlConnection(url, DELETE_REQUESTE_METHOD);
        setAccessToken(accessToken, urlConnection);

        int[] responseCode = new int[1];
        final String rawResponseWithReadingLists = requestData(urlConnection, responseCode);

        return responseCode[0] == HttpURLConnection.HTTP_OK;
    }



    private JSONObject getBooksForReadingList(String accessToken, int readingListId) throws Exception {
        String urlWithReadingListId = BOOK_TITLES_FOR_READING_LIST_URL.replace(READINGLIST_ID_REPLACEMENT_TOKEN, Integer.toString(readingListId));
        HttpsURLConnection urlConnection = createConnection(urlWithReadingListId, accessToken);
        final String rawResponseWithReadingLists = requestData(urlConnection);

        return new JSONObject(rawResponseWithReadingLists);
    }

    private HttpsURLConnection createConnection(String url, String accessToken) throws IOException {
        HttpsURLConnection urlConnection = createHttpsUrlConnection(url, GET_REQUEST_METHOD);
        setAccessToken(accessToken, urlConnection);

        return urlConnection;
    }

    private HttpsURLConnection createHttpsUrlConnection(String urlWithReadingListId, String requestMethod) throws IOException {
        final URL url = new URL(urlWithReadingListId);
        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        setHostnameVerifierToAvoidSslHandshakeException(urlConnection);
        urlConnection.setRequestMethod(requestMethod);
        urlConnection.setDoOutput(requestMethod.equals(POST_REQUESTE_METHOD));
        urlConnection.setDoInput(true);

        return urlConnection;
    }

    private void setAccessToken(String accessToken, HttpsURLConnection urlConnection) {
        urlConnection.addRequestProperty("Authorization", "Bearer " + accessToken);
    }

    public String requestData(HttpsURLConnection urlConnection) throws Exception {
        int[] responseCode = new int[1];
        return requestData(urlConnection, responseCode);
    }

    public String requestData(HttpsURLConnection urlConnection, int[] responseCode) throws Exception {
        InputStream inputStream;
        responseCode[0] = urlConnection.getResponseCode();
        if (responseCode[0] < HttpURLConnection.HTTP_BAD_REQUEST) {
            inputStream = urlConnection.getInputStream();
        } else {
            inputStream = urlConnection.getErrorStream();
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String temp, response = "";
        while ((temp = bufferedReader.readLine()) != null) {
            response += temp;
        }

        return response;
    }

    private void setMasheryLoginInHeader(HttpsURLConnection urlConnection) {
        byte[] apiKeyAsByteArray = Base64.encodeBase64(MASHERY_REQUEST_HEADER_LOGIN.getBytes());
        urlConnection.addRequestProperty("Authorization", "Basic " + new String(apiKeyAsByteArray));
    }

    private void setHostnameVerifierToAvoidSslHandshakeException(HttpsURLConnection urlConnection) {
        urlConnection.setHostnameVerifier(new HostnameVerifier()
        {
            public boolean verify(String hostname, SSLSession session)
            {
                System.out.println(hostname + "   " + session.toString());
                return hostname.equals(HOST_NAME);
            }
        });
    }

    private void writeLoginFormParameters(HttpsURLConnection urlConnection) throws IOException {
        OutputStream outputStream = urlConnection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, UTF_8));
        try {
            final String result = createQuery(createFormParameterList());
            writer.write(result);
            writer.flush();
        }
        catch (IOException e){
            Log.e("OauthClient", "Writing parameters to connection crashed", e);
        }
        finally {
            if(writer != null){
                writer.close();
            }
            if(outputStream != null){
                outputStream.close();
            }
        }
    }

    private void writeFormParameters(LinkedHashMap<String, String> formParameterList, HttpsURLConnection urlConnection) throws IOException {

        OutputStream outputStream = urlConnection.getOutputStream();

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, UTF_8));
        final String result = createQuery(formParameterList);
        writer.write(result);
        writer.flush();
        writer.close();

        outputStream.close();

    }


    @NonNull
    private LinkedHashMap<String, String> createFormParameterList() {
        LinkedHashMap<String, String> formParameterList = new LinkedHashMap<>();
        formParameterList.put("grant_type", "password");
        formParameterList.put("username", getUserName());
        formParameterList.put("password", getUserPassword());

        return formParameterList;
    }

    @NonNull
    private String getUserPassword() {
        return password;
    }

    @NonNull
    private String getUserName() {
        return userName;
    }

    private String createQuery(LinkedHashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        LinkedHashSet<String> keys = new LinkedHashSet(params.keySet());
        for (String key : keys) {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(getUrlEncoded(key));
            result.append("=");
            result.append(getUrlEncoded(params.get(key)));
        }

        return   result.toString();
    }

    private String getUrlEncoded(String key) throws UnsupportedEncodingException {
        return URLEncoder.encode(key, UTF_8);
    }
}
