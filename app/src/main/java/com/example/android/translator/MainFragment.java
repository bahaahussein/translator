package com.example.android.translator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;

/**
 * Created by Bo2o on 1/13/2017.
 */
public class MainFragment extends Fragment {

    private final String TAG = MainFragment.class.getSimpleName();
    private static final String SHARED_PREFS_NAME = "MY_SHARED_PREF";
    private LinkedList<String> saved;
    private TextView mTranslatedText;
    private Button mTranslate;
    private EditText mWord;
    private Button mSave;
    private Button mListen;
    private TextToSpeech mTextToSpeech;

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.go_to_saved) {
            Intent mIntent = new Intent(getActivity(), SavedActivity.class);
            Bundle mBundle = new Bundle();
            mBundle.putStringArray("array",saved.toArray(new String[saved.size()]));
            mIntent.putExtras(mBundle);
            startActivity(mIntent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        saveArray();
        super.onStop();
    }
    public void onDestroy() {
        if (mTextToSpeech != null) {
            mTextToSpeech.stop();
            mTextToSpeech.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        setHasOptionsMenu(true);
        saved = getArray();
        mTextToSpeech = new TextToSpeech(getContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status){
            }
        });
        mTranslatedText = (TextView) rootView.findViewById(R.id.translatedText);
        mTranslate = (Button) rootView.findViewById(R.id.translate);
        mWord = (EditText) rootView.findViewById(R.id.word);
        mListen = (Button) rootView.findViewById(R.id.listen);
        mListen.setVisibility(View.GONE);
        mSave = (Button) rootView.findViewById(R.id.save);
        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mWord.getText()!=null && mWord.getText().length()>0) {
                    if (mTranslatedText.getText() != null && mTranslatedText.getText().length() > 0) {
                        String x = "";
                        if(languageDetector(mWord.getText().toString()))
                            x = mWord.getText().toString() +'\n'+ mTranslatedText.getText().toString();
                        else
                            x = mTranslatedText.getText().toString() +'\n'+ mWord.getText().toString();
                        if (!saved.contains(x)){
                            saved.addFirst(x);
                            Toast.makeText(getContext(), R.string.saved_succesfully, Toast.LENGTH_SHORT).show();
                        }else
                            Toast.makeText(getContext(), R.string.already_saved, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        mTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(haveNetworkConnection())
                    new Translate().execute(mWord.getText().toString());
                else
                    Toast.makeText(getContext(), R.string.check_internet, Toast.LENGTH_SHORT).show();
            }
        });
        mListen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mTranslatedText.getText() != null && mTranslatedText.getText().length() > 0) {
                    if(languageDetector(mTranslatedText.getText().toString()))
                        mTextToSpeech.setLanguage(Locale.US);
                    mTextToSpeech.speak(mTranslatedText.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
        return rootView;
    }
    private boolean haveNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    return true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    return true;
        }
        return false;
    }
    public boolean languageDetector(String x) {
        if(x.length()==0)
            return false;
        switch (x.toLowerCase().charAt(0)) {
            case 'a' :
            case 'b' :
            case 'c' :
            case 'd' :
            case 'e' :
            case 'f' :
            case 'g' :
            case 'h' :
            case 'i' :
            case 'j' :
            case 'k' :
            case 'l' :
            case 'm' :
            case 'n' :
            case 'o' :
            case 'p' :
            case 'q' :
            case 'r' :
            case 's' :
            case 't' :
            case 'u' :
            case 'v' :
            case 'w' :
            case 'x' :
            case 'y' :
            case 'z' : return true;
            case ' ' : return languageDetector(x.substring(1));
            default: return false;
        }
    }
    public boolean saveArray() {
        SharedPreferences sp = getContext().getSharedPreferences(SHARED_PREFS_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor mEdit1 = sp.edit();
        Set<String> set = new HashSet<String>();
        set.addAll(saved);
        mEdit1.putStringSet("list", set);
        return mEdit1.commit();
    }
    public LinkedList<String> getArray() {
        SharedPreferences sp = getContext().getSharedPreferences(SHARED_PREFS_NAME, Activity.MODE_PRIVATE);
        Set<String> set = sp.getStringSet("list", new HashSet<String>());
        return new LinkedList<String> (set);
    }

    class Translate extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {

            if(params==null || params.length==0)
                return null;
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr = null;
            if(languageDetector(params[0]))
            {
                String ur = "http://api.mymemory.translated.net/get?q="+params[0]+"&langpair=en|ar-EG";
                try {
                    URL url = new URL(ur);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        return null;
                    }
                    forecastJsonStr = buffer.toString();
                    try {
                        JSONObject forecastJson = new JSONObject(forecastJsonStr);
                        JSONArray arr = forecastJson.getJSONArray("matches");
                        for(int i=0; i<arr.length(); i++) {
                            if(!languageDetector(arr.getJSONObject(i).getString("translation")))
                                return arr.getJSONObject(i).getString("translation");
                        }
                        return null;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error ", e);
                    return null;
                }
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(TAG, "Error closing stream", e);
                        }
                    }
                }
            } else {
                Uri.Builder builder = new Uri.Builder();
                builder.scheme("http")
                        .authority("api.mymemory.translated.net")
                        .appendPath("get")
                        .appendQueryParameter("q", params[0])
                        .appendQueryParameter("langpair", "ar-EG|en");
                String ur = builder.build().toString();
                try {
                    URL url = new URL(ur);
                    urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setRequestMethod("GET");
                    urlConnection.connect();

                    InputStream inputStream = urlConnection.getInputStream();
                    StringBuffer buffer = new StringBuffer();
                    if (inputStream == null) {
                        return null;
                    }
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }

                    if (buffer.length() == 0) {
                        return null;
                    }
                    forecastJsonStr = buffer.toString();
                    try {
                        JSONObject forecastJson = new JSONObject(forecastJsonStr);
                        JSONArray arr = forecastJson.getJSONArray("matches");
                        for(int i=0; i<arr.length(); i++) {
                            if(languageDetector(arr.getJSONObject(i).getString("translation")))
                                return arr.getJSONObject(i).getString("translation");
                        }
                        return null;
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return null;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error ", e);
                    return null;
                }
                finally {
                    if (urlConnection != null) {
                        urlConnection.disconnect();
                    }
                    if (reader != null) {
                        try {
                            reader.close();
                        } catch (final IOException e) {
                            Log.e(TAG, "Error closing stream", e);
                        }
                    }
                }
            }
        }
        protected void onPostExecute(String strings) {
            if(strings!=null && strings.length()>0) {
                mTranslatedText.setText(strings);
                if(languageDetector(mTranslatedText.getText().toString()))
                    mListen.setVisibility(View.VISIBLE);
                else
                    mListen.setVisibility(View.GONE);
            }
        }
    }
}
