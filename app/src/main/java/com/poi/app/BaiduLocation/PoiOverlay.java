package com.poi.app.BaiduLocation;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.baidu.mapapi.cloud.CloudSearchResult;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.poi.PoiResult;
import com.poi.app.R;

import java.util.ArrayList;
import java.util.List;

import static com.baidu.mapapi.BMapManager.getContext;

/**
 * 用于显示poi的overly
 */
public class PoiOverlay extends OverlayManager {

    private static final int MAX_POI_SIZE = 10;

    private CloudSearchResult mPoiResult = null;

    /**
     * 构造函数
     *
     * @param baiduMap 该 PoiOverlay 引用的 BaiduMap 对象
     */
    public PoiOverlay(BaiduMap baiduMap) {
        super(baiduMap);
    }

    /**
     * 设置POI数据
     *
     * @param poiResult 设置POI数据
     */
    public void setData(CloudSearchResult poiResult) {
        this.mPoiResult = poiResult;
    }

    @Override
    public final List<OverlayOptions> getOverlayOptions() {
        if (mPoiResult == null) {
            return null;
        }
        List<OverlayOptions> markerList = new ArrayList<>();
        int markerSize = 0;
        for (int i = 0; i < mPoiResult.poiList.size() && markerSize < MAX_POI_SIZE; i++) {
            if (mPoiResult.poiList == null) {
                continue;
            }

            View view = View.inflate(getContext(), R.layout.areaoverlay_view, null);
            TextView tvName = view.findViewById(R.id.title);
            tvName.setText(mPoiResult.poiList.get(i).title);
            BitmapDescriptor DefaultBD = BitmapDescriptorFactory.fromView(view);

            markerSize++;
            Bundle bundle = new Bundle();
            bundle.putInt("index", i);
            LatLng latLng = new LatLng(mPoiResult.poiList.get(i).latitude, mPoiResult.poiList.get(i).longitude);
            markerList.add(new MarkerOptions()
                    .icon(DefaultBD)
                    .extraInfo(bundle)
                    .position(latLng));

        }
        return markerList;
    }

    /**
     * 获取该 PoiOverlay 的 poi数据
     *
     * @return
     */
    public CloudSearchResult getPoiResult() {
        return mPoiResult;
    }

    /**
     * 覆写此方法以改变默认点击行为
     *
     * @param i 被点击的poi在
     *          {@link com.baidu.mapapi.search.poi.PoiResult#getAllPoi()} 中的索引
     * @return
     */
    public boolean onPoiClick(int i) {
//        if (mPoiResult.getAllPoi() != null
//                && mPoiResult.getAllPoi().get(i) != null) {
//            Toast.makeText(BMapManager.getInstance().getContext(),
//                    mPoiResult.getAllPoi().get(i).name, Toast.LENGTH_LONG)
//                    .show();
//        }
        return false;
    }

    @Override
    public final boolean onMarkerClick(Marker marker) {
        if (!mOverlayList.contains(marker)) {
            return false;
        }
        if (marker.getExtraInfo() != null) {
            return onPoiClick(marker.getExtraInfo().getInt("index"));
        }
        return false;
    }

    @Override
    public boolean onPolylineClick(Polyline polyline) {
        // TODO Auto-generated method stub
        return false;
    }
}
