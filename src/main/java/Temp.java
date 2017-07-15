import utils.FileIO;

import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Created by Yangjiali on 2017/3/28 0028.
 * Version 1.0
 */
public class Temp {
    public static void main(String[] args) throws IOException{

        String matrixpath = System.getProperty("user.dir")+"/data/matrixpath.dat";
        StringBuffer str = new StringBuffer();
        int times = 0;
        Random rd = new Random();
        str.append("100000\n");
        while (times < 140000){
            int n1 = rd.nextInt(10000);
            int n2 = rd.nextInt(10000);
            if(n1 > n2){
                str.append(n2+" "+n1+" 1\n");
            }
            else {
                str.append(n1+" "+n2+" 1\n");
            }
            times++;
        }
        FileIO.appendToFile(matrixpath,str.toString());
    }
    public static void writeToFile(String in,String out){

    }
    public static void normFile(String filepath,String newFile){
        List<String> list = FileIO.readFileByLines(filepath);
        StringBuffer str = new StringBuffer();
        for (String s : list) {
            String[] record = s.split("\t");
            for (String s1 : record[2].split(" ")) {
                str.append(record[0]+"\t"+record[1]+"\t"+s1+"\n");
            }
        }
        FileIO.appendToFile(newFile,str.toString());
    }
}
