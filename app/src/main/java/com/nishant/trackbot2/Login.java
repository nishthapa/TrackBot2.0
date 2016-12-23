package com.nishant.trackbot2;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Login extends AppCompatActivity
{
    public EditText username,password,new_usrname,new_pass,rep_new_pass;
    public BluetoothAdapter bt;
    public Button login;
    public String commmode,contrmode,zero="0";
    public String usrstr,paswstr,realpassword,realusername,nuser,npass,nreppass,
            createtable="CREATE TABLE IF NOT EXISTS Users (Username VARCHAR, Password VARCHAR, CommMode VARCHAR, ContrMode VARCHAR);",
            tablename="Users", realusr,realpass;
    SQLiteDatabase usrdb=null;
    AlertDialog.Builder builder=null;
    AlertDialog dialog=null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("TrackBot 2.0 - Login");
        //setActionBar(Toolbar toolbar);
        bt=BluetoothAdapter.getDefaultAdapter();
        username=(EditText)findViewById(R.id.username);
        password=(EditText)findViewById(R.id.password);
        login=(Button)findViewById(R.id.login);
        try
        {
            usrdb=this.openOrCreateDatabase("UsersDB.db", SQLiteDatabase.CREATE_IF_NECESSARY, null);
            usrdb.execSQL(createtable);
            //usrdb.execSQL("INSERT INTO Users" + "(Username, Password)" + "VALUES ('nishant','1234567890');");
            //usrdb.execSQL("INSERT INTO Users" + "(Username, Password)" + "VALUES ('bidur','1111111111');");
            //usrdb.execSQL("INSERT INTO Users" + "(Username, Password)" + "VALUES ('tilak','0000000000');");
        }
        catch(Exception e)
        {
            Toaster(e.toString());
        }
        login.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                usrstr=username.getText().toString();
                paswstr=password.getText().toString();
                Authenticate(usrdb,tablename,usrstr,paswstr);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id)
        {
            case R.id.add_new_user : AddUser();
                break;

            case R.id.exit : Exit();
                break;

            case R.id.about : About();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void Exit()
    {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        //builder.setTitle("Exit");
        builder.setMessage("Are you sure you want to Exit ?");
        builder.setPositiveButton("No",null);
        builder.setNegativeButton("Yes", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int id)
            {
                //bt.disable();
                finish();
                System.exit(0);
            }
        });
        builder.create();
        builder.show();
    }

    public void Authenticate(SQLiteDatabase usrdb, String tablename, String usrstr, String paswstr)
    {
        String q="SELECT * FROM Users";
        int result=0;
        try
        {
            Cursor c = usrdb.rawQuery(q, null);
            int Column1 = c.getColumnIndex("Username");
            int Column2 = c.getColumnIndex("Password");
            int Column3 = c.getColumnIndex("CommMode");
            int Column4 = c.getColumnIndex("ContrMode");
            c.moveToFirst();
            if(c!=null)
            {
                do
                {
                    realusr = c.getString(Column1);
                    realpass = c.getString(Column2);
                    commmode = c.getString(Column3);
                    contrmode = c.getString(Column4);
                    if((realusr.equals(usrstr)) && (realpass.equals(paswstr)))
                    {
                        result=1;
                        break;
                    }
                    else
                    {
                        result=-1;
                        username.setText("");
                        password.setText("");
                    }
                }
                while(c.moveToNext());
            }
            c.close();
        }
        catch(Exception e)
        {
            //Toaster(e.toString());
            if((e.toString()).equals("android.database.CursorIndexOutOfBoundsException: Index 0 requested, with a size of 0"))
            {
                Toaster("NO USERS IN DATABASE !\nYou must first add a New User from the Menu");
                username.setText("");
                password.setText("");
            }
            else
            {
                Toaster(e.toString());
            }
        }
        if(result==1)
        {
            Toaster("temp_contr = "+String.valueOf(contrmode)+"\ntemp_comm = "+String.valueOf(commmode));
            Toaster("Authentication Successful");
            Intent intent=new Intent(this,Communication.class);
            intent.putExtra("Username",realusr);
            intent.putExtra("CommMode",commmode);
            intent.putExtra("ContrMode",contrmode);
            startActivity(intent);
            finish();
        }
        else
        {
            Toaster("Authentication Failure  :\nIncorrect Username or Password");
        }
    }
    public void AddUser()
    {
        LayoutInflater linf=LayoutInflater.from(this);
        View inflator=linf.inflate(R.layout.add_user,null);
        builder=new AlertDialog.Builder(this);
        builder.setTitle("Add New User");
        builder.setView(inflator);
        new_usrname=(EditText)inflator.findViewById(R.id.new_usrname);
        new_pass=(EditText)inflator.findViewById(R.id.new_pass);
        rep_new_pass=(EditText)inflator.findViewById(R.id.rep_new_pass);
        builder.setPositiveButton("Cancel",null);
        builder.setNegativeButton("Create",new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog,int which)
            {

            }
        });
        dialog=builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Boolean close=false;
                nuser=new_usrname.getText().toString();
                npass=new_pass.getText().toString();
                nreppass=rep_new_pass.getText().toString();
                if(nuser.equals(""))
                {
                    Toaster("You must create a Username");
                }
                else
                {
                    if(npass.equals("") || nreppass.equals(""))
                    {
                        Toaster("You must create a new Password");
                    }
                    else if((npass.length())<6)
                    {
                        Toaster("The new password must be atleast 6 Characters long");
                        new_pass.setText("");
                        rep_new_pass.setText("");
                        new_pass.requestFocus();
                        close=false;
                    }
                    else
                    {
                        if(npass.equals(nreppass))
                        {
                            String query="SELECT * FROM Users";
                            int res=0;
                            try
                            {
                                Cursor c=usrdb.rawQuery(query,null);
                                int col1=c.getColumnIndex("Username");
                                int col2=c.getColumnIndex("Password");
                                c.moveToFirst();
                                if(c!=null)
                                {
                                    do
                                    {
                                        String realusr=c.getString(col1);
                                        //String realpass=c.getString(col2);
                                        if(realusr.equals(nuser))
                                        {
                                            Toaster("Username already Exists\nPick a different Username");
                                            new_usrname.setText("");
                                            new_pass.setText("");
                                            rep_new_pass.setText("");
                                        }
                                        else
                                        {
                                            //usrdb.execSQL("INSERT INTO Users" + "(Username, Password, CommMode, ContrMode)" + "VALUES ("+"'"+nuser+"','"+npass+"',0,0);");
                                            usrdb.execSQL("INSERT INTO Users" + "(Username, Password, CommMode, ContrMode)" + "VALUES ("+"'"+nuser+"','"+npass+"','"+zero+"','"+zero+"');");
                                            close=true;
                                            Toaster("New User successfully Added");
    			    						/*AlertDialog.Builder builder=new AlertDialog.Builder(getBaseContext());
    			    						builder.setTitle("New User successfully Added");
    			    						builder.setMessage("Username  : "+nuser+"\nPassword  : "+npass);
    			    						builder.setPositiveButton("OK",null);
    			    						builder.create();
    			    						builder.show();*/
                                        }
                                    }
                                    while(c.moveToNext());
                                }
                            }
                            catch(Exception e)
                            {
                                if((e.toString()).equals("android.database.CursorIndexOutOfBoundsException: Index 0 requested, with a size of 0"))
                                {
                                    //usrdb.execSQL("INSERT INTO Users" + "(Username, Password)" + "VALUES ("+"'"+nuser+"','"+npass+"');");
                                    usrdb.execSQL("INSERT INTO Users" + "(Username, Password, CommMode, ContrMode)" + "VALUES ("+"'"+nuser+"','"+npass+"','"+zero+"','"+zero+"');");
                                    close=true;
                                    Toaster("New User successfully Added");
                                }
                                else
                                {
                                    Toaster(e.toString());
                                }
                            }
                        }
                        else
                        {
                            new_pass.setText("");
                            rep_new_pass.setText("");
                            new_pass.requestFocus();
                            Toaster("Password Mismatch.\nEnter again");
                            close=false;
                        }
                    }
                }
                if(close)
                {
                    dialog.dismiss();
                }
            }
        });
        //builder.show();
    }

    public void Toaster(String msg)
    {
        Toast.makeText(getApplicationContext(),msg,Toast.LENGTH_LONG).show();
    }
    public void About()
    {
        LayoutInflater linf=LayoutInflater.from(this);
        View inflator=linf.inflate(R.layout.about,null);
        builder=new AlertDialog.Builder(this);
        //builder.setTitle("TrackBot 2.0 - About");
        builder.setView(inflator);
        builder.setPositiveButton("OK",null);
        builder.create();
        builder.show();
    }

    @Override
    public void onBackPressed()
    {
        Exit();
    }
}
