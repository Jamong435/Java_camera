package com.kim9212.ex71camera;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.audiofx.EnvironmentalReverb;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ImageView iv;

    //캡쳐한 이미지 경로 Uri 참조변수
    Uri imgUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageView iv;

        //외부저장소 사용에 대한 동적퍼미션
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ){
            int permissionResult= checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);

            if(permissionResult== PackageManager.PERMISSION_DENIED){
                //퍼미션 체크 다이얼로그
                String[] permissions= new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
                requestPermissions(permissions, 100);
            }
        }
    }//onCreate method..

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode){
            case 100:
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "사진저장이 가능합니다.", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this, "촬영된 사진저장이 불가합니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    public void clickFab(View view) {
        //카메라 앱 실행
        Intent intent= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        //카메라 앱에게 캡쳐한 이미지를 저장하게 하려면..
        //저장할 이미지의 파일경로 Uri를 미리 지정해 줘야 만 함.

        //캡쳐된 이미지 Uri를 정하는 메소드(코드가 복잡해서..메소드로 따로 만들어 사용)
        setImageUri();
        if(imgUri!=null) intent.putExtra(MediaStore.EXTRA_OUTPUT, imgUri);

        startActivityForResult(intent, 30);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case 30:
                if(resultCode==RESULT_OK){
                    //사전정보를 가지고 돌아온 Intent객체에게 정보달라고
                    // 저장하도록 putExtra작업을 하면 Intent가 안오는 경우도 있음.
                    if(data!=null){
                        //돌아온 Intent객체에게 Data달라고
                        Uri uri= data.getData();
                        if(uri!=null){//Uri로 올때
                            Glide.with(this).load(uri).into(iv);
                            Toast.makeText(this, "Uri로 받음", Toast.LENGTH_SHORT).show();

                        }else{ //Bitmap으로 올때
                            //Bitmap을 보여주면 해상도가 좋지 않으므로..
                            if(imgUri!=null) Glide.with(this).load(imgUri).into(iv);
                            Toast.makeText(this, "Bitmap으로 돌아옴.", Toast.LENGTH_SHORT).show();

                        }

                    }else{
                        //돌아온 인텐트 객체가 없는 경우가 있으므로..
                        //카메라앱에게 저장하도록 요청했던 사진경고 Uri를 이용하여
                        //이미지 보여주기
                        if(imgUri!=null) Glide.with(this).load(imgUri).into(iv);
                        Toast.makeText(this, "인텐트가 안 돌아왔음.", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }

    }

    //캡쳐한 이미지를 저장할 파일경로 Uri 정하는 메소드
    void setImageUri(){

        //외부저장소에(SDcard) 저장하는 것을 권장함.
        //이때 외부저장소의 2가지 영역이 존재함.
        //1. 외부저장소 중에서 본인 앱에 할당된 영역 - 퍼미션 없어도 됨. 특징 : 앱을 지우면 저장된 사진도 같이 지워짐.
        //File path= getExternalFilesDir("photo");

        //2. 외부저장소 중에서 공용영역- 동적 퍼미션 필요. 특징: 앱을 지워도 사진이 저장되어 있음.
        File path= Environment.getExternalStorageDirectory();//외부메모리의 최상위(root) 경로 [ storage/emulated/0/인 경로 ]
        path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES); // [ storage/emulated/0/pictures/인 경로]

        //경로를 정했으므로... 파일명 정하기..
        //같은 이름으로 저장하면 덮어쓰기가 되므로 저장할 때마다 다른 이름으로..
        //통상 일시를 이용해서 파일명 정함 ex) "IMG_20200611104231.jpg"
        //1) 날짜를 이용하는 방법
        SimpleDateFormat sdf= new SimpleDateFormat("yyyyMMddhhmmss");
        String fileName= "IMG_"+ sdf.format(new Date()) +".jpg";
        //파일명과 경로를 합쳐서 File객체 생성
        File file= new File(path, fileName);

        //2) 자동으로 임시파일명을 만들어주는 메소드 이용하는 방법
//        try {
//            file= File.createTempFile("IMG_", ".jpg", path);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //카메라앱에 전달해줘야할 저장파일경로는 File객체가 아니라 Uri객체여야 함.
        // File --> Uri 변환
        // 누가(N) 버전(api 24)부터 이 변환이 어려워 졌음...
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.N){
            imgUri= Uri.fromFile(file);
        }else{

            // 다른 앱에게 파일의 접근을 허용하도록 하는 Content Provider 를 이용해야 함.
            // Provider중에서 안드로이드에 이미 만들어져 있는 FileProvider를 이용
            // 작업순서
            // 1. Manifext.xml에 <provider>태그를 이용하여 FileProvider클래스 등록
            // 2. FileProvider가 공개할 파일의 경로를 res>>xml폴더안에 "paths.xml"이라는 이름으로 만들어서 <path>태그로 경로 지정
            // 3. 자바에서 <provider>태그에 작성한 속성 중 authorities=""에 작성한 값을 사용

            imgUri= FileProvider.getUriForFile(this, "com.mrhi2020.ex71cameratest2.provider", file);
        }

        //imgUri작업 잘 되었는지 확인
        new AlertDialog.Builder(this).setMessage(imgUri.toString()).create().show();

    }

}
