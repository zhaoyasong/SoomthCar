package com.cnpc.hyxt.zys.smoothcar;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Polyline;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;

import java.util.ArrayList;
import java.util.List;

/*
 * 创建车辆平滑移动的界面
 */
public class MainActivity extends AppCompatActivity {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private Polyline mPolyline;
    private Handler mHandler;

    private List<List<LatLng>> tuoche;
    private List<LatLng> latlngs1;//拖车1经纬度集合
    private List<LatLng> latlngs2;//拖车2经纬度集合

    // 通过设置间隔时间和距离可以控制速度和图标移动的距离
    private static final int TIME_INTERVAL = 150;
    private static final double DISTANCE = 0.00009;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        MapStatus.Builder builder = new MapStatus.Builder();
        //设置地图中心点
        builder.target(new LatLng(40.069187, 116.353557));//故障车当前位置
        builder.zoom(19.0f);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        mHandler = new Handler(Looper.getMainLooper());

        initData();

        for (int i = 0; i < tuoche.size(); i++) {
            drawPolyLine(tuoche.get(i));
        }
    }

    private void drawPolyLine(List<LatLng> polylines) {

        polylines.add(polylines.get(0));//将轨迹连成一个完整的环形

        PolylineOptions polylineOptions = new PolylineOptions().points(polylines).width(8).color(Color.RED);
        mPolyline = (Polyline) mBaiduMap.addOverlay(polylineOptions);
        OverlayOptions markerOptions;
        markerOptions = new MarkerOptions().flat(true).anchor(0.5f, 0.5f)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car01)).position(polylines.get(0))
                .rotate((float) getAngle(0));

        //mMoveMarker局部变量，一辆小车一个移动箭头
        Marker mMoveMarker = (Marker) mBaiduMap.addOverlay(markerOptions);

        moveLooper(polylines, mMoveMarker);
    }

    /**
     * 循环进行移动逻辑
     */
    public void moveLooper(final List<LatLng> polylines, final Marker mMoveMarker) {
        new Thread() {
            public void run() {
//                while (true) {
                for (int i = 0; i < polylines.size() - 1; i++) {
                    final LatLng startPoint = polylines.get(i);
                    final LatLng endPoint = polylines.get(i + 1);
                    Log.e("--", startPoint.latitude + "/" + startPoint.longitude);
                    mMoveMarker.setPosition(startPoint);
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            // refresh marker's rotate
                            if (mMapView == null) {
                                return;
                            }
                            mMoveMarker.setRotate((float) getAngle(startPoint,
                                    endPoint));
                        }
                    });
                    double slope = getSlope(startPoint, endPoint);
                    // 是不是正向的标示
                    boolean isReverse = (startPoint.latitude > endPoint.latitude);

                    double intercept = getInterception(slope, startPoint);

                    double xMoveDistance = isReverse ? getXMoveDistance(slope) : -1 * getXMoveDistance(slope);


                    for (double j = startPoint.latitude; !((j > endPoint.latitude) ^ isReverse);
                         j = j - xMoveDistance) {
                        LatLng latLng = null;
                        if (slope == Double.MAX_VALUE) {
                            latLng = new LatLng(j, startPoint.longitude);
                        } else {
                            latLng = new LatLng(j, (j - intercept) / slope);
                        }

                        final LatLng finalLatLng = latLng;
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mMapView == null) {
                                    return;
                                }
                                mMoveMarker.setPosition(finalLatLng);//走完一圈回到起点
                            }
                        });
                        try {
                            Thread.sleep(TIME_INTERVAL);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }
//            }

        }.start();
    }

    //拖车经纬度集合
    public void initData() {

        tuoche = new ArrayList<List<LatLng>>();
        latlngs1 = new ArrayList<LatLng>(); //第一辆车经纬度集合
//        latlngs2 = new ArrayList<LatLng>();//第二辆车经纬度集合


//        builder.target(new LatLng(31.9695208489, 118.7736524881));//故障车当前位置
        latlngs1.add(new LatLng(40.069187, 116.353557));
        latlngs1.add(new LatLng(40.070402, 116.353341));
        latlngs1.add(new LatLng(40.072445, 116.357006));
        latlngs1.add(new LatLng(40.074764, 116.360887));
        latlngs1.add(new LatLng(40.077469, 116.365415));
        latlngs1.add(new LatLng(40.0796772, 116.36908));
        latlngs1.add(new LatLng(40.081665, 116.374757));
        latlngs1.add(new LatLng(40.085142, 116.374326));
        latlngs1.add(new LatLng(40.087242, 116.373679));
        latlngs1.add(new LatLng(40.0867983, 116.36793));
        latlngs1.add(new LatLng(40.086246, 116.356432));
        latlngs1.add(new LatLng(40.083123, 116.351329));


//        latlngs2.add(new LatLng(31.9733642960, 118.7662912632));
//        latlngs2.add(new LatLng(31.9735350000, 118.7557740000));
//        latlngs2.add(new LatLng(31.9714564110, 118.7504893514));
//        latlngs2.add(new LatLng(31.9755604110, 118.7513513514));
//        latlngs2.add(new LatLng(31.9767854110, 118.7518543514));
//        latlngs2.add(new LatLng(31.9776434110, 118.7491953514));
//        latlngs2.add(new LatLng(31.9812311339, 118.7433553933));

        tuoche.add(latlngs1);
//        tuoche.add(latlngs2);
    }

    /**
     * 根据点获取图标转的角度
     */
    private double getAngle(int startIndex) {
        if ((startIndex + 1) >= mPolyline.getPoints().size()) {
            throw new RuntimeException("index out of bonds");
        }
        LatLng startPoint = mPolyline.getPoints().get(startIndex);
        LatLng endPoint = mPolyline.getPoints().get(startIndex + 1);
        return getAngle(startPoint, endPoint);
    }

    /**
     * 根据两点算取图标转的角度
     */
    private double getAngle(LatLng fromPoint, LatLng toPoint) {
        double slope = getSlope(fromPoint, toPoint);
        if (slope == Double.MAX_VALUE) {
            if (toPoint.latitude > fromPoint.latitude) {
                return 0;
            } else {
                return 180;
            }
        }
        float deltAngle = 0;
        if ((toPoint.latitude - fromPoint.latitude) * slope < 0) {
            deltAngle = 180;
        }
        double radio = Math.atan(slope);
        double angle = 180 * (radio / Math.PI) + deltAngle - 90;
        return angle;
    }

    /**
     * 根据点和斜率算取截距
     */
    private double getInterception(double slope, LatLng point) {

        double interception = point.latitude - slope * point.longitude;
        return interception;
    }

    /**
     * 算斜率
     */
    private double getSlope(LatLng fromPoint, LatLng toPoint) {
        if (toPoint.longitude == fromPoint.longitude) {
            return Double.MAX_VALUE;
        }
        double slope = ((toPoint.latitude - fromPoint.latitude) / (toPoint.longitude - fromPoint.longitude));
        return slope;

    }

    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        mBaiduMap.clear();
    }

    /**
     * 计算x方向每次移动的距离
     */
    private double getXMoveDistance(double slope) {
        if (slope == Double.MAX_VALUE) {
            return DISTANCE;
        }
        return Math.abs((DISTANCE * slope) / Math.sqrt(1 + slope * slope));
    }
}
