package am.tk.baemax;

import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceContext;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity implements AIListener {
    EditText et_user_input;
    RecyclerView rv_conversation;
    ImageButton imageButton;
    ArrayList<ResponseMessage> responseMessageList;
    MessageAdapter messageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final AIConfiguration config = new AIConfiguration("3868afeaf91c4e36b981ef82aa2fea2b",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        final AIService aiService = AIService.getService(this, config);



       aiService.setListener(this);

       final AIDataService aiDataService = new AIDataService(config);



        setContentView(R.layout.activity_main);
        et_user_input = findViewById(R.id.et_user_input);
        rv_conversation = findViewById(R.id.rv_conversation);
        imageButton = findViewById(R.id.imageButton);
        responseMessageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(responseMessageList);
        rv_conversation.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv_conversation.setAdapter(messageAdapter);
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
            }
        });


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

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }
}
