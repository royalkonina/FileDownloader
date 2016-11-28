package com.example.roman.filedownloader;

import android.app.DownloadManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


import java.io.File;

public class FileDownloaderActivity extends AppCompatActivity implements View.OnClickListener {
  public static final String IMAGE_URL = "http://www.spr.ru/forum_img/55/2012-11/2381321/2951242.jpg";
  public static final String IMAGE_FILENAME = "myImage.jpg";
  private DownloadManager downloadManager;
  private long downloadReference;
  private Button downloadButton;
  private Button openButton;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.a_file_downloader);
    downloadButton = (Button) findViewById(R.id.button_load);
    Button downloadButton = (Button) findViewById(R.id.button_load);
    openButton = (Button) findViewById(R.id.button_open);
    downloadButton.setOnClickListener(this);
    openButton.setOnClickListener(this);
    if (imageExists()) {
      downloadButton.setVisibility(View.GONE);
      openButton.setVisibility(View.VISIBLE);
    }
  }



  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.button_load:

        downloadButton.setEnabled(false);
        Uri downloadUri = Uri.parse(IMAGE_URL);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, IMAGE_FILENAME);
        downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        downloadReference = downloadManager.enqueue(request);

        break;
      case R.id.button_open:
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.parse((Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+"/" + IMAGE_FILENAME)), "image/*");
        startActivity(intent);
    }
  }

  private boolean imageExists() {
    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/"+ IMAGE_FILENAME);
    Log.d("CHECK FILE",file.getAbsolutePath());
    Log.d("CHECK FILE", String.valueOf(file.exists()));
    return file.exists();
  }
}
