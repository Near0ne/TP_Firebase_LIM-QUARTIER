package io.gresse.hugo.tp3;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import android.view.MenuItem;
import android.view.Menu;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.app.AlertDialog;
import android.content.DialogInterface;

import java.util.ArrayList;
import java.util.List;

/**
 * First app open
 * 1. blank SP
 * 2. MainActivty avec SP vide
 *import android.app.AlertDialog.Builder;

 *
 *
 * Display a simple chat connected to Firebase
 */
public class MainActivity extends AppCompatActivity implements ValueEventListener, MessageAdapter.Listener {

    public static final String TAG = MainActivity.class.getSimpleName();

    EditText       mInputEditText;
    ImageButton    mSendButton;
    MessageAdapter mMessageAdapter;
    static User mUser;

    DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!UserStorage.isUserLoggedIn(this)) {
            Intent intent = new Intent(this, NamePickerActivity.class);
            this.startActivity(intent);
            finish();
        }


        setContentView(R.layout.activity_main);

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        mInputEditText = findViewById(R.id.inputEditText);
        mSendButton = findViewById(R.id.sendButton);

        mUser = UserStorage.getUserInfo(this);
        Toast.makeText(this, "Mail: " + mUser.email, Toast.LENGTH_SHORT).show();
        mMessageAdapter = new MessageAdapter(this, new ArrayList<Message>(), mUser);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mMessageAdapter);

        connectAndListenToFirebase();

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitNewMessage(mInputEditText.getText().toString());
                mInputEditText.setText("");
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDatabaseReference.removeEventListener(this);
    }

    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
        List<Message> items = new ArrayList<>();
        for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
            Message message = messageSnapshot.getValue(Message.class);
            message.key = messageSnapshot.getKey();
            items.add(message);
        }
        mMessageAdapter.setData(items);
        RecyclerView recyclerView =  findViewById(R.id.recyclerView);
        recyclerView.scrollToPosition(mMessageAdapter.getItemCount() - 1);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
        Toast.makeText(this, "Error: " + databaseError, Toast.LENGTH_SHORT).show();

    }

    private void connectAndListenToFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mDatabaseReference = database.getReference(Constant.FIREBASE_PATH);

        mDatabaseReference.addValueEventListener(this);
    }

    private void submitNewMessage(String message) {
        User user = UserStorage.getUserInfo(this);
        if (message.isEmpty() || user == null) {
            Toast.makeText(getApplicationContext(),"user NULL",Toast.LENGTH_LONG).show();
            return;
        }
        DatabaseReference newData  = mDatabaseReference.push();
        newData.setValue(
                new Message(message,
                        user.name,
                        user.email,
                        System.currentTimeMillis()));
    }

    @Override
    public void onItemClick(int position, Message message) {
        mDatabaseReference.child(message.key).removeValue();
    }

    @Override
    public void onItemLongClick(int position, Message message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        CharSequence listeOptions[] = getResources().getStringArray(R.array.longPressList);
        builder.setTitle("Options");
        final Message tempo = message;
        builder.setItems(listeOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch(which) {
                    case 0:
                        mDatabaseReference.child(tempo.key).removeValue();
                        break;
                    case 1:

                        break;
                    default:

                }
            }
        });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout:
                UserStorage.logOut(this);
                Toast.makeText(getApplicationContext(),"Vous avez été déconnecté",Toast.LENGTH_LONG).show();
                goToNamePickerActivity();
                return true;
            case R.id.item2:
                Toast.makeText(getApplicationContext(),"Item 2 Selected",Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void goToNamePickerActivity() {
        Intent intent = new Intent(this, NamePickerActivity.class);
        this.startActivity(intent);
    }
}
