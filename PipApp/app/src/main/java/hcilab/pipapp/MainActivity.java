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

import com.galvanic.pipsdk.PIP.Pip;
import com.galvanic.pipsdk.PIP.PipAnalyzerListener;
import com.galvanic.pipsdk.PIP.PipAnalyzerOutput;
import com.galvanic.pipsdk.PIP.PipConnectionListener;
import com.galvanic.pipsdk.PIP.PipInfo;
import com.galvanic.pipsdk.PIP.PipManager;
import com.galvanic.pipsdk.PIP.PipManagerListener;
import com.galvanic.pipsdk.PIP.PipStandardAnalyzer;

import java.util.ArrayList;

// PIP-specific imports


public class MainActivity
        extends Activity
        implements PipManagerListener, PipConnectionListener,
        PipAnalyzerListener
{

    //Single instance of PipManager object
    private PipManager pipManager = null;

    //Only discovering one PIP in this app.
    private boolean pipDiscovered = false;

    private TextView tvConnectedToPip, tvDiscoveredPip, tvStatus;
    private Button btnConnectPip, btnDiscoverPip;


    private void init() {

        //TextView initialiations
        tvConnectedToPip = (TextView) findViewById(R.id.tvConnectedToPip);
        tvDiscoveredPip = (TextView) findViewById(R.id.tvDiscoveredPip);
        tvStatus = (TextView) findViewById(R.id.tvStatus);

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


    public void connectPip()
    {
        // We stop discovery after the first PIP has been found, so
        // the list of discovered PIPs will contain a single entry at index zero.
        Pip pip = pipManager.getPip(pipManager.getDiscoveryAtIndex(0).pipID);
        // Register listeners for connection and data analysis events.
        pip.setPipAnalyzerListener(this);
        pip.setPipConnectionListener(this);
        // Connect to the PIP.
        pip.connect();
    }

    //*************************************************
    //* PipManagerListener interface implementation
    //*************************************************

    // Once this event is raised, the PipManager object is initialized
    // and ready for use by the application.
    @Override
    public void onPipManagerReady()
    {
        tvStatus.setText("Ready.");
    }

    // This event is raised when a PIP has been discovered. For simplicity
    // in the current app, we terminate discovery after a single PIP has been
    // found, but in general, the discovery process continues until all
    // PIPs in range have been found.
    @Override
    public void onPipDiscovered()
    {
        // We have found our first PIP - terminate discovery.
        pipManager.cancelDiscovery();

        String statusMsg = "Discovered PIP: ";
        PipInfo info = pipManager.getDiscoveryAtIndex(0);
        if ( info != null )
        {
            if ( info.name != null && info.name.length() != 0 )
                statusMsg = statusMsg.concat(info.name);
            else
                statusMsg.concat("Unknown PIP");
        }

        tvStatus.setText(statusMsg);
        pipDiscovered = true;
        btnDiscoverPip.setEnabled(true);
        btnConnectPip.setEnabled(true);
    }


    // onPipDiscoveryComplete is fired when a discovery process ends.
    // In this case, check whether or not at least one PIP was found -
    // if not, then display an appropriate message.
    @Override
    public void onPipDiscoveryComplete(int numDiscovered)
    {
        if ( !pipDiscovered )
            tvStatus.setText("Discovery complete.");

        btnDiscoverPip.setEnabled(true);
    }

    // onPipsResumed will be called when the application resumes
    // from the suspended state. The SDK automatically attempts to
    // re-connect to any PIPs that were connected prior to the app
    // suspending.
    @Override
    public void onPipsResumed(int status)
    {
    }

    //*************************************************
    //* PipConnectionListener interface implementation
    //*************************************************

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
                tvStatus.setText("Connected.");
                btnDiscoverPip.setEnabled(true);
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
                tvStatus.setText("Connect failed.");
                btnConnectPip.setEnabled(true);
                btnDiscoverPip.setEnabled(true);
                break;
            }
        }
    }

    // This event is raised when a connection to a PIP is terminated.
    @Override
    public void onPipDisconnected(int status, int pipId)
    {
        tvStatus.setText("Disconnected.");
        btnConnectPip.setEnabled(true);
        btnDiscoverPip.setEnabled(true);
        //buttonDisconnect.setEnabled(false);
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
                    tvStatus.setText("Trend: Relaxing");
                    break;
                case PipAnalyzerListener.STRESS_TREND_STRESSING:
                    tvStatus.setText("Trend: Stressing");
                    break;
                case PipAnalyzerListener.STRESS_TREND_CONSTANT:
                    tvStatus.setText("Trend: Constant");
                    break;
                case PipAnalyzerListener.STRESS_TREND_NONE:
                    tvStatus.setText("Trend: None");
                    break;
            }
        }
        else
        {
            // The PIP is in the streaming state, but is not being held.
            tvStatus.setText("Streaming: Inactive");
        }
    }


}
