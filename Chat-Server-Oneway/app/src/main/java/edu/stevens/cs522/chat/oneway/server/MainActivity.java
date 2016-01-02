    package edu.stevens.cs522.chat.oneway.server;

    import android.app.Activity;
    import android.content.DialogInterface;
    import android.content.Intent;
    import android.os.Bundle;
    import android.view.View;
    import android.widget.Button;
    import android.widget.EditText;

    import edu.stevens.cs522.chat.oneway.server.ChatApp;
    import edu.stevens.cs522.chat.oneway.server.R;

    /**
     * Created by Sandeep on 3/15/2015.
     */
    public class MainActivity extends Activity implements View.OnClickListener {
        public Button sendButton;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            sendButton = (Button) findViewById(R.id.next);
            sendButton.setOnClickListener(this);
        }

        public void sendMessage(View view) {
            Intent intent = new Intent(this, ChatApp.class);
            EditText editText = (EditText) findViewById(R.id.edit_message);
            String message = editText.getText().toString();
            intent.putExtra(ChatApp.CLIENT_NAME_KEY, message);
            startActivity(intent);
        }

        public void onClick(View view) {
            //call subsequent screen
            switch (view.getId()){
                case R.id.next:
                    sendMessage(view);
                    break;
                default:
            }
        }
    }
