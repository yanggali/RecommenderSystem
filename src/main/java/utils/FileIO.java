package utils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YangJiali on 2017/3/20 0020.
 */
public class FileIO {
    //将矩阵写入文件中
    public static void matrixToFile(double[][] matrix,String fileName){
        FileWriter writer = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件,false表示覆盖的方式写入
            writer = new FileWriter(fileName, false);
            StringBuilder content = new StringBuilder();
            for (int i = 0 ;i < matrix.length;i++){
                for (int j = 0;j < matrix[0].length;j++){
                    content.append(matrix[i][j]+" ");
                }
                content.append("\n");
            }
            writer.write(content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public static List<String> readFileByLines(String fileName) {
        File file = new File(fileName);
        BufferedReader reader = null;
        List<String> filelist = new ArrayList<>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 0;  //记录行数
            while ((tempString = reader.readLine()) != null) {
                filelist.add(tempString);
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return filelist;
    }
    /**
     * 向文件中追加内容
     */
    public static void appendToFile(String fileName, String content) {
        try {
            //打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
            FileWriter writer = new FileWriter(fileName, true);
            writer.write(content);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param fileName
     * @param content
     */
    public static void writeToFile(String fileName, String content) {
        FileWriter writer = null;
        try {
            // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件,false表示覆盖的方式写入
            writer = new FileWriter(fileName, false);
            writer.write(content);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 向文件中写内容
     * @param fileName
     * @param array
     */
    public static void writeToFile(String fileName, char[][] array) {
        File file = new File(fileName);
        try {
            FileWriter fw = new FileWriter(file,true);
            for (int i = 0;i < array.length;i++)
            {
                for (int j = 0;j < array[0].length;j++)
                {
                    if (array[i][j]!='0')
                    {
                        fw.write(i+" "+j+" "+array[i][j]+"\r\n");
                    }
                }
            }
            fw.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * 读取某个文件夹下的所有文件
     */
    public static void readFileBydirs(String filepath)  {
        File file = new File(filepath);
        try {
            //该路径是文件
            if (file.isFile()) {
                System.out.println("文件:" + file.getName());
            }
            //该路径下是目录
            else if (file.isDirectory()) {
                System.out.println("文件夹:"+file.getName());
                String[] filelist = file.list();
                for (int i = 0; i < filelist.length; i++) {
                    String temppath = filepath + "\\" + filelist[i];
                    File readfile = new File(filepath + "\\" + filelist[i]);
                    readFileBydirs(temppath);
//                    if (!readfile.isDirectory()) {
//                        System.out.println("path=" + readfile.getPath());
//                        System.out.println("absolutepath="
//                                + readfile.getAbsolutePath());
//                        System.out.println("name=" + readfile.getName());
//
//                    } else if (readfile.isDirectory()) {
//                        readfile(filepath + "\\" + filelist[i]);
//                    }
                }
            }
        } finally {
        }
    }
}
