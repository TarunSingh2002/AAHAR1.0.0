package com.example.aahar100;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class uploadProfilePicActivity extends AppCompatActivity {

    private AppCompatButton upload_pic_choose_btn , upload_pic_btn;
    private ImageView imageView_Profile_dp;
    private FirebaseAuth authProfile;
    private StorageReference storageReference;
    private FirebaseUser firebaseUser;
    private static final int PICK_IMAGE_REQUEST=1;
    private Uri uriImage;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_profile_pic);

        imageView_Profile_dp=findViewById(R.id.imageView_Profile_dp);
        upload_pic_choose_btn=findViewById(R.id.upload_pic_choose_btn);
        upload_pic_btn=findViewById(R.id.upload_pic_btn);
        progressBar=findViewById(R.id.progressBar);
        authProfile =FirebaseAuth.getInstance();
        firebaseUser =authProfile.getCurrentUser();
        storageReference= FirebaseStorage.getInstance().getReference("DisplayPics");
        Uri uri=firebaseUser.getPhotoUrl();
        Picasso.with(uploadProfilePicActivity.this).load(uri).into(imageView_Profile_dp);
        upload_pic_choose_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFileChooser();
            }
        });
        upload_pic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                uploadPic();
            }
        });
    }

    private void uploadPic() {
        if(uriImage!=null)
        {
            StorageReference fileReference = storageReference.child(authProfile.getCurrentUser().getUid()+"/displayspic." +getFileExtension(uriImage));
            fileReference.putFile(uriImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Uri downloadUri=uri;
                            firebaseUser=authProfile.getCurrentUser();
                            UserProfileChangeRequest profileUpdates=new UserProfileChangeRequest.Builder().setPhotoUri(downloadUri).build();
                            firebaseUser.updateProfile(profileUpdates).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(uploadProfilePicActivity.this, "Upload successful", Toast.LENGTH_LONG).show();
                                    Intent intent = new Intent(uploadProfilePicActivity.this, UserProfileActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    intent.putExtra("imageUri", downloadUri.toString()); // Pass the download URL as an extra to the intent
                                    startActivity(intent);
                                    finish(); // Finish the activity to prevent the user from going back to the upload screen
                                }
                            });
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(uploadProfilePicActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();

                }
            });
        }
        else{
            progressBar.setVisibility(View.GONE);
            Toast.makeText(uploadProfilePicActivity.this,"No File selected", Toast.LENGTH_LONG).show();
        }
        //progressBar.setVisibility(View.GONE);
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR= getContentResolver();
        MimeTypeMap mime= MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void openFileChooser() {
        Intent intent =new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==PICK_IMAGE_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            uriImage=data.getData();
            imageView_Profile_dp.setImageURI(uriImage);
        }
    }
}