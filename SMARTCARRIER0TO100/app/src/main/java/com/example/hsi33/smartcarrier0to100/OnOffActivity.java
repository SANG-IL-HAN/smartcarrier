package com.example.hsi33.smartcarrier0to100;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

public class OnOffActivity extends AppCompatActivity implements BeaconConsumer {
    ImageButton on_off;
    private BeaconManager beaconManager;
    // 감지된 비콘들을 임시로 담을 리스트
    private List<Beacon> beaconList = new CopyOnWriteArrayList<Beacon>();
    TextView textView;
    Thread th = new Thread();
    double[] dist_temp;
    Boolean bool2 = false;
    double[] dist_temp2 = new double[4];
    TextView status;
    private Beacon[] beacon_val;
    int[] b_count = new int[4];
    int stop = 777;
    double shortest = 777;
    double past_shortest = Double.MAX_VALUE;
    int s_index = 555;
    double checksum = 0;
    private BluetoothSPP bt;
    int a, b;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_off);

        if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
        if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beacon_val = new Beacon[7];
        dist_temp = new double[7];
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));
        beaconManager.setBackgroundScanPeriod(500l);
        //beaconManager.setForegroundScanPeriod(0l);
        beaconManager.setBackgroundBetweenScanPeriod(1500l);
        beaconManager.setForegroundBetweenScanPeriod(100l);
        status = (TextView) findViewById(R.id.status);
        beaconManager.bind(this);
        bt = new BluetoothSPP(this); //Initializing

        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }

        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() { //데이터 수신
            public void onDataReceived(byte[] data, String message) {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }
        });

        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() { //연결됐을 때
            public void onDeviceConnected(String name, String address) {
                Toast.makeText(getApplicationContext()
                        , "Connected to " + name + "\n" + address
                        , Toast.LENGTH_SHORT).show();
            }

            public void onDeviceDisconnected() { //연결해제
                Toast.makeText(getApplicationContext()
                        , "Connection lost", Toast.LENGTH_SHORT).show();
            }

            public void onDeviceConnectionFailed() { //연결실패
                Toast.makeText(getApplicationContext()
                        , "Unable to connect", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnConnect = findViewById(R.id.btnConnect);
        Button btnStop = findViewById(R.id.btnStop);
        Button btnsend = findViewById(R.id.btnSend);

        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Thread th = new Thread(new Runnable() {
                    int count;
                    int count2 = 0;

                    @Override
                    public void run() {
                        while (true) {
                            if (stop == 44) {
                                stop = 0;
                                break;
                            }
                            count = 0;

                            for (Beacon beacon : beaconList) {
                            int IDX = id_to_idx(beacon.getId2().toInt());
                            double distance = beacon.getDistance();
                            dist_temp[IDX] = distance;
                            Log.i("Thread", "" + "| INDEX ID " + IDX + "|" + dist_temp[IDX]);
                            b_count[IDX]++;
                        }

                        for (int i = 0; i < 3; i++) {

                            if (shortest > dist_temp[i] && dist_temp[i] > 0) {
                                shortest = dist_temp[i];
                                s_index = i;
                            }
                            dist_temp[i] = Double.MAX_VALUE;
                            b_count[i] = 0;
                        }

                            if (past_shortest != shortest) {
                                if (shortest < 1.0) {
                                    status.setText("stopped \n");
                                    bt.send("c5", true);
                                }

                                if (shortest > 1.0 && shortest != 777)
                                {
                                    status.append("s_index :" + s_index + "|" + "shortest : " + String.format("%.3f", shortest) + "\n");
                                    bt.send(s_index + "," + shortest, true);

                                    count2++;
                                }
                            }

                            if(count2==10)
                            {
                                status.setText("");
                                count2=0;
                            }

                            past_shortest = shortest;

                            shortest = 777;
                            s_index = 666;
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }



                    }


                });
                th.start();
            }
        });


        btnConnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (bt.getServiceState() == BluetoothState.STATE_CONNECTED) {
                    bt.disconnect();
                } else {
                    Intent intent = new Intent(getApplicationContext(), DeviceList.class);
                    startActivityForResult(intent, BluetoothState.REQUEST_CONNECT_DEVICE);
                }
            }

        });
        btnStop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stop = 44;
                status.setText("");
            }
        });
    }


    public void onDestroy() {
        super.onDestroy();
        bt.stopService(); //블루투스 중지
    }

    public void onBeaconServiceConnect() {
        beaconManager.setRangeNotifier(new RangeNotifier() {
            @Override
            // 비콘이 감지되면 해당 함수가 호출된다. Collection<Beacon> beacons에는 감지된 비콘의 리스트가,
            // region에는 비콘들에 대응하는 Region 객체가 들어온다.
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    beaconList.clear();
                    for (Beacon beacon : beacons) {
                        beaconList.add(beacon);
                    }
                }
            }

        });

        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {
        }
    }

    public void onStart() {
        super.onStart();
        if (!bt.isBluetoothEnabled()) { //
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리
            }
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BluetoothState.REQUEST_CONNECT_DEVICE) {
            if (resultCode == Activity.RESULT_OK)
                bt.connect(data);
        } else if (requestCode == BluetoothState.REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER);
            } else {
                Toast.makeText(getApplicationContext()
                        , "Bluetooth was not enabled."
                        , Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    public double two_Case(double shortest, int s_index) {

        return shortest;
    }

    public int id_to_idx(int id) {
        if (id == 0)
            return 0;
        else if (id == 7)
            return 2;
        else if (id == 1)
            return 1;
        else
            return 3;
    }


}
