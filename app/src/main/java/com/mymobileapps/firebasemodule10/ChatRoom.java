/*
 *************** ASSIGNMENT# - In Class Assignment 09 ***************
 *************** FILE NAME - ChatRoom.java ***************
 *************** FULL NAME - Aishwarya Nandkumar Pachange & Janani Krishnan (Group 18) ***************
 */

package com.mymobileapps.firebasemodule10;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.ocpsoft.prettytime.PrettyTime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

public class ChatRoom extends AppCompatActivity {
    private static final int CHOOSE_IMAGE = 101;
    private static  String sFirstName;

    private boolean hasImage;
    private FirebaseAuth mAuth;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    private TextView lbl_UserName;
    private ImageView btn_Logoff;
    private ImageView btn_AddImg;
    private ImageView btn_Send;
    private ProgressBar prog_Loading;
    private EditText txt_Msg;
    String firstName = "", lastName = "";

    //private TextView lbl_downloadedurl;
    Uri imgUri;
    String imageName;
    String key;

    Uri imglink;
    StorageReference ref;
    FirebaseStorage storage;
    private MessageUO objMessageUO;
    String sImage;
    DatabaseReference myRef = database.getReference("ChatRoomMessages/");
    ArrayList<MessageUO> chatMessages = new ArrayList<>();
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CHOOSE_IMAGE && data != null && resultCode == RESULT_OK && data.getData() != null) {
            imgUri = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imgUri);
                btn_AddImg.setImageBitmap(bitmap);
                hasImage = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);
        setTitle("Chat Room");
        mAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

//        lbl_downloadedurl = (TextView) findViewById(R.id.lblDownloadUrl);

        txt_Msg = (EditText) findViewById(R.id.txtMessage);
        lbl_UserName = (TextView) findViewById(R.id.lblUserlName);
        prog_Loading = (ProgressBar) findViewById(R.id.progLoading);
        lbl_UserName.setText(mAuth.getCurrentUser().getDisplayName());

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerChats);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(chatMessages);
        mRecyclerView.setAdapter(mAdapter);

prog_Loading.setVisibility(View.INVISIBLE);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                chatMessages.clear();


                ArrayList<MessageUO> ChatList = new ArrayList<>();

                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    MessageUO obj = dataSnapshot1.getValue(MessageUO.class);
                    ChatList.add(obj);
                }

                chatMessages.addAll(ChatList);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btn_Logoff = (ImageView) findViewById(R.id.btnLogOff);

        btn_Logoff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mAuth.getCurrentUser() != null) {
                    mAuth = null;
                    finish();
                    Intent loginIntent = new Intent(ChatRoom.this, MainActivity.class);
                    startActivity(loginIntent);
                }
            }
        });

        btn_AddImg = (ImageView) findViewById(R.id.imgAddImage);

        btn_AddImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowImageChooser();
                hasImage = true;
            }
        });

        btn_Send = (ImageView) findViewById(R.id.imgSend);


        btn_Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(txt_Msg.getText())) {
                    Toast.makeText(ChatRoom.this, "Enter a message", Toast.LENGTH_LONG).show();
                } else {
                        prog_Loading.setVisibility(View.VISIBLE);
                    if(imgUri!=null)
                    {
                        uploadImageToFirebase(imgUri);
                        mAdapter.notifyDataSetChanged();
                    }
                    else
                    {

                        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
                        String fName[] = mAuth.getCurrentUser().getDisplayName().split(" " );
                        if(fName.length > 0) {
                             firstName = fName[0].toString();
                             lastName = fName[1].toString();
                        }
                        else{
                            firstName = mAuth.getCurrentUser().getDisplayName();
                            lastName = "";
                        }
                        key = myRef.push().getKey();
                        imageName="";
                        imgUri=null;
                        MessageUO messages=new MessageUO();
                        messages.chatId=key;
                        messages.messageText=txt_Msg.getText().toString();
                        messages.timeCreated=new Date().toString();
                        //messages.fName=mAuth.getCurrentUser().getDisplayName();
                        messages.fName = firstName;
                        messages.lName = lastName;
                        messages.imageDetail=imageName;

                        myRef.child(key).setValue(messages);
                        Toast.makeText(getApplicationContext(),
                                "Message sent successfully", Toast.LENGTH_SHORT).show();
                        mAdapter.notifyDataSetChanged();
                        txt_Msg.setText("");
                        prog_Loading.setVisibility(View.INVISIBLE);
                    }
                    //txt_Msg.setText("");
//                    btn_AddImg.setImageResource(R.drawable.addimage);
//                    prog_Loading.setVisibility(View.INVISIBLE);

                }

            }
        });
       /* btn_Send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //begin click
                btn_AddImg.setDrawingCacheEnabled(true);
                btn_AddImg.buildDrawingCache();
                Bitmap bitmap = btn_AddImg.getDrawingCache();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                btn_AddImg.setDrawingCacheEnabled(false);
                byte[] data = baos.toByteArray();

                String path = "myChatImages/" + UUID.randomUUID() + ".png";
                ref = storage.getReference(path);

                String note = txt_Msg.getText().toString();

                if (!note.equals("")) {

                    String time = String.valueOf(Calendar.getInstance().getTime());
                    txt_Msg.setText("");

                    String key = myRef.push().getKey();
                    *//*String fName[] = mAuth.getCurrentUser().getDisplayName().split(" " );
                    String firstName = fName[0].toString();
                    String lastName = fName[1].toString();*//*

                    MessageUO task = new MessageUO(key, note, time, mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getDisplayName(), "");

                    myRef.child(key).setValue(task);

                    //mAdapter.notifyDataSetChanged();

                } else if (note.equals("") && hasImage == false) {
                    txt_Msg.setError("Enter note");
                }
                if (hasImage)
                {
                    UploadTask uploadTask = ref.putBytes(data);

                    Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }
                            // Continue with the task to get the download URL
                            return ref.getDownloadUrl();                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                imglink = task.getResult();
                                if (imglink == null)
                                    return ;
                                objMessageUO = new MessageUO();
                                objMessageUO.messageText = txt_Msg.getText().toString();
                                objMessageUO.fName = mAuth.getCurrentUser().getDisplayName();
                                objMessageUO.lName = mAuth.getCurrentUser().getDisplayName();
                                objMessageUO.imageDetail = imglink.toString();
                                Date currentTime = (Date) Calendar.getInstance().getTime();
                                objMessageUO.timeCreated = currentTime.toString();

                                String key = myRef.push().getKey();

                                MessageUO msgUO = new MessageUO(key, objMessageUO.messageText, objMessageUO.timeCreated, objMessageUO.fName, objMessageUO.lName, objMessageUO.imageDetail);

                                myRef.child(key).setValue(msgUO);
                                hasImage = false;

                            }
                        }
                    });





                }

                mAdapter.notifyDataSetChanged();
                btn_AddImg.setImageResource(R.drawable.addimage);
            }

        });*/


    }

    static class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
        ArrayList<MessageUO> chatMessages;

        public MyAdapter(ArrayList<MessageUO> chatMessages) {
            this.chatMessages = chatMessages;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_chatmsg_list, parent, false);
            MyViewHolder vh = new MyViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {

            try {

                MessageUO obj = chatMessages.get(position);
                holder.txtMsg.setText(obj.messageText);
                holder.txtFirstName.setText(obj.fName);
                holder.id = obj.chatId;


                if (obj.imageDetail != null  && !(obj.imageDetail.equals(""))) {
                //    Picasso.get().load(obj.imageDetail).into(holder.imgChatImage);

                    StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("myChatImages");

                    holder.imgChatImage.setVisibility(View.VISIBLE);
                    StorageReference stMsgRef=storageReference.child(obj.imageDetail);

                    final long ONE_MEGABYTE=(640 * 480)* 2;
                    stMsgRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap btm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            holder.imgChatImage.setImageBitmap(btm);
                        }
                    });

                } else {
                    holder.imgChatImage.setVisibility(View.GONE);
                }


                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy");
                simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

                try {
                    Date date = simpleDateFormat.parse(obj.timeCreated);
                    PrettyTime prettyTime = new PrettyTime();
                    holder.txtTime.setText(prettyTime.format(date));

                } catch (ParseException e) {
                    e.printStackTrace();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return chatMessages.size();
        }

        public static class MyViewHolder extends RecyclerView.ViewHolder {

            public TextView txtMsg;
            public TextView txtFirstName;
            public TextView txtTime;
            public String id;
            public ImageView imgChatImage;
            public ImageView btnDelete;

            public String status = "";

            public MyViewHolder(View v) {
                super(v);
                this.txtMsg = v.findViewById(R.id.txtMsg);
                this.txtFirstName = v.findViewById(R.id.txtFirstName);
                this.txtTime = v.findViewById(R.id.txtTime);
                this.imgChatImage = v.findViewById(R.id.imgChatImage);
                this.btnDelete = v.findViewById(R.id.btnDelete);


                //Delete click event handler
                this.btnDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseDatabase database = FirebaseDatabase.getInstance();
                        DatabaseReference myRef = database.getReference("ChatRoomMessages/");

                        myRef.child(id).removeValue();
                        Toast.makeText(v.getContext(), "Message deleted", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }

    }

    //Picking Image from Gallery
    private void ShowImageChooser() {
        hasImage = true;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose Image"), CHOOSE_IMAGE);
    }

    private void uploadImageToFirebase(final Uri imageFile) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        if (imageFile != null) {
            prog_Loading.setVisibility(View.VISIBLE);
            imageName=mAuth.getCurrentUser().getUid()+UUID.randomUUID().toString();
            final StorageReference ref = storageRef.child("myChatImages/" + imageName);
            ref.putFile(imageFile)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                          //  Toast.makeText(ChatRoom.this, ref.getDownloadUrl().toString(), Toast.LENGTH_SHORT).show();

                            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yy hh:mm a");
                            myRef=FirebaseDatabase.getInstance().getReference("ChatRoomMessages");
                            key = myRef.push().getKey();


                            MessageUO messages=new MessageUO();
                            messages.chatId=key;
                            messages.messageText=txt_Msg.getText().toString();
                            messages.timeCreated=new Date().toString();
                            messages.fName=firstName;
                            messages.lName = lastName;
                            messages.imageDetail=imageName;
                            myRef.child(key).setValue(messages);
                            imageName="";
                            imgUri=null;
                            Toast.makeText(getApplicationContext(),
                                    "Message sent", Toast.LENGTH_SHORT).show();
                            txt_Msg.setText("");
                            btn_AddImg.setImageResource(R.drawable.addimage);
                            prog_Loading.setVisibility(View.INVISIBLE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(ChatRoom.this, " Sending Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
