package win.lioil.bluetooth.util;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import win.lioil.bluetooth.bean.In1FileBean;

public class Ou1FileUtil {

    //n表示测段数目，t表示未知点数目
    public static  double[][] initAMatrix(List<In1FileBean> in1BeanList, int n, int t)
    {
        //A矩阵 n*t
        double[][] a = new double[n][t];
        ArrayList<String> tPointList = new ArrayList<>();
        for (int i=0; i<t; i++)
        {
            tPointList.add(in1BeanList.get(i).getEndPtName());
        }

        for (int i=0; i<n; i++) {
            In1FileBean bean = in1BeanList.get(i);
            String startPName = bean.getStartPtName();
            for (int j=0; j<t; j++) {
                a[i][j] = 0.0;
                String endPName = tPointList.get(j);
                if (j == i) {
                    a[i][j] = 1.0;
                }else if (TextUtils.equals(startPName,endPName))
                {
                    a[i][j] = -1.0;
                }

            }
        }
        return a;
    }


    //初始化P矩阵
    public static double[][] initPMatrix(List<In1FileBean> list)
    {
        int n = list.size();
        //P矩阵 n*n
        double[][] a = new double[n][n];
        for (int i = 0; i <a.length; i++) {
            for (int j = 0; j <a[i].length; j++) {
                a[i][j] = 0.0;
                if (j == i) {
                    a[i][j] = Arith.div(1.0, list.get(i).getStationNum(), 3);;
                }
            }
        }
        return a;
    }

    //初始化H矩阵
    public static double[][] initHMatrix(List<In1FileBean> list)
    {
        int n = list.size();
        //A矩阵 n*t
        double[][] H = new double[n][1];
        for (int i = 0; i <list.size(); i++) {
            H[i][0] = list.get(i).getDiffH();
        }
        return H;
    }



    //初始化X矩阵,t*1
    public static double[][] initXMatrix(List<In1FileBean> list,double startH,double endH)
    {
        int t = list.size()-1;
        double [][]X = new double[t][1];
        final double initXValue = 0.0;
        int scale = 4;
        for (int i=0; i<t; i++)
        {
            X[i][0] = initXValue;
        }

        double H = startH;
        for (int i = 0; i<t; i++) {
            In1FileBean bean1 = list.get(i);
            if (i == 0)
            {
                X[i][0] = Arith.add(startH,bean1.getDiffH());
                X[i][0] = Arith.round(X[i][0],scale);
                bean1.setAlmostH(X[i][0]);
//                bean1.setAlmostH(X[i][0]);
            }
            else if (i == t-1)
            {
                String startName = list.get(i+1).getStartPtName();
                int index = 0;
                double actualH = 0.0;
                for (int j=0; j<=i; j++)
                {
                    if (TextUtils.equals(startName,list.get(j).getEndPtName()))
                    {
                        X[j][0] = Arith.sub(endH,list.get(i+1).getDiffH());
                        actualH = X[j][0];
                        X[j][0] = Arith.round(X[j][0],scale);
                        bean1.setAlmostH(X[j][0]);
//                        bean1.setAlmostH(Arith.round(X[j][0],scale));
                        index = j;
                        break;
                    }
                }
                //以最后一个未测点为起点，重新计算l
                for (int j=index+1; j<=i; j++) {
                    if (TextUtils.equals(startName, list.get(j).getStartPtName())) {
                        double dH2 = list.get(j).getDiffH();
                        X[j][0] = Arith.add(actualH,dH2);
                        X[j][0] = Arith.round(X[j][0],scale);
                        list.get(j).setAlmostH(X[j][0]);

//                        list.get(j).setAlmostH(Arith.round(X[j][0],scale));
                    }
                }
                continue;
            }


            for (int j=i+1; j<t; j++)
            {
                In1FileBean bean2 = list.get(j);
                if (TextUtils.equals(bean1.getEndPtName(),bean2.getStartPtName()))
                {
                    X[j][0] = Arith.add(X[i][0],bean2.getDiffH());
                    X[j][0] = Arith.round(X[j][0],scale);
                    bean2.setAlmostH(X[j][0]);
//                    bean2.setAlmostH(Arith.round(X[j][0],scale));
                }

            }

        }
        return X;
    }

    //计算PVV
    public static double calcPVV(double[][]p,double[][]V)
    {
        double pvv = 0;
        for (int i=0; i<p.length; i++)
        {
            for (int j=0; j<p[0].length; j++)
            {
                if (i==j)
                {
                    double pValue = p[i][j];
                    double vValue = V[i][0];
                    pvv = Arith.add(pvv,pValue*vValue*vValue);
                    break;
                }
            }
        }

        return pvv;
    }

    //初始化L矩阵
    public static double[][] initLMatrix(List<In1FileBean> list , double [][]X, double startH , double endH)
    {
        int n = list.size();
        double [][]l = new double[n][1];

        double H = startH;
        for (int i = 0; i<n; i++) {
            In1FileBean bean1 = list.get(i);
            double dH = bean1.getDiffH();
            double almostH = bean1.getAlmostH();
            if (i == 0)
            {
                l[i][0] = Arith.sub(almostH,Arith.add(dH,startH));
                l[i][0] = Arith.round(l[i][0],5);
            }
            else if (i == n-1)
            {
                String startName = bean1.getStartPtName();
                int index = 0;
                for (int j=0; j<n; j++)
                {
                    if (TextUtils.equals(startName,list.get(j).getEndPtName()))
                    {
                        int startIndex = getEndPtNameIndex(list,list.get(j).getStartPtName());
                        almostH = Arith.add(list.get(startIndex).getAlmostH(),list.get(j).getDiffH());
                        l[j][0] = Arith.sub(X[j][0],almostH);
                        l[j][0] = Arith.round(l[j][0],5);
                        index = j;
                        break;
                    }
                }


                //以最后一个未测点为起点，重新计算l
                for (int j=index+1; j<=i; j++) {
                    if (TextUtils.equals(startName, list.get(j).getStartPtName())) {
                        double dH2 = list.get(j).getDiffH();
                        double almostH2 = list.get(j).getAlmostH();

                        if (j == i)
                        {
                            almostH2 = Arith.round(endH,4);
                        }

                        l[j][0] = Arith.sub(almostH2,Arith.add(X[index][0],dH2));
                        l[j][0] = Arith.round(l[j][0], 5);
                    }
                }
                continue;
            }


            for (int j=i+1; j<n-1; j++)
            {
                In1FileBean bean2 = list.get(j);
                double dH2 = bean2.getDiffH();
                double almostH2 = bean2.getAlmostH();
                if (TextUtils.equals(bean1.getEndPtName(),bean2.getStartPtName()))
                {
//                    l[j][0] = almostH2 - dH2 - almostH;
                    l[j][0] = Arith.sub(almostH2,Arith.add(dH2,almostH));
                    l[j][0] = Arith.round(l[j][0],5);
                }

            }

        }

        return l;
    }

    //计算高差中误差
    public static  double[] calcHeightDiffMidVk(List<In1FileBean> in1BeanList,double u,double [][]Qx)
    {
        ArrayList<String> tPointList = new ArrayList<>();
        for (int i=0; i<in1BeanList.size(); i++) {
            tPointList.add(in1BeanList.get(i).getEndPtName());
        }

        double [] Vk = new double[Qx.length+1];
        for (int i=0; i<=Qx.length; i++)
        {
            if (i == Qx.length){
                // 说明是最后一个点
//                double qjj = Qx[Qx.length - 1][Qx.length - 1];
//                Vk[i] = Arith.roundMinus(Arith.mul(u,Math.sqrt(qjj)),2);
                int index = getEndPtNameIndex(in1BeanList,in1BeanList.get(i).getStartPtName());
                double qii =  Arith.round(Qx[index][index],1);
                Vk[i] = u*Math.sqrt(qii);
                Vk[i] = Arith.roundMinus(Vk[i],2);

                for (int j=index+1; j<i; j++)
                {
                    double qij = Arith.round(Qx[index][j],1);
                    double qjj = Arith.round(Qx[j][j],1);
                    double sqrtV = Arith.sub(Arith.add(qii,qjj),Arith.add(qij,qij));
                    Vk[j] = Arith.mul(u,Math.sqrt(sqrtV));
                    Vk[j] = Arith.roundMinus(Vk[j],2);
                }

            } else {
                for (int j = 0 ; j <= Qx[i].length ; j ++){
                    if (i == j){
                        if (i == 0){
                            // 说明是第一个点qii 和qjj都是0
                            double qjj = Arith.round(Qx[j][j],1);
                            Vk[i] = Arith.mul(u,Math.sqrt(qjj));
                            Vk[i] = Arith.roundMinus(Vk[i],2);
                        } else {
                            int index = getEndPtNameIndex(in1BeanList,in1BeanList.get(j).getStartPtName());
                            double qii = Arith.round(Qx[index][index],1);
                            double qij = Arith.round(Qx[index][j],1);
                            double qjj = Arith.round(Qx[j][j],1);
                            double sqrtV = Arith.sub(Arith.add(qii,qjj),Arith.add(qij,qij));
                            Vk[i] = Arith.mul(u,Math.sqrt(sqrtV));
                            Vk[i] = Arith.roundMinus(Vk[i],2);
                        }
                    }
                }
            }
        }
        return Vk;
    }

    private static int getEndPtNameIndex(List<In1FileBean> in1BeanList,String startName)
    {
        int index = 0;
        for(int i=0; i<in1BeanList.size(); i++)
        {
            if (TextUtils.equals(in1BeanList.get(i).getEndPtName(),startName))
            {
                index = i;
                break;
            }
        }

        return index;
    }

    //计算高程中误差
    public static  double[] calcHeightMidVk(double u,double [][]Qx)
    {
        double [] Vk = new double[Qx.length];
        for (int i=0; i < Qx.length; i++)
        {
            Vk[i] = Arith.mul(u,Math.sqrt(Arith.round(Qx[i][i],1)));
            Vk[i] = Arith.roundMinus(Vk[i],2);
        }

        return Vk;
    }
}
