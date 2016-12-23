package com.nishant.trackbot2;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

/**
 * Created by nishant on 6/8/16.
 */
public class ButtonsFragmentBluetooth extends Fragment implements View.OnTouchListener
{
    public Button left,right,forward,reverse,sel_device,disconnect;
    public ListView bt_list;
    public int fwd=0,rev=0,lef=0,rit=0;
    private static final int ENABLE_BT_REQUEST_CODE = 1;
    private static final int REQUEST_ENABLE_BT = 1;
    public String item_value,temp = "@@@@@@@@@";
    private final static UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    public OutputStream out;
    public ProgressDialog pgd;
    public TextView status,sel_device_button_text;
    public BluetoothDevice remote_device;
    private Set<BluetoothDevice> paired_devices;
    private ArrayAdapter<String>bt_array_adapter;
    public BluetoothAdapter bth;
    public static MotionSensorFragmentBluetooth newInstance()
    {
        MotionSensorFragmentBluetooth buttonsfragmentbluetooth=new MotionSensorFragmentBluetooth();
        return buttonsfragmentbluetooth;
    }
    public ButtonsFragmentBluetooth()
    {}
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView=inflater.inflate(R.layout.buttons_fragment, container, false);
        return rootView;
    }
    public void onViewCreated(View view, Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        pgd = new ProgressDialog(getActivity());
        pgd.setMessage("Connecting ...");
        pgd.setCancelable(false);
        left = (Button) view.findViewById(R.id.left_butt);
        right = (Button) view.findViewById(R.id.right_butt);
        forward = (Button) view.findViewById(R.id.forward_butt);
        reverse = (Button) view.findViewById(R.id.reverse_butt);
        status = (TextView) view.findViewById(R.id.debug);
        sel_device_button_text = (TextView)view.findViewById(R.id.sel_device_button_text);
        forward.setOnTouchListener(this);
        reverse.setOnTouchListener(this);
        left.setOnTouchListener(this);
        right.setOnTouchListener(this);
        bth = BluetoothAdapter.getDefaultAdapter();
        if (bth == null)
        {
            Toaster("Bluetooth not Supported by Device");
        }
        SwitchOn(bth);
        sel_device = (Button) view.findViewById(R.id.sel_device_butt);
        bt_list = (ListView) view.findViewById(R.id.devices_butt);
        disconnect = (Button) view.findViewById(R.id.disconnect_butt);
        //sel_device = (Button) view.findViewById(R.id.sel_device_ms);
        //disconnect = (Button) view.findViewById(R.id.disconnect_ms);
        //bt_list = (ListView) view.findViewById(R.id.devices_ms);
        bt_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                item_value = (String) bt_list.getItemAtPosition(position);
                String MAC = item_value.substring(item_value.length() - 17);
                remote_device = bth.getRemoteDevice(MAC);
                bt_list.setVisibility(View.GONE);
                pgd.setMessage("Connecting to :\n"+item_value);
                //Toaster("Connecting to  :\n" + item_value);
                pgd.show();
                ConnectingThread t = new ConnectingThread(remote_device);
                t.start();
                //pgd.hide();
            }
        });
        disconnect.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
               ConnectingThread t = new ConnectingThread(remote_device);
                t.cancel();
                Reinitialize();
            }
        });

        bt_array_adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
        bt_list.setAdapter(bt_array_adapter);
        sel_device.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (!bth.isEnabled())
                {
                    SwitchOn(bth);
                }
                if (bth.isEnabled())
                {
                    try
                    {
                        get_paired(v);
                        sel_device.setVisibility(View.GONE);
                        sel_device_button_text.setVisibility(View.GONE);
                        bt_list.setVisibility(View.VISIBLE);
                    }
                    catch (Exception e)
                    {
                        Toaster(e.toString());
                    }
                }
            }
        });
    }

    public boolean onTouch(View v, MotionEvent event)
    {
        int action = event.getActionMasked();
        if(action == MotionEvent.ACTION_DOWN)
        {
            switch(v.getId())
            {
                case R.id.forward_butt:
                    fwd = 1;
                    break;

                case R.id.reverse_butt:
                    rev = 1;
                    break;

                case R.id.left_butt:
                    lef = 1;
                    break;

                case R.id.right_butt:
                    rit = 1;
                    break;

            }
            UpdateTotal();
        }

        if(action == MotionEvent.ACTION_UP)
        {
            switch(v.getId())
            {
                case R.id.forward_butt:
                    fwd = 0;
                    break;

                case R.id.reverse_butt:
                    rev = 0;
                    break;

                case R.id.left_butt:
                    lef = 0;
                    break;

                case R.id.right_butt:
                    rit = 0;
                    break;

            }
            UpdateTotal();
        }

        if(action == MotionEvent.ACTION_POINTER_DOWN)
        {
            switch(v.getId())
            {
                case R.id.forward_butt:
                    fwd = 1;
                    break;

                case R.id.reverse_butt:
                    rev = 1;
                    break;

                case R.id.left_butt:
                    lef = 1;
                    break;

                case R.id.right_butt:
                    rit = 1;
                    break;

            }
            UpdateTotal();
        }

        if(action == MotionEvent.ACTION_POINTER_UP)
        {
            switch(v.getId())
            {
                case R.id.forward_butt:
                    fwd = 0;
                    break;

                case R.id.reverse_butt:
                    rev = 0;
                    break;

                case R.id.left_butt:
                    lef = 0;
                    break;

                case R.id.right_butt:
                    rit = 0;
                    break;

            }
            UpdateTotal();
        }
        return(true);
    }

    public void UpdateTotal()
    {
        if(fwd == 0 && rev == 0 && lef == 0 && rit == 0)
        {
            temp = "@@@*x!@@@";
        }
        if(fwd == 0 && rev == 0 && lef == 0 && rit == 1)
        {
            temp = "@@@*a!@@@";
        }
        if(fwd == 0 && rev == 0 && lef == 1 && rit == 0)
        {
            temp = "@@@*b!@@@";
        }

        /*tentative
        if(fwd == 0 && rev == 0 && lef == 1 && rit == 1)
        {
            temp = "@@@*x!@@@";
        }
        */

        if(fwd == 0 && rev == 1 && lef == 0 && rit == 0)
        {
            temp = "@@@*c!@@@";
        }
        if(fwd == 0 && rev == 1 && lef == 0 && rit == 1)
        {
            temp = "@@@*d!@@@";
        }
        if(fwd == 0 && rev == 1 && lef == 1 && rit == 0)
        {
            temp = "@@@*e!@@@";
        }

        /*tentative
        if(fwd == 0 && rev == 1 && lef == 1 && rit == 1)
        {
            temp = "@@@*x!@@@";
        }
        */

        if(fwd == 1 && rev == 0 && lef == 0 && rit == 0)
        {
            temp = "@@@*f!@@@";
        }
        if(fwd == 1 && rev == 0 && lef == 0 && rit == 1)
        {
            temp = "@@@*g!@@@";
        }
        if(fwd == 1 && rev == 0 && lef == 1 && rit == 0)
        {
            temp = "@@@*h!@@@";
        }

        /*tentative
        if(fwd == 1 && rev == 0 && lef == 1 && rit == 1)
        {
            temp = "@@@*x!@@@";
        }
        //tent

        //tent
        if(fwd == 1 && rev == 1 && lef == 0 && rit == 0)
        {
            temp = "@@@*x!@@@";
        }
        //tent

        //tent
        if(fwd == 1 && rev == 1 && lef == 0 && rit == 1)
        {
            temp = "@@@*x!@@@";
        }
        //tent

        //tent
        if(fwd == 1 && rev == 1 && lef == 1 && rit == 0)
        {
            temp = "@@@*x!@@@";
        }
        //tent

        //tent
        if(fwd == 1 && rev == 1 && lef == 1 && rit == 1)
        {
            temp = "@@@*x!@@@";
        }
        */

        status.setText(temp);
        SendingThread snd=new SendingThread(temp);
        snd.start();
    }

    public void Toaster(String msg)
    {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT).show();
    }

    public void get_paired(View view)
    {
        paired_devices = bth.getBondedDevices();
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
                        Toast.makeText(getActivity(), "Successfully Connected", Toast.LENGTH_SHORT).show();
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
                getActivity().runOnUiThread(new Runnable()
                {
                    public void run()
                    {
                        Toast.makeText(getActivity(), "Disconnected !", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            catch(IOException e)
            {
                Toaster("Connection Termination Unsuccessful");
            }
        }
    }

    public void ConnectedView()
    {
        pgd.hide();
        sel_device_button_text.setVisibility(View.GONE);
        status.setVisibility(View.VISIBLE);
        left.setVisibility(View.VISIBLE);
        right.setVisibility(View.VISIBLE);
        forward.setVisibility(View.VISIBLE);
        reverse.setVisibility(View.VISIBLE);
        disconnect.setVisibility(View.VISIBLE);
    }

    public void Reinitialize()
    {
        pgd.hide();
        bt_array_adapter.clear();
        left.setVisibility(View.GONE);
        right.setVisibility(View.GONE);
        status.setVisibility(View.GONE);
        forward.setVisibility(View.GONE);
        reverse.setVisibility(View.GONE);
        disconnect.setVisibility(View.GONE);
        bt_list.setVisibility(View.GONE);
        sel_device.setVisibility(View.VISIBLE);
        sel_device_button_text.setVisibility(View.VISIBLE);
    }

    public class SendingThread extends Thread
    {
        byte buffer[];
        public SendingThread(String data)

        {
            buffer = data.getBytes();
        }
        public void run()
        {
            try
            {
                out.write(buffer);
                //Thread.sleep(50);
                //buffer1 = buffer;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
