package com.testapp.testapp.arcui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.testapp.testapp.arcui.barcode.BarcodeCaptureActivity;
import com.token.v1.os.launcher.ICustomerScreenService;
import com.token.v1.os.launcher.INotificationService;
import com.token.v1.os.launcher.IPrinterService;

import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {


    private static final String LOG_TAG = "MainActivity";
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

    private EditText idEditText;
    private TextView balanceTextView;
    private Button getBalanceButton;
    private EditText receiverEditText;
    private EditText amountEditText;
    private Button makeTransferButton;
    private EditText amountWithdraw;
    private Button withdrawButton;
    private ImageView qr_receive;
    private ImageView qr_send;
    private ImageView qrImageView;
    private LinearLayout mainLinearLayout;

    private final int BARCODE_READER_REQUEST_CODE = 1;

    final int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
            //| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            //| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
            //| View.SYSTEM_UI_FLAG_FULLSCREEN
            | View.SYSTEM_UI_FLAG_IMMERSIVE;

    public void getBalance(final String id){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://erc20-demo.appspot.com/balance");
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

                    JSONObject jsonParam = new JSONObject();
                    jsonParam.put("address", id);
                    OutputStream os = conn.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(jsonParam.toString());
                    Log.i(MainActivity.class.toString(), jsonParam.toString());
                    writer.flush();
                    writer.close();
                    os.close();
                    conn.connect();
                    InputStream response = conn.getInputStream();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(response));
                    StringBuilder sb = new StringBuilder();

                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line + "\n");
                    }
                    JSONObject obj = new JSONObject(sb.toString());

                    balanceTextView.setText(obj.getString("amount"));

                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        thread.start();

    }

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

        qr_receive = (ImageView) findViewById(R.id.qrReceive);
        qr_receive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BarcodeCaptureActivity.class);
                startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
            }
        });
        mainLinearLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);
        qrImageView = (ImageView) findViewById(R.id.qrImageView);
        qr_send = (ImageView) findViewById(R.id.qrSend);
        qr_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text= idEditText.getText().toString();
                MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
                try {
                    BitMatrix bitMatrix = multiFormatWriter.encode(text, BarcodeFormat.QR_CODE,200,200);
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                    Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                    mainLinearLayout.setVisibility(View.GONE);
                    qrImageView.setVisibility(View.VISIBLE);
                    qrImageView.setImageBitmap(bitmap);
                    qrImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            qrImageView.setVisibility(View.GONE);
                            mainLinearLayout.setVisibility(View.VISIBLE);
                        }
                    });
                } catch (WriterException e) {
                    e.printStackTrace();
                }
            }
        });

        balanceTextView = (TextView) findViewById(R.id.balanceTextView);

        idEditText = (EditText)findViewById(R.id.idEditText);
        receiverEditText = (EditText)findViewById(R.id.receiverEditText);
        amountEditText = (EditText)findViewById(R.id.amountEditText);

        getBalanceButton = findViewById(R.id.getBalanceButton);
        getBalanceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String id = idEditText.getText().toString();
                getBalance(id);

            }
        });

        makeTransferButton = findViewById(R.id.makeTransferButton);
        makeTransferButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String sender = idEditText.getText().toString();
                final String receiver = receiverEditText.getText().toString();
                final String amount = amountEditText.getText().toString();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL("https://erc20-demo.appspot.com/transfer");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

                            JSONObject jsonParam = new JSONObject();
                            jsonParam.put("sender_address", sender);
                            jsonParam.put("receiver_address", receiver);
                            jsonParam.put("amount", amount);

                            OutputStream os = conn.getOutputStream();
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                            writer.write(jsonParam.toString());
                            Log.i(MainActivity.class.toString(), jsonParam.toString());
                            writer.flush();
                            writer.close();
                            os.close();
                            conn.connect();
                            InputStream response = conn.getInputStream();

                            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
                            StringBuilder sb = new StringBuilder();

                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line + "\n");
                            }
                            JSONObject obj = new JSONObject(sb.toString());

                            String isOK = obj.getString("OK");

                            conn.disconnect();
                            getBalance(sender);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();



            }
        });
        amountWithdraw = (EditText) findViewById(R.id.amountWithdraw);
        withdrawButton = findViewById(R.id.withdrawButton);
        withdrawButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String address = idEditText.getText().toString();
                final String amount = amountWithdraw.getText().toString();

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL("https://erc20-demo.appspot.com/sell");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("POST");
                            conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");

                            JSONObject jsonParam = new JSONObject();
                            jsonParam.put("address", address);
                            jsonParam.put("amount", amount);

                            OutputStream os = conn.getOutputStream();
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                            writer.write(jsonParam.toString());
                            Log.i(MainActivity.class.toString(), jsonParam.toString());
                            writer.flush();
                            writer.close();
                            os.close();
                            conn.connect();
                            InputStream response = conn.getInputStream();

                            BufferedReader reader = new BufferedReader(new InputStreamReader(response));
                            StringBuilder sb = new StringBuilder();

                            String line = null;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line + "\n");
                            }
                            JSONObject obj = new JSONObject(sb.toString());

                            String isOK = obj.getString("OK");

                            conn.disconnect();
                            getBalance(address);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

                thread.start();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        balanceTextView.setText("0");
        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra("Barcode");
                    String[] parts = barcode.displayValue.split(" ");
                    receiverEditText.setText(parts[0]);
                    amountEditText.setText(parts[1]);
                }
            } else
                Log.e(LOG_TAG, String.format(getString(R.string.barcode_error_format),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }
}

