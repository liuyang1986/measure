package win.lioil.bluetooth.bean;

import java.io.Serializable;

public class MeasureInfoBean implements Serializable {
    //水准线路id
    private String levelingLineId;
    //测量记录名称
    private String measureName;
    //仪器品牌
    private String instrumentBrand;
    //仪器型号
    private String instrumentModel;
    //温度
    private String temperature;
    //气压
    private String pressure;
    //天气
    private String weather;
    //观测类型
    private String measureType;
    //司镜人员姓名
    private String operaterName;
    //司镜人员身份证号码
    private String operaterIDCard;
    //设备序列号
    private String serialNo;
    //工作基点名称序列
    private String workPointNo;
    //水准线路编码
    private String levelingNo;

    public String getLevelingLineId() {
        return levelingLineId;
    }

    public void setLevelingLineId(String levelingLineId) {
        this.levelingLineId = levelingLineId;
    }

    public String getMeasureName() {
        return measureName;
    }

    public void setMeasureName(String measureName) {
        this.measureName = measureName;
    }

    public String getInstrumentBrand() {
        return instrumentBrand;
    }

    public void setInstrumentBrand(String instrumentBrand) {
        this.instrumentBrand = instrumentBrand;
    }

    public String getInstrumentModel() {
        return instrumentModel;
    }

    public void setInstrumentModel(String instrumentModel) {
        this.instrumentModel = instrumentModel;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getPressure() {
        return pressure;
    }

    public void setPressure(String pressure) {
        this.pressure = pressure;
    }

    public String getWeather() {
        return weather;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public String getMeasureType() {
        return measureType;
    }

    public void setMeasureType(String measureType) {
        this.measureType = measureType;
    }

    public String getOperaterName() {
        return operaterName;
    }

    public void setOperaterName(String operaterName) {
        this.operaterName = operaterName;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getWorkPointNo() {
        return workPointNo;
    }

    public void setWorkPointNo(String workPointNo) {
        this.workPointNo = workPointNo;
    }

    public String getLevelingNo() {
        return levelingNo;
    }

    public void setLevelingNo(String levelingNo) {
        this.levelingNo = levelingNo;
    }

    public String getOperaterIDCard() {
        return operaterIDCard;
    }

    public void setOperaterIDCard(String operaterIDCard) {
        this.operaterIDCard = operaterIDCard;
    }
}
