package com.poi.app;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.cloud.CloudListener;
import com.baidu.mapapi.cloud.CloudManager;
import com.baidu.mapapi.cloud.CloudRgcResult;
import com.baidu.mapapi.cloud.CloudSearchResult;
import com.baidu.mapapi.cloud.DetailSearchResult;
import com.baidu.mapapi.cloud.NearbySearchInfo;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.PoiInfo;
import com.poi.app.BaiduLocation.BaiduMapUtilByRacer;
import com.poi.app.BaiduLocation.BaiduMapUtilByRacer.GeoCodePoiListener;
import com.poi.app.BaiduLocation.BaiduMapUtilByRacer.LocateListener;
import com.poi.app.BaiduLocation.LocationBean;
import com.poi.app.BaiduLocation.PoiOverlay;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements CloudListener {

    private static final String TAG = "MainActivity";

    @BindView(R.id.mMapView)
    MapView mMapView;
    @BindView(R.id.tvLocation)
    TextView tvLocation;
    @BindView(R.id.imgLocation)
    ImageView imgLocation;

    private CloudManager mCloudManager;
    private BaiduMap mBaiduMap;
    private LocationBean mLocationBean;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mCloudManager = CloudManager.getInstance();
        mCloudManager.init();
        mCloudManager.registerListener(MainActivity.this);
        locate();
        BaiduMapUtilByRacer.goneMapViewChild(mMapView, true, true);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(20));
        mBaiduMap.setOnMapStatusChangeListener(mapStatusChangeListener);
        mBaiduMap.setOnMapClickListener(mapOnClickListener);
        mMapView.showZoomControls(false);//去掉放大缩小按钮
        mBaiduMap.getUiSettings().setZoomGesturesEnabled(true);// 缩放手势
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);

        /*定位监听*/
        imgLocation.setOnClickListener(v -> {
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.zoomTo(20));
            locate();
        });
    }


    @Override
    public void onGetSearchResult(CloudSearchResult result, int error) {
        if (result != null && result.poiList != null && result.poiList.size() > 0) {
            mBaiduMap.clear();
            PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(overlay);
            overlay.setData(result);
            overlay.addToMap();
            return;
        }
    }

    @Override
    public void onGetDetailSearchResult(DetailSearchResult result, int error) {

    }

    @Override
    public void onGetCloudRgcResult(CloudRgcResult result, int error) {

    }


    /*Marks点击事件*/
    private class MyPoiOverlay extends PoiOverlay {

        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            CloudSearchResult cloudSearchResult = getPoiResult();
            String address = cloudSearchResult.poiList.get(index).address;
            Toast.makeText(MainActivity.this, address, Toast.LENGTH_SHORT).show();
            Log.i(TAG, "onPoiClick: " + address);
            return true;
        }
    }


    /*定位当前位置*/
    public void locate() {
        BaiduMapUtilByRacer.locateByBaiduMap(MainActivity.this, 2000, new LocateListener() {

            @Override
            public void onLocateSucceed(LocationBean locationBean) {
                mLocationBean = locationBean;
                if (mMarker != null) {
                    mMarker.remove();
                } else {
                    mBaiduMap.clear();
                }
                mMarker = BaiduMapUtilByRacer.showMarkerByResource(
                        locationBean.getLatitude(),
                        locationBean.getLongitude(), R.mipmap.a5a,
                        mBaiduMap, 0, true);
            }

            @Override
            public void onLocateFiled() {

            }

            @Override
            public void onLocating() {

            }
        });
    }


    private boolean isCanUpdateMap = true;
    BaiduMap.OnMapStatusChangeListener mapStatusChangeListener = new BaiduMap.OnMapStatusChangeListener() {
        /**
         * 手势操作地图，设置地图状态等操作导致地图状态开始改变。
         * @param status  地图状态改变开始时的地图状态
         */
        public void onMapStatusChangeStart(MapStatus status) {
        }

        @Override
        public void onMapStatusChangeStart(MapStatus mapStatus, int i) {

        }

        /**
         * 地图状态变化中
         * @param status 当前地图状态
         */
        public void onMapStatusChange(MapStatus status) {
        }

        /**
         * 地图状态改变结束
         * @param status  地图状态改变结束后的地图状态
         */
        public void onMapStatusChangeFinish(MapStatus status) {
            if (isCanUpdateMap) {
                String latLng = status.target.longitude + "," + status.target.latitude;
                NearbySearchInfo info = new NearbySearchInfo();
                info.ak = "7ACh0QxRhqWGopY2fauK3hTsVzmEg8Y9";
                info.geoTableId = 190051;
                info.radius = 100;
                info.location = latLng;
                mCloudManager.nearbySearch(info);
                // 反Geo搜索
                LatLng ptCenter = new LatLng(status.target.latitude, status.target.longitude);
                reverseGeoCode(ptCenter);
            } else {
                isCanUpdateMap = true;
            }
        }
    };

    /*当前位置*/
    public void reverseGeoCode(LatLng ll) {
        BaiduMapUtilByRacer.getPoisByGeoCode(ll.latitude, ll.longitude, new GeoCodePoiListener() {

            @Override
            public void onGetSucceed(LocationBean locationBean, List<PoiInfo> poiList) {
                mLocationBean = (LocationBean) locationBean.clone();
                String locateAddress = mLocationBean.getProvince()
                        + mLocationBean.getCity()
                        + mLocationBean.getDistrict()
                        + mLocationBean.getStreet()
                        + mLocationBean.getStreetNum();
                tvLocation.setVisibility(View.VISIBLE);
                tvLocation.setText(String.valueOf("发货地址:" + locateAddress));

            }

            @Override
            public void onGetFailed() {
                tvLocation.setVisibility(View.INVISIBLE);
                Toast.makeText(MainActivity.this, "抱歉，未能找到结果", Toast.LENGTH_SHORT).show();
            }
        });
    }


    /*地图点击事件*/
    BaiduMap.OnMapClickListener mapOnClickListener = new BaiduMap.OnMapClickListener() {
        /**
         * 地图单击事件回调函数
         *  @param point  点击的地理坐标
         */
        public void onMapClick(LatLng point) {
        }

        /**
         * 地图内 Poi 单击事件回调函数
         * @param poi  点击的 poi 信息
         */
        public boolean onMapPoiClick(MapPoi poi) {
            return false;
        }
    };

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        mLocationBean = null;
        /* 释放监听者 */
        mCloudManager.unregisterListener();
        mCloudManager.destroy();
        mCloudManager = null;
        if (mBaiduMap != null) {
            // 关闭定位图层
            mBaiduMap.setMyLocationEnabled(false);
            mBaiduMap = null;
        }
        if (mMapView != null) {
            mMapView.destroyDrawingCache();
            mMapView.onDestroy();
            mMapView = null;
        }
        mMarker = null;
        super.onDestroy();
        System.gc();
    }
}
