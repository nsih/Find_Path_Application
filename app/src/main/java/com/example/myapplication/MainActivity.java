package com.example.myapplication;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;




public class MainActivity extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback
{

    private String tMapKey = "l7xx0cbb43a68b824ac79560cdf4d46b8060";

    private boolean isDraw;
    private boolean isNodeDraw;

    private ArrayList<WayPoint> m_mapPoint = new ArrayList<WayPoint>();
    private ArrayList<WayPoint> m_nodePoint = new ArrayList<WayPoint>();
    private ArrayList<WayPoint> m_warnPoint = new ArrayList<WayPoint>();

    //private ArrayList<TMapPoint> points = new ArrayList<TMapPoint>();

    public static int safety = 0;

    public static Context context;
    public static TMapView tMapView;

    TMapGpsManager tMapGPS = null;
    public static TMapPoint myPoint;    //gps 포인트
    public static TMapPoint endPoint;   //목적지 포인트

    static final int SMS_RECEIVE_PERMISSON=1;

    public String sAddr = null;
    public String eAddr = null;

    /*
    private DrawerLayout drawerLayout;
    private View drawerView;

     */


    //public String result;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate((savedInstanceState));
        setContentView(R.layout.activity_main);
        setContentView(R.layout.activity_main);


        ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setIcon(R.mipmap.ic_cp);


        LinearLayout linearLayoutTmap = (LinearLayout) findViewById(R.id.linearLayoutTmap);
        tMapView =  new TMapView(this);
        context = this;

        tMapView.setZoomLevel(14);
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);

        /*
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawerView = (View) findViewById(R.id.drawerView);
         */

        ShowMap(linearLayoutTmap, tMapView);

        button(tMapView);

        //Locating Permission
        if(ActivityCompat.checkSelfPermission
                (this,
                        Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED)
        {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1); //위치권한 탐색 허용 관련 내용
            }
            return;
        }
        //context permission
        if(ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.READ_CONTACTS}, 1); //연락처 탐색 허용 관련 내용
            }
            return;
        }

        tMapView.setIconVisibility(true);
        Locating();

        //RecievePoint();   //자기위치, 클릭지정.
        DrawMarker();


    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu1, menu);
        return true;
    }
    @Override
    public void onLocationChange(Location location)
    {
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());

        myPoint = new TMapPoint(location.getLongitude(), location.getLatitude());


        //
        GetWarningData mGetWarningData = new GetWarningData();
        mGetWarningData.start();

    }

    public String receiveName = null;
    public String receivePhone = null;
    public String imgUrl = null;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        if(requestCode == 0)
        {
            if(resultCode == RESULT_CANCELED)
            {
                Toast.makeText(getApplicationContext(), "RESULT_CANCELED", Toast.LENGTH_LONG).show();
            }

            else if (resultCode == RESULT_OK)
            {
                Cursor cursor = getContentResolver().query(
                        data.getData(),
                        new String[]{ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                                ContactsContract.CommonDataKinds.Phone.NUMBER},
                        null,null,null);
                cursor.moveToFirst();
                receiveName = cursor.getString(0);
                receivePhone = cursor.getString(1);
                cursor.close();

                Toast.makeText(getApplicationContext(), "마중번호 : "+receivePhone, Toast.LENGTH_LONG).show();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }


    public void GetEndPoint()
    {
        endPoint = tMapView.getCenterPoint();

        double Latitude = endPoint.getLatitude();
        double Longtitude = endPoint.getLongitude();

        TMapPoint tMapPointMarker = new TMapPoint(Latitude ,Longtitude);

        TMapMarkerItem tItem = new TMapMarkerItem();

        tItem.setTMapPoint(tMapPointMarker);
        tItem.setName("end point");
        tItem.setVisible(TMapMarkerItem.VISIBLE);

        //thread start

        GetEndPointAddr mGetEndPointAddr = new GetEndPointAddr();
        mGetEndPointAddr.start();




        Bitmap bitmapPass = BitmapFactory.decodeResource(context.getResources(),R.drawable.poi);
        tItem.setIcon(bitmapPass);

        tItem.setPosition(0.5f, 1.0f);         // 마커의 중심점을 하단, 중앙으로 설정

        tMapView.addMarkerItem(tItem.getName(),tItem);
    }
    void ShowMap( android.widget.LinearLayout linearLayoutTmap , com.skt.Tmap.TMapView tMapView)
    {
        tMapView.setSKTMapApiKey(tMapKey);
        linearLayoutTmap.addView(tMapView);
    }
    void Locating()
    {
        tMapGPS = new TMapGpsManager(this);

        tMapGPS.setMinTime(5000);
        tMapGPS.setMinDistance(10);
        tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);
        tMapGPS.setProvider(tMapGPS.GPS_PROVIDER);

        tMapGPS.OpenGps();
    }

    void button(com.skt.Tmap.TMapView tMapView)
    {
        Button buttonDefault = (Button)findViewById(R.id.buttonDefault);
        Button buttonAdvance = (Button)findViewById(R.id.buttonAdvance);

        Button buttonSetEnd = (Button)findViewById(R.id.buttonSetEnd);
        Button buttonStart = (Button)findViewById(R.id.buttonStart);


        buttonDefault.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                safety = 0;
                Toast.makeText(getApplicationContext(), "Basic Mode로 길을 찾습니다.", Toast.LENGTH_LONG).show();
            }
        });

        buttonAdvance.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                safety = 1;
                Toast.makeText(getApplicationContext(), "Advenced Mode로 길을 찾습니다.", Toast.LENGTH_LONG).show();

            }
        });

        buttonStart.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(isDraw == true)
                {
                    isDraw = false;
                }

                else
                {
                    isDraw = true;
                }


                GetData mGetData = new GetData();
                mGetData.start();
            }
        });

        buttonSetEnd.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                GetEndPoint();
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)    //option
    {
        GetNodeData mGetNodeData = new GetNodeData();

        switch (item.getItemId()) {
            case R.id.item1:
                if(isNodeDraw == true)
                    isNodeDraw = false;
                else
                    isNodeDraw = true;

                mGetNodeData.start();

                return true;


                //public String receiveName = null;
            //    public String receivePhone = null;
            case R.id.item2:
                mDistance(myPoint.getLatitude(), myPoint.getLongitude(), m_mapPoint.get(m_mapPoint.size()-1).getLatitude(),m_mapPoint.get(m_mapPoint.size()-1).getLongitude());
                SendSMS(receivePhone,"");

                return true;

            case R.id.item3:
                //단말기에 내장되어 있는 연락처앱을 호출한다.
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(ContactsContract.CommonDataKinds.Phone.CONTENT_URI);

                setResult(Activity.RESULT_OK, intent);
                startActivityForResult(intent, 0);

                //Toast.makeText(getApplicationContext(), "SMS 권한 있음", Toast.LENGTH_SHORT).show();
                return true;

            case R.id.item4:
                isNodeDraw = false;
                NodeMarkerCon mNodeMarkerCon = new NodeMarkerCon();
                mNodeMarkerCon.start();

                return true;
        }
        return false;
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (Build.VERSION.SDK_INT >= 23)
        {
            if(grantResults.length > 0  && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                Log.d( ".","Permission: "+permissions[0]+ "was "+grantResults[0]);

            }
            else {
                Log.d(".","Permission denied");
            }
        }
    }

    ///////////////
    //Send MSG


    private void SendSMS(String phoneNumber, String message)
    {
        //SMS Permission check
        int permissonCheck= ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS);
        if(permissonCheck == PackageManager.PERMISSION_GRANTED)
        {
            //Toast.makeText(getApplicationContext(), "SMS 권한 있음", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(getApplicationContext(), "SMS 권한 없음", Toast.LENGTH_SHORT).show();

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.SEND_SMS))
            {
                Toast.makeText(getApplicationContext(), "SMS 권한이 필요합니다", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.SEND_SMS},       1);
            }
            else
            {
                ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.SEND_SMS}, 1);
            }
        }

        //////
        message = "http://api.floodnut.com/api/sms?longi="+myPoint.getLatitude()+"&lati="+myPoint.getLongitude();

        try
        {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, "현재위치", null, null);
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            smsManager.sendTextMessage(phoneNumber, null, "목적지 : " + eAddr, null, null);
            smsManager.sendTextMessage(phoneNumber, null, "약" + (int)mdistance/66 + "분 후 도착예정이니 마중 부탁드립니다.", null, null);




            Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show();
        }
        catch (Exception e)
        {
            Toast.makeText(getApplicationContext(), "전송 오류!", Toast.LENGTH_LONG).show();
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();//오류 원인이 찍힌다.
            e.printStackTrace();
        }
    }

    ///////////////
    // Find Path

    void RecievePoint() //시작점 끝점 찍혀있는 마크로 업데이트
    {
        m_mapPoint.add( new WayPoint("Start",myPoint.getLongitude(),myPoint.getLatitude(),0) );
        m_mapPoint.add( new WayPoint("End",endPoint.getLatitude(), endPoint.getLongitude(), 6) );
    }
    void DrawMarker()   //언젠가 지우는거 여기에 추가해야함.
    {
        //0 출발지 / 6 목적지
        //1 CCTV / 2 경찰서,파출소,지구대 같은거 / 3 교통정보 /4 가로등 / 5 편의점

        for (int i = 0 ; i < m_mapPoint.size() ; i++)
        {
            if(m_mapPoint.get(i).getType() == 1)    //cctv
            {
                TMapPoint tMapPointMarker = new TMapPoint(m_mapPoint.get(i).getLatitude() , m_mapPoint.get(i).getLongitude());

                TMapMarkerItem tItem = new TMapMarkerItem();

                tItem.setTMapPoint(tMapPointMarker);
                tItem.setName("point " + i);
                tItem.setVisible(TMapMarkerItem.VISIBLE);

                Bitmap bitmapCCTV = BitmapFactory.decodeResource(context.getResources(),R.drawable.cctv);
                tItem.setIcon(bitmapCCTV);

                tMapView.addMarkerItem(m_mapPoint.get(i).getName(),tItem);
            }


            if(m_mapPoint.get(i).getType() == 2)    //경찰서,파출소,지구대 같은거
            {
                TMapPoint tMapPointMarker = new TMapPoint(m_mapPoint.get(i).getLatitude() , m_mapPoint.get(i).getLongitude());

                TMapMarkerItem tItem = new TMapMarkerItem();

                tItem.setTMapPoint(tMapPointMarker);
                tItem.setName("point " + i);
                tItem.setVisible(TMapMarkerItem.VISIBLE);

                Bitmap bitmapCCTV = BitmapFactory.decodeResource(context.getResources(),R.drawable.police);
                tItem.setIcon(bitmapCCTV);

                tMapView.addMarkerItem(m_mapPoint.get(i).getName(),tItem);
            }

            else if(m_mapPoint.get(i).getType() == 3)   //교통량
            {
                TMapPoint tMapPointMarker = new TMapPoint(m_mapPoint.get(i).getLatitude() , m_mapPoint.get(i).getLongitude());

                TMapMarkerItem tItem = new TMapMarkerItem();

                tItem.setTMapPoint(tMapPointMarker);
                tItem.setName("point " + i);
                tItem.setVisible(TMapMarkerItem.VISIBLE);

                Bitmap bitmapPass = BitmapFactory.decodeResource(context.getResources(),R.drawable.pop);
                tItem.setIcon(bitmapPass);

                tItem.setPosition(-0.01f, 0.95f);         // 마커의 중심점을 하단, 중앙으로 설정

                tMapView.addMarkerItem(m_mapPoint.get(i).getName(),tItem);
            }

            else if(m_mapPoint.get(i).getType() == 4)   //가로등
            {
                TMapPoint tMapPointMarker = new TMapPoint(m_mapPoint.get(i).getLatitude() , m_mapPoint.get(i).getLongitude());

                TMapMarkerItem tItem = new TMapMarkerItem();

                tItem.setTMapPoint(tMapPointMarker);
                tItem.setName("point " + i);
                tItem.setVisible(TMapMarkerItem.VISIBLE);

                Bitmap bitmapLight = BitmapFactory.decodeResource(context.getResources(),R.drawable.light);
                tItem.setIcon(bitmapLight);

                tItem.setPosition(-0.01f, 0.95f);         // 마커의 중심점을 하단, 중앙으로 설정

                tMapView.addMarkerItem(m_mapPoint.get(i).getName(),tItem);
            }

            else if(m_mapPoint.get(i).getType() == 5)   //편의점
            {
                TMapPoint tMapPointMarker = new TMapPoint(m_mapPoint.get(i).getLatitude() , m_mapPoint.get(i).getLongitude());

                TMapMarkerItem tItem = new TMapMarkerItem();

                tItem.setTMapPoint(tMapPointMarker);
                tItem.setName("point " + i);
                tItem.setVisible(TMapMarkerItem.VISIBLE);

                Bitmap bitmapLight = BitmapFactory.decodeResource(context.getResources(),R.drawable.conv);
                tItem.setIcon(bitmapLight);

                tItem.setPosition(-0.02f, 0.92f);         // 마커의 중심점을 하단, 중앙으로 설정

                tMapView.addMarkerItem(m_mapPoint.get(i).getName(),tItem);
            }



            else if(m_mapPoint.get(i).getType() == 0 || m_mapPoint.get(i).getType() == 6)    //출발 목적.
            {
                TMapPoint tMapPointMarker = new TMapPoint(m_mapPoint.get(i).getLatitude() , m_mapPoint.get(i).getLongitude());

                TMapMarkerItem tItem = new TMapMarkerItem();

                tItem.setTMapPoint(tMapPointMarker);
                tItem.setName("point " + i);
                tItem.setVisible(TMapMarkerItem.VISIBLE);

                Bitmap bitmapPass = BitmapFactory.decodeResource(context.getResources(),R.drawable.poi);
                tItem.setIcon(bitmapPass);

                tItem.setPosition(0.5f, 1.0f);         // 마커의 중심점을 하단, 중앙으로 설정

                tMapView.addMarkerItem(m_mapPoint.get(i).getName(),tItem);
            }
        }
    }
    public class FindPath extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                if(isDraw == true)
                {
                    for (int i = 0 ; i < m_mapPoint.size() ; i++) //draw path
                    {
                        TMapPoint tMapPointStart = new TMapPoint(m_mapPoint.get(i).getLatitude() , m_mapPoint.get(i).getLongitude());
                        TMapPoint tMapPointEnd = new TMapPoint(m_mapPoint.get(i+1).getLatitude() , m_mapPoint.get(i+1).getLongitude());

                        //TMapPolyLine tMapPolyLine = new TMapData().findPathData(tMapPointStart, tMapPointEnd); //자동차
                        TMapPolyLine tMapPolyLine = new TMapData().findPathDataWithType(TMapData.TMapPathType.PEDESTRIAN_PATH,tMapPointStart, tMapPointEnd,null,0);

                        tMapPolyLine.setLineColor(Color.CYAN);
                        tMapPolyLine.setLineWidth(15);
                        tMapView.addTMapPolyLine(m_mapPoint.get(i).getName(), tMapPolyLine);

                        //TMapPolyLine tMapPolyLine = tmapdata.findPathDataWithType(TMapPathType.CAR_PATH, point1, point2);

                        Thread.sleep(450);
                    }
                }

                else
                {
                    m_mapPoint.clear();
                    tMapView.removeAllTMapPolyLine();
                    tMapView.removeAllMarkerItem();
                }

                Thread.interrupted();

            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    public class GetData extends Thread
    {
        String _url;
        String result;

        @Override
        public void run()
        {
            try
            {
                m_mapPoint.clear();
                RecievePoint(); //초기화.

                Double sLa = m_mapPoint.get(0).getLatitude();
                Double sLo = m_mapPoint.get(0).getLongitude();
                Double dLa = m_mapPoint.get(1).getLatitude();
                Double dLo = m_mapPoint.get(1).getLongitude();


                //http://api.floodnut.com/safe/routing?srcLati=35.248687&srcLongti=128.682841&dstLati=35.237031&dstLongti=128.666649&passList=&safeDegree=1
                _url =  "http://api.floodnut.com/safe/routing?" +
                        "srcLati="+  sLa +"&srcLongti="+ sLo +
                        "&dstLati=" + dLa + "&dstLongti=" + dLo +
                        "&passList=&safeDegree=" + safety;

                java.net.URL url = new URL(_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                if( connection != null)
                {
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);

                    InputStream is = connection.getInputStream();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));

                    while((result = br.readLine())!=null)
                    {
                        sb.append(result+"\n");
                    }

                    result = sb.toString();
                }


                //parsing
                parseJSON(result);

                DrawMarker();

                FindPath mFindPath = new FindPath();
                mFindPath.start();

                Thread.interrupted();

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
    public void parseJSON(String src)   //parsing
    {
        try {
            JSONObject root = (JSONObject) new JSONTokener(src).nextValue();

            JSONArray array = new JSONArray(root.getString("data"));

            for(int i = 0; i < array.length()-1; i++)
            {
                String name     = "point" + i + "th";
                String lati     = array.getJSONObject(i).getString("lati");
                String longti   = array.getJSONObject(i).getString("longti");
                String type = array.getJSONObject(i).getString("type");

                m_mapPoint.add( i+1, new WayPoint( name, Double.parseDouble(lati), Double.parseDouble(longti), Integer.parseInt(type) ) );
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /////////////////////////
    //node Marker

    public class GetNodeData extends Thread
    {
        String _url;
        String result;

        @Override
        public void run()
        {
            try
            {
                m_nodePoint.clear();
                RecievePoint(); //초기화.

                Double sLa = myPoint.getLongitude();
                Double sLo = myPoint.getLatitude();

                //http://api.floodnut.com/safe/node?srcLati=35.248687&srcLongti=128.682841&mode=1
                _url =  "http://api.floodnut.com/safe/node?" +
                        "srcLati="+  sLa +"&srcLongti="+ sLo +
                        "&mode=" + 1;

                java.net.URL url = new URL(_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                if( connection != null)
                {
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);

                    InputStream is = connection.getInputStream();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));

                    while((result = br.readLine())!=null)
                    {
                        sb.append(result+"\n");
                    }

                    result = sb.toString();
                }

                //parsing
                parseJSON_(result);

                NodeMarkerCon nodeMarkerCon = new NodeMarkerCon();
                nodeMarkerCon.run();

                Thread.interrupted();

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
    void DrawNodeMarker()
    {
        for (int i = 0 ; i < m_nodePoint.size() ; i++)
        {
            if(m_nodePoint.get(i).getType() == 1)
            {
                TMapPoint tMapPointMarker = new TMapPoint(m_nodePoint.get(i).getLatitude() , m_nodePoint.get(i).getLongitude());

                TMapMarkerItem tItem = new TMapMarkerItem();

                tItem.setTMapPoint(tMapPointMarker);
                tItem.setName("node " + i);
                tItem.setVisible(TMapMarkerItem.VISIBLE);

                Bitmap bitmapCCTV = BitmapFactory.decodeResource(context.getResources(),R.drawable.cctv);
                tItem.setIcon(bitmapCCTV);

                tMapView.addMarkerItem(m_nodePoint.get(i).getName(),tItem);
            }

            else if(m_nodePoint.get(i).getType() == 2)
            {
                TMapPoint tMapPointMarker = new TMapPoint(m_nodePoint.get(i).getLatitude() , m_nodePoint.get(i).getLongitude());

                TMapMarkerItem tItem = new TMapMarkerItem();

                tItem.setTMapPoint(tMapPointMarker);
                tItem.setName("point " + i);
                tItem.setVisible(TMapMarkerItem.VISIBLE);

                Bitmap bitmapCCTV = BitmapFactory.decodeResource(context.getResources(),R.drawable.police);
                tItem.setIcon(bitmapCCTV);

                tMapView.addMarkerItem(m_nodePoint.get(i).getName(),tItem);
            }

            else if(m_nodePoint.get(i).getType() == 3)   //교통량
            {
                TMapPoint tMapPointMarker = new TMapPoint(m_nodePoint.get(i).getLatitude() , m_nodePoint.get(i).getLongitude());

                TMapMarkerItem tItem = new TMapMarkerItem();

                tItem.setTMapPoint(tMapPointMarker);
                tItem.setName("point " + i);
                tItem.setVisible(TMapMarkerItem.VISIBLE);

                Bitmap bitmapPass = BitmapFactory.decodeResource(context.getResources(),R.drawable.pop);
                tItem.setIcon(bitmapPass);

                tMapView.addMarkerItem(m_nodePoint.get(i).getName(),tItem);
            }

            else if(m_nodePoint.get(i).getType() == 4)   //가로등
            {
                TMapPoint tMapPointMarker = new TMapPoint(m_nodePoint.get(i).getLatitude() , m_nodePoint.get(i).getLongitude());

                TMapMarkerItem tItem = new TMapMarkerItem();

                tItem.setTMapPoint(tMapPointMarker);
                tItem.setName("point " + i);
                tItem.setVisible(TMapMarkerItem.VISIBLE);

                Bitmap bitmapLight = BitmapFactory.decodeResource(context.getResources(),R.drawable.light);
                tItem.setIcon(bitmapLight);

                tItem.setPosition(-0.02f, 0.92f);         // 마커의 중심점을 하단, 중앙으로 설정

                tMapView.addMarkerItem(m_nodePoint.get(i).getName(),tItem);
            }

            else if(m_nodePoint.get(i).getType() == 5)   //편의점
            {
                TMapPoint tMapPointMarker = new TMapPoint(m_nodePoint.get(i).getLatitude() , m_nodePoint.get(i).getLongitude());

                TMapMarkerItem tItem = new TMapMarkerItem();

                tItem.setTMapPoint(tMapPointMarker);
                tItem.setName("point " + i);
                tItem.setVisible(TMapMarkerItem.VISIBLE);

                Bitmap bitmapLight = BitmapFactory.decodeResource(context.getResources(),R.drawable.conv);
                tItem.setIcon(bitmapLight);

                tMapView.addMarkerItem(m_nodePoint.get(i).getName(),tItem);
            }
        }
    }
    public class NodeMarkerCon extends Thread
    {
        @Override
        public void run()
        {
            try
            {
                DrawNodeMarker();

                if(isNodeDraw == false)
                {
                    m_nodePoint.clear();
                    tMapView.removeAllMarkerItem();
                }

                Thread.interrupted();

            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }
    public void parseJSON_(String src)   //parsing
    {
        try {
            JSONObject root = (JSONObject) new JSONTokener(src).nextValue();

            JSONArray array = new JSONArray(root.getString("data"));

            for(int i = 0; i < array.length(); i++)
            {
                String name     = "point" + i + "th";
                String lati     = array.getJSONObject(i).getString("lati");
                String longti   = array.getJSONObject(i).getString("longi");
                String type = array.getJSONObject(i).getString("type");

                m_nodePoint.add( i, new WayPoint( name, Double.parseDouble(lati), Double.parseDouble(longti), Integer.parseInt(type) ) );
            }

            m_nodePoint.add(0,new WayPoint( "0000",myPoint.getLatitude(), myPoint.getLongitude(),4));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /////////////////////////////////
    //warning!!

    public class GetWarningData extends Thread
    {
        String _url;
        String result;

        @Override
        public void run()
        {
            try
            {
                m_warnPoint.clear();

                Double sLa = myPoint.getLongitude();
                Double sLo = myPoint.getLatitude();

                //http://api.floodnut.com/accident/frequentzone?srcLati=35.221401&srcLongi=128.686280&mode=1
                _url =  "http://api.floodnut.com/accident/frequentzone?" +
                        "srcLati=" + sLa +"&srcLongi="+ sLo +
                        "&mode=1";

                java.net.URL url = new URL(_url);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                if( connection != null)
                {
                    connection.setRequestMethod("GET");
                    connection.setDoInput(true);

                    InputStream is = connection.getInputStream();
                    StringBuilder sb = new StringBuilder();
                    BufferedReader br = new BufferedReader(new InputStreamReader(is,"UTF-8"));

                    while((result = br.readLine())!=null)
                    {
                        sb.append(result+"\n");
                    }

                    result = sb.toString();
                }


                //parsing
                parseJSON_W(result);
                Warning();

                Thread.interrupted();

            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }
    public void parseJSON_W(String src)   //parsing
    {
        try {
            JSONObject root = (JSONObject) new JSONTokener(src).nextValue();

            JSONArray array = new JSONArray(root.getString("data"));

            for(int i = 0; i < array.length(); i++)
            {
                String name     = "point" + i + "th";
                String lati     = array.getJSONObject(i).getString("lati");
                String longti   = array.getJSONObject(i).getString("longi");
                String type = ("9");

                m_warnPoint.add( i, new WayPoint( name, Double.parseDouble(lati), Double.parseDouble(longti), Integer.parseInt(type) ) );
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static double distance = 0;
    public static double mdistance = 0;

    public void Warning()
    {
        boolean warn = false;


        for(int i = 0 ; i < m_warnPoint.size() ; i++)
        {
            Distance( myPoint.getLatitude(), myPoint.getLongitude(), m_warnPoint.get(i).getLatitude(),m_warnPoint.get(i).getLongitude());

            if(  distance < 200 )
            {
                warn = true;
                break;
            }
        }

        if(warn == true)
        {
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run()
                {
                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);  //진동
                    vibrator.vibrate(500);

                    Toast.makeText(MainActivity.this, "warning!" , Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this, "현재 보행자 사고 다발지역 " + (int) distance + "M 이내 입니다." , Toast.LENGTH_SHORT).show();
                }
            });
        }
        else
        {
            /*
            MainActivity.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "nowarn : " + distance , Toast.LENGTH_SHORT).show();
                }
            });
             */
        }
    }

    public void Distance(double lat1, double lon1, double lon2, double lat2)
    {
        double theta = lon1 - lon2;
        distance = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        distance = Math.acos(distance);
        distance = rad2deg(distance);
        distance = distance * 60 * 1.1515;

        distance = distance * 1609.344;
    }

    public void mDistance(double lat1, double lon1, double lon2, double lat2)
    {
        double theta = lon1 - lon2;
        mdistance = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        mdistance = Math.acos(mdistance);
        mdistance = rad2deg(mdistance);
        mdistance = mdistance * 60 * 1.1515;

        mdistance = mdistance * 1609.344;
    }


    private static double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }
    private static double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }


    /////////////////


    public class GetEndPointAddr extends Thread
    {
        double lat = endPoint.getLatitude();
        double lon = endPoint.getLongitude();

        @Override
        public void run()
        {
            try{
                eAddr = new TMapData().convertGpsToAddress(lat, lon);
            }catch (Exception e){
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
            Thread.interrupted();
        }

}


    ///////end
}