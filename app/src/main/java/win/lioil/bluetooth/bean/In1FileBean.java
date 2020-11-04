package win.lioil.bluetooth.bean;

public class In1FileBean {

    //起点名称
    private String startPtName;

    //终点名称
    private String endPtName;

    //测段高差
    private double diffH;

    //测段距离
    private double measureL;

    //测站数
    private int stationNum;

    //概略高程
    private double almostH;

    public String getStartPtName() {
        return startPtName;
    }

    public void setStartPtName(String startPtName) {
        this.startPtName = startPtName;
    }

    public String getEndPtName() {
        return endPtName;
    }

    public void setEndPtName(String endPtName) {
        this.endPtName = endPtName;
    }

    public double getDiffH() {
        return diffH;
    }

    public void setDiffH(double diffH) {
        this.diffH = diffH;
    }

    public double getMeasureL() {
        return measureL;
    }

    public void setMeasureL(double measureL) {
        this.measureL = measureL;
    }

    public int getStationNum() {
        return stationNum;
    }

    public void setStationNum(int stationNum) {
        this.stationNum = stationNum;
    }

    public double getAlmostH() {
        return almostH;
    }

    public void setAlmostH(double almostH) {
        this.almostH = almostH;
    }
}
