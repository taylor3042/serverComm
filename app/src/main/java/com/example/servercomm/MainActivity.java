package com.example.servercomm;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    Toast toast;
    private Button btnSendMessage;
    private Button btnSendPicture;
    private Button btnTakePicture;
    boolean picReady = false;
    Bitmap imageBitmap;
    String currentPhotoPath;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnSendPicture = findViewById(R.id.button);
        btnTakePicture = findViewById(R.id.sendPicture);
        btnSendMessage = findViewById(R.id.btnSendMessage);
        imageView = findViewById(R.id.imageView);
        btnTakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });
        btnSendPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            SendPictureTask sendPictureTask = new SendPictureTask();
            sendPictureTask.execute();
            }
        });
        btnSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SendMessageTask sendMessageTask = new SendMessageTask();
                sendMessageTask.execute();
            }
        });
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                picReady = true;
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.servercomm.fileprovider", //you NEED to change this to your app name or it will crash
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
    }

    private class SendMessageTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                // Create a connection to the server
                URL url = new URL("http://192.168.1.22:8000");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Set the request method to POST
                connection.setRequestMethod("POST");

                // Enable writing data to the connection
                connection.setDoOutput(true);

                // Write the message to the connection's output stream
                String message = "Hello World";
                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(message.getBytes());
                outputStream.flush();
                outputStream.close();

                // Get the response code
                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    //showToast("It Works!!");
                    // Message sent successfully
                    // You can handle the response here if needed
                } else {
                    //showToast("It didn't work :(");
                    // Error occurred
                    // Handle the error response here if needed
                }

                // Disconnect the connection
                connection.disconnect();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }}

        private class SendPictureTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {
                try {
                    URL url = new URL("http://10.15.58.252:8000");
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    // Set the request method to POST
                    connection.setRequestMethod("POST");

                    // Enable writing data to the connection
                    connection.setDoOutput(true);

                    OutputStream outputStream = connection.getOutputStream();

                    outputStream.write("sendingPhoto".getBytes());
                    outputStream.flush();
                    // Add the image bytes as a binary part (if needed)
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] b = baos.toByteArray();

                    // Write the message to the connection's output stream
                    outputStream.write(b);
                    outputStream.write(b);
                    outputStream.flush();

                    outputStream.write("stop".getBytes());

                    outputStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }



        public void showToast(final String message) {
            // Show the toast on the main UI thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            });
    }}
}