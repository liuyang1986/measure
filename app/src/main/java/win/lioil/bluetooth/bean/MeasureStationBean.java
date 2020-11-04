package win.lioil.bluetooth.bean;

import java.io.Serializable;


public class MeasureStationBean implements Serializable {

    //第1个前视点高程
    private String Rf1 = "";

    //第1个前视点视距
    private String Rf1HD = "";

    //第2个前视点高程
    private String Rf2 = "";

    //第2个前视点视距
    private String Rf2HD = "";

    //第1个后视点高程
    private String Rb1 = "";

    //第1个后视点视距
    private String Rb1HD = "";

    //第2个后视点高程
    private String Rb2 = "";

    //第2个后视点视距
    private String Rb2HD = "";

//    private String height = "";
//
//    private String diffHeight = "";

    //测量时间
    private String measureTime ="";

    //累积视距
    private String sumSightDistance = "";

    private int measureIndex = 0;

    //前后视距差1
    private String deltaHD1;

    //前后视距差2
    private String deltaHD2;

    //后视高程差
    private String deltaB;

    //前视高程差
    private String deltaF;

    //前后视高差1
    private String deltaR1;

    //前后是高差2
    private String deltaR2;

    //高差
    private double h = 0.0;

    //测端的距离
    private double l = 0.0;

    //起点测点名称
    private String startPName;

    //终点测点名称
    private String endPName;

    //当前测站对应的测点bean
//    private MeasurePointBean startMPB;
//
//    private MeasurePointBean endMPB;

    //测点类型
    private String measureType;

    //测点高程
    private String Rz;

    //测点视距
    private String HD;

    public String getRz() {
        return Rz;
    }

    public void setRz(String rz) {
        Rz = rz;
    }

    public String getHD() {
        return HD;
    }

    public void setHD(String HD) {
        this.HD = HD;
    }

    public String getMeasureType() {
        return measureType;
    }

    public void setMeasureType(String measureType) {
        this.measureType = measureType;
    }

    public String getRf1() {
        return Rf1;
    }

    public void setRf1(String rf1) {
        Rf1 = rf1;
    }

    public String getRf1HD() {
        return Rf1HD;
    }

    public void setRf1HD(String rf1HD) {
        Rf1HD = rf1HD;
    }

    public String getRf2() {
        return Rf2;
    }

    public void setRf2(String rf2) {
        Rf2 = rf2;
    }

    public String getRf2HD() {
        return Rf2HD;
    }

    public void setRf2HD(String rf2HD) {
        Rf2HD = rf2HD;
    }

    public String getRb1() {
        return Rb1;
    }

    public void setRb1(String rb1) {
        Rb1 = rb1;
    }

    public String getRb1HD() {
        return Rb1HD;
    }

    public void setRb1HD(String rb1HD) {
        Rb1HD = rb1HD;
    }

    public String getRb2() {
        return Rb2;
    }

    public void setRb2(String rb2) {
        Rb2 = rb2;
    }

    public String getRb2HD() {
        return Rb2HD;
    }

    public void setRb2HD(String rb2HD) {
        Rb2HD = rb2HD;
    }


    public int getMeasureIndex() {
        return measureIndex;
    }

    public void setMeasureIndex(int measureIndex) {
        this.measureIndex = measureIndex;
    }

//    public String getHeight() {
//        return height;
//    }
//
//    public void setHeight(String height) {
//        this.height = height;
//    }
//
//    public String getDiffHeight() {
//        return diffHeight;
//    }
//
//    public void setDiffHeight(String diffHeight) {
//        this.diffHeight = diffHeight;
//    }

    public String getMeasureTime() {
        return measureTime;
    }

    public void setMeasureTime(String measureTime) {
        this.measureTime = measureTime;
    }

    public String getSumSightDistance() {
        return sumSightDistance;
    }

    public void setSumSightDistance(String sumSightDistance) {
        this.sumSightDistance = sumSightDistance;
    }

    public double getH() {
        return h;
    }

    public void setH(double h) {
        this.h = h;
    }

    public double getL() {
        return l;
    }

    public void setL(double l) {
        this.l = l;
    }

    public String getStartPName() {
        return startPName;
    }

    public void setStartPName(String startPName) {
        this.startPName = startPName;
    }

    public String getEndPName() {
        return endPName;
    }

    public void setEndPName(String endPName) {
        this.endPName = endPName;
    }

    public String getDeltaHD1() {
        return deltaHD1;
    }

    public void setDeltaHD1(String deltaHD1) {
        this.deltaHD1 = deltaHD1;
    }

    public String getDeltaHD2() {
        return deltaHD2;
    }

    public void setDeltaHD2(String deltaHD2) {
        this.deltaHD2 = deltaHD2;
    }

    public String getDeltaB() {
        return deltaB;
    }

    public void setDeltaB(String deltaB) {
        this.deltaB = deltaB;
    }

    public String getDeltaF() {
        return deltaF;
    }

    public void setDeltaF(String deltaF) {
        this.deltaF = deltaF;
    }

    public String getDeltaR1() {
        return deltaR1;
    }

    public void setDeltaR1(String deltaR1) {
        this.deltaR1 = deltaR1;
    }

    public String getDeltaR2() {
        return deltaR2;
    }

    public void setDeltaR2(String deltaR2) {
        this.deltaR2 = deltaR2;
    }

    //    public MeasurePointBean getMeasurePointBean() {
//        return measurePointBean;
//    }
//
//    public void setMeasurePointBean(MeasurePointBean measurePointBean) {
//        this.measurePointBean = measurePointBean;
//    }

    //    public MeasurePointBean getBeginMPB() {
//        return beginMPB;
//    }
//
//    public void setBeginMPB(MeasurePointBean beginMPB) {
//        this.beginMPB = beginMPB;
//    }
//
//    public MeasurePointBean getEndMPB() {
//        return endMPB;
//    }
//
//    public void setEndMPB(MeasurePointBean endMPB) {
//        this.endMPB = endMPB;
//    }
}
