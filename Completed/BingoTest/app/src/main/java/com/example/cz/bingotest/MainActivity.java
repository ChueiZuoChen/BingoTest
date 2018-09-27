package com.example.cz.bingotest;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements FirebaseAuth.AuthStateListener, View.OnClickListener {


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int RC_SIGN_IN = 100;
    private FirebaseAuth auth;
    private TextView nickText;
    private ImageView avatar;
    private Group groupAvatar;
    int[] avartars = {R.drawable.avatar_0,
            R.drawable.avatar_1,
            R.drawable.avatar_2,
            R.drawable.avatar_3,
            R.drawable.avatar_4,
            R.drawable.avatar_5,
            R.drawable.avatar_6};
    private Member member;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final EditText roomEdit = new EditText(MainActivity.this);
                roomEdit.setText("Welcome");
                new AlertDialog.Builder( MainActivity.this)
                        .setTitle("Room name")
                        .setMessage("Please enter your room name")
                        .setView(roomEdit)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String roomName = roomEdit.getText().toString();
                                Room room = new Room(roomName, member);
                                FirebaseDatabase.getInstance()
                                        .getReference("rooms")
                                        .push()
                                        .setValue(room);
                            }
                        }).show();
            }
        });
        auth = FirebaseAuth.getInstance();
        findViews();

    }

    private void findViews() {
        nickText = findViewById(R.id.nickname);
        avatar = findViewById(R.id.avatar);
        groupAvatar = findViewById(R.id.group_avatar);
        groupAvatar.setVisibility(View.GONE);
        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean visible = groupAvatar.getVisibility() == View.GONE ? false : true;
                groupAvatar.setVisibility(visible ? View.GONE : View.VISIBLE);

            }
        });
        findViewById(R.id.avartar_0).setOnClickListener(this);
        findViewById(R.id.avartar_1).setOnClickListener(this);
        findViewById(R.id.avartar_2).setOnClickListener(this);
        findViewById(R.id.avartar_3).setOnClickListener(this);
        findViewById(R.id.avartar_4).setOnClickListener(this);
        findViewById(R.id.avartar_5).setOnClickListener(this);
        findViewById(R.id.avartar_6).setOnClickListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        //Firebase auth
        auth.addAuthStateListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Firebase auth
        auth.removeAuthStateListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_signout:
                auth.signOut();
                break;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        Log.d(TAG, "onAuthStateChanged: "+ firebaseAuth.getUid());
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            final String displayName = user.getDisplayName();
            String uid = user.getUid();

            // store uid/displayname.setValue()
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .child("displayName")
                    .setValue(displayName);
            //uid
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .child("uid")
                    .setValue(uid);

            // setListener on users/uid/
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(uid)
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            member = dataSnapshot.getValue(Member.class);
                            if (member.nickname == null) {
                                showNicknameDialog(displayName);;
                            } else {
                                nickText.setText(member.nickname);
                            }
                            avatar.setImageResource(avartars[member.getAvatarId()]);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
//            //getNickName
//            FirebaseDatabase.getInstance()
//                    .getReference("users")
//                    .child(uid)
//                    .child("nickname")
//                    .addListenerForSingleValueEvent(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                            if (dataSnapshot.getValue() != null) {
//                                String nickname = (String) dataSnapshot.getValue();
//                            } else {
//                                showNicknameDialog(displayName);
//                            }
//
//                        }
//
//                        @Override
//                        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//                        }
//                    });

        } else {
            startActivityForResult(
                    AuthUI.getInstance().createSignInIntentBuilder()
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build()
                            )).setIsSmartLockEnabled(false,true)
                            .build()
                    ,RC_SIGN_IN);

        }
    }

    private void showNicknameDialog(String displayName) {
        final EditText nickEdit = new EditText(this);
        nickEdit.setText(displayName);
        new AlertDialog.Builder(this)
                .setTitle("Nick name")
                .setMessage("Please enter your nick name")
                .setView(nickEdit)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nickname = nickEdit.getText().toString();
                        FirebaseDatabase.getInstance()
                                .getReference("users")
                                .child(auth.getUid())
                                .child("nickname")
                                .setValue(nickname);
                    }
                }).show();
    }

    public void changeNickname(View view) {
        showNicknameDialog(nickText.getText().toString());
    }


    @Override
    public void onClick(View v) {
        if (v instanceof ImageView) {
            int selectedAvartarId = 0;
            switch (v.getId()) {
                case R.id.avartar_1:
                    selectedAvartarId = 1;
                    break;
                case R.id.avartar_2:
                    selectedAvartarId = 2;
                    break;
                case R.id.avartar_3:
                    selectedAvartarId = 3;
                    break;
                case R.id.avartar_4:
                    selectedAvartarId = 4;
                    break;
                case R.id.avartar_5:
                    selectedAvartarId = 5;
                    break;
                case R.id.avartar_6:
                    selectedAvartarId = 6;
                    break;
            }
            FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(auth.getCurrentUser().getUid())
                    .child("avatarId")
                    .setValue(selectedAvartarId);
            groupAvatar.setVisibility(View.GONE);
        }
    }
}















