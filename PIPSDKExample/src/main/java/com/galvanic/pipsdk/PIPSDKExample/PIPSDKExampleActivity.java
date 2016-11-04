/* Copyright (c) 2014 Galvanic Ltd.
 * All rights reserved.
 *
 * This software is the confidential and proprietary information of Galvanic Limited.
 *
 * 
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR "AS IS" AND ANY EXPRESS OR 
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES 
 * OF TITLE, NON-INFRINGEMENT, MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL GALVANIC LIMITED BE LIABLE FOR ANY 
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED 
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 * 
 */

/* PIPSDKExample is a bare-bones application illustrating how to
 * integrate PIP functionality into a Java native application. In order
 * to focus on the PIP-specific code, UI has been pared back to the minimum.
 * Also, only the core PIP concepts of discovering, connecting to and streaming
 * from a PIP device are covered. The application allows the user to
 * discover a single PIP, connect to it and start streaming, in order to
 * receive events about the user's state of stress/relaxation.
 */
package com.galvanic.pipsdk.PIPSDKExample;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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