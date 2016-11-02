package hcilab.pipapp;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

// PIP-specific imports
import com.galvanic.pipsdk.PIP.Pip;
import com.galvanic.pipsdk.PIP.PipAnalyzerListener;
import com.galvanic.pipsdk.PIP.PipAnalyzerOutput;
import com.galvanic.pipsdk.PIP.PipConnectionListener;
import com.galvanic.pipsdk.PIP.PipControlListener;
import com.galvanic.pipsdk.PIP.PipManager;
import com.galvanic.pipsdk.PIP.PipManagerListener;
import com.galvanic.pipsdk.PIP.PipStandardAnalyzer;

import java.util.ArrayList;


public class MainActivity
        extends Activity
        implements PipManagerListener, PipConnectionListener,PipControlListener,
        PipAnalyzerListener
{

    //Single instance of PipManager object
    private PipManager pipManager = null;

    //Only discovering one PIP in this app.
    private boolean pipDiscovered = false;

    private TextView tvConnectedToPip, tvDiscoveredPip;
    private Button btnConnectPip, btnDiscoverPip;


    private void init() {

        //TextView initialiations
        tvConnectedToPip = (TextView) findViewById(R.id.tvConnectedToPip);
        tvDiscoveredPip = (TextView) findViewById(R.id.tvDiscoveredPip);

        //Button initializations
        btnConnectPip = (Button) findViewById(R.id.btnConnectPip);
        btnDiscoverPip = (Button) findViewById(R.id.btnDiscoverPip);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init(); //Initialize all necessary things
        //Initial connectivity output
        // TextView tvConnectedToPip = (TextView) findViewById(R.id.tvConnectedToPip);

        //Kickoff a PIP discovery process when the Discover button is clicked
        btnDiscoverPip.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pipDiscovered = false;
                btnDiscoverPip.setEnabled(false);
                btnConnectPip.setEnabled(false);
                //buttonDisconnect.setEnabled(false);
                pipManager.resetManager();
                tvDiscoveredPip.setText("Discovering...");
                pipManager.discoverPips();
            }
        });



        //Connect to Pip button stuff
        btnConnectPip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Connect();
                //tvConnectedToPip.setText("Pip is connected!");
            }
        });
    }



    private void Connect(){

        tvConnectedToPip.setText("Pip is connected!");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    //PipManagerListener interface implementation

    // This event is raised when a connection attempt to a PIP
    // completes.
    @Override
    public void onPipConnected(int status, int pipID)
    {
        switch ( status )
        {
            case Pip.PIP_CS_OK:
            {
                Pip pip = pipManager.getPip(pipID);
                if (pip != null)
                    pip.startStreaming();
                textViewStatus.setText("Connected.");
                buttonDisconnect.setEnabled(true);
                break;
            }
            case Pip.PIP_CS_PAIRING_FAILED:
            {
                new AlertDialog.Builder(this)
                        .setCancelable(false)
                        .setTitle("PIP SDK Example")
                        .setMessage("Pairing failed. Please place PIP in pairing mode and connect again.")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int whichButton)
                            {
                                dialog.cancel();
                            }
                        })
                        .show();
            }
            // Intentional fall-through
            default:
            {
                textViewStatus.setText("Connect failed.");
                buttonConnect.setEnabled(true);
                buttonDiscover.setEnabled(true);
                break;
            }
        }
    }

    // This event is raised when a connection to a PIP is terminated.
    @Override
    public void onPipDisconnected(int status, int pipId)
    {
        textViewStatus.setText("Disconnected.");
        buttonConnect.setEnabled(true);
        buttonDiscover.setEnabled(true);
        buttonDisconnect.setEnabled(false);
    }

    //*************************************************
    //* PipAnalyzerListener interface implementation
    //*************************************************

    // This event is raised when the PIP's signal analyzer processes
    // new sample data and updates its output(s). While it is not
    // a requirement that an analyzer generate an event on a per-sample
    // basis, the SDK's standard analyzer does so.
    @Override
    public void onAnalyzerOutputEvent(int pipID, int status)
    {
        if( pipManager.getPip(pipID).isActive())
        {
            // Retrieve the analyzer's current output
            ArrayList<PipAnalyzerOutput> op =  pipManager.getPip(pipID).getAnalyzerOutput();

            // Get the analyzer's CURRENT_TREND output
            int currentTrendEvent = (int)op.get(PipStandardAnalyzer.CURRENT_TREND_EVENT.ordinal()).outputValue ;

            // Update the UI based on the current trend - relaxing, stressing, constant or none.
            switch ( currentTrendEvent )
            {
                case PipAnalyzerListener.STRESS_TREND_RELAXING:
                    textViewStatus.setText("Trend: Relaxing");
                    break;
                case PipAnalyzerListener.STRESS_TREND_STRESSING:
                    textViewStatus.setText("Trend: Stressing");
                    break;
                case PipAnalyzerListener.STRESS_TREND_CONSTANT:
                    textViewStatus.setText("Trend: Constant");
                    break;
                case PipAnalyzerListener.STRESS_TREND_NONE:
                    textViewStatus.setText("Trend: None");
                    break;
            }
        }
        else
        {
            // The PIP is in the streaming state, but is not being held.
            textViewStatus.setText("Streaming: Inactive");
        }
    }

}
