package test;
import algos.*;
import java.util.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.ImageIO;
public class TestClass {

	/**
     */
	private static byte GetNumberOfBits(int n)
	{

		if(n==0) return 0;
		byte carry = 0;
		if(n<0) 
			{
				n = -n;
				carry = 1;
			}
		byte result =  (byte)( Math.floor(Math.log(1.0*n)/ Math.log(2.0)) + 1);
		return (byte) (result + carry);
		/*int upper = 1<<12;
		byte result = 12;
		
		for(int m = upper; (m >0) &&  n  < m; m >>>= 1, --result );
		result++;
		return result;*/
	}
	private static int GetCode(int n)
	{		
		if(n<0)
		{
			n = -n;
			return n;
		}
		return n;
	}
	private static int GetNumber(int code,byte bitsCount)
	{
		
		int bitsCountCalculated = GetNumberOfBits(code);
		if(bitsCountCalculated < bitsCount)
		{			
			return -code;
		}
		return code;		
		
	}
	public static void main(String[] args) {
		
		int n = 4095;
		int bits = GetNumberOfBits(n);
		int code = GetCode(n);
		int codeLen = GetNumberOfBits(code);
		int r = GetNumber(code,(byte)bits);
		System.out.println(n + " " + bits + " " + code + " " + codeLen + " " + r);
	}

}
