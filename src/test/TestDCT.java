package test;

public class TestDCT extends TestClass {
	private final static int BLOCKSIZE = 8;
	private static double[][] C;
	private static double[][][][] CC;
	/**
     */
	private static double[][] DiscreteCosinusTransformation2(double[][] matrix, boolean forward)
	{
		double[][] result = new double[BLOCKSIZE][BLOCKSIZE];
		for(int u = 0; u < BLOCKSIZE; ++u)
		{
			for(int v = 0; v < BLOCKSIZE; ++v)
			{		
				
				result[u][v] = 0;
				for(int x = 0; x < BLOCKSIZE; ++x)
				{
					for(int y = 0; y < BLOCKSIZE; ++y)
					{
						if(forward)
						{
							result[u][v] += CC[x][u][y][v] * matrix[x][y];
						}
						else
						{
							result[u][v] += CConst(x) * CConst(y)*CC[u][x][v][y] * matrix[x][y];
						}
					}
				}
				if(forward)
				{
					result[u][v]*= CConst(u)*CConst(v);
				}
				result[u][v] /= 4;				
			}
		}
		return result;
	}
	private static double CConst(double u)
	{
		double c = 1;
		if(u == 0)
		{
			
			c = 1.0/Math.sqrt(2.0);
		}
		return c;
	}
	private static double Coefficient(int i, int u)
	{
		double  c = 1;
		/*if(u == 0)
		{
			
			c = 1.0/Math.sqrt(2.0);
		}*/
		double result = c * Math.cos( ((2*i+1)*u*Math.PI )/2/BLOCKSIZE);
		return result;
	}
	private static void PreCalcCoefficients()
	{
		C = new double[BLOCKSIZE][BLOCKSIZE];
		for(int i=0;i<BLOCKSIZE;++i)
		{
			for(int j=0;j<BLOCKSIZE;++j)
			{
				C[i][j] = Coefficient(i,j);
			}
		}
		CC = new double[BLOCKSIZE][BLOCKSIZE][BLOCKSIZE][BLOCKSIZE];
		for(int i=0;i<BLOCKSIZE;++i)
		{
			for(int j=0;j<BLOCKSIZE;++j)
			{
				for(int k=0;k<BLOCKSIZE;++k)
				{
					for(int l = 0; l < BLOCKSIZE;++l)
					{
						CC[i][j][k][l] = C[i][j] * C[k][l];
					}
				}
			}
		}
	}
	public static void main(String[] args) {
		PreCalcCoefficients();
		double[][] testMatrix = {{251,118,-13,6,-2,6,-1,0},
								 {279,-68,-8,-7,-1,4,-4,-1},
								 {-51,-14,34,-14,5,0,-1,0},
								 {27,5,-10,8,-7,4,-5,1},
								 {-22,-7,14,-9,4,-2,1,1},
								 {-3,15,-18,15,-6,2,-1,2},
								 {7,-9,6,-6,4,0,0,2},
								 {3,7,-9,3,0,-2,-1,0}};
		double [][] source = { 
				{255,76,75,75,69,66,77,71},
				{73,74,	73,	74,	63,	64,	68,	69},
				{69,68,	71,	72,	67,	5,	48,	41},
				{59,55,	56,	52,	47,	40,	24,	9},
				{51,50, 45,	255,	0,	22,	7,	-5},
				{43,37,	32,	24,	15,	5,	-6,	-25},
				{29,21,	9,	-2,	-10,-21,-44,-69},
				{9,	-4,	-17,-35,-52,-61,-57,-35}};
		double[][] result = DiscreteCosinusTransformation2(source,true);
		for(int i=0;i< result.length;++i)
		{
			for(int j=0;j<result[i].length;++j)
			{
				System.out.print(Math.round(result[i][j])+" ");
			}
			System.out.println();
		}
		System.out.println();
		double[][] result2 = DiscreteCosinusTransformation2(result,false);
		for(int i=0;i< result2.length;++i)
		{
			for(int j=0;j<result2[i].length;++j)
			{
				System.out.print(Math.round(result2[i][j])+" ");
			}
			System.out.println();
		}

	}

}
