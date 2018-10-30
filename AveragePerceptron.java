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

import java.util.Map;
import java.util.LinkedHashMap;

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
        
        //String name = "gameclear_Databalancing_pickup"; // 0
        //String name = "gameclear_Databalancing_pickup_onehot_pt"; // 2-1
        //String name = "gameclear_Databalancing_pickup_onehot_st"; // 2-2
        String name = "gameclear_Databalancing_pickup_onehot_ar"; // 2-3
        //String name = "gameclear_Databalancing_pickup_onehot_ptstar"; // 2-4
        //String name = "iris"; // test
        //String name = "gameclear_Databalancing_pickup_onehot_ar_dataadd2";
        
        // csvデータのディレクトリ
        String dir = name + "/";
        
        // フォルダの作成
        String folderName = dir + txtfilename;
        File file = new File(folderName);
        if(file.mkdir() == true){
            // succece
        } else {
            System.out.println("フォルダの作成に失敗しました");
        }

        // 階層分計算し，
        String csvfilenameWithDir;
        String txtfilenameWithDir;
        String imgfilenameWithDir;
        int mode = 0; // 交差検証ありなし
        int trial = 1000;
        int check = 0; // テスト性能などの確認タイミング
        int col = 13; // 特徴量+ラベルの数
        for(int flr = 0; flr < 4; flr++){
            csvfilenameWithDir = dir + "data_" + flr + "f_" + name;
            txtfilenameWithDir = folderName + "/" + txtfilename;
            imgfilenameWithDir = folderName + "/" + txtfilename + "_" + flr + "f";
            // 学習回数，学習方法，確認タイミング，重みの数（特長量＋ラベル），ファイルネーム×３，グラフ表示の有り無し
            run(trial, mode, check, col, csvfilenameWithDir, txtfilenameWithDir, imgfilenameWithDir, false);
        }
        
        // テスト用
        //run(trial, mode, check, col, dir + "iris2", folderName + "/" + txtfilename, folderName + "/" + txtfilename, false);
        //run(trial, mode, check, 13, dir + "data_2f_" + name, folderName + "/" + txtfilename, folderName + "/" + txtfilename + "_2f", false);
    }
    
    public static void run(int trial, int mode, int check, int col, String csvfilename, String txtfilename, String imgfilename, boolean graphOutputFlag){
        String txtFile = txtfilename + ".txt"; // 保存時の拡張子
        String csvFile = csvfilename + ".csv"; // 読み込みに使用
        //String imgFile = imgfilename + ".png";
        
        
        ArrayList<double[]> dataset = new ArrayList<>(); // データセット
        ArrayList<Integer> label = new ArrayList<>(); // データセットの順に対応したラベルセット
        double[] initWeight = new double[col]; // 重みの初期値
        
        
        // csvファイルから特長量＋ラベルの読み込み
        //<editor-fold defaultstate="collapsed" desc="データ読み込み">
        BufferedReader br = null;
        String line = "";
        String csvSplitBy = ",";
        String[] Header = new String[col];
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
                    //int[] ele = new int[col];
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
        
        
        // 重み初期値の読み込み
        //<editor-fold defaultstate="collapsed" desc="データ読み込み">
        try{
            File fname = new File(csvfilename + "_initWeight.csv");
            br = new BufferedReader(new FileReader(fname));
            line = br.readLine();
            for (int row = 0; line != null; row++) {
                String[] str = line.split(",", 0);
                for (int i = 0; i < str.length; i++) {
                    initWeight[i] = Double.parseDouble(str[i]);
                }
                line = br.readLine();
            }
            br.close();
            
            for(int i = 0; i < initWeight.length; i++){
                System.out.println("init_weight[" + i + "]:" + initWeight[i]);
            }
        }
        catch(IOException e){
            System.out.println(e);
        }
        //</editor-fold>
        
        
        avgPerceptronUnit avgP = new avgPerceptronUnit(dataset.get(0).length, initWeight);
        Map<Integer, Double> correctRatioTesting = new LinkedHashMap<Integer, Double>(); // 汎化性能
        Map<Integer, Double> correctRatioTraining = new LinkedHashMap<Integer, Double>(); // 学習時性能
        Map<Integer, Double> correctRatioTestingLogarithm = new LinkedHashMap<Integer, Double>(); // 汎化性能，対数
        Map<Integer, Double> correctRatioTrainingLogarithm = new LinkedHashMap<Integer, Double>(); // 学習時性能，対数
        Map<Integer, Double> precisionTesting = new LinkedHashMap<Integer, Double>(); // 精度
        Map<Integer, Double> recallTesting = new LinkedHashMap<Integer, Double>(); // 再現性
        Map<Integer, Double> f_measureTesting = new LinkedHashMap<Integer, Double>(); // f値
        int[] ohVisCount = new int[5]; // ワンホットな部分の訪問回数収集のため
        Map<Integer, Map<Integer, Double>> weightTransition = new LinkedHashMap<Integer, Map<Integer, Double>>(); // 重みの推移，<重みの要素番号<回数，重み>>
        Map<Integer, Map<Integer, Double>> weightTransitionLogarithm = new LinkedHashMap<Integer, Map<Integer, Double>>(); // 重みの推移，<重みの要素番号<回数，重み>>, 対数
        Map<Integer, Map<Integer, Double>> avgWeightTransition = new LinkedHashMap<Integer, Map<Integer, Double>>(); // 重みの推移，<重みの要素番号<回数，重み>>
        Map<Integer, Map<Integer, Double>> avgWeightTransitionLogarithm = new LinkedHashMap<Integer, Map<Integer, Double>>(); // 重みの推移，<重みの要素番号<回数，重み>>, 対数
        for(int cn = 0; cn < col; cn++){
            weightTransition.put(cn, new LinkedHashMap<Integer, Double>()); // LHMをもつLHM
            weightTransitionLogarithm.put(cn, new LinkedHashMap<Integer, Double>());
            avgWeightTransition.put(cn, new LinkedHashMap<Integer, Double>());
            avgWeightTransitionLogarithm.put(cn, new LinkedHashMap<Integer, Double>());
        }
        
        
        
        
        int datasize = dataset.size(); // 全データ数
        int last10Per = (int)(dataset.size() * 0.1); // テストデータ数
        int learningdatasize = dataset.size() - last10Per; // 学習データ数
        System.out.println("datasize:" + datasize);
        System.out.println("learningdatasize:" + learningdatasize);
        System.out.println("last10Per:" + last10Per);
        Random rnd = new Random(); // 学習データのとり方がランダムの時
        
        
        
        
        //<editor-fold defaultstate="collapsed" desc="学習データ順序のシャッフル">
        
        // 順序入れ替え
        
        //</editor-fold>
        
        
        //<editor-fold defaultstate="collapsed" desc="n-folds，未完">
        // 優先度：低，n-folds
        if(mode == 0){
            // hold-out
            
        }else{
            // n-folds
            // 学習データをn分割し，順にテストデータ，学習データとする
            int divNum = 10;
//            for(int i=0; i<n; i++){
//                
//            }
        }
        //</editor-fold>
        
        
        int k = 0; // 学習ステップ数
        int j = 0; // 学習するデータのインデックス
        int n = 1; // 対数データ収集の判定に使用
        int learningCount = 0; // トータルの学習回数
        while(k < trial){
            boolean updateFlag = avgP.learning(dataset.get(j), label.get(j)); // true:更新あり,false:更新なし
            learningCount++; // 学習回数のカウント
            j++; // 学習データのインデックスを次に変更
            if(j == learningdatasize){ j = 0; k++; } // 学習データが一周したとき，最初から＆ステップ数更新
            
            // 重みの訪問回数記録
            for(int wn = 0; wn < 5; wn++){
                ohVisCount[wn] += (int)(dataset.get(j)[wn + 4] + 0.5);
            }
            
            //<editor-fold defaultstate="collapsed" desc="データ記録">
            // 100点刻み，汎化性能，再現性等
            if ((learningCount % (learningdatasize * trial / 100) == 0)) {
                int correct = 0; // 正解数
                int truePosNum = 0; // 正例を正例と判定した正解数
                int trueNegNum = 0; // 負例を負例と判定した正解数 
                int posTestDataSize = 0; // 正例のテストデータ数
                int negTestDataSize = 0; // 負例のテストデータ数
                
                // 汎化性能の測定
                for (int l = learningdatasize; l < dataset.size(); l++) {
                    // 判定が正しいとき，カウント＆TFTN収集
                    if (isDoubleValueEqual(avgP.predict(dataset.get(l)), label.get(l)) == true) {
                        correct++;
                        if (label.get(l) == 1)  truePosNum++;
                        else                    trueNegNum++;
                    }

                    // 正例テストデータ数と負例テストデータ数の収集
                    if (label.get(l) == 1)  posTestDataSize++;
                    if (label.get(l) == -1) negTestDataSize++;
                }
                
                int testDataSize = last10Per;
                int falsePosNum = testDataSize - posTestDataSize - trueNegNum;
                int falseNegNum = testDataSize - negTestDataSize - truePosNum;

                double precision = ((double) truePosNum / (truePosNum + falsePosNum)) * 100; // 精度，適合率
                double recall = ((double) truePosNum / (truePosNum + falseNegNum)) * 100; // 再現率
                double f_measure = 2 * recall * precision / (recall + precision); // f値

                System.out.println("learning..." + (learningCount / (learningdatasize * trial / 100)) + "%");
                System.out.println("correct ratio : " + ((double) correct / testDataSize) * 100 + "(" + correct + "/" + testDataSize + ")");
                System.out.println("precision : " + precision);
                System.out.println("recall : " + recall);
                System.out.println("f_measure : " + f_measure);

                correctRatioTesting.put(learningCount, correct / (double) (last10Per));
                precisionTesting.put(learningCount, precision);
                recallTesting.put(learningCount, recall);
                f_measureTesting.put(learningCount, f_measure);

                // 学習時性能の測定
                correct = 0;
                for (int l = 0; l < learningdatasize; l++) {
                    //if (Math.abs(avgP.predict(dataset.get(l)) - label.get(l)) < 0.00001) correct++;
                    if (isDoubleValueEqual(avgP.predict(dataset.get(l)), label.get(l)) == true) {
                        correct++;
                    }
                }
                correctRatioTraining.put(learningCount, correct / (double)(learningdatasize));

                // 重みの推移の記録
                for (int wn = 0; wn < avgP.weight.length; wn++) {
                    weightTransition.get(wn).put(learningCount, avgP.weight[wn]); // key:cnのLHMに追加
                    avgWeightTransition.get(wn).put(learningCount, avgP.avgWeight[wn]);
                }
            }
            // 対数軸
            int npow10 = (int) (Math.pow(10, n));
            if (learningCount <= npow10 && learningCount % (int) (Math.pow(10, n - 1)) == 0) {
                int correct; // 正負判定が正しいときカウント

                // 汎化性能の確認
                correct = 0;
                for (int l = learningdatasize; l < dataset.size(); l++) {
                    //if (Math.abs(avgP.predict(dataset.get(l)) - label.get(l)) < 0.00001) correct++;
                    if (isDoubleValueEqual(avgP.predict(dataset.get(l)), label.get(l)) == true) {
                        correct++;
                    }
                }
                correctRatioTestingLogarithm.put(learningCount, correct / (double) (last10Per));

                // 学習時性能の確認
                correct = 0;
                for (int l = 0; l < learningdatasize; l++) {
                    //if (Math.abs(avgP.predict(dataset.get(l)) - label.get(l)) < 0.00001) correct++;
                    if (isDoubleValueEqual(avgP.predict(dataset.get(l)), label.get(l)) == true) {
                        correct++;
                    }
                }
                correctRatioTrainingLogarithm.put(learningCount, correct / (double) (learningdatasize));

                // 重みの推移の記録
                for (int wn = 0; wn < avgP.weight.length; wn++) {
                    weightTransitionLogarithm.get(wn).put(learningCount, avgP.weight[wn]); // key:cnのLHMに追加
                    avgWeightTransitionLogarithm.get(wn).put(learningCount, avgP.avgWeight[wn]);
                }

                if (learningCount == npow10) n++; // 次の対数の位？へ
            }
            //</editor-fold>
        }
        System.out.println("Finish learning");
        
        
        
        
        
        
//        //<editor-fold defaultstate="collapsed" desc="学習の流れがおかしい？旧学習＆記録">
//        while(k <= trial){
//            //int j = rnd.nextInt(last10Per);
//            //int j = rnd.nextInt(datasize);
//            //int j = rnd.nextInt(learningdatasize);
//            int j = k % learningdatasize;
//            
//            
//            //System.out.println("learning..." + k + " data [" + dataset.get(j)[0] + "," + dataset.get(j)[1] +","+ dataset.get(j)[2] + "][" + label.get(j)+"]" );
////            System.out.print("learning..." + k + " data [");
////            for(int i = 0; i < col; i++){
////                System.out.print(dataset.get(j)[i] + ", ");
////            }
////            System.out.println("][" + label.get(j)+"]" );
//            //System.out.println("learning..." + k  );
//            
//            
//            
////            for(int n=0; n<5; n++){
////                ohVisCount[n] += (int)(dataset.get(j)[n + 4] + 0.5);
////            }
//            
//            
//            // true:更新あり,false:更新なし
//            boolean updateFlag = avgP.learning(dataset.get(j), label.get(j));
//            
//            if(check == 0){
//                //<editor-fold defaultstate="collapsed" desc="毎回">
//                // 汎化性能
//                if((k % (trial / 100) == 0)){
//                    //boolean allcorrect = true;
//                    int correct = 0; // 正解数
//                    int truePosNum = 0; // 正例を正例と判定した正解数
//                    int trueNegNum = 0; // 負例を負例と判定した正解数 
//                    int posTestDataSize = 0; // 正例のテストデータ数
//                    int negTestDataSize = 0; // 負例のテストデータ数
//                    for(int l = learningdatasize; l < dataset.size(); l++){
//                        //int index = rnd.nextInt(dataset.size());
//                        //allcorrect = allcorrect && (avgP.predict(dataset.get(l)) == label.get(l));
//
//                        //if (Math.abs(avgP.predict(dataset.get(l)) - label.get(l)) < 0.00001) correct++;
//                        if(isDoubleValueEqual(avgP.predict(dataset.get(l)), label.get(l)) == true){
//                            correct++;
//                            if(label.get(l) == 1)   truePosNum++;
//                            else                    trueNegNum++;
//                        }
//                        
//                        if(label.get(l) == 1)   posTestDataSize++;
//                        if(label.get(l) == -1)  negTestDataSize++;
//                    }
//                    
//                    int testDataSize = last10Per;
//                    int falsePosNum = testDataSize - posTestDataSize - trueNegNum;
//                    int falseNegNum = testDataSize - negTestDataSize - truePosNum;
//                    
//                    double precision = ((double)truePosNum / (truePosNum + falsePosNum)) * 100; // 精度，適合率
//                    double recall = ((double)truePosNum / (truePosNum + falseNegNum)) * 100; // 再現率
//                    double f_measure = 2 * recall * precision / (recall + precision); // f値
//
//                    System.out.println("learning..." + (k / (trial / 100)) + "%");
//                    System.out.println("correct ratio : " + ((double)correct / testDataSize) * 100 + "(" + correct + "/" + testDataSize + ")");
//                    System.out.println("precision : " + precision);
//                    System.out.println("recall : " + recall);
//                    System.out.println("f_measure : " + f_measure);
//                    
//                    correctRatioTesting.put(k, correct/(double)(last10Per));
//                    precisionTesting.put(k, precision);
//                    recallTesting.put(k, recall);
//                    f_measureTesting.put(k, f_measure);
//                    
//                    // 重みの推移の記録
//                    for(int wn = 0; wn < avgP.weight.length; wn++){
//                        weightTransition.get(wn).put(k, avgP.weight[wn]); // key:cnのLHMに追加
//                    }
//                }
//                // 対数軸
//                int npow10 = (int)(Math.pow(10, n));
//                if(k <= npow10 && k % (int)(Math.pow(10, n-1)) == 0){
//                    int correct; // 正負判定が正しいときカウント
//
//                    // 汎化性能の確認
//                    correct = 0;
//                    for(int l = learningdatasize; l < dataset.size(); l++){
//                        //if (Math.abs(avgP.predict(dataset.get(l)) - label.get(l)) < 0.00001) correct++;
//                        if(isDoubleValueEqual(avgP.predict(dataset.get(l)), label.get(l)) == true) correct++;
//                    }
//                    correctRatioTestingLogarithm.put(k, correct/(double)(last10Per));
//
//                    // 学習時性能の確認
//                    correct = 0;
//                    for(int l = 0; l < learningdatasize; l++){
//                        //if (Math.abs(avgP.predict(dataset.get(l)) - label.get(l)) < 0.00001) correct++;
//                        if(isDoubleValueEqual(avgP.predict(dataset.get(l)), label.get(l)) == true) correct++;
//                    }
//                    correctRatioTrainingLogarithm.put(k, correct/(double)(learningdatasize));
//
//                    // 重みの推移の記録
//                    for(int wn = 0; wn < avgP.weight.length; wn++){
//                        weightTransitionLogarithm.get(wn).put(k, avgP.weight[wn]); // key:cnのLHMに追加
//                    }
//                    
//                    if(k == npow10) n++; // 次の対数の位？へ
//                }
//                k++;
//                //</editor-fold>
//            }
//            else if(check == 1){
//                //<editor-fold defaultstate="collapsed" desc="重み更新時のみ">
//                // 汎化性能の確認
//                if((k % (trial / 100) == 0 || k == trial) && updateFlag == true){
//                    //boolean allcorrect = true;
//                    int correct = 0;
//                    for(int l = learningdatasize; l < dataset.size(); l++){
//                        //int index = rnd.nextInt(dataset.size());
//                        //allcorrect = allcorrect && (avgP.predict(dataset.get(l)) == label.get(l));
//
//                        //if (Math.abs(avgP.predict(dataset.get(l)) - label.get(l)) < 0.00001) correct++;
//                        if(isDoubleValueEqual(avgP.predict(dataset.get(l)), label.get(l)) == true) correct++;
//                    }
//                    System.out.println("learning..." + (k / (trial / 100)) + "%");
//                    System.out.println("correct ratio : " + correct);
//
//                    correctRatioTesting.put(k, correct/(double)(last10Per)); 
//                }
//                // 対数
//                int npow10 = (int)(Math.pow(10, n));
//                if(k <= npow10 && k % (int)(Math.pow(10, n-1)) == 0 && updateFlag == true){
//                    int correct; // 正負判定が正しいときカウント
//
//                    // 汎化性能の確認
//                    correct = 0;
//                    for(int l = learningdatasize; l < dataset.size(); l++){
//                        //if (Math.abs(avgP.predict(dataset.get(l)) - label.get(l)) < 0.00001) correct++;
//                        if(isDoubleValueEqual(avgP.predict(dataset.get(l)), label.get(l)) == true) correct++;
//                    }
//                    correctRatioTestingLogarithm.put(k, correct/(double)(last10Per));
//
//                    // 学習時性能の確認
//                    correct = 0;
//                    for(int l = 0; l < learningdatasize; l++){
//                        //if (Math.abs(avgP.predict(dataset.get(l)) - label.get(l)) < 0.00001) correct++;
//                        if(isDoubleValueEqual(avgP.predict(dataset.get(l)), label.get(l)) == true) correct++;
//                    }
//                    correctRatioTrainingLogarithm.put(k, correct/(double)(learningdatasize));
//
//                    if(k == npow10) n++; // 次の対数の位？へ
//                }
//                if(updateFlag == true) k++; // 学習回数のカウント
//                //</editor-fold>
//            }
//            
//            
//            
////            if (allcorrect)
////                break;
//
//
//        }
//        //</editor-fold>
        
        
        
        
            
        
        
        
        
        
        
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

    
        //<editor-fold defaultstate="collapsed" desc="重みテキスト＆コンソール出力">
        // 得られた重みweight
        // 得られた平均重みweight
        // 得られた学習時性能
        // 得られた汎化性能
        
        
        
        System.out.println("\n//----- result -----//");
        int weightSize = avgP.avgWeight.length;
        StringBuilder strbuilder;
        
        
        // 最終的な重み，平均重み等
        strbuilder = new StringBuilder();
        strbuilder.append(csvFile + System.getProperty("line.separator"));
        for(int i = 0 ; i < weightSize; i++){
            strbuilder.append("weight[" + Header[i] + "],"+avgP.weight[i] + System.getProperty("line.separator"));
        }
        for(int i = 0 ; i < weightSize; i++){
            strbuilder.append("average weight[" + Header[i] + "],"+avgP.avgWeight[i] + System.getProperty("line.separator"));
        }
        strbuilder.append("Correctness," + correctRatioTesting.get(learningCount) + System.getProperty("line.separator"));
        strbuilder.append(System.getProperty("line.separator"));
        for(int wn=0; wn<5; wn++){
            strbuilder.append("visitCount_weight[" + (wn+4) + "]," + ohVisCount[wn] + System.getProperty("line.separator"));
        }
        OutputFile(txtfilename + "_result.csv", new String(strbuilder), true);
        
        System.out.print(new String(strbuilder)); // 最終的な出力
        
        
        // 重みの推移
        strbuilder = new StringBuilder();
        strbuilder.append(csvFile + System.getProperty("line.separator"));
        for(int wn = 0; wn < avgP.weight.length; wn++){
            strbuilder.append("weight[" + wn + "]" + System.getProperty("line.separator"));
            for(Map.Entry<Integer, Double> entry : weightTransition.get(wn).entrySet()){
                strbuilder.append(entry.getKey() + "," + entry.getValue() + System.getProperty("line.separator"));
            }
            strbuilder.append(System.getProperty("line.separator"));
        }
        strbuilder.append(System.getProperty("line.separator"));
        OutputFile(txtfilename + "_weight.csv", new String(strbuilder), true);
        
        
        // 重みの推移，対数
        strbuilder = new StringBuilder();
        strbuilder.append(csvFile + System.getProperty("line.separator"));
        for(int wn = 0; wn < avgP.weight.length; wn++){
            strbuilder.append("weight[" + wn + "]" + System.getProperty("line.separator"));
            for(Map.Entry<Integer, Double> entry : weightTransitionLogarithm.get(wn).entrySet()){
                strbuilder.append(entry.getKey() + "," + entry.getValue() + System.getProperty("line.separator"));
            }
            strbuilder.append(System.getProperty("line.separator"));
        }
        strbuilder.append(System.getProperty("line.separator"));
        OutputFile(txtfilename + "_weightLogarithm.csv", new String(strbuilder), true);
        
        
        // 平均重みの推移，100分割
        strbuilder = new StringBuilder();
        strbuilder.append(csvFile + System.getProperty("line.separator"));
        for(int wn = 0; wn < avgP.weight.length; wn++){
            strbuilder.append("average weight[" + wn + "]" + System.getProperty("line.separator"));
            for(Map.Entry<Integer, Double> entry : avgWeightTransition.get(wn).entrySet()){
                strbuilder.append(entry.getKey() + "," + entry.getValue() + System.getProperty("line.separator"));
            }
            strbuilder.append(System.getProperty("line.separator"));
        }
        strbuilder.append(System.getProperty("line.separator"));
        OutputFile(txtfilename + "_avgweight.csv", new String(strbuilder), true);

        
        // 平均重みの推移，対数
        strbuilder = new StringBuilder();
        strbuilder.append(csvFile + System.getProperty("line.separator"));
        for(int wn = 0; wn < avgP.weight.length; wn++){
            strbuilder.append("average weight[" + wn + "]" + System.getProperty("line.separator"));
            for(Map.Entry<Integer, Double> entry : avgWeightTransitionLogarithm.get(wn).entrySet()){
                strbuilder.append(entry.getKey() + "," + entry.getValue() + System.getProperty("line.separator"));
            }
            strbuilder.append(System.getProperty("line.separator"));
        }
        strbuilder.append(System.getProperty("line.separator"));
        OutputFile(txtfilename + "_avgweightLogarithm.csv", new String(strbuilder), true);
        
        
        // 汎化性能
        strbuilder = new StringBuilder();
        strbuilder.append(csvFile + System.getProperty("line.separator"));
        for(Map.Entry<Integer, Double> entry : correctRatioTesting.entrySet()){
            strbuilder.append(entry.getKey() + "," + entry.getValue() + System.getProperty("line.separator"));
        }
        strbuilder.append(System.getProperty("line.separator"));
        OutputFile(txtfilename + "_testing.csv", new String(strbuilder), true);
        
        
        // 汎化性能，対数
        strbuilder = new StringBuilder();
        strbuilder.append(csvFile + System.getProperty("line.separator"));
        for(Map.Entry<Integer, Double> entry : correctRatioTestingLogarithm.entrySet()){
            strbuilder.append(entry.getKey() + "," + entry.getValue() + System.getProperty("line.separator"));
        }
        strbuilder.append(System.getProperty("line.separator"));
        OutputFile(txtfilename + "_testingLogarithm.csv", new String(strbuilder), true);
        
        
        // 学習時性能
        strbuilder = new StringBuilder();
        strbuilder.append(csvFile + System.getProperty("line.separator"));
        for(Map.Entry<Integer, Double> entry : correctRatioTraining.entrySet()){
            strbuilder.append(entry.getKey() + "," + entry.getValue() + System.getProperty("line.separator"));
        }
        strbuilder.append(System.getProperty("line.separator"));
        OutputFile(txtfilename + "_training.csv", new String(strbuilder), true);
        
        
        // 学習時性能，対数
        strbuilder = new StringBuilder();
        strbuilder.append(csvFile + System.getProperty("line.separator"));
        for(Map.Entry<Integer, Double> entry : correctRatioTrainingLogarithm.entrySet()){
            strbuilder.append(entry.getKey() + "," + entry.getValue() + System.getProperty("line.separator"));
        }
        strbuilder.append(System.getProperty("line.separator"));
        OutputFile(txtfilename + "_trainingLogarithm.csv", new String(strbuilder), true);
        
        
//        for(int i = 0 ; i < weightSize; i++){
//            strbuilder.append("weight[" + Header[i] + "] : "+avgP.weight[i] + System.getProperty("line.separator"));
//        }
//        strbuilder.append(avgP.iteration + System.getProperty("line.separator"));
        
        
        //</editor-fold>


        

        

        
        // グラフの表示・保存
        //<editor-fold defaultstate="collapsed" desc="グラフの表示・保存">
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
               // map, ファイル名，y範囲0-1 or variable，x範囲通常0or対数10
               createAndShowGui(correctRatioTesting, imgfilename + "_Testing", 0, 0, graphOutputFlag);
               createAndShowGui(correctRatioTestingLogarithm, imgfilename + "_Testing_Logarithm", 0, 10, graphOutputFlag);
               createAndShowGui(correctRatioTrainingLogarithm, imgfilename + "_Training_Logarithm", 0, 10, graphOutputFlag);
               
               for(int wn = 0; wn < avgP.weight.length; wn++){
                   createAndShowGui(weightTransition.get(wn), imgfilename + "_Weight" + wn, 1, 0, graphOutputFlag);
               }
               for(int wn = 0; wn < avgP.weight.length; wn++){
                   createAndShowGui(weightTransitionLogarithm.get(wn), imgfilename + "_Weight" + wn + "_Logarithm", 1, 10, graphOutputFlag);
               }
            }
        });
        // 外部からスレッドを終了したい
        // thread.stop?
        if(graphOutputFlag == false);
        //</editor-fold>
        
        
//        correctRatioTesting.clear();
//        precisionTesting.clear();
//        recallTesting.clear();
//        f_measureTesting.clear();
//        correctRatioTestingLogarithm.clear();
//        correctRatioTrainingLogarithm.clear();
//        weightTransition.clear();
//        weightTransitionLogarithm.clear();
//        avgWeightTransition.clear();
//        avgWeightTransitionLogarithm.clear();
        
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
    
    private static void createAndShowGui(Map<Integer, Double> map, String str, int ymode, int xmode, boolean outputFlag) {
//        List<Double> scores = new ArrayList<>();
//        Random random = new Random();
//        int maxDataPoints = score.size();
//        int maxScore = 1;
//        for (int i = 0; i < maxDataPoints; i++) {
//            scores.add((double) random.nextDouble() * maxScore);
////            scores.add((double) i);
//        }

        // map -> list
        //List<Double> score = new ArrayList<>(map.values());
        
        Graph mainPanel = new Graph(map, ymode, xmode);
        mainPanel.setPreferredSize(new Dimension(800, 600));
        JFrame frame = new JFrame(str);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().add(mainPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(outputFlag);
        saveImage(mainPanel, str + ".png");
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
    
    // 引数として与えられた浮動小数点２値の比較
    // ほぼ同じ：true，ことなる:false
    public static boolean isDoubleValueEqual(double dv1, double dv2){
        if(Math.abs(dv1 - dv2) <= 0.00001)  return true;
        else                                return false;
    }
}
