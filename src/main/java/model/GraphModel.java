//package Model;
//import java.util.ArrayList;
//import java.util.HashMap;
///**
// * Created by yangq0a on 16/9/1.
// */
//public class GraphModel {
//    //邻接矩阵类
//    static final int maxWeight=-1; //如果两个结点之间没有边，权值为-1；
//    ArrayList<String> vertices = new ArrayList<String>();//存放结点的集合
//    int[][] edges; //邻接矩阵的二维数组
//    int numOfEdges; //边的数量
//
//    public GraphModel(int n)
//    {
//        edges = new int[n][n];
//        for(int i=0;i<n;i++)
//        {
//            for(int j=0;j<n;j++)
//            {
//                if(i==j) //对角线上的元素为0
//                {
//                    edges[i][j]=0;
//                }
//                else
//                {
//                    edges[i][j]=maxWeight;
//                }
//            }
//        }
//        numOfEdges = 0;
//    }
//
//    public int[][] getEdges() {
//        return edges;
//    }
//    public void setEdges(int[][] edges) {
//        this.edges = edges;
//    }
//    //返回边的数量
//    public int getNumOfEdges()
//    {
//        return this.numOfEdges;
//    }
//    //返回结点的数量
//    public int getNumOfVertice()
//    {
//        return this.vertices.size();
//    }
//    //返回所有的节点值
//    public ArrayList<String> getValueOfVertice()
//    {
//        return this.vertices;
//    }
//    //返回结点的值
//    public String getValueOfVertice(int index)
//    {
//        return this.vertices.get(index);
//    }
//    //获得某条边的权值
//    public int getWeightOfEdges(int v1,int v2) throws Exception
//    {
//        if((v1 < 0 || v1 >= vertices.size())||(v2 < 0||v2 >= vertices.size()))
//        {
//            throw new Exception("v1或者v2参数越界错误！");
//        }
//        return this.edges[v1][v2];
//
//    }
//    //获取每一列的值
//    public ArrayList<Integer> getSumOfCol(){
//        ArrayList<Integer> sumList = new ArrayList<>();
//        for(int i=0; i<edges.length; i++){
//            int sum=0;
//            for(int j=0; j<edges[0].length; j++){
//                if(edges[j][i]==1){
//                    sum+=edges[j][i];
//                }
//            }
//            sumList.add(sum);
//        }
//        return sumList;
//    }
//    //获得所有边的权值之和
//    public int getWeightOfAllEdges() throws Exception
//    {
//        int weightSum = 0;
//        for(int i=0;i<vertices.size();i++)
//            for(int j=0;j<vertices.size();j++){
//                if(this.edges[i][j]!=-1){
//                    weightSum+=this.edges[i][j];
//                }
//            }
//        return weightSum;
//    }
//    //插入结点
//    public void insertVertice(String obj)
//    {
//        this.vertices.add(obj);
//    }
//
//    //插入带权值的边
//    public void insertEdges(int v1,int v2,int weight) throws Exception
//    {
//        if((v1 < 0 || v1 >= vertices.size())||(v2 < 0||v2 >= vertices.size()))
//        {
//            throw new Exception("v1或者v2参数越界错误！");
//        }
//
//        this.edges[v1][v2]=weight;
//        this.numOfEdges++;
//    }
//
//    //删除某条边
//    public void deleteEdges(int v1,int v2) throws Exception
//    {
//        if((v1 < 0 || v1 >= vertices.size())||(v2 < 0||v2 >= vertices.size()))
//        {
//            throw new Exception("v1或者v2参数越界错误！");
//        }
//        if( v1==v2 || this.edges[v1][v2]==maxWeight)//自己到自己的边或者边不存在则不用删除。
//        {
//            throw new Exception("边不存在！");
//        }
//
//        this.edges[v1][v2]=maxWeight;
//        this.numOfEdges--;
//    }
//
//    //打印邻接矩阵
//    public void print()
//    {
//        for(int i=0;i<this.edges.length;i++ )
//        {
//            for(int j=0;j<this.edges[i].length;j++)
//            {
//                System.out.print(edges[i][j]+"  ");
//            }
//            System.out.println();
//        }
//    }
//
//    //获得邻接矩阵中每个节点的度
//    public HashMap<Integer, Integer> getPointWeightSum()
//    {
//        HashMap<Integer, Integer> pointWeight = new HashMap<Integer, Integer>();
//        for(int k=0; k<vertices.size();k++){
//            int weightSum = 0;
//            //String point = vertices.get(k);
//            //行
//            for(int j=0;j<this.edges.length;j++){
//                if(edges[k][j]!= -1){
//                    weightSum+= edges[k][j];
//                }
//            }
//            //列
//            for(int i=0; i<this.edges.length;i++){
//                if(edges[i][k]!= -1){
//                    weightSum+= edges[i][k];
//                }
//            }
//            pointWeight.put(k, weightSum);
//        }
//        return pointWeight;
//    }
//
//    /**
//     * @param args
//     */
//    public static void main(String[] args) {
//        int n=5; //5个结点
//        int e=5; //5条边
//        GraphModel g = new GraphModel(n);
//        String[] vertices = new String[]{"A","B","C","D","E"};
//        Weight[] weights = new Weight[]{new Weight(0,1,10),new Weight(0,4,20),new Weight(2,1,40),new Weight(1,3,30),new Weight(3,2,50)};
//
//        try
//        {
//            Weight.createAdjGraphic(g, vertices, n, weights, e);
//            System.out.println("--------该邻接矩阵如下---------");
//            g.print();
//            System.out.println("结点的个数："+g.getNumOfVertice());
//            System.out.println("边的个数："+g.getNumOfEdges());
//            g.deleteEdges(0, 4);
//            System.out.println("--------删除之后---------");
//            g.print();
//            System.out.println("结点的个数："+g.getNumOfVertice());
//            System.out.println("边的个数："+g.getNumOfEdges());
//        }
//        catch(Exception ex)
//        {
//            System.out.println(ex.toString());
//        }
//    }
//}
