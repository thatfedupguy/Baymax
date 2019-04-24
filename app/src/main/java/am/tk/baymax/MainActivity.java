package am.tk.baymax;

import android.content.Intent;
import android.os.AsyncTask;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Locale;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity implements AIListener {
    public static final String TAG = "MainActivity";
    TextToSpeech mtts;
    private static final int REQUEST_CODE_SPECCH_INPUT = 100;
    EditText et_user_input;
    RecyclerView rv_conversation;
    ImageButton imageButton;
    ImageButton imageButtonMic;
    ArrayList<ResponseMessage> responseMessageList;
    MessageAdapter messageAdapter;
    private String result1;
    final AIConfiguration config = new AIConfiguration("3868afeaf91c4e36b981ef82aa2fea2b",
            AIConfiguration.SupportedLanguages.English,
            AIConfiguration.RecognitionEngine.System);
    AIService aiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mtts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int result = mtts.setLanguage(Locale.ENGLISH);
                    if(result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED){
                        Log.d(TAG, "onInit: Language not supported");
                    }else{
                         // Nothing
                    }
                }else{
                    Log.d(TAG, "onInit: Unable to Speak");
                }
            }
        });

        aiService = AIService.getService(this, config);

       aiService.setListener(this);

       final AIDataService aiDataService = new AIDataService(config);



        setContentView(R.layout.activity_main);
        et_user_input = findViewById(R.id.et_user_input);
        imageButtonMic =findViewById(R.id.imageButtonMic);
        rv_conversation = findViewById(R.id.rv_conversation);
        imageButton = findViewById(R.id.imageButton);
        responseMessageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(responseMessageList);
        rv_conversation.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_conversation.setAdapter(messageAdapter);
        et_user_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageButtonMic.setVisibility(View.INVISIBLE);
            }
        });
        imageButtonMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speak();
            }
        });
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ResponseMessage message1 = new ResponseMessage(et_user_input.getText().toString(), true);
                responseMessageList.add(message1);
                final AIRequest aiRequest = new AIRequest();
                aiRequest.setQuery(message1.message);
                messageAdapter.notifyDataSetChanged();
                if(!isVisible()){
                    rv_conversation.smoothScrollToPosition(messageAdapter.getItemCount()-1);
                }
                clear();
                //ResponseMessage message2 = new ResponseMessage(et_user_input.getText().toString(), false);
                //responseMessageList.add(message2);
                if(aiRequest==null) {
                    throw new IllegalArgumentException("aiRequest must be not null");
                }

                final AsyncTask<AIRequest, Integer, AIResponse> task =
                        new AsyncTask<AIRequest, Integer, AIResponse>() {
                            private AIError aiError;

                            @Override
                            protected AIResponse doInBackground(final AIRequest... params) {
                                final AIRequest request = params[0];
                                try {
                                    final AIResponse response =    aiDataService.request(request);
                                    // Return response
                                    return response;
                                } catch (final AIServiceException e) {
                                    aiError = new AIError(e);
                                    return null;
                                }
                            }

                            @Override
                            protected void onPostExecute(final AIResponse response) {
                                if (response != null) {
                                    onResult(response);
                                } else {
                                    onError(aiError);
                                }
                            }
                        };
                task.execute(aiRequest);
                imageButtonMic.setVisibility(View.VISIBLE);
            }
        });


    }

    private void speak() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say Something");
        try {
            startActivityForResult(intent, REQUEST_CODE_SPECCH_INPUT);
        }catch (Exception e){
            Toast.makeText(this, "Did not get what you say", Toast.LENGTH_SHORT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){

            case REQUEST_CODE_SPECCH_INPUT:{
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                et_user_input.setText(result.get(0));

            }
        }
    }

    public boolean isVisible(){
        LinearLayoutManager layoutManager = (LinearLayoutManager)rv_conversation.getLayoutManager();
        int positionOfLastVisibleItem = layoutManager.findLastCompletelyVisibleItemPosition();
        int itemCount = rv_conversation.getAdapter().getItemCount();
        return (positionOfLastVisibleItem>=itemCount);
    }
    public void clear(){
        String nothing = "";
        et_user_input.setText(nothing);
    }

    @Override
    public void onResult(AIResponse result) {
        Result result1 = result.getResult();
        ResponseMessage responseMessage = new ResponseMessage(result1.getFulfillment().getSpeech(), false);
        responseMessageList.add(responseMessage);
        mtts.speak(responseMessage.message, TextToSpeech.QUEUE_FLUSH, null);
        messageAdapter.notifyDataSetChanged();
        if(!isVisible()){
            rv_conversation.smoothScrollToPosition(messageAdapter.getItemCount()-1);
        }
    }


    @Override
    public void onError(AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {
        aiService.startListening();

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }
}
