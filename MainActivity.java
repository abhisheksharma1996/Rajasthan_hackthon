package com.pk.hack5;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private TextToSpeech tts;
    private String name="", source="", dest="", day="", date="", month="", year="";
    private TextView textView;
    static int x = 0;
    private DatabaseReference db;
    int i;
    String s=null;
    String cities[];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getcities();
        restart();
    }

    private void getcities(){
        db = FirebaseDatabase.getInstance().getReference().child("stations");
        db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                s= dataSnapshot.getValue().toString();
                s = s.replaceAll("]","");
                s = s.replaceAll(" ","");
                String str[] = s.split(",");
                cities = new String[str.length-1];
                for(i=1;i<str.length;i++)
                {
                    cities[i-1] = str[i];
                    textView.append("["+cities[i-1]+"]");

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void restart()
    {
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS)
                {
                    int result = tts.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "This Language is not supported");
                    }
                    else
                    {
                        Wlcm();
                        Ask_name();
                    }
                }
                else {
                    Log.e("TTS", "Initilization Failed!");
                }
            }
        });
        findViewById(R.id.microphoneButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listen();
            }
        });
        textView = (TextView)findViewById(R.id.textView);
    }

    private void recognition(String text){
        Log.e("Speech",""+text);
        String[] speech = text.split(" ");

        if(x==1)
        {
            name = speech[0];
            speak("Welcome  "+name);
        }

        if(x==2)
        {
            source = speech[0];
            textView.append(source);
            int a=city_exist(source);
            if(a==1)
                speak("Selected Source Station is  "+source);
            else
            {
                speak("Selected Source Station is  not available. Try another");
                source="";
            }

        }
        if(x==3)
        {
            dest = speech[0];
            textView.append(dest);
            int a=city_exist(dest);
            if(a==1)
                speak("Selected Destination is  "+dest);
            else
            {
                speak("Selected Destination is  not available. Try another");
                dest="";
            }


        }
        if(x==4)
        {
            day = speech[0];
            date = speech[1];
            month = speech[2];
            year = speech[3];
            textView.setText(day+" "+date+" "+month+" "+year);
            speak("Selected Date is  "+day+" "+date+" "+month+" "+year);

        }
        if(text.contentEquals("yes"))
        {
            if(x==-1)
                restart();
            if(x==5)
            {
                speak("Your Ticket has been Canceled. To fill a new Form answer in yes or no");
                x = -1;
            }
            else
            {
                speak("Your Ticket has been Confirmed");
                x = 6;
            }
        }
        if(text.contentEquals("no"))
        {
            if(x==-1)
            {
                speak("Thank You");
                this.finish();
            }
            if(x==6)
            {
                speak("Your Ticket has been Confirmed. To fill a new Form answer in yes or no");
                x = -1;
            }
            else
            {
                speak("Your Ticket has been Canceled");
                x = 5;
                reset();
            }
        }
        if(name!=""&&source==""&&dest==""&&day==""&&month==""&&year==""&&x==1)
            Ask_source();
        else if(name!=""&&source!=""&&dest==""&&day==""&&month==""&&year==""&&x==2)
            Ask_dest();
        else if(name!=""&&source!=""&&dest!=""&&day==""&&month==""&&year==""&&x==3)
            Ask_date();
        else if(name!=""&&source!=""&&dest!=""&&day!=""&&month!=""&&year!=""&&x==4)
            Ask_conf();
    }

    private void speak(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_ADD, null, null);
        }else{
            tts.speak(text, TextToSpeech.QUEUE_ADD, null);
        }
    }

    private void listen(){
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something");

        try {
            startActivityForResult(i, 1);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(MainActivity.this, "Your device doesn't support Speech Recognition", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> res = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                String inSpeech = res.get(0);
                recognition(inSpeech);
            }
        }
    }


    private void Wlcm()
    {
        tts.speak("Welcome to book my journey", TextToSpeech.QUEUE_FLUSH, null);
    }

    private void Ask_name()
    {
        tts.speak("Please tell me your name", TextToSpeech.QUEUE_ADD, null);
        x = 1;
    }
    private void Ask_source()
    {
        tts.speak("Please tell me your Source Station", TextToSpeech.QUEUE_ADD, null);
        x = 2;
    }
    private void Ask_dest()
    {
        tts.speak("Please tell me your Destination", TextToSpeech.QUEUE_ADD, null);
        x = 3;
    }
    private void Ask_date()
    {
        tts.speak("Please tell me the Day and Date of Journey", TextToSpeech.QUEUE_ADD, null);
        x = 4;
    }
    private void Ask_conf()
    {
        tts.speak("Hi "+name+" Your Details are :", TextToSpeech.QUEUE_ADD, null);
        tts.speak("Source Station is "+source, TextToSpeech.QUEUE_ADD, null);
        tts.speak("Destination is "+dest, TextToSpeech.QUEUE_ADD, null);
        tts.speak("Selected Date is  "+day+" "+month+" "+year, TextToSpeech.QUEUE_ADD, null);
        tts.speak("Do you want to Confirm Your Ticket ?", TextToSpeech.QUEUE_ADD, null);
        tts.speak("Answer in yes or no", TextToSpeech.QUEUE_ADD, null);
        x=0;
    }

    private void reset()
    {
        name=""; source=""; dest=""; day=""; date=""; month=""; year="";
    }

    private int city_exist(String city)
    {

        for(int j=0;j<cities.length;j++)
        {
            if(cities[j].equalsIgnoreCase(city))
                return 1;
        }

        return 0;
    }

      @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}
