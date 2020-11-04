package win.lioil.bluetooth.util;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import win.lioil.bluetooth.bean.BDBean;
import win.lioil.bluetooth.bean.GDBean;
import win.lioil.bluetooth.bean.WorkPointBean;
import win.lioil.bluetooth.bean.XMBean;
import win.lioil.bluetooth.config.Config;

public class Util {
    private static final String TAG =  "Test Util";
    public static final Executor EXECUTOR = Executors.newCachedThreadPool();
    private static final String Cache_Projects = "cache_project_";
    private static final String Cache_Sections = "cache_section_";
    private static final String Cache_WorkPoints = "cache_workpoint_";

    private static final String SDCARD_FILE_PATH = "/sdcard/bluetooth_measure/";

    private static List<XMBean> projectBeanList = new ArrayList<>();

    private static List<BDBean> sectionBeanList = new ArrayList<>();

    private static List<GDBean> workPointBeanList = new ArrayList<>();

    private static List<WorkPointBean> allWorkPointBeanList = new ArrayList<>();



    public static void mkdirs(String filePath) {
        boolean mk = new File(filePath).mkdirs();
        Log.d(TAG, "mkdirs: " + mk);
    }

    public static int dip2px(Context context, float dpValue) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dpValue, context.getResources().getDisplayMetrics());
    }

    public static void setAppCacheProjectListData(List<XMBean> list)
    {
        synchronized (projectBeanList)
        {
            if (!projectBeanList.isEmpty())
            {
                projectBeanList.clear();
            }
            projectBeanList.addAll(list);
        }
    }

    public static List<XMBean>  getAppCacheProjectListData()
    {
        synchronized (projectBeanList)
        {
            return projectBeanList;
        }

    }


    public static void setAppCacheSectionListData(List<BDBean> list)
    {
        synchronized(sectionBeanList)
        {
            if (!sectionBeanList.isEmpty())
            {
                sectionBeanList.clear();
            }

            sectionBeanList.addAll(list);
        }
    }

    public static List<BDBean>  getAppCacheSectionListData()
    {
        synchronized (sectionBeanList)
        {
            return sectionBeanList;
        }
    }

    public static void setAppCacheWorkPointListData(List<GDBean> list)
    {
        synchronized (workPointBeanList)
        {
            if (!workPointBeanList.isEmpty())
            {
                workPointBeanList.clear();
            }

            workPointBeanList.addAll(list);
        }
    }

    public static List<GDBean>  getAppCacheWorkPointListData()
    {
        synchronized (workPointBeanList)
        {
            return workPointBeanList;
        }
    }

    public static void setAllWorkPointListData(List<WorkPointBean> list)
    {
        synchronized (allWorkPointBeanList)
        {
            if (!allWorkPointBeanList.isEmpty())
            {
                allWorkPointBeanList.clear();
            }

            allWorkPointBeanList.addAll(list);
        }
    }

    public static List<WorkPointBean>  getAllWorkPointListData()
    {
        synchronized (allWorkPointBeanList)
        {
            return allWorkPointBeanList;
        }
    }

    public static <T> void setData(Context context, List<T> list, int type, String tag)
    {
        File file = context.getCacheDir();
        File Cache = null;
        String name;
        if(type == Config.XM_TYPE){
            name = Cache_Projects + tag;
            Cache = new File(file,name);
        }else if(type == Config.BD_TYPE){
            name = Cache_Sections + tag;
            Cache = new File(file,name);
        }else if (type == Config.GD_TYPE){
            name = Cache_WorkPoints + tag;
            Cache = new File(file,name);
        }
        else
        {
            return ;
        }

        if(Cache.exists()){
            Cache.delete();
        }
        try {
            ObjectOutputStream outputStream =
                    new ObjectOutputStream(new FileOutputStream(Cache));
            outputStream.writeObject(list);
        } catch (IOException e) {
            Log.e(TAG,e.getMessage());
        }
    }

    public static <T> List<T> getData(Context context,String tag, int type) throws IllegalAccessException, InstantiationException {
        File file = context.getCacheDir();
        String name;
        File cache;
        List<T> list = null;
        if(type==Config.XM_TYPE){
            name = Cache_Projects + tag;
            cache = new File(file,name);
            if(!cache.exists()){
                return null;
            }
            try {
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(cache));
                list = (List<T>) inputStream.readObject();
                return list;
            } catch (Exception e) {
                Log.e(TAG,e.getMessage());
            }
        }else if(type==Config.BD_TYPE){
            name = Cache_Sections + tag;
            cache = new File(file,name);
            if(!cache.exists()){
                return null;
            }
            try {
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(cache));
                list = (List<T>) inputStream.readObject();
                return list;
            } catch (Exception e) {
                Log.e(TAG,e.getMessage());
            }
        }else if(type==Config.GD_TYPE){
            name = Cache_WorkPoints + tag;
            cache = new File(file,name);
            if(!cache.exists()){
                return null;
            }
            try {
                ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(cache));
                list = (List<T>) inputStream.readObject();
                return list;
            } catch (Exception e) {
                Log.e(TAG,e.getMessage());
            }
        }
        return null;
    }


    public static String createNewDatFile(String fileName)
    {
        long currentTime = System.currentTimeMillis();
        Date date = new Date(currentTime);
        String filePath = SDCARD_FILE_PATH + File.separator + new SimpleDateFormat("yyyy").
                format(date) + File.separator + new SimpleDateFormat("MM").format(date) +
                File.separator + new SimpleDateFormat("dd").format(date);
        makeFilePath(filePath, fileName);

        File file = new File(filePath + File.separator + fileName);
        if (file.exists())
        {
            file.delete();
        }
        try {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }catch (Exception e)
        {
            Log.e(TAG,e.getMessage());
        }

        return filePath + File.separator + fileName;
    }

    // 将字符串写入到文本文件中
    public static void writeTxtToSDPathFile(String strcontent) {
        // 每次写入时，都换行写
        long currentTime = System.currentTimeMillis();
        Date date = new Date(currentTime);
        String filePath = SDCARD_FILE_PATH + File.separator + new SimpleDateFormat("yyyy").
                format(date) + File.separator + new SimpleDateFormat("MM").format(date) +
                File.separator + new SimpleDateFormat("dd").format(date);
        String fileName = new SimpleDateFormat("yyyyMMdd").format(date) + ".log";
        makeFilePath(filePath, fileName);

        fileName = filePath + File.separator + fileName;

        String strContent = strcontent;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                Log.d(TAG, "Create the file:" + fileName);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e(TAG, "Error on write File:" + e);
        }
    }

    // 将字符串写入到文本文件中
    public static void writeTxtToSDPathFile(String fileName,String strcontent) {
        // 每次写入时，都换行写
        String strContent = strcontent;
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                Log.d(TAG, "Create the file:" + fileName);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e(TAG, "Error on write File:" + e);
        }
    }


    public static String getMeasureFileDir()
    {
        long currentTime = System.currentTimeMillis();
        Date date = new Date(currentTime);
        String filePath = SDCARD_FILE_PATH + File.separator + new SimpleDateFormat("yyyy").
                format(date) + File.separator + new SimpleDateFormat("MM").format(date) +
                File.separator + new SimpleDateFormat("dd").format(date);
        return filePath;
    }

    // 将字符串写入到文本文件中
    public static void writeMeasureDataToSDPathFile(String strcontent,String fileName) {
//        long currentTime = System.currentTimeMillis();
//        Date date = new Date(currentTime);
//        String filePath = SDCARD_FILE_PATH + File.separator + new SimpleDateFormat("yyyy").
//                format(date) + File.separator + new SimpleDateFormat("MM").format(date) +
//                File.separator + new SimpleDateFormat("dd").format(date);
//        makeFilePath(filePath, fileName);
//
//        String strFilePath = filePath + File.separator + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(fileName);
            if (!file.exists()) {
                Log.d(TAG, "Create the file:" + fileName);
                file.getParentFile().mkdirs();
                file.createNewFile();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e(TAG, "Error on write File:" + e);
        }
    }

    public static File makeFilePath(String fileName)
    {
        return makeFilePath(SDCARD_FILE_PATH,fileName);
    }

    //生成文件
    public static File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + File.separator + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    //生成文件夹
    private static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdirs();
            }
        } catch (Exception e) {
            Log.e(TAG, " error " + e.getMessage());
        }
    }

    public static boolean isFileExist(String fileName)
    {
        File file = null;
        makeRootDirectory(SDCARD_FILE_PATH);
        try {
            file = new File(SDCARD_FILE_PATH + fileName);
            return file.exists();
        } catch (Exception e) {
            Log.e(TAG, " error " + e.getMessage());
        }
        return false;
    }

    /**
     * 保存对象
     *
     * @param ser
     * @param keyName 文件名
     * @throws IOException
     */
    public static boolean saveSerialzableObject(Context context, String keyName, Serializable ser) {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = context.openFileOutput(keyName, Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(ser);
            oos.flush();
            return true;
        } catch (Exception e) {
            Log.e(TAG, " error " + e.getMessage());
            return false;
        } finally {
            try {
                oos.close();
            } catch (Exception e) {
                Log.e(TAG, " error " + e.getMessage());
            }
            try {
                fos.close();
            } catch (Exception e) {
                Log.e(TAG, " error " + e.getMessage());
            }
        }
    }

    /**
     * 读取对象
     *
     * @param keyName 文件名
     * @return
     * @throws IOException
     */
    public static Serializable readObject(Context context, String keyName) {
        if (!isExistDataCache(context, keyName))
            return null;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = context.openFileInput(keyName);
            ois = new ObjectInputStream(fis);
            return (Serializable) ois.readObject();
        } catch (FileNotFoundException e) {
        } catch (Exception e) {
            e.printStackTrace();
            // 反序列化失败 - 删除缓存文件
            if (e instanceof InvalidClassException) {
                File data = context.getFileStreamPath(keyName);
                data.delete();
            }
        } finally {
            try {
                ois.close();
            } catch (Exception e) {
                Log.e(TAG, " error " + e.getMessage());
            }
            try {
                fis.close();
            } catch (Exception e) {
                Log.e(TAG, " error " + e.getMessage());
            }
        }
        return null;
    }

    /**
     * 判断缓存是否存在
     *
     * @param keyName 文件名
     * @return
     */
    public static boolean isExistDataCache(Context context, String keyName) {
        if (context == null)
            return false;
        boolean exist = false;
        File data = context.getFileStreamPath(keyName);
        if (data.exists())
            exist = true;
        return exist;
    }

    /**
     * 删除缓存文件
     *
     * @param context
     * @param fileName
     */
    public static void delCacheFile(Context context, String fileName) {
        File data = context.getFileStreamPath(fileName);
        data.delete();
    }
}
