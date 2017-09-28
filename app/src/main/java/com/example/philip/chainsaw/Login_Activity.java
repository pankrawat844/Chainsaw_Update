package com.example.philip.chainsaw;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class Login_Activity extends AppCompatActivity {
    String url="https://www.facebook.com/v2.6/dialog/oauth?redirect_uri=fb464891386855067%3A%2F%2Fauthorize%2F&scope=user_birthday,user_photos,user_education_history,email,user_relationship_details,user_friends,user_work_history,user_likes&response_type=token%2Csigned_request&client_id=464891386855067";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Button login = (Button) findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
//                    List<ResolveInfo> browserList = getPackageManager().queryIntentActivities(intent, 0);
//                    intent.setPackage(browserList.get(0).activityInfo.packageName);
                startActivity(intent);
            }
        });


        Uri uriData = getIntent().getData();
        if (uriData == null) {

            Log.e("URi data", "uri data is null");

        } else {

            try {
                Log.e("URi data", "uri data is not null" + uriData);


            Map<String, String> queryParams = splitQuery(uriData.toString());
                String accesstoken = queryParams.get("access_token");
                Intent intent= new Intent(Login_Activity.this,MainActivity.class);
                intent.putExtra("access_token",accesstoken);
                startActivity(intent);
            Log.e("Get Auth Token", " params are  " + queryParams.toString());
            Log.e("URi data", "uri data is not null" + accesstoken);

    }catch (Exception e){
                e.printStackTrace();
            }
        }
    }



    public static Map<String, String> splitQuery(String url) throws UnsupportedEncodingException {

        int index  = url.indexOf("#");
        if(index!=-1)
            url = url.substring(index+1);

        Map<String, String> query_pairs = new LinkedHashMap<String, String>();

        String[] pairs = url.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
        }
        return query_pairs;
    }

}
