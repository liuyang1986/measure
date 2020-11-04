package win.lioil.bluetooth.util;

public class MatrixOperation {
    /**
     * 自动填充矩阵，将不足的行后自动补0
     *
     * @param M
     * @return
     */
    public static int[][] fillMatrix(int[][] M) {
        int ml = 0;// 最长行
        for (int i = 0; i < M.length; i++) {
            ml = ml < M[i].length ? M[i].length : ml;
        }
        int Nm[][] = new int[M.length][ml];
        for (int i = 0; i < M.length; i++) {
            for (int j = 0; j < M[i].length; j++) {
                Nm[i][j] = M[i][j];
            }
        }
        return Nm;
    }

    /**
     * 矩阵乘法A*B
     *
     * @param A
     * @param B
     * @return
     * @throws Exception
     */
    public static int[][] multiplication(int[][] A, int[][] B) throws Exception {
        // 先判断A矩阵的列是否等于B矩阵的行，A矩阵与B矩阵可以进行乘法运算的条件就是A的列数等于B的行数
        A = fillMatrix(A);
        B = fillMatrix(B);
        if (A[0].length != B.length) {
            throw new Exception("矩阵A的列不等于矩阵B的行！");
        }
        int C[][] = new int[A.length][B[0].length];
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < B[i].length; j++) {
                for (int k = 0; k < A[i].length; k++) {
                    C[i][j] += A[i][k] * B[k][j];
                }
            }
        }
        return C;
    }

    /**
     * 矩阵转置
     *
     * @param A
     * @return
     */
    public static int[][] transposed(int[][] A) {
        A = fillMatrix(A);
        int[][] AT = new int[A[0].length][A.length];
        for (int i = 0; i < AT.length; i++) {
            for (int j = 0; j < AT[0].length; j++) {
                AT[i][j] = A[j][i];
            }
        }
        return AT;
    }

    //矩阵减法 C=A-B
    public static double[][] MatrixMinus(double[][] m1,double[][] m2){
        if(m1==null||m2==null||
                m1.length!=m2.length||
                m1[0].length!=m2[0].length) {
            return null;
        }

        double[][] m = new double[m1.length][m1[0].length];

        for(int i=0;i<m.length;++i){
            for (int j=0;j<m[i].length;++j){
                m[i][j]=m1[i][j]-m2[i][j];
            }
        }

        return m;
    }

    //矩阵加法 C=A+B
    public static double[][] MatrixAdd(double[][] m1,double[][] m2){
        if(m1==null||m2==null||
                m1.length!=m2.length||
                m1[0].length!=m2[0].length) {
            return null;
        }

        double[][] m = new double[m1.length][m1[0].length];

        for(int i=0;i<m.length;++i){
            for (int j=0;j<m[i].length;++j){
                m[i][j]=m1[i][j]+m2[i][j];
            }
        }

        return m;
    }

    //矩阵转置
    public static double[][] MatrixTranspose(double[][] m){
        if (m==null) return null;
        double[][] mt=new double[m[0].length][m.length];
        for(int i=0;i<m.length;++i){
            for (int j=0;j<m[i].length;++j){
                mt[j][i]=m[i][j];
            }
        }
        return mt;
    }

    //矩阵转置
    public static double[][] MatrixTimes(double[][] m,double times){
        if (m==null) return null;
        double[][] mt=new double[m.length][m[0].length];
        for(int i=0;i<m.length;++i){
            for (int j=0;j<m[i].length;++j){
                mt[i][j]=Arith.mul(m[i][j],times);
                mt[i][j]=Arith.roundMinus(mt[i][j],2);
            }
        }
        return mt;
    }

    //矩阵相乘 C=A*B
    public static double[][] MatrixMultiply(double[][] m1,double[][] m2){
        if(m1==null||m2==null||m1[0].length!=m2.length)
            return null;

        double[][] m=new double[m1.length][m2[0].length];
        for(int i=0;i<m1.length;++i){
            for(int j=0;j<m2[0].length;++j){
                for (int k=0;k<m1[i].length;++k){
                    m[i][j]+=m1[i][k]*m2[k][j];
                }
            }
        }

        return m;
    }

    //求矩阵行列式（需为方阵）
    public static double MatrixDet(double[][] m){
        if (m==null||m.length!=m[0].length)
            return 0;

        if (m.length==1)
            return m[0][0];
        else if (m.length==2)
            return Matrix2Det(m);
        else if (m.length==3)
            return Matrix3Det(m);
        else {
            int re=0;
            for (int i=0;i<m.length;++i){
                re+=(((i+1)%2)*2-1)*MatrixDet(CompanionMatrix(m,i,0))*m[i][0];
            }
            return re;
        }
    }



    //求二阶行列式
    public static double Matrix2Det(double[][] m){
        if (m==null||m.length!=2||m[0].length!=2)
            return 0;

        return m[0][0]*m[1][1]-m[1][0]*m[0][1];
    }

    //求三阶行列式
    public static double Matrix3Det(double[][] m){
        if (m==null||m.length!=3||m[0].length!=3)
            return 0;

        double re=0;
        for (int i=0;i<3;++i){
            int temp1=1;
            for(int j=0,k=i;j<3;++j,++k){
                temp1*=m[j][k%3];
            }
            re+=temp1;
            temp1=1;
            for(int j=0,k=i;j<3;++j,--k){
                if (k<0) k+=3;
                temp1*=m[j][k];
            }
            re-=temp1;
        }

        return re;
    }


    //方阵转换为一维数组
    public static double[] MatrixConvertTo1DArray(double[][] m1){
        if(m1==null) {
            return null;
        }

        double[] m = new double[m1.length];
        //方阵转换
        if ((m1.length>1 && m1[0].length>1)
            && (m1.length==m1[0].length))
        {
            for(int i=0;i<m1.length;++i){
                for (int j=0;j<m1[i].length;++j){
                    if (i==j)
                    {
                        m[i] = m1[i][j];
                        break;
                    }
                }
            }
        }
        else if (m1[0].length==1 && m1.length>1)
        {
            for(int i=0;i<m1.length;++i){
                m[i] = m1[i][0];
            }
        }
        else
        {
            return null;
        }

        return m;
    }

    //求矩阵的逆（需方阵）
    public static double[][] MatrixInv(double[][] m){
        if (m==null||m.length!=m[0].length)
            return null;

        double A=MatrixDet(m);
        double[][] mi=new double[m.length][m[0].length];
        for(int i=0;i<m.length;++i){
            for (int j=0;j<m[i].length;++j){
                double[][] temp=CompanionMatrix(m,i,j);
                mi[j][i]=(((i+j+1)%2)*2-1)*MatrixDet(temp)/A;
            }
        }

        return mi;
    }

    //求方阵代数余子式
    public static double[][] CompanionMatrix(double[][] m,int x,int y){
        if (m==null||m.length<=x||m[0].length<=y||
                m.length==1||m[0].length==1)
            return null;

        double[][] cm=new double[m.length-1][m[0].length-1];

        int dx=0;
        for(int i=0;i<m.length;++i){
            if(i!=x){
                int dy=0;
                for (int j=0;j<m[i].length;++j){
                    if (j!=y){
                        cm[dx][dy++]=m[i][j];
                    }
                }
                ++dx;
            }
        }
        return cm;
    }
}
