package test;


import static org.junit.Assert.*;
import java.util.*;
import org.junit.Before;
import org.junit.Test;
import algos.LZW;

public class LzwTests {

    private LZW lzw;
    private int numberOfTestStrings = 100;
    private int[] testStringLength = new int[numberOfTestStrings];
    private Random random = new Random(System.currentTimeMillis());
    @Before
    public void setUp(){
        lzw = new LZW();
    }
    @Test
    public void SingleZero(){

    }

    public static void main(String[] args) {

        LZW lzw = new LZW();
    }


}
