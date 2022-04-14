package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
    private ArrayList<TMapPoint> points = new ArrayList<TMapPoint>();


    private ArrayList<WayPoint> m_nodePoint = new ArrayList<WayPoint>();

    public static int safety = 0;
    public static int node = 0;

    public static Context context;
    public static TMapView tMapView;

    TMapGpsManager tMapGPS = null;
    public static TMapPoint myPoint;
    public static TMapPoint endPoint;

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

        /*
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        */

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




        //Locating
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED){

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1); //위치권한 탐색 허용 관련 내용
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
        tMapGPS.setMinDistance(100);
        tMapGPS.setProvider(tMapGPS.NETWORK_PROVIDER);
        tMapGPS.setProvider(tMapGPS.GPS_PROVIDER);

        tMapGPS.OpenGps();
    }

    void button(com.skt.Tmap.TMapView tMapView)
    {
        Button buttonDefault = (Button)findViewById(R.id.buttonDefault);
        Button buttonAdvance = (Button)findViewById(R.id.buttonAdvance);
        Button buttonStart = (Button)findViewById(R.id.buttonStart);

        buttonDefault.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                safety = 0;
            }
        });

        buttonAdvance.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                safety = 1;

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

    }



    ///////////////

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        GetNodeData mGetNodeData = new GetNodeData();

        switch (item.getItemId()) {
            case R.id.item1:
                node = 1;
                if(isNodeDraw == true)
                    isNodeDraw = false;
                else
                    isNodeDraw = true;

                mGetNodeData.start();

                return true;


            case R.id.item2:
                node = 2;

                if(isNodeDraw == true)
                    isNodeDraw = false;
                else
                    isNodeDraw = true;

                mGetNodeData.start();

                return true;

            case R.id.item3:
                node = 3;

                if(isNodeDraw == true)
                    isNodeDraw = false;
                else
                    isNodeDraw = true;

                mGetNodeData.start();
                return true;


            case R.id.item4:
                node = 4;

                if(isNodeDraw == true)
                    isNodeDraw = false;
                else
                    isNodeDraw = true;

                mGetNodeData.start();
                return true;


            case R.id.item5:
                isNodeDraw = false;

                NodeMarkerCon mNodeMarkerCon = new NodeMarkerCon();
                mNodeMarkerCon.start();

                return true;
        }
        return false;
    }

    ///////////////

    void RecievePoint() //시작점 끝점 찍혀있는 마크로 업데이트
    {

        m_mapPoint.add( new WayPoint("Start",myPoint.getLongitude(),myPoint.getLatitude(),0) );
        m_mapPoint.add( new WayPoint("End",35.2222, 128.6928,5) );

        //35.2222, 128.6834 상남 시장
        //35.2303, 128.6803
        //35.2222, 128.6928
    }

    void DrawMarker()   //언젠가 지우는거 여기에 추가해야함.
    {
        //0 출발지 / 5 목적지
        //1 CCTV / 2 경찰서,파출소,지구대 같은거 / 3 교통정보 /4 가로등

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

                Bitmap bitmapCCTV = BitmapFactory.decodeResource(context.getResources(),R.drawable.pass);
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

                Bitmap bitmapPass = BitmapFactory.decodeResource(context.getResources(),R.drawable.pass);
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



            else if(m_mapPoint.get(i).getType() == 0 || m_mapPoint.get(i).getType() == 5)    //출발 목적.
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
    //node

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

                Double sLa = m_mapPoint.get(0).getLatitude();
                Double sLo = m_mapPoint.get(0).getLongitude();

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

                Bitmap bitmapCCTV = BitmapFactory.decodeResource(context.getResources(),R.drawable.pass);
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

                Bitmap bitmapPass = BitmapFactory.decodeResource(context.getResources(),R.drawable.pass);
                tItem.setIcon(bitmapPass);

                tItem.setPosition(-0.01f, 0.95f);         // 마커의 중심점을 하단, 중앙으로 설정

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

                tItem.setPosition(-0.01f, 0.95f);         // 마커의 중심점을 하단, 중앙으로 설정

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
}