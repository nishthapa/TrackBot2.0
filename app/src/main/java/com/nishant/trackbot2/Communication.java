package com.nishant.trackbot2;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import java.io.OutputStream;

public class Communication extends AppCompatActivity
{
	public String drawerListViewItems[],cont_mode_str,query,
		comm_mode_str,db_commmode,db_contrmode,dirvalue,item_value,hatdir,user,
		angvalue,magvalue,total,temp;
	int throttle,len,comm,contr,db_comm_int,db_contr_int,temp_comm,temp_contr;
	public OutputStream out;
	public DrawerLayout drawerLayout;
	public BluetoothAdapter adapt;
	public ContentValues cv;
	//public ActionBar actionbar;
	SQLiteDatabase usrdb=null;
	public ListView drawerListView;
	public ActionBarDrawerToggle actionBarDrawerToggle;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.communication);
		Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
		//setSupportActionBar(toolbar);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_drawer);
		usrdb=this.openOrCreateDatabase("UsersDB.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
		cv = new ContentValues();
		//actionbar = getSupportActionBar();
		if(savedInstanceState==null)
		{
			Bundle extras = getIntent().getExtras();
			user = extras.getString("Username");
			db_commmode = extras.getString("CommMode");
			db_contrmode = extras.getString("ContrMode");
			try
			{
				db_comm_int = Integer.parseInt(db_commmode);//.toString());
			}
			catch (NumberFormatException nfe)
			{}
			try
			{
				db_contr_int = Integer.parseInt(db_contrmode);//.toString());
			}
			catch(NumberFormatException nfe)
			{}
			temp_comm = db_comm_int;
			temp_contr = db_contr_int;

			// Setting the Controller mode according to values in the database when first logging in
			if(temp_contr == 1)
			{
				getSupportActionBar().setTitle("TrackBot 2.0 - "+user+" Logged in.(Motion Sensor)");
				FragmentManager fm=getFragmentManager();
				boolean addToBackStack=true;
				Fragment fragment=null;
				fragment=new MotionSensorFragmentBluetooth();
				if(fragment!=null)
				{
					FragmentTransaction ft=fm.beginTransaction();
					ft.replace(R.id.container,fragment);
					if(addToBackStack==true)
					{
						ft.addToBackStack(null);
					}
					ft.commit();
				}

			}

			//if(cont_mode_str.equals("Digital Buttons"))
			if(temp_contr == 0)
			{
				getSupportActionBar().setTitle("TrackBot 2.0 - "+user+" Logged in.(Digital Buttons)");
				FragmentManager fm=getFragmentManager();
				boolean addToBackStack=true;
				Fragment fragment=null;
				fragment=new ButtonsFragmentBluetooth();
				if(fragment!=null)
				{
					FragmentTransaction ft=fm.beginTransaction();
					ft.replace(R.id.container,fragment);
					if(addToBackStack==true)
					{
						ft.addToBackStack(null);
					}
					ft.commit();
				}

			}
		}
		else
		{
			user=(String)savedInstanceState.getSerializable("Username");
		}
		//getSupportActionBar().setTitle("TrackBot 2.0 - "+user+" Logged in.(Digital Buttons)");
		adapt=BluetoothAdapter.getDefaultAdapter();
		drawerListViewItems=getResources().getStringArray(R.array.drawer_items);
		drawerListView=(ListView)findViewById(R.id.left_drawer);
		drawerListView.setAdapter(new ArrayAdapter<String>(this,R.layout.drawer_listview_item,drawerListViewItems));
		drawerLayout=(DrawerLayout)findViewById(R.id.drawer_layout);
		actionBarDrawerToggle=new ActionBarDrawerToggle(this,drawerLayout,R.drawable.ic_drawer,
		R.string.drawer_open,R.string.drawer_close);
		drawerLayout.setDrawerListener(actionBarDrawerToggle);
		drawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		drawerListView.setOnItemClickListener(new DrawerItemClickListener());
	}
	
	@Override
	public void onPostCreate(Bundle savedInstanceState)
	{
		super.onPostCreate(savedInstanceState);
		actionBarDrawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
		actionBarDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if(actionBarDrawerToggle.onOptionsItemSelected(item))
		{
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	public class DrawerItemClickListener implements ListView.OnItemClickListener
	{
		public void onItemClick(AdapterView parent, View view,int position,long id)
		{
			switch(position)
			{
				case 1 : CharSequence comm_mode_items[]=new CharSequence[]{"Bluetooth","WiFi"};
						 CommMode(comm_mode_items);
						 break;
						 
				case 2 : CharSequence contr_mode[]=new CharSequence[]{"Digital Buttons","Motion Sensor"};
						 ContrMode(contr_mode,"Select Controller Mode :");
						 break;
						 
				case 3 : LogOut();
				
			}
			//Toast.makeText(Communication.this,((TextView)view).getText(),Toast.LENGTH_LONG).show();
			drawerLayout.closeDrawer(drawerListView);
		}
	}
	public void Toaster(String msg)
	{
		Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
	}
	public void ContrMode(final CharSequence contents[],String title)
	{
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setPositiveButton("Cancel",null);
		builder.setSingleChoiceItems(contents, temp_contr, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog,int which)
			{
				//which = db_contr_int;
				//cont_mode_str=(contents[which]).toString();
				temp_contr = which;
			}
		});
		builder.setNegativeButton("OK", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int id)
			{
				Toaster(String.valueOf(temp_contr));
				try
				{
					/*if(cont_mode_str.equals("Analog Joystick"))
					{
						getActionBar().setTitle("IoR - User "+user+" Logged in. (Controller : Joystick)");
						FragmentManager fm=getFragmentManager();
						boolean addToBackStack=true;
						Fragment fragment=null;
						fragment=new JoystickFragmentBluetooth();
						if(fragment!=null)
						{
							FragmentTransaction ft=fm.beginTransaction();
							ft.replace(R.id.container,fragment);
							if(addToBackStack==true)
							{
								ft.addToBackStack(null);
							}
							ft.commit();
						}
					}*/

					//if(cont_mode_str.equals("Motion Sensor"))
					if(temp_contr == 1)
					{
						getSupportActionBar().setTitle("TrackBot 2.0 - "+user+" Logged in.(Motion Sensor)");
						FragmentManager fm=getFragmentManager();
						boolean addToBackStack=true;
						Fragment fragment=null;
						fragment=new MotionSensorFragmentBluetooth();
						if(fragment!=null)
						{
							FragmentTransaction ft=fm.beginTransaction();
							ft.replace(R.id.container,fragment);
							if(addToBackStack==true)
							{
								ft.addToBackStack(null);
							}
							ft.commit();
						}
						
					}

					//if(cont_mode_str.equals("Digital Buttons"))
					if(temp_contr == 0)
					{
						getSupportActionBar().setTitle("TrackBot 2.0 - "+user+" Logged in.(Digital Buttons)");
						FragmentManager fm=getFragmentManager();
						boolean addToBackStack=true;
						Fragment fragment=null;
						fragment=new ButtonsFragmentBluetooth();
						if(fragment!=null)
						{
							FragmentTransaction ft=fm.beginTransaction();
							ft.replace(R.id.container,fragment);
							if(addToBackStack==true)
							{
								ft.addToBackStack(null);
							}
							ft.commit();
						}

					}
				}
				catch(Exception e)
				{
					Toaster(e.toString());
				}
			}
		});
		builder.create();
		builder.show();	
	}
	public void LogOut()
	{
		final Intent intnt=new Intent(getBaseContext(),Login.class);
		intnt.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		//builder.setTitle("Log Out");
		builder.setMessage("Are you sure you want to Log Out ?");
		builder.setPositiveButton("No",null);
		builder.setNegativeButton("Yes", new DialogInterface.OnClickListener()
		{
			public void onClick(DialogInterface dialog,int id)
			{
				/*the 2 values temp_contr and temp_comm are the values to be updated in the database for Communication
				  mode and controller mode when logging out*/
				 Toaster("temp_contr = "+String.valueOf(temp_contr)+"\ntemp_comm = "+String.valueOf(temp_comm));
				 adapt.disable();
				 cv.put("ContrMode",String.valueOf(temp_contr));
				 cv.put("CommMode",String.valueOf(temp_comm));
				 usrdb.update("Users",cv,"Username="+"'"+user+"'",null);
				 //query = "UPDATE Users SET ContrMode = "+String.valueOf(temp_contr)+","+" CommMode = "+String.valueOf(temp_comm)+" WHERE Username = "+" ' "+user+" ' ";
				 //usrdb.execSQL(query);
				 Toaster("Logged Out");
				 finish();
				 startActivity(intnt);
			}
		});
		builder.create();
		builder.show();
	}
	
	@Override
	public void onBackPressed()
	{
		Toaster("You must Log Out");
	}
	public void CommMode(final CharSequence comm_mode_items[])
	{
		AlertDialog.Builder builder=new AlertDialog.Builder(this);
		builder.setTitle("Select Communication Mode");
		builder.setPositiveButton("Cancel", null);
		builder.setSingleChoiceItems(comm_mode_items, temp_comm, new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog,int which)
			{
				//which = db_comm_int;
				//comm_mode_str = (comm_mode_items[which]).toString();
				temp_comm  = which;
			}
		});
		builder.setNegativeButton("OK", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int id)
			{
				Toaster(String.valueOf(temp_comm));
			}
		});
		builder.create();
		builder.show();
	}
}
		