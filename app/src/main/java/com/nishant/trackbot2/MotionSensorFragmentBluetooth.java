package com.nishant.trackbot2;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

public class MotionSensorFragmentBluetooth extends Fragment implements SensorEventListener
{
	public Sensor rvectorsensor;
	SensorManager sensormgr;
	public float accelValues[]=new float[3];
	public float orientationVals[]=new float[3];
	public float compassValues[]=new float[3];
	//public ProgressBar connecting;
	public float inR[]=new float[9];
	public float inclineMatrix[]=new float[9];
	public float mRotationMatrix[]=new float[16];
	public boolean ready=false;
	public TextView leftvalue,rightvalue,forwardvalue,reversevalue,sent; //conn;
	public ImageView left,right,forward,reverse;
	public int magleft,magright,magforward,magreverse,xmag,ymag;
	public OutputStream out;
	public ProgressDialog pgd;
	private static final int ENABLE_BT_REQUEST_CODE = 1;
	public BluetoothAdapter bth;
	public Button sel_device,disconnect;
	private final static UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private Set<BluetoothDevice>paired_devices;
	private ArrayAdapter<String>bt_array_adapter;
	private ListView bt_list;
	private static final int REQUEST_ENABLE_BT=1;
	public BluetoothDevice remote_device;
	public String item_value,temp,total="";
	public static MotionSensorFragmentBluetooth newInstance()
	{
		MotionSensorFragmentBluetooth motionsensorfragbluetooth=new MotionSensorFragmentBluetooth();
		return motionsensorfragbluetooth;
	}
	public MotionSensorFragmentBluetooth()
	{}
	public View onCreateView(LayoutInflater inflater,ViewGroup container,Bundle savedInstanceState)
	{
		View rootView=inflater.inflate(R.layout.motion_sensor_fragment, container, false);
		return rootView;
	}
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		super.onViewCreated(view, savedInstanceState);
		pgd = new ProgressDialog(getActivity());
		pgd.setMessage("Connecting ...");
		pgd.setCancelable(false);
		sensormgr=(SensorManager)getActivity().getSystemService(Context.SENSOR_SERVICE);
		rvectorsensor=sensormgr.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
		bth=BluetoothAdapter.getDefaultAdapter();
        if(bth==null)
		{
			Toaster("Bluetooth not Supported by Device");
		}
       	SwitchOn(bth);
		sel_device=(Button)view.findViewById(R.id.sel_device_ms);
		disconnect=(Button)view.findViewById(R.id.disconnect_ms);
		//conn=(TextView)view.findViewById(R.id.conn_ms);
		bt_list=(ListView)view.findViewById(R.id.devices_ms);
		//connecting=(ProgressBar)getActivity().findViewById(R.id.connecting_ms);
        bt_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
       	{
       		@Override
       		public void onItemClick(AdapterView<?>parent, View view, int position,long id)
        	{
        		item_value=(String)bt_list.getItemAtPosition(position);
        		String MAC=item_value.substring(item_value.length()-17);
        		remote_device=bth.getRemoteDevice(MAC);
				pgd.setMessage("Connecting to :\n"+item_value);
				bt_list.setVisibility(View.GONE);
        		//Toaster("Connecting to  :\n"+item_value);
				pgd.show();
        		ConnectingThread t=new ConnectingThread(remote_device);
        		t.start();
				//pgd.hide();
        	}
        });
        bt_array_adapter=new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1);
		bt_list.setAdapter(bt_array_adapter);
		leftvalue=(TextView)getActivity().findViewById(R.id.left_value);
		rightvalue=(TextView)getActivity().findViewById(R.id.right_value);
		reversevalue=(TextView)getActivity().findViewById(R.id.reverse_value);
		forwardvalue=(TextView)getActivity().findViewById(R.id.forward_value);
		left=(ImageView)getActivity().findViewById(R.id.left_butt);
		right=(ImageView)getActivity().findViewById(R.id.right_butt);
		forward=(ImageView)getActivity().findViewById(R.id.forward_butt);
		reverse=(ImageView)getActivity().findViewById(R.id.reverse_butt);
		sent=(TextView)getActivity().findViewById(R.id.sent_ms);
		sel_device.setOnClickListener(new OnClickListener()
       	{
       		public void onClick(View v)
       		{
        		if(!bth.isEnabled())
        		{
        			SwitchOn(bth);
        		}
        		if(bth.isEnabled())
        		{
        			try
        			{
        				get_paired(v);
        				sel_device.setVisibility(View.GONE);
        				bt_list.setVisibility(View.VISIBLE);
        			}
        			catch(Exception e)
        			{
        				Toaster(e.toString());
        			}
        		}
        	}
        });
		disconnect.setOnClickListener(new OnClickListener()
       	{
        	public void onClick(View v)
        	{
        		try
        		{
        			Disconnect(remote_device);
        			//disconnect.setVisibility(View.GONE);
        			Reinitialize();
        		}
        		catch(Exception e)
        		{
        			Toaster(e.toString());
        		}
        	}
        });
	}
	public void onPause()
	{
		super.onPause();
		//sensormgr.unregisterListener(this);
	}
	public void onResume()
	{
		super.onResume();
		//sensormgr.registerListener(this,rvectorsensor,SensorManager.SENSOR_DELAY_GAME);
		//ConnectedView();
	}
	public void onAccuracyChanged(Sensor sensor, int accuracy)
    {}
	public void onSensorChanged(SensorEvent event)
	{
		try
		{
			forward.setVisibility(View.INVISIBLE);
			reverse.setVisibility(View.INVISIBLE);
			left.setVisibility(View.INVISIBLE);
			right.setVisibility(View.INVISIBLE);
			forwardvalue.setVisibility(View.INVISIBLE);
			reversevalue.setVisibility(View.INVISIBLE);
			leftvalue.setVisibility(View.INVISIBLE);
			rightvalue.setVisibility(View.INVISIBLE);
		}
		catch(Exception e)
		{
			Toaster(e.toString());
		}
		if(event.sensor.getType()==Sensor.TYPE_ROTATION_VECTOR);
		{
			SensorManager.getRotationMatrixFromVector(mRotationMatrix, event.values);
			//SensorManager.remapCoordinateSystem(mRotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, mRotationMatrix);
			SensorManager.getOrientation(mRotationMatrix, orientationVals);
			orientationVals[0]=(int)Math.toDegrees(orientationVals[0]);
			orientationVals[1]=(int)Math.toDegrees(orientationVals[1]); //roll
			orientationVals[2]=(int)Math.toDegrees(orientationVals[2]); //pitch
			xmag=(int)orientationVals[2]; // X - Axis -> Pitch
			ymag=(int)orientationVals[1]; // Y - Axis -> Roll
			if(orientationVals[2]>10)
			{
				magforward=(int)orientationVals[2];
				forward.setVisibility(View.VISIBLE);
				forwardvalue.setVisibility(View.VISIBLE);
				if(orientationVals[2]>30)
				{
					forwardvalue.setText("FORWARD - MAX");
					xmag=30;
				}
				if(orientationVals[2]<=30)
				{
					forwardvalue.setText((String.valueOf(magforward)));
					xmag=magforward;
				}
			}
			if(orientationVals[2]<-10)
			{
				magreverse=-((int)orientationVals[2]);
				reverse.setVisibility(View.VISIBLE);
				reversevalue.setVisibility(View.VISIBLE);
				if(orientationVals[2]<-30)
				{
					reversevalue.setText("REVERSE - MAX");
					xmag=-30;
				}
				if(orientationVals[2]>=-30)
				{
					reversevalue.setText((String.valueOf(magreverse)));
					xmag=-magreverse;
				}
				
			}
			if(xmag<=10 && xmag>=-10)
			{
				xmag=0;
			}
			if(ymag<=10 && ymag>=-10)
			{
				ymag=0;
			}
			if(orientationVals[1]>10)
			{
				magleft=(int)orientationVals[1];
				left.setVisibility(View.VISIBLE);
				leftvalue.setVisibility(View.VISIBLE);
				if(orientationVals[1]>30)
				{
					leftvalue.setText("LEFT - MAX");
					ymag=30;
				}
				if(orientationVals[1]<=30)
				{
					leftvalue.setText((String.valueOf(magleft)));
					ymag=magleft;
				}
			}
			if(orientationVals[1]<-10)
			{
				magright=-((int)orientationVals[1]);
				right.setVisibility(View.VISIBLE);
				rightvalue.setVisibility(View.VISIBLE);
				if(orientationVals[1]<-30)
				{
					rightvalue.setText("RIGHT - MAX");
					ymag=-30;
				}
				if(orientationVals[1]>=-30)
				{
					rightvalue.setText((String.valueOf(magright)));
					ymag=-magright;
				}
			}
			//int x, y;
			//xmag=map(xmag,-30,30,-9,9);
			//ymag=map(ymag,-30,30,-9,9);
			String xstr=String.valueOf(xmag);
			String ystr=String.valueOf(ymag);
			String temp=xstr+ystr;
			int len=temp.length();
			/*switch(len)
			{
				case 2 : total="@@*"+xstr+","+ystr+"!@@";
				break;
				
				case 3 : total="@@*"+xstr+","+ystr+"!@";
				break;
				
				case 4 : total="@*"+xstr+","+ystr+"!@";
				break;
				
				default : total="DEFAULT_IT";
			}*/
			switch(len)
			{
				case 2 : total="@@@@*"+xstr+","+ystr+"!@@@@";
				break;
				
				case 3 : total="@@@@*"+xstr+","+ystr+"!@@@";
				break;
				
				case 4 : total="@@@*"+xstr+","+ystr+"!@@@";
				break;
				
				case 5 : total="@@@*"+xstr+","+ystr+"!@@";
				break;
				
				case 6 : total="@@*"+xstr+","+ystr+"!@@";
				break;
				
				default : total="DEFAULT_ITEMS";
			}
			//total="@@@@@*"+String.valueOf(xmag)+","+String.valueOf(ymag)+"!@@@@@";
			SendingThread snd=new SendingThread(total);
			snd.start();
			/*try
			{
				Send(total);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}*/
			/*if(xmag>0 && ymag>0)
			{
				if(xmag==ymag)
				{
					total="fl";
				}
				else
				{
					total=("@@@@*"+String.valueOf(xmag)+","+String.valueOf(ymag)+"!@@@@");
				}
			}
			if(xmag>0 && ymag<0)
			{
				if(xmag==-(ymag))
				{
					dir="fr";
				}
				else
				{
					total=("@@@@*"+String.valueOf(xmag)+","+String.valueOf(ymag)+"!@@@@");
				}
			}
			if(xmag<0 && ymag<0)
			{
				if(-(xmag)==-(ymag))
				{
					dir="@@@*rr";
				}
				else
				{
					total=("@@@@*"+String.valueOf(xmag)+","+String.valueOf(ymag)+"!@@@@");
				}
			}
			if(xmag<0 && ymag>0)
			{
				if(-(xmag)==ymag)
				{
					dir="rl";
				}
				else
				{
					total=("@@@@*"+String.valueOf(xmag)+","+String.valueOf(ymag)+"!@@@@");
				}
			}
			if(xmag==0)
			{
				if(ymag>0)
				{
					total="@@@@*0,"+String.valueOf(ymag)+"!@@@";
				}
				if(ymag<0)
				{
					total="@@@*0,"+String.valueOf(ymag)+"!@@";
				}
			}
			if(ymag==0)
			{
				if(xmag>0)
				{
					total=String.valueOf(xmag)+"0";
				}
				if(xmag<0)
				{
					total=String.valueOf(xmag)+"0";
				}
			}
			if(xmag==0 && ymag==0)
			{
				total="xx";
			}*/
			//int len=total.length();
			try
			{
				sent.setText(String.valueOf(total));
			}
			catch(Exception e)
			{
				Toaster(e.toString());
			}
			xmag=0;
			ymag=0;
			
			//total=("@@@@*"+String.valueOf(xmag)+","+String.valueOf(ymag)+"!@@@@");
		}
	}
	public void get_paired(View view)
	{
		paired_devices=bth.getBondedDevices();
		for(BluetoothDevice device:paired_devices)
		{
			bt_array_adapter.add(device.getName()+"\n"+device.getAddress());
		}
		Toaster("List of Paired Devices");
	}     
	public void SwitchOn(BluetoothAdapter ba)
	{
		ba=bth;
	    if(!ba.isEnabled())
	    {
	    	Intent turnOn=new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	    	startActivityForResult(turnOn,REQUEST_ENABLE_BT);
	    }
	    else
	    {
	    	Toaster("Bluetooth already Enabled");
	    }
	}
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
	    if (requestCode == ENABLE_BT_REQUEST_CODE)
		{
	    	if (resultCode == Activity.RESULT_OK)
			{
	            Toast.makeText(getActivity(), "Bluetooth has been Enabled",Toast.LENGTH_SHORT).show();
	            //ListeningThread t = new ListeningThread();
	            //t.start();
	        }
			if(resultCode==Activity.RESULT_CANCELED)
			{ 
				Toast.makeText(getActivity(), "Please Enable Bluetooth to use this app",Toast.LENGTH_SHORT).show();
	        }
	    }
	}
	public class ConnectingThread extends Thread
	{
		public final BluetoothSocket bluetoothSocket;
		public final BluetoothDevice bluetoothDevice;
		public ConnectingThread(BluetoothDevice device)
		{
			BluetoothSocket temp=null;
			bluetoothDevice=device;
			try
			{
				temp=bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
			}
			catch(IOException e)
			{
				Toaster("Unable to initiate Bluetooth Device");
			}
			bluetoothSocket=temp;
		}
		public void run()
		{
			/*try
			{
				pgd.getHandler().post(new Runnable()
				{
					public void run()
					{
						bt_list.setVisibility(View.GONE);
						//connecting.setVisibility(View.VISIBLE);
						conn.setVisibility(View.VISIBLE);
						pgd.show();
					}
				});
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}*/
			//pgd.show();
			bth.cancelDiscovery();
			try
			{
				bluetoothSocket.connect();
				out=bluetoothSocket.getOutputStream();
				getActivity().runOnUiThread(new Runnable()
				{
					public void run()
					{
						Toast.makeText(getActivity(), "Connection Successful", Toast.LENGTH_SHORT).show();
						ConnectedView();
					}
				});
			}
			catch(IOException e)
			{
				getActivity().runOnUiThread(new Runnable()
				{
					public void run()
					{
						Toast.makeText(getActivity(), "Unable to connect !\nCheck that the device is turned ON", Toast.LENGTH_LONG).show();
						Reinitialize();
					}
				});
			}
			/*try
			{
				connecting.getHandler().post(new Runnable()
				{
					public void run()
					{
						//connecting.setVisibility(View.GONE);
						//conn.setVisibility(View.GONE);
					}
				});
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}*/
		}
		public void cancel()
		{
			try
			{
				out.close();
				bluetoothSocket.close();
			}
			catch(IOException e)
			{
				Toaster("Connection Termination Unsuccessful");
			}
		}
	}
	public class SendingThread extends Thread
	{
		byte buffer[];
		public SendingThread(String data)
		{
			buffer=data.getBytes();
		}
		public void run()
		{
			try
			{
				//Thread.sleep(100);
				out.write(buffer);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	/*private void Send(String data)throws IOException
    {
    	byte buffer[]=data.getBytes();
    	try
    	{
    		out.write(buffer);
    	}
    	catch(IOException e)
    	{
    		e.printStackTrace();
    	}
    }*/
	public void Toaster(String msg)
	{
		Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
	}
	public void Disconnect(BluetoothDevice dev)
	{
		try
		{
			ConnectingThread disc=new ConnectingThread(dev);
			disc.cancel();
			sensormgr.unregisterListener(this);
			Reinitialize();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	public void ConnectedView()
	{
		try
		{
			sensormgr.registerListener(this,rvectorsensor,SensorManager.SENSOR_DELAY_GAME); //60 ms delay
			disconnect.setVisibility(View.VISIBLE);
			sent.setVisibility(View.VISIBLE);
			pgd.hide();
		//bt_list.setVisibility(View.GONE);
		//forward.setVisibility(View.GONE);
		//reverse.setVisibility(View.GONE);
		//left.setVisibility(View.GONE);
		//right.setVisibility(View.GONE);
		//forwardvalue.setVisibility(View.GONE);
		//reversevalue.setVisibility(View.GONE);
		//leftvalue.setVisibility(View.GONE);
		//rightvalue.setVisibility(View.GONE);
		}
		catch(Exception e)
		{
			Toaster(e.toString());
		}
	}
	public void Reinitialize()
	{
		try
		{
			bt_array_adapter.clear();
			bt_list.setVisibility(View.GONE);
			disconnect.setVisibility(View.GONE);
			forward.setVisibility(View.GONE);
			reverse.setVisibility(View.GONE);
			left.setVisibility(View.GONE);
			right.setVisibility(View.GONE);
			forwardvalue.setVisibility(View.GONE);
			reversevalue.setVisibility(View.GONE);
			leftvalue.setVisibility(View.GONE);
			rightvalue.setVisibility(View.GONE);
			sel_device.setVisibility(View.VISIBLE);
			pgd.hide();
			sent.setVisibility(View.GONE);

		}
		catch(Exception e)
		{
			Toaster(e.toString());
		}
	}
	public int map(int x, int in_min, int in_max, int out_min, int out_max)
	{
		return(x-in_max)*(out_max-out_min)/(in_max-in_min)+out_min;
	}
}