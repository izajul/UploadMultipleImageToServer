package com.rashid.projectuploadimagesingle;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    String mediaPath;
    private LinearLayout lnrImages;
    private Button btnAddPhots;
    private ArrayList<String> imagesPathList;
    private Bitmap yourbitmap;
    private final int PICK_IMAGE_MULTIPLE = 1;
    private boolean flag = false;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );

        lnrImages = (LinearLayout) findViewById( R.id.lnrImages );
        btnAddPhots = (Button) findViewById( R.id.btnAddPhots );
        btnAddPhots.setOnClickListener( this );
        progressDialog = new ProgressDialog( this );
        progressDialog.setMessage( "Uploading...." );


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnAddPhots:
                if (!flag) {
                    Intent intent = new Intent( MainActivity.this, CustomGallery.class );
                    startActivityForResult( intent, PICK_IMAGE_MULTIPLE );
                } else {
                    if (imagesPathList.size()>0) {
                        uploadFile();
                        flag = false;
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult( requestCode, resultCode, data );
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_MULTIPLE) {
                imagesPathList = new ArrayList<String>();
                String[] imagesPath = data.getStringExtra( "data" ).split( "\\|" );
                try {
                    lnrImages.removeAllViews();
                    if (imagesPath.length > 0) {
                        btnAddPhots.setText( "Upload Images" );
                        flag = true;
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                for (int i = 0; i < imagesPath.length; i++) {
                    imagesPathList.add( imagesPath[i] );
                    yourbitmap = BitmapFactory.decodeFile( imagesPath[i] );
                    ImageView imageView = new ImageView( this );
                    imageView.setImageBitmap( yourbitmap );
                    imageView.setAdjustViewBounds( true );
                    lnrImages.addView( imageView );
                }
            }
        }
    }

    private void uploadFile() {
        progressDialog.show();

        // Map is used to multipart the file using okhttp3.RequestBody
        //mediaPath = imagesPathList.get( 0 );
       // RequestBody fileToUpload[];
        File file[] = new File[imagesPathList.size()];
        for (int i=0;i<imagesPathList.size();i++){
             file[i] = new File(imagesPathList.get( i ));
        }
        MultipartBody.Part fileToUpload[] = new MultipartBody.Part[imagesPathList.size()];
        RequestBody filename[] = new RequestBody[imagesPathList.size()];
        for (int i=0;i<imagesPathList.size();i++) {
            RequestBody requestBody = RequestBody.create( MediaType.parse( "*/*" ), file[i] );
            fileToUpload[i] = MultipartBody.Part.createFormData( "photos[]", file[i].getName(), requestBody );
            filename[i] = RequestBody.create( MediaType.parse( "text/plain" ), file[i].getName() );

        }

        ApiService getResponse = ApiClient.getClient().create(ApiService.class);
        Call<ServerResponse> call = getResponse.uploadFile(fileToUpload, filename);
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                ServerResponse serverResponse = response.body();
                if (serverResponse != null) {
                    if (serverResponse.getSuccess()) {
                        Toast.makeText(getApplicationContext(), serverResponse.getMessage(),Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), serverResponse.getMessage(),Toast.LENGTH_SHORT).show();
                    }
                } else {
                    assert serverResponse != null;
                    Log.v("Response", serverResponse.toString());
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {

            }
        });
    }


}
