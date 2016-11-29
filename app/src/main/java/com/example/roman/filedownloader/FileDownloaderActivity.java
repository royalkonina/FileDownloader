package com.example.roman.filedownloader;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;

public class FileDownloaderActivity extends AppCompatActivity implements View.OnClickListener {
  public static final String IMAGE_URL = "http://www.spr.ru/forum_img/55/2012-11/2381321/2951242.jpg";
  public static final String IMAGE_FILENAME = "myImage.jpg";
  public static final String STATUS_DOWNLOADING = "Status: Downloading";
  private static final String STATUS_DOWNLOADED = "Status: Downloaded";
  private static final String DOWNLOAD_REFERENCE = "downloadManager";
  private static final String STATUS_PAUSE = "Status: Paused";
  private static final String NO_CONNECTION = "No connection to the Internet";
  public static final String DOWNLOAD_FAIL = "Downloading failed";
  private DownloadManager downloadManager;
  private long downloadReference = -1;
  private Button downloadButton;
  private Button openButton;
  private ImageView imageView;
  private ProgressBar progressBar;
  private TextView statusTextView;
  private BroadcastReceiver receiver;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.a_file_downloader);
    statusTextView = (TextView) findViewById(R.id.status_text_view);
    progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    imageView = (ImageView) findViewById(R.id.image_view);
    imageView.setImageBitmap(null);
    downloadButton = (Button) findViewById(R.id.button_load);
    openButton = (Button) findViewById(R.id.button_open);
    downloadButton.setOnClickListener(this);
    openButton.setOnClickListener(this);
    downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

    receiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
          if (getDownloaderStatus(downloadReference) == DownloadManager.STATUS_SUCCESSFUL) {
            setupDownloadedImage();
          }
        }
      }
    };
    registerReceiver(receiver, new IntentFilter(
            DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    if (savedInstanceState != null && savedInstanceState.getLong(DOWNLOAD_REFERENCE) != -1) {
      downloadReference = savedInstanceState.getLong(DOWNLOAD_REFERENCE);
      switch (getDownloaderStatus(downloadReference)) {
        case DownloadManager.STATUS_SUCCESSFUL:
          if (imageExists()) {
            setupDownloadedImage();
          }
          break;
        case DownloadManager.STATUS_RUNNING:
          progressBar.setVisibility(View.VISIBLE);
          downloadButton.setEnabled(false);
          break;
        case DownloadManager.STATUS_PAUSED:
          progressBar.setVisibility(View.VISIBLE);
          downloadButton.setEnabled(false);
          statusTextView.setText(STATUS_PAUSE);
          if (!isOnline()) {
            Snackbar.make(findViewById(R.id.a_file_downloader), NO_CONNECTION, Snackbar.LENGTH_SHORT).show();
          }
          break;
      }
    } else if (imageExists()) {
      setupDownloadedImage();
    }
  }

  public boolean isOnline() {
    ConnectivityManager cm =
            (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo netInfo = cm.getActiveNetworkInfo();
    return netInfo != null && netInfo.isConnectedOrConnecting();
  }


  private int getDownloaderStatus(long reference) {
    DownloadManager.Query q = new DownloadManager.Query();
    q.setFilterById(reference);
    final Cursor cursor = downloadManager.query(q);
    cursor.moveToFirst();
    return cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
  }

  @Override
  protected void onDestroy() {
    unregisterReceiver(receiver);
    super.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    outState.putLong(DOWNLOAD_REFERENCE, downloadReference);
    super.onSaveInstanceState(outState);
  }

  private void setupDownloadedImage() {
    imageView.setImageBitmap(BitmapFactory.decodeFile((Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + IMAGE_FILENAME)));
    downloadButton.setVisibility(View.GONE);
    progressBar.setVisibility(View.INVISIBLE);
    statusTextView.setText(STATUS_DOWNLOADED);
    openButton.setVisibility(View.VISIBLE);
  }


  @Override
  public void onClick(View view) {
    switch (view.getId()) {
      case R.id.button_load:
        progressBar.setVisibility(View.VISIBLE);
        downloadButton.setEnabled(false);
        Uri downloadUri = Uri.parse(IMAGE_URL);
        DownloadManager.Request request = new DownloadManager.Request(downloadUri);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, IMAGE_FILENAME);
        downloadReference = downloadManager.enqueue(request);
        new Thread(setupDownloading).start();
        break;
      case R.id.button_open:
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri uriFromFile = Uri.fromFile(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + IMAGE_FILENAME));
        intent.setDataAndType(uriFromFile, "image/*");
        startActivity(intent);
    }
  }

  private boolean imageExists() {
    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/" + IMAGE_FILENAME);
    return file.exists();
  }

  Runnable setupDownloading = new Runnable() {

    @Override
    public void run() {

      boolean downloading = true;
      while (downloading) {
        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(downloadReference);
        final Cursor cursor = downloadManager.query(q);
        cursor.moveToFirst();
        int bytes_downloaded = cursor.getInt(cursor
                .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
        int bytes_total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

        if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
          downloading = false;
        }

        final double dl_progress = ((bytes_downloaded + .0) / bytes_total) * 100;

        switch (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
          case DownloadManager.STATUS_FAILED:
            downloading = false;
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                Snackbar.make(findViewById(R.id.a_file_downloader), DOWNLOAD_FAIL, Snackbar.LENGTH_LONG).show();
              }
            });
            break;
          case DownloadManager.STATUS_RUNNING:
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                progressBar.setProgress((int) dl_progress);
                statusTextView.setText(STATUS_DOWNLOADING);
              }
            });
            break;
          case DownloadManager.STATUS_SUCCESSFUL:
            runOnUiThread(new Runnable() {
              @Override
              public void run() {
                statusTextView.setText(STATUS_DOWNLOADED);
                progressBar.setVisibility(View.INVISIBLE);
                setupDownloadedImage();
              }
            });
            downloading = false;
            break;
          /*case DownloadManager.STATUS_PAUSED:
            if (!isOnline()) {
              Snackbar.make(findViewById(R.id.a_file_downloader), NO_CONNECTION, Snackbar.LENGTH_SHORT).show();
            }
            break;*/
        }
        cursor.close();


      }

    }
  };


}
