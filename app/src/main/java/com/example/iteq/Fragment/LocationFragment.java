package com.example.iteq.Fragment;

import android.Manifest;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.trace.LBSTraceClient;
import com.baidu.trace.Trace;
import com.baidu.trace.api.entity.EntityInfo;
import com.baidu.trace.api.entity.EntityListRequest;
import com.baidu.trace.api.entity.EntityListResponse;
import com.baidu.trace.api.entity.FilterCondition;
import com.baidu.trace.api.entity.OnEntityListener;
import com.baidu.trace.model.CoordType;
import com.baidu.trace.model.OnTraceListener;
import com.baidu.trace.model.PushMessage;
import com.example.iteq.R;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.trello.rxlifecycle2.components.support.RxFragment;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;


public class LocationFragment extends RxFragment implements View.OnClickListener {
    private View mView;
    private Unbinder unbinder;
    @BindView(R.id.bmapView)
    MapView mMapView;
    private BaiduMap mBaiduMap;
    @BindView(R.id.refresh)
    ImageButton mRefreshButton;
    private LocationClient mLocationClient;
    private LatLng latLng;
    private LBSTraceClient mTraceClient;
    private Trace mTrace;
    // 轨迹服务ID
    private long serviceId = 213621;
    private static final String TAG = "LocationFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_location, container, false);
        unbinder = ButterKnife.bind(this, mView);
        mBaiduMap = mMapView.getMap();
        mRefreshButton.setOnClickListener(this);
        initPermission();
        return mView;

    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
        initTrace();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTraceClient.stopTrace(mTrace, mTraceListener);
        mTraceClient = null;
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        unbinder.unbind();
    }


    private void initPermission() {
        new RxPermissions(this).request(Manifest.permission.ACCESS_FINE_LOCATION).subscribe(new Consumer<Boolean>() {
            @Override
            public void accept(Boolean aBoolean) throws Exception {
                if (aBoolean) {
                    mBaiduMap.setMyLocationEnabled(true);
                    initLocation();
                } else {
                    Toast.makeText(getActivity(), "请授予本软件定位权限", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    //初始化定位
    private void initLocation() {
        mLocationClient = new LocationClient(getActivity());

        LocationClientOption option = new LocationClientOption();
        option.setOpenGps(true);
        option.setCoorType("bd09ll");
//        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);
        MyLocationListener myLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);
        mLocationClient.start();
    }

    //初始化轨迹服务
    private void initTrace() {
        // 设备标识
        String entityName = "ID150222";
        boolean isNeedObjectStorage = false;
        mTrace = new Trace(serviceId, entityName, isNeedObjectStorage);

        mTraceClient = new LBSTraceClient(getActivity());
        // 定位周期(单位:秒)
        int gatherInterval = 5;
        // 打包回传周期(单位:秒)
        int packInterval = 10;
        mTraceClient.setInterval(gatherInterval, packInterval);
        mTraceClient.startTrace(mTrace, mTraceListener);
        Observable.interval(5, TimeUnit.SECONDS).compose(bindToLifecycle()).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                queryEntity();
            }
        });

    }

    //获取最近一天线设备
    private void queryEntity() {
        // 请求标识
        int tag = 5;
//设置活跃时间
        long activeTime = System.currentTimeMillis() / 1000 - 12 * 60 * 60;
// 过滤条件
        FilterCondition filterCondition = new FilterCondition();
// 查找当前时间5分钟之内有定位信息上传的entity
        filterCondition.setActiveTime(activeTime);
// 返回结果坐标类型
        CoordType coordTypeOutput = CoordType.bd09ll;
// 分页索引
        int pageIndex = 1;
// 分页大小
        int pageSize = 1000;


// 创建Entity列表请求实例
        EntityListRequest request = new EntityListRequest(tag, serviceId, filterCondition, coordTypeOutput, pageIndex, pageSize);


// 查询Entity列表
        mTraceClient.queryEntityList(request, entityListener);
    }

    @Override
    public void onClick(View view) {
        initPermission();
    }


    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            //mapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    // 此处设置开发者获取到的方向信息，顺时针0-360
                    .direction(location.getDirection()).latitude(location.getLatitude())
                    .longitude(location.getLongitude()).build();
            mBaiduMap.setMyLocationData(locData);
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(latLng, 12f);
            mBaiduMap.setMapStatus(mapStatusUpdate);
        }
    }

    public OnTraceListener mTraceListener = new OnTraceListener() {

        @Override
        public void onBindServiceCallback(int i, String s) {

        }

        @Override
        public void onStartTraceCallback(int i, String s) {
            if (i == 0) {
                mTraceClient.startGather(this);
            }
        }

        @Override
        public void onStopTraceCallback(int i, String s) {
            mTraceClient.stopGather(this);

        }

        @Override
        public void onStartGatherCallback(int i, String s) {


        }

        @Override
        public void onStopGatherCallback(int i, String s) {
        }

        @Override
        public void onPushCallback(byte b, PushMessage pushMessage) {

        }

        @Override
        public void onInitBOSCallback(int i, String s) {

        }
    };
    // 初始化监听器
    public OnEntityListener entityListener = new OnEntityListener() {
        @Override
        public void onEntityListCallback(EntityListResponse response) {
            Log.e(TAG, "onEntityListCallback: ");
            if (response.getSize() > 0) {
                //定义Maker坐标点
                Log.e(TAG, "onEntityListCallback: " + response.getSize());
                List<EntityInfo> infos = response.getEntities();
                for (EntityInfo i : infos
                ) {
                    LatLng latLng = new LatLng(i.getLatestLocation().getLocation().getLatitude(), i.getLatestLocation().getLocation().getLongitude());
                    showPerson(latLng);

                }
            }
        }
    };

    private void showPerson(LatLng latLng) {
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.person);
        OverlayOptions option = new MarkerOptions()
                .perspective(true)
                .position(latLng)
                .icon(bitmap);
        mBaiduMap.addOverlay(option);
    }

}
