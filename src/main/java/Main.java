//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

public class Main {

    public static void main(String[] args) {
        //movielens数据集
        String itemPath = "E:\\ADA_Project\\RecommenderSystem\\src\\main\\resources\\movies.dat";
        String ratePath = "E:\\ADA_Project\\RecommenderSystem\\data\\ratings.dat";
        String tagPath = "E:\\ADA_Project\\RecommenderSystem\\data\\hetrectags.dat";
        //初始化
        Factory factory = new Factory(itemPath, ratePath, tagPath);
        factory.work();
        RecommendationSystem recommendationS = new RecommendationSystem(factory);
        for (int recNum = 400; recNum <= 400; recNum += 50) {
            System.out.println("推荐数："+recNum);
            CheckSystem checkS = new CheckSystem(recNum, factory.getUsers(), recommendationS);
            checkS.checkAllData(1000);
//            for (int threshold = 35;threshold >= 5;threshold-=5){
//                System.out.println("对标签数小于"+threshold+"的用户推荐");
//                checkS.checkAllData(threshold);
//            }
        }
        //checkS.checkaResult(190);

        //豆瓣数据集
//        String itemPath = "E:\\ADA_Project\\RecommenderSystem\\src\\main\\resources\\doubanset\\movies.dat";
//        String ratePath = "E:\\ADA_Project\\RecommenderSystem\\src\\main\\resources\\doubanset\\rates.dat";
//        String tagPath = "E:\\ADA_Project\\RecommenderSystem\\src\\main\\resources\\doubanset\\triples.dat";
//        //初始化
//        Factory factory = new Factory(itemPath, ratePath, tagPath);
//        factory.work();
//        RecommendationSystem recommendationS = new RecommendationSystem(factory);
//        CheckSystem checkS = new CheckSystem(400, factory.getUsers(), recommendationS);
//        checkS.checkaResult(175);
////        checkS.checkAllData();
    }
}
