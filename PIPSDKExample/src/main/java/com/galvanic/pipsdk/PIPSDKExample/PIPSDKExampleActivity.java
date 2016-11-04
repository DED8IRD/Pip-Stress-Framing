/*
 * PipApp modified from Galvanic's PIPSDKExample
 */

package com.galvanic.pipsdk.PIPSDKExample;

import java.io.*;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.DialogInterface;

import java.util.ArrayList;

import com.galvanic.pipsdk.PIP.PipInfo;
// PIP-specific imports
import com.galvanic.pipsdk.PIP.PipManager;
import com.galvanic.pipsdk.PIP.Pip;
import com.galvanic.pipsdk.PIP.PipAnalyzerOutput;
import com.galvanic.pipsdk.PIP.PipStandardAnalyzer;
import com.galvanic.pipsdk.PIP.PipConnectionListener;
import com.galvanic.pipsdk.PIP.PipManagerListener;
import com.galvanic.pipsdk.PIP.PipAnalyzerListener;
// Visual imports
import android.graphics.Color;
// PIP-specific imports

/* The application's user interface must inherit and implement the
 * PipManagerListener, PipConnectionListener and PipAnalyzerListener 
 * interfaces in order to handle events relating to PIP discovery, 
 * connection status and streaming/data analysis respectively.
 */
public class PIPSDKExampleActivity 
	extends Activity 
	implements PipManagerListener, PipConnectionListener, PipAnalyzerListener   
{
	// Singleton instance of PipManager object.
	private PipManager pipManager = null;
	// We will only be discovering a single PIP in this app.
	private boolean pipDiscovered = false;
	
	// Minimal UI implementation.
	Button buttonDiscover = null;
	Button buttonConnect = null;
	Button buttonDisconnect = null;
	TextView textViewStatus = null;
    TextView tvRaw = null;
    TextView tvPrevious = null;
    ImageView ivStatus = null;
	TextView dynamicColorBlock = null;

    double currentRawValue;
    double accumulated;

	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pipsdkexample);

		textViewStatus = (TextView)findViewById(R.id.Status);
		buttonDiscover = (Button)findViewById(R.id.Discover);
        tvRaw = (TextView) findViewById(R.id.tvRaw);
        tvPrevious = (TextView) findViewById(R.id.tvPrevious);
		buttonDiscover.setEnabled(true);
		dynamicColorBlock = (TextView)findViewById(R.id.dynamic_color_block);

        //ivStatus = (ImageView) findViewById(R.id.ivStatus);





		
		// Kick off a PIP discovery process when the Discover button is clicked.
		buttonDiscover.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                pipDiscovered = false;
                buttonDiscover.setEnabled(false);
                buttonConnect.setEnabled(false);
                buttonDisconnect.setEnabled(false);
                pipManager.resetManager();
                textViewStatus.setText("Discovering...");
                pipManager.discoverPips();

            }
        });
				
		buttonConnect = (Button)findViewById(R.id.Connect);
		buttonConnect.setEnabled(true);
		
		// Initiate a connection to a discovered PIP when the Connect
		// button is clicked.
		buttonConnect.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				buttonDiscover.setEnabled(false);
				buttonConnect.setEnabled(false);
				textViewStatus.setText("Connecting...");
				connectPip();
			}
		});
		
		buttonDisconnect = (Button) findViewById(R.id.Disconnect);
		buttonDisconnect.setEnabled(false);
		
		// Disconnect from a connected PIP when the Disconnect button is clicked.
		buttonDisconnect.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				buttonDisconnect.setEnabled(false);
				textViewStatus.setText("Disconnecting...");
				// We terminate discovery after a single PIP has been found, so
				// the list of discovered PIPs will contain just a single entry.
				pipManager.getPip(pipManager.getDiscoveryAtIndex(0).pipID).disconnect();
			}
		});
		
		pipManager = PipManager.getInstance();
		pipManager.initialize(this, this);
	}
	
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
		textViewStatus.setText("Ready.");
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
		
		textViewStatus.setText(statusMsg);
		pipDiscovered = true;
		buttonDiscover.setEnabled(true);
		buttonConnect.setEnabled(true);
	}	

	
	// onPipDiscoveryComplete is fired when a discovery process ends.
	// In this case, check whether or not at least one PIP was found - 
	// if not, then display an appropriate message.
	@Override
	public void onPipDiscoveryComplete(int numDiscovered)
	{


		textViewStatus.setText("Pip discovered");

		if ( !pipDiscovered )
			textViewStatus.setText("Discovery incomplete.");

		buttonDiscover.setEnabled(true);
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

            currentRawValue = op.get(PipStandardAnalyzer.SKIN_CONDUCTANCE.ordinal()).outputValue;
            tvRaw.setText(String.valueOf(currentRawValue));

            accumulated =  op.get(PipStandardAnalyzer.ACCUMULATED_TREND.ordinal()).outputValue;
            tvPrevious.setText(String.valueOf(accumulated));



			// Update the UI based on the current trend - relaxing, stressing, constant or none.
			switch ( currentTrendEvent )
			{
				case PipAnalyzerListener.STRESS_TREND_RELAXING:
					textViewStatus.setText("Trend: Relaxing");
					dynamicColorBlock.setBackgroundColor(Color.BLUE);

                    break;
				case PipAnalyzerListener.STRESS_TREND_STRESSING:
					textViewStatus.setText("Trend: Stressing");
					dynamicColorBlock.setBackgroundColor(Color.YELLOW);

                    break;
				case PipAnalyzerListener.STRESS_TREND_CONSTANT:
					textViewStatus.setText("Trend: Constant");
					dynamicColorBlock.setBackgroundColor(Color.GRAY);

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