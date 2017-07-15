//package algorithms;
//
//import Model.GraphModel;
//import java.util.ArrayList;
//import java.util.HashMap;
//
///**
// /**
// * Created by yangq0a on 16/9/11.
// */
//public class RandomWalk {
//    public static final long MAX_ITERATIOM_TIMES = 1000;
//    public static final double MIN_ERRORS = 0.0001;
//    public static final float alpha = 0.8f;
//    ReferenceNet rn = new ReferenceNet();
//    HashMap<Long, GraphModel> oringalClusters = rn.createReferenceNetwork();
//
//    public HashMap<Long, double[][]> getTransMatrix(){
//        HashMap<Long, double[][]> transMatrix = new HashMap<Long, double[][]>();
//        for(long id: oringalClusters.keySet()){
//            GraphModel gm = oringalClusters.get(id);
//            ArrayList<Integer> sumList = gm.getSumOfCol();
//            double edges[][] = new double[sumList.size()][sumList.size()];
//            for(int i=0; i< gm.getEdges().length; i++){
//                for(int j=0; j<gm.getEdges()[0].length; j++){
//                    if(gm.getEdges()[j][i]==1){
//                        edges[j][i] = sumList.get(i)*1.0/gm.getEdges()[0].length;
//                    }else{
//                        edges[j][i] = 0.0;
//                    }
//                }
//            }
//            transMatrix.put(id, edges);
//        }
//        return transMatrix;
//    }
//
//    //for every node from every grahph compute their rank scores
//    public void RWRGraph(){
//        ArrayList<String> vertices = gm.getValueOfVertice();
//        for(int i=0; i<vertices.size(); i++){
//            System.out.println("ID为"+id+"的referenceNet中"+"节点"+vertices.get(i)+"的ranking scroe为:");
//            randomWalkRestart(alpha, i, MAX_ITERATIOM_TIMES, MIN_ERRORS, transMatrix.get(id));
//        }
//    }
//    //RWR
//    public void randomWalkRestart(float alpha, int startPoint, long maxIterationTimes, double minErrors, double transMatrix[][]){
//        int iterationTimes = 0;
//        double[] rank_sp = new double[transMatrix[0].length];   //过程中得到的向量
//        double[] e = new double[transMatrix[0].length];    //开始时的向量
//        //init rank_sp, set identify vector
//        for(int i=0; i<transMatrix[0].length; i++){
//            if(i == startPoint){
//                rank_sp[i] = 1.0;
//                e[i] = 1.0;
//            }else{
//                rank_sp[i] =0.0;
//                e[i] =0.0;
//            }
//        }
//        boolean flag = true;
//        while(iterationTimes < maxIterationTimes){
//            double[] Temp = rank_sp;
//            if(flag == true){
//                for(int i= 0; i<transMatrix.length; i++){
//                    for(int j=0; j<transMatrix[0].length; j++){
//                        rank_sp[i]+= alpha*transMatrix[i][j]*rank_sp[j];
//                    }
//                    rank_sp[i]+= (1-alpha)*e[i];
//                }
//                if(judge(Temp,rank_sp,minErrors)){
//                    flag = false;
//                }
//            }else
//                break;
//            iterationTimes++;
//        }
//        System.out.println("迭代"+iterationTimes+"次后节点["+startPoint+"]的rank score为:");
//        for(int i=0; i<rank_sp.length; i++){
//            System.out.println(rank_sp[i]);
//        }
//    }
//    //judge the difference between two interations
//    public boolean judge(double a[], double b[], double minErrors){
//        boolean flag = true;
//        for(int i=0; i<a.length; i++){
//            if(Math.abs(a[i]-b[i])<minErrors)
//                continue;
//            else{
//                flag = false;
//                break;
//            }
//        }
//        return flag;
//    }
//    public static void main(String[] args){
//        RandomWalk rw = new RandomWalk();
//        rw.RWRGraph();
//    }
//}
