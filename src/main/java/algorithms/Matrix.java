package algorithms;

import utils.FileIO;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Yangjiali on 2017/4/19 0019.
 * Version 1.0
 */
public class Matrix {
    public static Map<String, Integer> user = new HashMap<>();
    public static Map<String, Integer> tag = new HashMap<>();
    public static Map<String, Integer> item = new HashMap<>();

    public static void initialMap() {
        String userpath = System.getProperty("user.dir") + "/data/matrix/movielens/user.dat";
        String tagpath = System.getProperty("user.dir") + "/data/matrix/movielens/movie.dat";
        String moviepath = System.getProperty("user.dir") + "/data/matrix/movielens/movie.dat";
        //初始化所有用户
        readToMap(userpath, user,1);
        //初始化电影
        readToMap(moviepath, item,user.size()+1);
        //初始化标签
        readToMap(tagpath, tag,user.size()+item.size()+1);

    }

    public static void readToMap(String filepath, Map<String, Integer> map,int index) {
        List<String> list = FileIO.readFileByLines(filepath);
        for (String str : list) {
            map.put(str, index);
            index++;
        }
        index--;
        System.out.println(index);
    }

    public static void readToMatrix(String triples) throws IOException{
        int length = user.size() + item.size() + tag.size();
        System.out.println(length);
        //char[][] matrix = new char[length][length];
        List<String> list = FileIO.readFileByLines(triples);
        File file = new File("data/matrix/movielens/smallmatrix.dat");
        FileWriter fw = new FileWriter(file,true);
        for (String str : list) {
            String[] triple = str.split("::");
            int userid = user.get(triple[0]);
            int itemid = item.get(triple[1]);
            int tagid = tag.get(triple[2]);
            int a = userid + itemid,b=userid + itemid + tagid;
            //1System.out.println(a+" "+b);
            fw.write(userid+" "+itemid+" "+1+"\r\n");
            fw.write(userid+" "+tagid+" "+1+"\r\n");
            fw.write(itemid+" "+tagid+" "+1+"\r\n");
            fw.write(itemid+" "+userid+" "+1+"\r\n");
            fw.write(tagid+" "+userid+" "+1+"\r\n");
            fw.write(tagid+" "+itemid+" "+1+"\r\n");
//            matrix[userid - 1][userid + itemid - 1] = 1;
//            matrix[userid - 1][userid + itemid + tagid - 1] = 1;
//            matrix[itemid - 1][userid + itemid + tagid - 1] = 1;
//            matrix[userid + itemid - 1][userid - 1] = 1;
//            matrix[userid + itemid + tagid - 1][userid - 1] = 1;
//            matrix[userid + itemid + tagid - 1][itemid - 1] = 1;
        }
        fw.close();
//        String file = System.getProperty("user.dir") + "/matrix/movielens/matrix.dat";
        //FileIO.writeToFile("matrix.dat", matrix);
    }
}
