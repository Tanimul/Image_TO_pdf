package com.example.imagetopdf.UI;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.example.imagetopdf.Adapter.AdapterImageList;
import com.example.imagetopdf.App;
import com.example.imagetopdf.KEYS;
import com.example.imagetopdf.R;
import com.example.imagetopdf.Tools;
import com.example.imagetopdf.databinding.ActivityHomeBinding;
import com.facebook.internal.LockOnGetVariable;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ActivityHome extends AppCompatActivity implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "ActivityHome";
    private ActivityHomeBinding activityHomeBinding;
    private TextView headerName;
    Animation fav_open, fav_close, rotate_clockwise, rotate_anticlockwise;
    boolean isOpen = false;
    private int image_rec_code = 1;
    Bitmap bitmap;
    ArrayList<Uri> mArrayUri;
    int position = 0;
    private AdapterImageList adapterImageList;
    static final int REQUEST_IMAGE_CAPTURE = 0;
    private long backpressed;
    private Toast backtost;
    private AlertDialog dialog;
    private EditText editTextfilename;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityHomeBinding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(activityHomeBinding.getRoot());

 //       initHeader();
        initNavAndToolbar();
        initAnimation();
        initAleartDialog();


        mArrayUri = new ArrayList<Uri>();

        activityHomeBinding.recyclerviewImage.setFitsSystemWindows(true);
        activityHomeBinding.recyclerviewImage.setLayoutManager(new GridLayoutManager(this, 2));

        adapterImageList = new AdapterImageList(mArrayUri);
        activityHomeBinding.recyclerviewImage.setAdapter(adapterImageList);

        activityHomeBinding.navViewActivityHome.getMenu().getItem(0).setChecked(true);

        activityHomeBinding.flotingbuttonHomeAdd.setOnClickListener(this);
        activityHomeBinding.extflotingbuttonTakeimage.setOnClickListener(this);
        activityHomeBinding.extflotingbuttonHomeImportfromgalary.setOnClickListener(this);
        activityHomeBinding.extflotingbuttonHomeNewfolder.setOnClickListener(this);
        activityHomeBinding.navViewActivityHome.setNavigationItemSelectedListener(this);
    }

//    private void initHeader() {
//        View header = activityHomeBinding.navViewActivityHome.getHeaderView(0);
//        headerName = header.findViewById(R.id.header_name);
//        headerName.setText(Tools.getPref(KEYS.USER_NAME, "User Name"));
//    }

    private void initNavAndToolbar() {
        setTitle(null);
        setSupportActionBar(activityHomeBinding.toolbarActivityHome);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, activityHomeBinding.drwerlayoutActivityMain,
                activityHomeBinding.toolbarActivityHome,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        activityHomeBinding.drwerlayoutActivityMain.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
    }

    private void initAnimation() {
        fav_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fav_open);
        fav_close = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fav_close);
        rotate_clockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_clockwise);
        rotate_anticlockwise = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate_anticlockwise);
    }

    private void initAleartDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityHome.this);
        final View layout_filesaving = getLayoutInflater().inflate(R.layout.layout_file_saving, null);
        builder.setView(layout_filesaving);
        builder.setTitle("Set the File Name");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                editTextfilename = layout_filesaving.findViewById(R.id.edittext_layout_saving_fileName);
                if (!editTextfilename.getText().toString().isEmpty()) {
                    Log.d(TAG, "File Saving");
                    dialog.cancel();
                    createPDFWithMultipleImage(editTextfilename.getText().toString());
                    Toast.makeText(ActivityHome.this, "File Saving", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "Please Enter a File Name");
                    Toast.makeText(ActivityHome.this, "Please Enter a File Name", Toast.LENGTH_SHORT).show();

                }

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(ActivityHome.this, "File Saving Cancel", Toast.LENGTH_SHORT).show();
                dialog.cancel();
            }
        });

        dialog = builder.create();
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.homemenu, menu);
        if (menu instanceof MenuBuilder) {
            MenuBuilder menuBuilder = (MenuBuilder) menu;
            menuBuilder.setOptionalIconsVisible(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.listview:
                Toast.makeText(this, "List View", Toast.LENGTH_SHORT).show();
                activityHomeBinding.recyclerviewImage.setLayoutManager(new LinearLayoutManager(this));
                activityHomeBinding.recyclerviewImage.setAdapter(adapterImageList);
                break;
            case R.id.gridview:
                Toast.makeText(this, "Gridview", Toast.LENGTH_SHORT).show();
                activityHomeBinding.recyclerviewImage.setLayoutManager(new GridLayoutManager(this, 2));
                activityHomeBinding.recyclerviewImage.setAdapter(adapterImageList);
                break;

            case R.id.save:
                if (mArrayUri.size() != 0) {
                    dialog.show();
                } else {
                    Toast.makeText(this, "Please Select Images", Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.share:
                share();
                break;

//            case R.id.sortby:
//                Toast.makeText(this, "Sort By", Toast.LENGTH_SHORT).show();
//                break;
//            case R.id.select:
//                Toast.makeText(this, "Select", Toast.LENGTH_SHORT).show();
//                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v == activityHomeBinding.flotingbuttonHomeAdd) {
            extendFloatingButton();
        }
        if (v == activityHomeBinding.extflotingbuttonHomeImportfromgalary) {
            Log.d(TAG, "Import from Gallery .");
            extendFloatingButton();
            openGallery();
        }
        if (v == activityHomeBinding.extflotingbuttonTakeimage) {
            Log.d(TAG, "Take Image .");
            extendFloatingButton();
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            try {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
// Else ask for permission
                else {
                    ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }

            } catch (ActivityNotFoundException e) {
                // display error state to the user
                Log.d(TAG, "display error state to the user");
            }
        }
        if (v == activityHomeBinding.extflotingbuttonHomeNewfolder) {
            Log.d(TAG, "Create a New Folder .");
            extendFloatingButton();
            Toast.makeText(this, "Create a New Folder(UnderDeveloping) ", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Log.d(TAG, "Navigation Item selected: " + item.toString());
        switch (item.getItemId()) {
            case R.id.nav_home:
                startActivity(new Intent(ActivityHome.this, ActivityHome.class));
                finish();
                break;

            case R.id.nav_setting:
                startActivity(new Intent(ActivityHome.this, ActivitySetting.class));
                break;

//            case R.id.nav_logout:
//                logout();
//                break;
        }
        return true;
    }

    private void getDropboxIMGSize(Uri uri) {
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            int imageWidth = bitmap.getWidth();
            int imageHeight = bitmap.getHeight();
            Log.d(TAG, "check Information: h=" + imageHeight + " w=" + imageWidth);
        } catch (Exception e) {
            Log.d(TAG, "check Information: error:" + e.getMessage());
        }

    }

    public void share() {
        Intent intent = new Intent(Intent.ACTION_SEND);
        ApplicationInfo api = getApplicationContext().getApplicationInfo();
        String apkpath = api.sourceDir;
        /// intent.putExtra(Intent.EXTRA_TEXT, "apps address "))
        intent.putExtra(Intent.EXTRA_TEXT, Uri.fromFile(new File(apkpath)));
        intent.setType("text/plain");
        startActivity(Intent.createChooser(intent, "Share with"));
    }

    private void createPDFWithMultipleImage(String filename) {
        File file = getOutputFile(filename);
        if (file != null) {
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                PdfDocument pdfDocument = new PdfDocument();
                Log.d(TAG, "getOutputFile: try");
                for (int i = 0; i < mArrayUri.size(); i++) {
                    bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), mArrayUri.get(i));
                    // Bitmap bitmap = BitmapFactory.decodeFile(mArrayUri.get(i).getPath());
                    PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), (i + 1)).create();
                    PdfDocument.Page page = pdfDocument.startPage(pageInfo);
                    Canvas canvas = page.getCanvas();
                    Paint paint = new Paint();
                    paint.setColor(Color.BLUE);
                    canvas.drawPaint(paint);
                    canvas.drawBitmap(bitmap, 0f, 0f, null);
                    pdfDocument.finishPage(page);
                    bitmap.recycle();
                }
                pdfDocument.writeTo(fileOutputStream);
                pdfDocument.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private File getOutputFile(String filename) {
        File root = new File(Environment.getExternalStorageDirectory(), "Image TO Pdf");
        boolean isFolderCreated = true;

        if (!root.exists()) {
            Log.d(TAG, "getOutputFile: not exist");
            isFolderCreated = root.mkdir();
        } else {
            Log.d(TAG, "getOutputFile:  exist");
        }

        if (isFolderCreated) {
            Log.d(TAG, "getOutputFile: created");
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            String imageFileName = filename + "_" + timeStamp;

            return new File(root, imageFileName + ".pdf");
        } else {
            Toast.makeText(this, "Folder is not created", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    public void extendFloatingButton() {
        if (isOpen) {
            activityHomeBinding.flotingbuttonHomeAdd.startAnimation(rotate_clockwise);
            activityHomeBinding.extflotingbuttonHomeImportfromgalary.startAnimation(fav_close);
            //   activityHomeBinding.extflotingbuttonHomeNewfolder.startAnimation(fav_close);
            activityHomeBinding.extflotingbuttonTakeimage.startAnimation(fav_close);
            isOpen = false;
        } else {
            activityHomeBinding.flotingbuttonHomeAdd.startAnimation(rotate_anticlockwise);
            activityHomeBinding.extflotingbuttonHomeImportfromgalary.startAnimation(fav_open);
            //   activityHomeBinding.extflotingbuttonHomeNewfolder.startAnimation(fav_open);
            activityHomeBinding.extflotingbuttonTakeimage.startAnimation(fav_open);
            isOpen = true;
        }
    }

    //Galary open for place picture
    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        galleryIntent.setType("image/*");
        startActivityForResult(Intent.createChooser(galleryIntent, "Select Picture"), image_rec_code);
    }

//    //logout current user
//    private void logout() {
//        Tools.savePrefBoolean(KEYS.IS_LOGGED_IN, false);
//        FirebaseAuth.getInstance().signOut();
//        finish();
//    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        closeDrawer();
    }

    public void closeDrawer() {
        activityHomeBinding.drwerlayoutActivityMain.closeDrawer(Gravity.LEFT, false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == image_rec_code && resultCode == RESULT_OK && data != null) {
            // Get the Image from data
            mArrayUri.clear();
            if (data.getClipData() != null) {
                ClipData mClipData = data.getClipData();
                int cout = data.getClipData().getItemCount();
                for (int i = 0; i < cout; i++) {
                    // adding imageuri in array
                    Uri imageurl = data.getClipData().getItemAt(i).getUri();
                    Log.d(TAG, "" + imageurl);
                    mArrayUri.add(imageurl);
                }
                position = 0;
            } else {
                Uri imageurl = data.getData();
                mArrayUri.add(imageurl);
                position = 0;
            }
            adapterImageList.notifyDataSetChanged();
        }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {

            Bitmap imageBitmap = (Bitmap) data.getExtras().get("data");
            // activityHomeBinding.imagetest.setImageBitmap(imageBitmap);
            Uri tempUri = getImageUri(getApplicationContext(), imageBitmap);
            Log.d(TAG, "Uri: " + tempUri);
            mArrayUri.add(tempUri);
            adapterImageList.notifyDataSetChanged();
        }
//        else {
//            // show this if no image is selected
//            Toast.makeText(this, "You haven't picked Image", Toast.LENGTH_LONG).show();
//        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    public void onBackPressed() {
        if (backpressed + 2000 > System.currentTimeMillis()) {
            backtost.cancel();
            super.onBackPressed();
            return;
        } else {
            backtost = Toast.makeText(ActivityHome.this, "press BACK again to Exit", Toast.LENGTH_SHORT);
            backtost.show();
        }

        backpressed = System.currentTimeMillis();
    }


}