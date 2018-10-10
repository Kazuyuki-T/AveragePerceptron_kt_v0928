/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.io.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.Graphics2D;

/**
 *
 * @author Ikeda-labPC7
 */
public class AveragePerceptron {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        // 結果出力用
        // フォルダ・ログ用の名前（日時）作成
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String txtfilename = simpleDateFormat.format(new Date(System.currentTimeMillis()));
        
        // csvデータのディレクトリ
        String dir = "gameclear_Databalancing_pickup_onehot_st/";
        
        // フォルダの作成
        String folderName = dir + txtfilename;
        File file = new File(folderName);
        if(file.mkdir() == true){
            // succece
        } else {
            System.out.println("フォルダの作成に失敗しました");
        }

        // 階層分計算し，
        for(int flr = 0; flr < 4; flr++){
            // 結果（重みなど）の文字列を返す
            run(dir + "data_" + flr + "f_gameclear_Databalancing_pickup_onehot_st", folderName + "/" + txtfilename, folderName + "/" + txtfilename + "_" + flr + "f");
        }
    }
    
    public static void run(String csvfilename, String txtfilename, String imgfilename){
        String txtFile = txtfilename + ".txt";
        String csvFile = csvfilename + ".csv";
        BufferedReader br = null;
        String line = "";
        String csvSplitBy = ",";
        int col = 13; // 特徴量+ラベルの数
        String[] Header = new String[col];
        ArrayList<double[]> dataset = new ArrayList<>();
        ArrayList<Integer> label = new ArrayList<>(); 
        
        
        // csvファイルから特長量＋ラベルの読み込み
        //<editor-fold defaultstate="collapsed" desc="データ読み込み">
        try
	{
		File fname = new File(csvFile);
		br = new BufferedReader(new FileReader(fname));
		line = br.readLine();
		String[] hsplt = line.split(",", 0);
                for(int j = 0; j < hsplt.length -1 ; j++){
                        //ele[i] = Integer.parseInt(str[i]);
                        Header[j] = hsplt[j];
                }
                Header[col-1] = "Bias";
                
                line = br.readLine();
                for(int row = 1; line != null; row++)
		{
//                    int[] ele = new int[col];
                    double[] ele = new double[col];
                    String[] str = line.split(",", 0);
                    int i = 0;
                    for(i = 0; i < str.length -1 ; i++){
                        //ele[i] = Integer.parseInt(str[i]);
                        ele[i] = Double.parseDouble(str[i]);
                    }
                    ele[str.length - 1] = 1;
                    dataset.add(ele);
                    label.add(Integer.parseInt(str[str.length - 1]));
                    line = br.readLine();
		}
		br.close();
	}
	catch(IOException e)
	{
		System.out.println(e);
	}
        //</editor-fold>
        
        
        avgPerceptronUnit avgP = new avgPerceptronUnit(dataset.get(0).length);
        List<Double> correctratio = new ArrayList<>();
        
        int datasize = dataset.size(); // 全データ数
        int last10Per = (int)(dataset.size() * 0.9); // テストデータ数
        int learningdatasize = dataset.size() - last10Per; // 学習データ数
        Random rnd = new Random();
//        int j;
        int k = 0;
//        int j = 1790;
        
        while(k < 500000){
//            int j = rnd.nextInt(last10Per);
//            int j = rnd.nextInt(datasize);
            int j = k%learningdatasize;
//            System.out.println("learning..." + k + " data [" + dataset.get(j)[0]+"," + dataset.get(j)[1] +","+ dataset.get(j)[2] + "][" + label.get(j)+"]" );
              //System.out.println("learning..." + k  );
            if(k % 10000 == 0) System.out.println("learning..." + (k / 10000) + "%");
            avgP.learning(dataset.get(j), label.get(j));
            
            if(k%10000 == 0){
//            boolean allcorrect = true;
                int correct = 0;
                for(int l = last10Per ; l < dataset.size() ; l++ ){
                    //int index = rnd.nextInt(dataset.size());
    //                allcorrect = allcorrect && (avgP.predict(dataset.get(l)) == label.get(l));
                    if (avgP.predict(dataset.get(l)) == label.get(l)){
                        correct++;
                    }
                }
                System.out.println("correct ratio : " + correct);
    //                        correctratio.add(correct/(double)(datasize)); 
                correctratio.add(correct/(double)(datasize - last10Per)); 
            }
            k++;
//            if (allcorrect)
//                break;
        }
        
        
        //<editor-fold defaultstate="collapsed" desc="テスト用">
//        avgPerceptronUnit a = new avgPerceptronUnit(3);
//        List<Double> correctratio = new ArrayList<>();
//        int[][] dataset = new int[][]{ {0,0,1},
//                                       {1,0,1},
//                                       {0,1,1},
//                                       {1,1,1}};
//        int[] datasetLabel = new int[]{-1,1,1,1};
//        Random rnd = new Random();
//        int j;
//        int k = 0;
//        while(k<1000){
//            j = rnd.nextInt(4);
//            a.learning(dataset[j], datasetLabel[j]);
//           
//            System.out.println("learning..."+ k +" current avg weight[" + a.avgWeight[0] + "," + a.avgWeight[1]+ ", " + a.avgWeight[2] + "]");
//            System.out.println("w[" + a.weight[0] + "," + a.weight[1]+ ", " + a.weight[2] + "]");
//            boolean allCorrect = true;
//            double correct = 0;
//            for(int i = 0; i < 4 ; i++ ){
//                System.out.println("Predict result " + i +" = " + a.predict(dataset[i]) );
//                allCorrect = allCorrect && (a.predict(dataset[i]) == datasetLabel[i]) ;
//                if (a.predict(dataset[i]) == datasetLabel[i]){
//                    correct++;
//                }
//            }
//            correctratio.add(correct/4d);
//            if(allCorrect)
//                break;
//            k++;
//        }
    //</editor-fold>

    
        System.out.println("Finish learning");
        int weightSize = avgP.avgWeight.length;
        StringBuilder strbuilder = new StringBuilder();
        strbuilder.append(csvFile + System.getProperty("line.separator"));
        for(int i = 0 ; i < weightSize; i++){
            strbuilder.append("average weight[" + Header[i] + "] : "+avgP.avgWeight[i] + System.getProperty("line.separator"));
        }
        strbuilder.append("Correctness = " + correctratio.get(correctratio.size() - 1) + System.getProperty("line.separator"));
        strbuilder.append(System.getProperty("line.separator"));
        
        System.out.print(new String(strbuilder));
        
        // txtファイルへ結果保存
        OutputFile(txtFile, new String(strbuilder), true);
        
        // グラフの表示・保存
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               createAndShowGui(correctratio, imgfilename);
            }
        });
    }
    
    public static void OutputFile(String name, String str, boolean tf) {
        try {
            File file = new File(name);
            if (checkBeforeWritefile(file)) {
                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, tf)));
                pw.print(str);
                pw.close();
            } else {
                System.out.println("ファイルに書き込めません");
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public static boolean checkBeforeWritefile(File file) {
        if (file.exists()) {
            if (file.isFile() && file.canWrite()) {
                return true;
            }
        } else {
            try {
                file.createNewFile();
                return true;
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        return false;
    }
    
    private static void createAndShowGui(List<Double> score, String str) {
//        List<Double> scores = new ArrayList<>();
//        Random random = new Random();
        int maxDataPoints = score.size();
        int maxScore = 1;
//        for (int i = 0; i < maxDataPoints; i++) {
//            scores.add((double) random.nextDouble() * maxScore);
////            scores.add((double) i);
//        }
        Graph mainPanel = new Graph(score);
        mainPanel.setPreferredSize(new Dimension(800, 600));
        JFrame frame = new JFrame("DrawGraph");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        saveImage(mainPanel, str + ".jpeg");
    }
    
    private static void saveImage(Graph panel , String a){
        BufferedImage imagebuf= new BufferedImage(800,600,BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = imagebuf.createGraphics();
        panel.paint(graphics2D);
		
        try {
            ImageIO.write(imagebuf,"jpeg", new File(a));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            System.out.println("error");
	}
    } 
}
