package edu.stevens.cs522.chat.oneway.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Sandeep Joshi on 2/11/2015.
 */
public class Parent extends Activity implements View.OnClickListener {

    public static String name = "Name";

    private String parent;
    private EditText editText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent);

        editText  = (EditText)findViewById(R.id.parent);
        Button   sendbt     = (Button)findViewById(R.id.send);

        sendbt.setOnClickListener(Parent.this);

    }

    public void onClick(View v) {
        // do something
        switch (v.getId()){
            case R.id.send:
                parent = new String(editText.getText().toString());

                Intent intent = new Intent(Parent.this, ChatClient.class);
                intent.putExtra(name,parent);
                startActivity(intent);
                break;
        }
    }
}



