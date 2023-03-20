package com.play.windman.UI.Activitys;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.play.windman.Beans.Picture;
import com.play.windman.R;
import com.play.windman.UI.Adapters.PictureAdapter;
import com.play.windman.ble.BleActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private Toolbar toolbar;

    private DrawerLayout mDrawerLayout;

    private NavigationView mNavigationView;

    private List<Picture> fruitList = new ArrayList<>();

    private PictureAdapter adapter;

    private SwipeRefreshLayout swipeRefresh;

    private CircleImageView headImg;
    private ViewGroup headLayout;

    private Picture[] pictures = {new Picture("水漫金山", R.mipmap.aa),
            new Picture("湖光山色", R.mipmap.aa),
            new Picture("郁郁葱葱", R.mipmap.aa), new Picture("草长莺飞", R.mipmap.aa),
            new Picture("春山如笑", R.mipmap.aa), new Picture("柳绿花红 ", R.mipmap.aa),
            new Picture("大好河山", R.mipmap.aa)};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBar mActionBar = getSupportActionBar();

        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeAsUpIndicator(R.mipmap.ic_menu);
        }

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        headLayout = (ViewGroup) mNavigationView.getHeaderView(0);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.e(TAG, "onNavigationItemSelected: " + item.getItemId());
                switch (item.getItemId()) {
                    case R.id.nav_map:
                        GaoDeMapActivity.actionStart(MainActivity.this, "");
                        break;
                    case R.id.nav_ble:
                        BleActivity.actionStart(MainActivity.this, "");
                        break;
                }
                mDrawerLayout.closeDrawers();

                return true;
            }
        });


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(MainActivity.this, "悬浮按钮被点击了", Toast.LENGTH_SHORT).show();

                Snackbar.make(view, "你好！", Snackbar.LENGTH_SHORT)
                        .setAction("发送", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toast.makeText(MainActivity.this, "消息已发送！", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
            }
        });


        initDatas();
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter = new PictureAdapter(fruitList);
        recyclerView.setAdapter(adapter);
        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
//        swipeRefresh.setColorSchemeColors(Color.RED, Color.BLUE);
        swipeRefresh.setColorSchemeResources(R.color.chinaRed);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshDatas();
            }
        });
        headImg = headLayout.findViewById(R.id.icon_image);
        headImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), 100);
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 101);
            }
        });
    }

    private void refreshDatas() {
        //网络获取数据
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // 获取数据
                initDatas();
                swipeRefresh.setRefreshing(false);
            }
        }, 2000);
    }

    private void initDatas() {
        for (int i = 0; i < pictures.length; i++) {
            fruitList.add(pictures[i]);
        }
    }

    @OnClick(R.id.weather)
    public void onWeatherClick() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                break;
            case R.id.backup:
                Toast.makeText(this, "按钮被点击了", Toast.LENGTH_SHORT).show();
                break;
            case R.id.delete:
                Toast.makeText(this, "按钮被点击了", Toast.LENGTH_SHORT).show();
                break;


            default:
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            final Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            Log.e(TAG, "onActivityResult: " + bitmap);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    headImg.setImageBitmap(bitmap);
                }
            });
        } else if (requestCode == 101) {
            Uri uri = data.getData();
            Log.e(TAG, "onActivityResult: " + uri.toString());
            Intent crop = crop(uri);
            startActivityForResult(crop, 100);
        }
    }

    public Intent crop(Uri uri){
        Intent intent=new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri,"image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 250);
        intent.putExtra("outputY", 250);
        intent.putExtra("outputFromet", "JPEG");
        intent.putExtra("return-data", true);
        return intent;
    }
}
