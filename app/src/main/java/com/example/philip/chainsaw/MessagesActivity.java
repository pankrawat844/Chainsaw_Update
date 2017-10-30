package com.example.philip.chainsaw;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.inputmethodservice.Keyboard;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.philip.chainsaw.Timer.MessageTimer;
import com.example.philip.chainsaw.Timer.UnmatchTimer;
import com.example.philip.chainsaw.Timer.UpdateTimer;
import com.example.philip.chainsaw.adapters.MatchAdapter;
import com.example.philip.chainsaw.apis.TinderServiceRetrofit;
import com.example.philip.chainsaw.apis.TinderServiceVolley;
import com.example.philip.chainsaw.interfaces.CallBack;
import com.example.philip.chainsaw.model.Match;
import com.example.philip.chainsaw.model.Message;
import com.example.philip.chainsaw.model.Rec;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MessagesActivity extends AppCompatActivity {
    private RelativeLayout loadingLayout;
    private EditText searchField;
    private RecyclerView lw;

    private String tinderToken;
    MatchAdapter mAdapter;
    private ArrayList<Match> matches;
    private ArrayList<String> zeromsgmatches=new ArrayList<>();
    private ArrayList<String> msgarr=new ArrayList<>();
    private ArrayList<String> finalarr=new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        msgarr.add("Hi Dear");
        msgarr.add("Hi, How r you");
        msgarr.add("Can We chat?");
        msgarr.add("I like your Dp");
        msgarr.add("Hi,What r you doing?");
        lw = (RecyclerView) findViewById(R.id.messageList);
        RecyclerView.LayoutManager manager= new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        lw.setLayoutManager(manager);
        lw.addOnItemTouchListener(new RecyclerItemClickListener(getApplicationContext(), lw, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                try {
                    Match m = (Match) matches.get(position);
                    Log.d("PDBug", "onItemClick: " + m.getName());
                    Intent i = new Intent(getApplicationContext(), ChatActivity.class);
                    i.putExtra("TINDER_TOKEN", tinderToken);
                    i.putExtra("NAME", m.getName());
                    i.putExtra("MATCH_ID", m.getMatchId());
                    i.putExtra("PHOTO_URL", m.getPhotoUrl());
                    i.putExtra("MESSAGES", m.getMessages());
                    startActivity(i);
                }catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {

            }
        }));
//        lw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                try {
//                    Match m = (Match) parent.getAdapter().getItem(position);
//                    Log.d("PDBug", "onItemClick: " + m.getName());
//                    Intent i = new Intent(getApplicationContext(), ChatActivity.class);
//                    i.putExtra("TINDER_TOKEN", tinderToken);
//                    i.putExtra("NAME", m.getName());
//                    i.putExtra("MATCH_ID", m.getMatchId());
//                    i.putExtra("PHOTO_URL", m.getPhotoUrl());
//                    i.putExtra("MESSAGES", m.getMessages());
//                    startActivity(i);
//                }catch (Exception e)
//                {
//                    e.printStackTrace();
//                }
//            }
        //});
        startService(new Intent(this, UpdateTimer.class));
        loadingLayout = (RelativeLayout) findViewById(R.id.loadingLayout);
        tinderToken = getIntent().getStringExtra("TINDER_TOKEN");
        matches = new ArrayList<>();
        getmsgwithservice();

            searchField = (EditText) findViewById(R.id.search_field);
        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchMatches(s.toString());
            }
        });

//        Timer timer= new Timer();
//        timer.scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                getmsg();
//            }
//        },0,5000);

    }

    @Override
    protected void onResume() {
            super.onResume();
        IntentFilter filter= new IntentFilter("updatereciver");
        registerReceiver(updatereciver,filter);
        IntentFilter filter1= new IntentFilter("sendmsgreciver");
        registerReceiver(msgrecevier,filter1);
        IntentFilter filter2= new IntentFilter("unmatchreciver");
        registerReceiver(unmatchrecevier,filter2);

    }
     public BroadcastReceiver updatereciver= new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             try {
                 Log.e("Broadcas Reciver","on Updatereiver");
              //   Toast.makeText(getApplicationContext(),"service called",Toast.LENGTH_SHORT).show();

                 addMatcheswithservice(new JSONObject(intent.getStringExtra("response")));
             }catch (Exception e)
             {
                 e.printStackTrace();
             }
         }
     };

     public BroadcastReceiver msgrecevier =new BroadcastReceiver() {
         @Override
         public void onReceive(Context context, Intent intent) {
             Log.e("Broadcas Reciver","on sendmsgreciver");
             try {
                 checkmsg(new JSONObject(intent.getStringExtra("response")));

             }catch (Exception e)
             {
                 e.printStackTrace();
             }

         }
     };

    public BroadcastReceiver unmatchrecevier =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("Broadcas Reciver","on unmatchrecevier");
            try {
                unmatch(new JSONObject(intent.getStringExtra("response")));

            }catch (Exception e)
            {
                e.printStackTrace();
            }

        }
    };

    public void getmsg(){
        TinderServiceVolley.getInstance(getApplicationContext()).getMessages(tinderToken, new CallBack() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.e("response getmsg",response.toString());
                addMatches(response);
            }

            @Override
            public void onFail(String msg) {

            }
        });
    }
    public void getmsgwithservice(){
        TinderServiceVolley.getInstance(getApplicationContext()).getMessages(tinderToken, new CallBack() {
            @Override
            public void onSuccess(JSONObject response) {
                Log.e("response getmsgwithservice",response.toString());
                addMatcheswithservice(response);
            }

            @Override
            public void onFail(String msg) {

            }
        });
    }
    public void addMatches(JSONObject response) {
        try {
            Log.e("addMatches","addMatches");
            String photo_url;
            matches.clear();
            JSONArray matchesJson = response.getJSONArray("matches");
            for (int i = 0; i < matchesJson.length(); i++) {
                String matchId = matchesJson.getJSONObject(i).getString("_id");
                JSONArray photo_url_arr=matchesJson.getJSONObject(i).getJSONObject("person").getJSONArray("photos");
                if(photo_url_arr.length()>0) {

                    photo_url=  photo_url_arr.getJSONObject(0).getJSONArray("processedFiles").getJSONObject(3).getString("url");
                    Log.e("url..............", photo_url);
                }else
                {
                    photo_url="";
                    Log.e("url..............", "nothing");

                }
                String name=matchesJson.getJSONObject(i).getJSONObject("person").getString("name");

                JSONArray messagesJson = matchesJson.getJSONObject(i).getJSONArray("messages");
                ArrayList<Message> messages = new ArrayList<>();

                    for (int j = 0; j < messagesJson.length(); j++) {
                        //Build messages here
                        JSONObject messageJson = messagesJson.getJSONObject(j);
                        Date date = new Date(messageJson.getLong("timestamp"));
                        Message message = new Message(messageJson.getString("from"), messageJson.getString("message"), date);
                        messages.add(message);
                    }

                //Matches are sorted depending on who likes first
                //Check for my id within the first 24 characters and substring accordingly
                String userId;
                if (matchId.substring(0, 24).equals("58cb16dd5ac3aa7e03bc6b12")) {
                    userId = matchId.substring(24, 48);
                } else {
                    userId = matchId.substring(0, 24);
                }
                Match match = new Match(userId, matchId, messages);
                match.setName(name);
                match.setPhotoUrl(photo_url);
                matches.add(match);
                //Log.d("PDBug", "onResponseUserID: "+match.getUserId() + " length" + match.getUserId().length());
                //Scalable only 10 at a time and dynamically add to the adapter?
                //Slow loading?
            }
            loadingLayout.setVisibility(View.GONE);

            mAdapter = new MatchAdapter(getApplicationContext(),  matches);
//

            mAdapter.notifyDataSetChanged();
            lw.setAdapter(mAdapter);
          //  getUsers();

            }   catch (JSONException ex) {
            Log.d("PDBug", "getMatches: "+ex.getLocalizedMessage());
        }
    }
    public void addMatcheswithservice(JSONObject response) {
        try {
            Log.e("addMatcheswithservice","addMatcheswithservice");
            String photo_url="";
            matches.clear();
            zeromsgmatches.clear();
            JSONArray matchesJson = response.getJSONArray("matches");
            for (int i = 0; i < matchesJson.length(); i++) {
                String matchId = matchesJson.getJSONObject(i).getString("_id");
                JSONArray photo_url_arr=matchesJson.getJSONObject(i).getJSONObject("person").getJSONArray("photos");
                if(photo_url_arr.length()>0) {

                   photo_url=  photo_url_arr.getJSONObject(0).getJSONArray("processedFiles").getJSONObject(3).getString("url");
                    Log.e("url..............", photo_url);
                }else
                {
                    photo_url="http://images.gotinder.com/0001unknown/84x84_pct_0_0_100_100_unknown.jpg";
                    Log.e("url..............", "nothing");

                }
                String name=matchesJson.getJSONObject(i).getJSONObject("person").getString("name");

                JSONArray messagesJson = matchesJson.getJSONObject(i).getJSONArray("messages");
                ArrayList<Message> messages = new ArrayList<>();
                if(messagesJson.length()==0){
                    zeromsgmatches.add(matchId);
                }else {
                    for (int j = 0; j < messagesJson.length(); j++) {
                        //Build messages here
                        JSONObject messageJson = messagesJson.getJSONObject(j);
                        Date date = new Date(messageJson.getLong("timestamp"));
                        Message message = new Message(messageJson.getString("from"), messageJson.getString("message"), date);
                        messages.add(message);
                    }
                }
                //Matches are sorted depending on who likes first
                //Check for my id within the first 24 characters and substring accordingly
                String userId;
                if (matchId.substring(0, 24).equals("58cb16dd5ac3aa7e03bc6b12")) {
                    userId = matchId.substring(24, 48);
                } else {
                    userId = matchId.substring(0, 24);
                }
                Match match = new Match(userId, matchId, messages);
                match.setName(name);
                match.setPhotoUrl(photo_url);
                matches.add(match);
                //Log.d("PDBug", "onResponseUserID: "+match.getUserId() + " length" + match.getUserId().length());
                //Scalable only 10 at a time and dynamically add to the adapter?
                //Slow loading?
            }
            loadingLayout.setVisibility(View.GONE);

            mAdapter = new MatchAdapter(getApplicationContext(),  matches);
//

            mAdapter.notifyDataSetChanged();
            lw.setAdapter(mAdapter);
            //getUserswithservice();
            if(zeromsgmatches.size()!=0){
                stopService(new Intent(this,UpdateTimer.class));
                startService(new Intent(this, MessageTimer.class));
            }else
            {
                startService(new Intent(this, UpdateTimer.class));

            }
        }   catch (JSONException ex) {
            Log.d("PDBug", "getMatches: "+ex.getLocalizedMessage());
        }
    }
    public void checkmsg(JSONObject response) {
        try {
            JSONArray matchesJson = response.getJSONArray("matches");
            for (int i = 0; i < matchesJson.length(); i++) {
                String matchId = matchesJson.getJSONObject(i).getString("_id");

                JSONArray messagesJson = matchesJson.getJSONObject(i).getJSONArray("messages");
                ArrayList<Message> messages = new ArrayList<>();
                    if(messagesJson.length()!=0) {
                        if (zeromsgmatches.contains(matchId)) {
                            zeromsgmatches.remove(matchId);
                        }
                    }



            }
            Log.e("zero msg arr",zeromsgmatches.toString());
            Set<String> hs = new HashSet<>();
                hs.addAll(zeromsgmatches);
                finalarr.addAll(hs);
            for(int j=0;j<finalarr.size();j++)
            {
                Random random=new Random();
                int ran=random.nextInt(5);
                TinderServiceVolley.getInstance(this).sendMessage(finalarr.get(j),tinderToken,msgarr.get(ran));
            }

            stopService(new Intent(this,MessageTimer.class));
            startService(new Intent(this, UnmatchTimer.class));
        }   catch (JSONException ex) {
            Log.d("PDBug", "getMatches: "+ex.getLocalizedMessage());
        }
    }


    private void unmatch(JSONObject response){
            try {
                JSONArray matchesJson = response.getJSONArray("matches");
                for (int i = 0; i < matchesJson.length(); i++) {
                    String matchId = matchesJson.getJSONObject(i).getString("_id");

                    JSONArray messagesJson = matchesJson.getJSONObject(i).getJSONArray("messages");
                    ArrayList<Message> messages = new ArrayList<>();
                 //   if(messagesJson.length()!=0) {
                        if (finalarr.contains(matchId)) {
                            for (int j = 0; j < messagesJson.length(); j++)
                            {
                                if(finalarr.contains(messagesJson.getJSONObject(j).getString("from")))
                                {
                                    finalarr.remove(messagesJson.getJSONObject(j).getString("from"));
                                }

                            }
                        }
                  //  }




                    }

                for(int j=0;j<finalarr.size();j++)
                {

                    TinderServiceVolley.getInstance(this).unmatch(finalarr.get(j),tinderToken,"");
                }
                finalarr.clear();
                stopService(new Intent(this,UnmatchTimer.class));

                startService(new Intent(this, UpdateTimer.class));
            }   catch (JSONException ex) {
                Log.d("PDBug", "getunmatchfunc: "+ex.getLocalizedMessage());
            }

    }
    public void getUsers() {
        for (int i = 0; i < matches.size(); i++) {
            Log.d("PDBus", "getUsers: "+matches.size());
            final int iterator = i;
            TinderServiceVolley.getInstance(getApplicationContext()).getUser(matches.get(i), tinderToken, new CallBack() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        //Log.d("PDBug", "onResponseGetUser: " + response.toString());
                        JSONObject results = response.getJSONObject("results");
                        String name = results.getString("name");
                        JSONArray photosArray = results.getJSONArray("photos");
                       // String photoUrl = "http://images.gotinder.com/" + matches.get(iterator).getUserId() + "/" + photosArray.getJSONObject(5).getString("fileName");
                        String photoUrl = photosArray.getJSONObject(5).getString("url");

                        matches.get(iterator).setName(name);
                        matches.get(iterator).setPhotoUrl(photoUrl);
                        if (iterator == (matches.size()-1)) {
                            searchField.setEnabled(true);

                            loadingLayout.setVisibility(View.GONE);
//                            mAdapter.registerDataSetObserver(new DataSetObserver() {
//                                @Override
//                                public void onChanged() {
//                                    update();
//                                }
//                            });

                            mAdapter = new MatchAdapter(getApplicationContext(), matches);

                                mAdapter.notifyDataSetChanged();
                                lw.setAdapter(mAdapter);

                        }
                    } catch (JSONException ex) {
                        Log.d("PDBug", "addUser: "+ex.getLocalizedMessage());
                    }
                }

                @Override
                public void onFail(String msg) {

                }
            });
        }

    }
    public void getUserswithservice() {
        for (int i = 0; i < matches.size(); i++) {
            Log.d("PDBus", "getUsers: "+matches.size());
            final int iterator = i;
            TinderServiceVolley.getInstance(getApplicationContext()).getUser(matches.get(i), tinderToken, new CallBack() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        //Log.d("PDBug", "onResponseGetUser: " + response.toString());
                        JSONObject results = response.getJSONObject("results");
                        String name = results.getString("name");
                        JSONArray photosArray = results.getJSONArray("photos");
                        String photoUrl = "http://images.gotinder.com/" + matches.get(iterator).getUserId() + "/" + photosArray.getJSONObject(0).getString("fileName");
                        matches.get(iterator).setName(name);
                        matches.get(iterator).setPhotoUrl(photoUrl);
                        if (iterator == (matches.size()-1)) {
                            searchField.setEnabled(true);

                            loadingLayout.setVisibility(View.GONE);
                            mAdapter = new MatchAdapter(getApplicationContext(),  matches);
//                            mAdapter.registerDataSetObserver(new DataSetObserver() {
//                                @Override
//                                public void onChanged() {
//                                    update();
//                                }
//                            });

                            mAdapter.notifyDataSetChanged();
                            lw.setAdapter(mAdapter);

                        }
                    } catch (JSONException ex) {
                        Log.d("PDBug", "addUser: "+ex.getLocalizedMessage());
                    }
                }

                @Override
                public void onFail(String msg) {

                }
            });
        }

    }
    public void searchMatches(String search) {
        ArrayList<Match> searchResults = new ArrayList<>();
        for (int i = 0; i < matches.size(); i++) {
            String name = matches.get(i).getName();
            if (search.length() <= name.length()) {
                String subName = name.substring(0, search.length());
                if (search.equalsIgnoreCase(subName)) {
                    Log.d("PDBug", "searchMatches: " + matches.get(i).getName());
                    searchResults.add(matches.get(i));
                }
            }
        }
        mAdapter = new MatchAdapter(getApplicationContext(),  searchResults);
        lw.setAdapter(mAdapter);
    }

    public void update() {
        lw.setAdapter(mAdapter);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        unregisterReceiver(updatereciver);
        unregisterReceiver(msgrecevier);
        unregisterReceiver(unmatchrecevier);

    }

    public void goBack(View v) {
        finish();
        unregisterReceiver(updatereciver);
        unregisterReceiver(msgrecevier);
        unregisterReceiver(unmatchrecevier);

    }


}
