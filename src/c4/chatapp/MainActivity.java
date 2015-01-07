package c4.chatapp;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import c4.chatapp.protocol.LoginMsg;
import c4.chatapp.protocol.Message;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements WebSocketClient.Listener
{
   private static final String LOG_TAG = "MainActivity";
   private static final String CHAT_URL = "ws://10.101.12.142/chat/ws";
   private WebSocketClient mChatClient;
   private TimerTask mPingPongTask;
   private Timer mHeartbeatTimer;
   private Button mConnectBtn;
   private Button mDisconnectBtn;


   @Override
   public void onCreate(Bundle savedInstanceState)
   {
      Log.d(LOG_TAG,"onCreate()");
      super.onCreate(savedInstanceState);
      setContentView(R.layout.main);
      setup();
   }


   @Override
   protected void onResume()
   {
      Log.d(LOG_TAG,"onResume()");
      super.onResume();
   }

   @Override
   protected void onPause()
   {
      Log.d(LOG_TAG,"onPause()");
      super.onPause();
   }

   @Override
   protected void onStart()
   {
      Log.d(LOG_TAG,"onStart()");
      super.onStart();
   }

   @Override
   protected void onRestart()
   {
      Log.d(LOG_TAG,"onRestart()");
      super.onRestart();
   }

   @Override
   protected void onStop()
   {
      Log.d(LOG_TAG,"onStop()");
      super.onStop();
   }

   @Override
   protected void onDestroy()
   {
      Log.d(LOG_TAG,"onDestroy()");

      if(mChatClient.getState() == WebSocketClient.State.CONNECTED){
         mChatClient.disconnect();
      }


      super.onDestroy();
   }


   private void setup()
   {
      mConnectBtn = (Button)findViewById(R.id.connect_btn);
      mDisconnectBtn = (Button)findViewById(R.id.disconnect_btn);
      mChatClient = new WebSocketClient(CHAT_URL,this);

      mConnectBtn.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            if(mChatClient.getState().equals(WebSocketClient.State.DISCONNECTED)){
               mChatClient.connect();
            }
         }
      });

      mDisconnectBtn.setOnClickListener(new View.OnClickListener()
      {
         @Override
         public void onClick(View v)
         {
            if(mChatClient.getState().equals(WebSocketClient.State.CONNECTED) ){
               doLogout();
               //mChatClient.close();
               //mChatClient.disconnect();
            }
         }
      });

      mPingPongTask = new TimerTask()
      {
         @Override
         public void run()
         {
            Message pingMsg = new Message();
            pingMsg.TYPE = "PING";
            sendMessage(pingMsg);
         }
      };

      mHeartbeatTimer = new Timer();
   }

   private void connect()
   {
      mChatClient.connect();
   }

   @Override
   public void onConnect()
   {
      Log.d(LOG_TAG,"onConnect()");
      doLogin();
   }

   @Override
   public void onMessage(final String message)
   {
      Log.d(LOG_TAG,"onMessage(String)");
      handleStringMessage(message);
   }

   @Override
   public void onMessage(byte[] data)
   {
      Log.d(LOG_TAG,"onMessage(Byte)");
   }

   @Override
   public void onDisconnect(int code, String reason)
   {
      Log.d(LOG_TAG,"onDisconnect()");
      //mChatClient.close();
      //mChatClient.disconnect();
   }

   @Override
   public void onError(Exception error)
   {
      Log.e(LOG_TAG,"onError()",error);
   }

   private void doLogout()
   {
      mHeartbeatTimer.cancel();

      Message msg = new Message();

      msg.TYPE = "ACCOUNT";
      msg.SUBTYPE = "LOGOUT";

      sendMessage(msg);
   }

   private void doLogin()
   {
      Message msg = new Message();
      LoginMsg loginMsg = new LoginMsg();

      msg.TYPE = "ACCOUNT";
      msg.SUBTYPE = "LOGIN";

      loginMsg.USER = "luke";
      loginMsg.PASSWD = "123";

      msg.LOGIN_MSG = loginMsg;

      sendMessage(msg);
   }

   private void handleStringMessage(String jsonStr)
   {
      Gson gson = new GsonBuilder().serializeNulls().create();

      try {
         jsonStr = jsonStr.trim();
         Log.d(LOG_TAG,"Recv.:" + jsonStr);
         Message serverMsg = gson.fromJson(jsonStr, Message.class);

         switch (serverMsg.TYPE){
            case "ACCOUNT": handleAccount(serverMsg); break;
            case "INFO": handleInfo(serverMsg); break;
            case "CHAT": handleChat(serverMsg); break;
            default: break;
         }

      } catch (JsonSyntaxException e) {
         Log.e(LOG_TAG, "handleStringMessage()",e);
      }
   }

   private void handleAccount(Message serverMsg)
   {
      if(serverMsg.SUBTYPE.equals("LOGIN") && serverMsg.RESULT_MSG.CODE.equals("OK")){
         mHeartbeatTimer.schedule(mPingPongTask,0,27000);
         return;
      }


   }

   private void handleInfo(Message serverMsg)
   {

   }

   private void handleChat(Message serverMsg)
   {

   }


   private void sendMessage(final Message clientMsg)
   {
      final Gson gson = new Gson();

      try {
         final String jsonStr = gson.toJson(clientMsg);
         if (mChatClient.getState().equals(WebSocketClient.State.CONNECTED)) {
            Log.d(LOG_TAG,"Send: " + jsonStr);
            mChatClient.send(jsonStr);
         }
      } catch (Exception e) {
         Log.e(LOG_TAG, "sendMessage()", e);
      }
   }

}
