package com.zain.deeplearning.converse_now;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

//Firebase imports
//Androidx imports

public class SubmitData extends AppCompatActivity {

    Button Img_choose,Img_upload ;
    ImageView Image_View;
    StorageReference storage_ref;
    private StorageTask uploadTask;
    private Uri image_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getSupportActionBar().hide();
        setContentView(R.layout.submit_data);

        storage_ref= FirebaseStorage.getInstance().getReference("ASL Images");
        Img_choose = findViewById(R.id.chse);
        Img_upload = findViewById(R.id.upld);

        Image_View = findViewById(R.id.imgv);
        Img_choose.setOnClickListener(view -> {
            browse_upload_image();
            Img_upload.setVisibility(View.VISIBLE);
        });

        Img_upload.setOnClickListener(v -> {
            if (uploadTask != null && uploadTask.isInProgress()) {
                Toast.makeText(SubmitData.this, "Image upload in process!", Toast.LENGTH_LONG).show();
            }
            else {
                if(Image_View.getDrawable() != null)
                    upload_image();
                else
                    Toast.makeText(SubmitData.this, "Please choose an Image to upload", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private String getExtension(Uri uri){
        ContentResolver cr=getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(cr.getType(uri));
    }
    private void upload_image(){
        StorageReference Ref = storage_ref.child(System.currentTimeMillis()+"."+getExtension(image_uri));
        uploadTask=Ref.putFile(image_uri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get a URL to the uploaded content
                    //Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    Toast.makeText(SubmitData.this, "Image Uploaded Successfully!", Toast.LENGTH_LONG).show();
                    recreate();
                })
                .addOnFailureListener(exception -> {
                    // Handle unsuccessful uploads
                    Toast.makeText(SubmitData.this, "Failed to Upload!...Please Try again!", Toast.LENGTH_LONG).show();
                    // TODO
                    // please add : after specific number of button click restart activity
                });
    }
    private void browse_upload_image(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==1 && resultCode==RESULT_OK && data!=null && data.getData()!=null)
        {
            image_uri = data.getData();
            Image_View.setImageURI(image_uri);
        }
    }
}
