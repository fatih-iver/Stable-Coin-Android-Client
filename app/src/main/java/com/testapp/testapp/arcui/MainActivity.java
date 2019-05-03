package com.testapp.testapp.arcui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.token.v1.os.launcher.ICustomerScreenService;
import com.token.v1.os.launcher.INotificationService;
import com.token.v1.os.launcher.IPrinterService;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {


    static String packageName = "com.token.v1.os.launcher" ;
    static String cs_serviceName = packageName + ".CustomerScreenService";
    static String prn_serviceName = packageName + ".PrinterService";
    static String notif_serviceName = packageName + ".NotificationService";

    private static final String TAG  = MainActivity.class.getName();

    public ServiceConnection m_conServis = null;
    public static ICustomerScreenService m_ics = null;

    public ServiceConnection m_conNotifService = null;
    public static INotificationService m_notif = null;

    ServiceConnection m_conPrnServis = null;
    IPrinterService m_prn = null;

    int m_screenWidth;
    int m_screenHeight;

    String mTotalStr = null ;

    private Button printerTestButton;
    private Button notificationOKButton;
    private Button notificationNOKButton;
    private Button customerScreenTestButton;



    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            //| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            //| View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getSupportActionBar().hide(); //<< this
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(flags);

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        m_screenWidth = dm.widthPixels;
        m_screenHeight = dm.heightPixels;


        printerTestButton = findViewById(R.id.printerTestButton);
        printerTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    printText("HELLO WORLD!!!", 10, false);
                    closeReceipt();
                }catch (RemoteException e){
                    Log.d(TAG, "Printer Remote exception ");
                    e.printStackTrace();
                }catch (NullPointerException e){
                    Log.d(TAG,"Printer Service is null");
                    e.printStackTrace();
                }
            }
        });

        notificationOKButton = findViewById(R.id.notifiationOKButton);
        notificationOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    m_notif.setNotification("Approved", 0xFF00FF00, 1500);
                }catch (RemoteException e){
                    Log.d(TAG, "Notification Remote exception ");
                    e.printStackTrace();
                }catch (NullPointerException e){
                    Log.d(TAG,"Notification Service is null");
                    e.printStackTrace();
                }

            }
        });

        notificationNOKButton = findViewById(R.id.notificationNOKButton);
        notificationNOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    m_notif.setNotification("Canceled", 0xFF800000, 2000);

                }catch (RemoteException e){
                    Log.d(TAG, "Notification Remote exception ");
                    e.printStackTrace();
                }catch (NullPointerException e){
                    Log.d(TAG,"Notification Service is null");
                    e.printStackTrace();
                }

            }
        });

        customerScreenTestButton = findViewById(R.id.customerScreenButton);
        customerScreenTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    m_ics.setHeaderAndText("CUSTOMER SCREEN TEST", 1, "AMOUNT : XXX", 3);
                }catch (RemoteException e){
                    Log.d(TAG, "CS Remote exception ");
                    e.printStackTrace();
                }catch (NullPointerException e){
                    Log.d(TAG,"Customer Service is null");
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ARCUI", "On Destroy unbind");
        serviceDisconnect();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("ARCUI", "on start");
        serviceConnect();
    }
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try{
                if(m_ics != null){
                    m_ics.setHeaderAndText("Total", 1, mTotalStr, 3);
                }else{
                    Log.e(TAG, "Customer screen service is null");
                }
            } catch(Exception e){
                // added try catch block to be sure of uninterupted execution
            }
        }
    };
    private void serviceDisconnect(){
        if( m_conServis != null){
            Log.d("ARCUI","Unbind m_conservis");
            unbindService(m_conServis);
            m_ics = null;
        }
        if( m_conNotifService != null){
            Log.d("ARCUI","Unbind m_conNotifService");
            unbindService(m_conNotifService);
            m_notif = null;
        }
        if( m_conPrnServis != null){
            Log.d("ARCUI","Unbind m_conPrnServis");

            unbindService(m_conPrnServis);
            m_prn = null;
        }

    }
    private void serviceConnect(){
        if( m_ics == null ){
            m_conServis = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.d("ARCUI", "m_conServis onServiceConnected");
                    m_ics = ICustomerScreenService.Stub.asInterface(service);

                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d("ARCUI", "m_conServis onServiceDisconnected");
                    m_ics = null;
                }
            };
            Intent cs = new Intent();
            cs.setComponent(new ComponentName(packageName, cs_serviceName));
            bindService(cs,m_conServis,BIND_ABOVE_CLIENT );
        }

        if( m_notif == null){
            m_conNotifService = new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    Log.d("ARCUI", "m_conNotifService onServiceConnected");
                    m_notif = INotificationService.Stub.asInterface(service);

                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.d("ARCUI", " m_conNotifService onServiceDisconnected");

                    m_notif = null;
                }
            };

            Intent notif = new Intent();
            notif.setComponent(new ComponentName(packageName, notif_serviceName));
            bindService(notif,m_conNotifService,BIND_ABOVE_CLIENT );
        }
        // Notification service bind

        if(m_prn == null){

            m_prn = getPrinterService();

        }

    }

    private IPrinterService getPrinterService() {
        IPrinterService mService = null;
        Method method = null;
        try {
            method = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            IBinder binder = (IBinder) method.invoke(null, "PrinterService");
            if (binder != null) {
                mService = IPrinterService.Stub.asInterface(binder);
                Log.d(getClass().getName(), "Service bounded.");
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return mService;
    }


    /**
     * print a line in receipt
     * @param line text to print
     * @param fontSize
     * @param bold true for bold
     * @throws RemoteException
     */
    public void printText(String line,  int fontSize, boolean bold) throws RemoteException {
        if (m_prn == null) {
            Log.d(getClass().getName(), "Binding Service ....");
            m_prn = getPrinterService();
        }
        try{
            m_prn.printText(line);
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }


    /**
     * add bottom margin to receipt. So customer can take receipt easily.
     * @throws RemoteException
     */
    public void closeReceipt() throws RemoteException {
        try{
            m_prn.addSpace(250);

        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }


}

