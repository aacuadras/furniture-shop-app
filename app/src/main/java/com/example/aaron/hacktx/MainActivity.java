package com.example.aaron.hacktx;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.RequiresApi;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;

import org.w3c.dom.Text;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

public class MainActivity extends AppCompatActivity {

    private String realPath;
    private Bitmap bitmap;
    private static final String TAG = "MainActivity";
    Vector<String> dictionary = new Vector<String>();


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri uri = data.getData();

            //Original: String realPath = ImageFilePath.getPath(SignupActivity.this, data.getData());
            realPath = ImageFilePath.getPath(this, data.getData());
//                realPath = RealPathUtil.getRealPathFromURI_API19(this, data.getData());

            Log.i(TAG, "onActivityResult: file path : " + realPath);
            System.out.println("File Path : " + realPath);
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                // Log.d(TAG, String.valueOf(bitmap));

                ImageView imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);
                imageView.setVisibility(View.VISIBLE);

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show();
        }
    }

    public VisionServiceClient visionServiceClient = new VisionServiceRestClient("d1de232aa71b48fa992a1b7f2a6f2601");
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private int PICK_IMAGE_REQUEST = 1;
    private int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 0;
    ////////////////////////Weird Stuff Ends

    public static String convertStreamToString(InputStream is) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        reader.close();
        return sb.toString();
    }

    public static String getStringFromFile (String filePath) throws Exception {
        File fl = new File(filePath);
        FileInputStream fin = new FileInputStream(fl);
        String ret = convertStreamToString(fin);
        //Make sure you close all streams.
        fin.close();
        return ret;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, ask for permission
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        }

        
        for(int i = 0; i < dictionary.size(); i++)
            System.out.println(dictionary.get(i));

        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);
        //This variable will display the image loaded in drawable
        ImageView imageView = findViewById(R.id.imageView);
        //This variable will display the description of the image
        //Previous statement: TextView textView = (textView)findViewById(R.id.txtDescription);
        //final TextView textView = findViewById(R.id.txtDescription);
        //This variable will display the button
        Button btnProcess = findViewById(R.id.button);
        Button slctImage = findViewById(R.id.button2);

        final Button buttons[] = new Button[8];
        buttons[0] = (Button)findViewById(R.id.button3);
        buttons[1] = (Button)findViewById(R.id.button4);
        buttons[2] = (Button)findViewById(R.id.button5);
        buttons[3] = (Button)findViewById(R.id.button6);
        buttons[4] = (Button)findViewById(R.id.button7);
        buttons[5] = (Button)findViewById(R.id.button8);
        buttons[6] = (Button)findViewById(R.id.button9);
        buttons[7] = (Button)findViewById(R.id.button10);


        //"Select Image" button Stuff
        slctImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

            }

        });

        //"Azure it!" button stuff
       // imageView.setImageBitmap(mBitmap);

        //Analyzes images and converts it to stream


        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100, outputStream);
                final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                final AsyncTask<InputStream,String,String> visionTask = new AsyncTask<InputStream, String, String>() {
                    ProgressDialog mDialog = new ProgressDialog(MainActivity.this);
                    @Override
                    protected String doInBackground(InputStream... params) {
                        try
                        {
                            publishProgress("Working on it...");
                            String[] features = {"Description"};
                            String[] details = {};
                            AnalysisResult result = visionServiceClient.analyzeImage(params[0], features, details);
                            String strResult = new Gson().toJson(result);
                            return strResult;
                        } catch (Exception e) {
                            return null;
                        }
                    }

                    @Override
                    protected void onPreExecute() {
                        mDialog.show();
                    }

                    @Override
                    protected void onPostExecute(String s) {
                        mDialog.dismiss();
                        AnalysisResult result = new Gson().fromJson(s, AnalysisResult.class);
                        Object altOutput[] = result.description.tags.toArray();
                        TextView whatISee = findViewById(R.id.textView);
                        whatISee.setVisibility(View.VISIBLE);

                        for(int i = 0; i < altOutput.length; i++)
                            System.out.println(altOutput[i].toString());
                        for(int j = 0; j < altOutput.length && j < buttons.length; j++)
                        {
                            buttons[j].setVisibility(View.VISIBLE);
                            buttons[j].setText(altOutput[j].toString());
                        }
                    }

                    @Override
                    protected void onProgressUpdate(String... values) {
                        mDialog.setMessage(values[0]);
                    }
                };
                visionTask.execute(inputStream);
            }
        });
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
