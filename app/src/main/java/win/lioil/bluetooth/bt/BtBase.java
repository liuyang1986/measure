package win.lioil.bluetooth.bt;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Environment;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import win.lioil.bluetooth.APP;
import win.lioil.bluetooth.util.Util;

/**
 * 客户端和服务端的基类，用于管理socket长连接
 */
public class BtBase {
    static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private static final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/bluetooth/";
    private static final int FLAG_MSG = 0;  //消息标记
    private static final int FLAG_FILE = 1; //文件标记

    private static final String TAG = "Test BtBase";

    private BluetoothSocket mSocket;
    private DataOutputStream mOut;
    private Listener mListener;
    private boolean isRead;
    private boolean isSending;

    BtBase(Listener listener) {
        mListener = listener;
    }

    /**
     * 循环读取对方数据(若没有数据，则阻塞等待)
     */
    void loopRead(BluetoothSocket socket) {
        mSocket = socket;
        try {
            if (!mSocket.isConnected())
                mSocket.connect();
            notifyUI(Listener.CONNECTED, mSocket.getRemoteDevice());
            mOut = new DataOutputStream(mSocket.getOutputStream());
            DataInputStream in = new DataInputStream(mSocket.getInputStream());
            isRead = true;
            while (isRead) {
                byte[] readBuff = new byte[1024];
                int length = -1;
                StringBuffer hexBuffer = new StringBuffer();
                while ((length = in.read(readBuff)) != -1) {
                    String hexStr = new String(readBuff,0,length);

                    byte[] hexCharBuff = hexStr.getBytes();
                    boolean bFlag = false;
                    for (int i=0; i<hexCharBuff.length; i++)
                    {
//                        //包结束标志，换行符号ASC码值10
                        if (hexCharBuff[i] == 10)
                        {
                            hexBuffer.append(new String(hexCharBuff,0,i));
//                            Log.d(TAG, "hexBuffer is " + hexBuffer + " , length is "
//                                    +hexBuffer.toString().length() );
                            if (hexBuffer.toString().length() == 80)
                            {
                                notifyUI(Listener.REC500, hexBuffer.toString());
                            }
                            else if (hexBuffer.toString().length() == 120)
                            {
                                notifyUI(Listener.M5, hexBuffer.toString());
                            }
                            hexBuffer.delete(0, hexBuffer.length());
                            hexBuffer.append(new String(hexCharBuff,i+1,hexCharBuff.length-i-1));
                            bFlag = true;
                            break;
                        }
                    }

                    if (!bFlag)
                    {
                        hexBuffer.append(hexStr);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            close();
        }
    }

    /**
     * 发送短消息
     */
    public void sendMsg(String msg) {
        if (checkSend()) return;
        isSending = true;
        try {
            mOut.writeInt(FLAG_MSG); //消息标记
            mOut.writeUTF(msg);
            mOut.flush();
            notifyUI(Listener.MSG, "发送短消息：" + msg);
        } catch (Throwable e) {
            close();
        }
        isSending = false;
    }


    // ============================================通知UI===========================================================
    private boolean checkSend() {
        if (isSending) {
            APP.toast("正在发送其它数据,请稍后再发...", 0);
            return true;
        }
        return false;
    }

    /**
     * 释放监听引用(例如释放对Activity引用，避免内存泄漏)
     */
    public void unListener() {
        mListener = null;
    }

    /**
     * 关闭Socket连接
     */
    public void close() {
        try {
            isRead = false;
            if (mSocket != null)
            {
                mSocket.close();
            }
            notifyUI(Listener.DISCONNECTED, null);
        } catch (Throwable e) {
            Log.e(TAG,e.getMessage());
        }
    }

    /**
     * 当前设备与指定设备是否连接
     */
    public boolean isConnected(BluetoothDevice dev) {
        boolean connected = (mSocket != null && mSocket.isConnected());
        if (dev == null)
            return connected;
        return connected && mSocket.getRemoteDevice().equals(dev);
    }

    public boolean isConnected() {
        return (mSocket != null && mSocket.isConnected());
    }

    public BluetoothDevice getConnectDevice() {
        if (isConnected()) {
            return mSocket.getRemoteDevice();
        }

        return null;
    }


    private void notifyUI(final int state, final Object obj) {
        APP.runUi(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mListener != null)
                        mListener.socketNotify(state, obj);
                } catch (Throwable e) {
                   Log.e(TAG,e.getMessage());
                }
            }
        });
    }

    public interface Listener {
        int DISCONNECTED = 0;
        int CONNECTED = 1;
        int MSG = 2;
        int REC500 = 3;
        int M5 = 4;

        void socketNotify(int state, Object obj);
    }
}
