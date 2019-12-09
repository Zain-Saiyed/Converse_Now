package com.zain.deeplearning.converse_now;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST = 1888;

    private ImageView imageView;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    public static final int RESULTS_TO_SHOW =3;
    public static final int IMAGE_MEAN = 128;
    public static final float IMAGE_STD = 128.0f;
    public static final float IMG_HEIGHT = 200;
    private List<String> labelList;

    private final Interpreter.Options tfliteOptions = new Interpreter.Options();
    private Interpreter tflite;
    int[] intValues;


    Button photoButton , submitdata;
    TextView tv_result,tx1,tx2;
    ByteBuffer imgData = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        this.imageView = this.findViewById(R.id.imageView1);
        photoButton = this.findViewById(R.id.btn_classify);
        submitdata = this.findViewById(R.id.btn_submit);
        tv_result = this.findViewById(R.id.tv_result);
//        tx1 =  this.findViewById(R.id.tv_result_organic);
//        tx2 =  this.findViewById(R.id.tv_result_recycle);

        photoButton.setOnClickListener(new View.OnClickListener()
        {
            @TargetApi(Build.VERSION_CODES.M)
            @Override
            public void onClick(View v)
            {
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                }
                else{
                    Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
                }
            }
        });

        submitdata.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                startActivity(new Intent(MainActivity.this, SubmitData.class));
            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_REQUEST);
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }
    protected void onActivityResult(int requestCode, int resultCode ,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            // Get image and store it in BitMap
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            int width = photo.getWidth();
            int height = photo.getHeight();
            tv_result.setText(width + "   " + height);

            imageView.setImageBitmap(photo);

//            imgData.rewind();
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(photo, 600, 600, false);

            imageView.setImageBitmap(Bitmap.createScaledBitmap(resizedBitmap, 800, 800, false));


            int[] pix = new int[200 * 200];
            resizedBitmap.getPixels(pix, 0, 200, 0, 0, 200, 200);


//            int bytes = resizedBitmap .getByteCount();
//            resizedBitmap.copyPixelsToBuffer(imgData);
//            byte[] array = imgData.array();
//
//            Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//            int[] intArray = new int[224 * 224];

//            float prediction = doInference(pix);
//            tv_result.setText(Float.toString(prediction));

            intValues = new int[200 * 200];
            try {
                tflite = new Interpreter(loadModelFile(), tfliteOptions);
            } catch (Exception e) {
                e.printStackTrace();
            }
            imgData = ByteBuffer.allocateDirect(4 * 200 * 200 * 3);

            imgData.order(ByteOrder.nativeOrder());

            float[][] labelProbArray = new float[1][29];
            Bitmap input_image = getResizedBitmap(photo, 200, 200);

            convertBitmapToByteBuffer(input_image);

            try {
                tflite.run(imgData, labelProbArray);
                labelList = loadLabelList();
            } catch (Exception ex) {
                ex.printStackTrace();
            }


//            tx1.setText((labelProbArray[0][0]*100)+" %");
//            tx2.setText((labelProbArray[0][1]*100)+" %");
//            if ( labelProbArray[0][0] > labelProbArray[0][1]  )
//                tv_result.setText("ORGANIC!");
//            else
//                tv_result.setText("RECYCLEABLE!");

            System.out.println("vvvvvvvvvvvvvvvvvvvvvvvvvv");

            int index = get_predicted(labelProbArray, labelList);
            tv_result.setText((labelProbArray[0][index] * 100) + " % -> " + labelList.get(index));
//
            for (int ii = 0; ii < 29; ii++)
                System.out.println((labelProbArray[0][ii] * 100));
        }
    }
    private int get_predicted(float[][] arr_prob, List labels)
    {
        float max = arr_prob[0][0];
        int index=0;
        for (int k=0;k<29;k++)
        {
            if(max < arr_prob[0][k])
            {
                max = arr_prob[0][k];
                index = k;
            }
        }
        return index;
    }
    private void convertBitmapToByteBuffer(Bitmap bitmap) {
        if (imgData == null) {
            return;
        }
        imgData.rewind();
        bitmap.getPixels(intValues, 0, 200, 0, 0,200,200 );     //bitmap.getWidth(), bitmap.getHeight());
        // loop through all pixels
        int pixel = 0;
        for (int i = 0; i < 200; ++i) {
            for (int j = 0; j < 200; ++j) {
                final int val = intValues[pixel++];
                // get rgb values from intValues where each int holds the rgb values for a pixel.
                // if quantized, convert each rgb value to a byte, otherwise to a float

                imgData.putFloat((((val >> 16) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val >> 8) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
                imgData.putFloat((((val) & 0xFF)-IMAGE_MEAN)/IMAGE_STD);
            }
        }
    }
    public Bitmap getResizedBitmap(Bitmap bm, int nwidth, int nheight){
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) nwidth )/width;
        float scaleHeight = ((float) nheight )/height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth,scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm,0,0,width, height,matrix,false);

        return resizedBitmap;

    }
    private MappedByteBuffer loadModelFile() throws IOException {

        AssetFileDescriptor fileDescriptor = this.getAssets().openFd("model_asl_96.tflite");
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
    // loads the labels from the label txt file in assets into a string array
    private List<String> loadLabelList() throws IOException {
        List<String> labelList = new ArrayList<String>();
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(this.getAssets().open("labels.txt")));
        String line;
        while ((line = reader.readLine()) != null) {
            labelList.add(line);
        }
        reader.close();
        return labelList;
    }
}
