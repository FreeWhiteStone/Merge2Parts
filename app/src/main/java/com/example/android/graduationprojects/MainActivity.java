package com.example.android.graduationprojects;

import android.app.ActionBar;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;
import org.opencv.calib3d.Calib3d;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {


    static {
        System.loadLibrary("opencv_java");
        System.loadLibrary("nonfree");
    }

    private FeatureDetector detector = FeatureDetector.create(FeatureDetector.SURF);
    private DescriptorExtractor descriptor = DescriptorExtractor.create(DescriptorExtractor.SURF);
    private DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_L1);
    Mat descriptors2, descriptors1;
    Mat img1;
    Mat img2;
    MatOfKeyPoint keypoints1, keypoints2;


    ListView lv;
    ImageView iwCam,iwDb;

    String CameraPicture = "";
    String DatabasePicture = "";

    ArrayAdapter<String> adapter;
    ArrayList<HashMap<String, String>> plant_list;
    String plant_names[];
    int plant_ids[];
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);

        Bitmap inputImage = BitmapFactory.decodeResource(getResources(), R.drawable.test);
        setContentView(R.layout.activity_main);
        ImageView imageView = this.findViewById(R.id.imageView);



        Button btnCamera = (Button)findViewById(R.id.compare);
        iwCam = (ImageView)findViewById(R.id.imageView12);
        iwDb = (ImageView)findViewById(R.id.imageView13);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Database db = new Database(getApplicationContext());
                HashMap<String, String> map = db.getProperImage();
                String image=(map.get("picture"));



                DatabasePicture=image;
                Log.d("database_pic:",DatabasePicture);

                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                Bitmap bitmap = BitmapFactory.decodeFile(image, options);
                iwDb.setImageBitmap(bitmap);

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent,0);


            }
        });

        NotificationAlarmService notificationAlarmService = new NotificationAlarmService(this);
        notificationAlarmService.startAlarm();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bitmap bmp = (Bitmap) data.getExtras().get("data");
            iwCam.setImageBitmap(bmp);

            iwCam.setImageBitmap(bmp);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] byteArray = stream.toByteArray();

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH).format(new Date());
            String fileName = "TEMP_IMG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File pictureFile = null;
            try {
                pictureFile = File.createTempFile(fileName, ".jpg", storageDir);
                CameraPicture=pictureFile.getAbsolutePath();
                boolean result=surf(CameraPicture,DatabasePicture);
                Log.d("sssss:",CameraPicture);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d("tttt",pictureFile.toString());
            String tempImageFilePath=pictureFile.getAbsolutePath();
            FileOutputStream fos = null;
            try {
                fos = new FileOutputStream(pictureFile);
                bmp.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.flush();
                fos.close();

            } catch (Exception e) {
                e.printStackTrace();

            }
            Database db = new Database(getApplicationContext());
            db.addTempPicture(tempImageFilePath);
            db.close();

        }
    }





    public void onResume()
    {
        super.onResume();
        Database db = new Database(getApplicationContext());
        plant_list = db.plants();
        if(plant_list.size()==0){
            Toast.makeText(getApplicationContext(), "There is no plant\nPlease use + button to add plant...", Toast.LENGTH_LONG).show();
        }else{
            plant_names = new String[plant_list.size()];
            plant_ids = new int[plant_list.size()];
            for(int i=0;i<plant_list.size();i++){
                plant_names[i] = plant_list.get(i).get("plant_name");

                plant_ids[i] = Integer.parseInt(plant_list.get(i).get("id"));
            }
            lv = (ListView) findViewById(R.id.list_view);

            adapter = new ArrayAdapter<String>(this, R.layout.list_item, R.id.plant_name, plant_names);
            lv.setAdapter(adapter);

            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                        long arg3) {
                    Intent intent = new Intent(getApplicationContext(), PlantDetail.class);
                    intent.putExtra("id", (int)plant_ids[arg2]);
                    startActivity(intent);

                }
            });
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);

        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add:
                AddPlant();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void AddPlant() {
        Intent i = new Intent(MainActivity.this, AddPlant.class);
        startActivity(i);
    }


    /////////////////////////////


    public boolean surf(String cam, String db) throws IOException {


        AssetManager assetManager = getAssets();
        InputStream istr = assetManager.open("c1.jpg");
        Bitmap bitmap = BitmapFactory.decodeStream(istr);
        img1 = new Mat();
        Utils.bitmapToMat(bitmap, img1);
        Imgproc.cvtColor(img1, img1, Imgproc.COLOR_RGB2GRAY);
        descriptors1 = new Mat();
        keypoints1 = new MatOfKeyPoint();
        detector.detect(img1, keypoints1);
        descriptor.compute(img1, keypoints1, descriptors1);

        img2 = new Mat();
        AssetManager assetManager2 = getAssets();
        InputStream istr2 = assetManager2.open("c2.jpg");
        Bitmap bitmap2 = BitmapFactory.decodeStream(istr2);
        Utils.bitmapToMat(bitmap2, img2);
        Imgproc.resize(img2, img2, img1.size());
        Imgproc.cvtColor(img2, img2, Imgproc.COLOR_RGB2GRAY);

        descriptors2 = new Mat();
        keypoints2 = new MatOfKeyPoint();
        detector.detect(img2, keypoints2);
        descriptor.compute(img2, keypoints2, descriptors2);


        List<MatOfDMatch> matches = new ArrayList<>();
        matcher.knnMatch(descriptors1, descriptors2, matches, 5);


        // ratio test
        LinkedList<DMatch> good_matches = new LinkedList<>();
        for (MatOfDMatch matOfDMatch : matches) {
            if (matOfDMatch.toArray()[0].distance / matOfDMatch.toArray()[1].distance < 0.7
                    ) {
                good_matches.add(matOfDMatch.toArray()[0]);
            }
        }

        // get keypoint coordinates of good matches to find homography and remove outliers using ransac
        List<Point> pts1 = new ArrayList<>();
        List<Point> pts2 = new ArrayList<>();
        for (int i = 0; i < good_matches.size(); i++) {
            pts1.add(keypoints1.toList().get(good_matches.get(i).queryIdx).pt);
            pts2.add(keypoints2.toList().get(good_matches.get(i).trainIdx).pt);
        }

        // convertion of data types - there is maybe a more beautiful way
        Mat outputMask = new Mat();
        MatOfPoint2f pts1Mat = new MatOfPoint2f();
        pts1Mat.fromList(pts1);
        MatOfPoint2f pts2Mat = new MatOfPoint2f();
        pts2Mat.fromList(pts2);

        // Find homography - here just used to perform match filtering with RANSAC, but could be used to e.g. stitch images
        // the smaller the allowed reprojection error (here 15), the more matches are filtered
        //Mat Homog = Calib3d.findHomography(pts1Mat, pts2Mat, Calib3d.RANSAC, 15, outputMask, 2000, 0.995);
        Mat Homog = Calib3d.findHomography(pts1Mat, pts2Mat, Calib3d.RANSAC, 0.995, outputMask);
        // outputMask contains zeros and ones indicating which matches are filtered
        LinkedList<DMatch> better_matches = new LinkedList<>();
        for (int i = 0; i < good_matches.size(); i++) {
            if (outputMask.get(i, 0)[0] != 0.0) {
                better_matches.add(good_matches.get(i));
            }
        }
        String matchesNumber = Integer.toString(matches.size());
        Log.w("MainActivity", "Orginal mathces number: " + matchesNumber);
        String goodMatchesNumber = Integer.toString(good_matches.size());
        Log.w("MainActivity", "Good mathces number: " + goodMatchesNumber);
        String betterMatchesNumber = Integer.toString(better_matches.size());
        Log.w("MainActivity", "Better mathces number: " + betterMatchesNumber);

        TextView mTextView = findViewById(R.id.textView);

        // DRAWING OUTPUT
        Mat outputImg = new Mat();
        // this will draw all matches, works fine
        MatOfDMatch better_matches_mat = new MatOfDMatch();
        better_matches_mat.fromList(better_matches);
        Features2d.drawMatches(img1, keypoints1, img2, keypoints2, better_matches_mat, outputImg);


        ImageView mImageView = findViewById(R.id.imageView);
        Bitmap bm = Bitmap.createBitmap(outputImg.cols(), outputImg.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(outputImg, bm);
        mImageView.setImageBitmap(bm);
        if (better_matches.size() < (20 * matches.size()) / 100) {
            mTextView.setText("False");
            //return false;
        }
        else {mTextView.setText("True");}
        return true;
    }

}