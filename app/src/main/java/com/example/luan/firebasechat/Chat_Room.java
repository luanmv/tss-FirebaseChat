package com.example.luan.firebasechat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Luís on 24/04/2017.
 */

public class Chat_Room extends AppCompatActivity {

    private EditText input_msg;
    private TextView chat_conversation;

    private String user_name;
    private DatabaseReference root;
    private String temp_key;
    private String room_name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_room);

        Button btn_send_msg = (Button) findViewById(R.id.btn_send);
        input_msg = (EditText) findViewById(R.id.msg_input);
        chat_conversation = (TextView) findViewById(R.id.textView);

        user_name = getIntent().getExtras().get("user_name").toString();
        room_name = getIntent().getExtras().get("room_name").toString();
        setTitle(" Chat - " + room_name);

        root = FirebaseDatabase.getInstance().getReference().child(room_name);

        btn_send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Map<String, Object> map = new HashMap<>();
                temp_key = root.push().getKey();
                root.updateChildren(map);

                DatabaseReference message_root = root.child(temp_key);
                Map<String, Object> map2 = new HashMap<>();
                map2.put("name", user_name);
                map2.put("msg", input_msg.getText().toString());

                message_root.updateChildren(map2);
                input_msg.setText("");

            }
        });

        root.addChildEventListener(new ChildEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
               /*
                * A DataSnapshot instance contains data from a Firebase Database location.
                * Any time you read Database data, you receive the data as a DataSnapshot.
               */
                append_chat_conversation(dataSnapshot);
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                append_chat_conversation(dataSnapshot);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void append_chat_conversation(DataSnapshot dataSnapshot) {
        Iterator i = dataSnapshot.getChildren().iterator();
        while (i.hasNext()) {

            String chat_msg = (String) ((DataSnapshot) i.next()).getValue();
            String chat_user_name = (String) ((DataSnapshot) i.next()).getValue();

            chat_conversation.append(chat_user_name + " : " + chat_msg + " \n");

            if (!i.hasNext() && !Objects.equals(user_name, chat_user_name)) {
                ShowNotification(chat_user_name, chat_msg);
            }

        }
    }

    /* Muestra notificaciones */
    private void ShowNotification(String title, String body) {
        Intent intent = new Intent(getApplicationContext(), Chat_Room.class);
        intent.putExtra("room_name", room_name);
        intent.putExtra("user_name", user_name);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder notificationBuilder = (NotificationCompat.Builder) new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setSound(sound)
                .setContentIntent(pendingIntent);

        NotificationManager notification = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notification.notify(0, notificationBuilder.build());

    }
}