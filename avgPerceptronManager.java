
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Ikeda-Lab15
 */
public class avgPerceptronManager {
    public avgPerceptronManager(){
        
    }
    
    public void run(int trial, int mode, int check, int col, String csvfilename, String txtfilename, String imgfilename, boolean graphOutputFlag){
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
                    label.add((int)Double.parseDouble(str[str.length - 1]));
                    //label.add(Integer.parseInt(str[str.length - 1]));
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
            
            avgP.init(initWeight);
        }
        catch(IOException e){
            System.out.println(e);
            avgP.init();
        }
        //</editor-fold>
        
        
        // LearningResultにまとめたらええんじゃないの？
        // mapに突っ込む
        
        // 画像の作り方との兼ね合い，画像生成用に残す？
        // LRmapに重み追加したら？
        
        Map<Integer, Double> accuracyTesting_avg = new LinkedHashMap<Integer, Double>();
        Map<Integer, Double> accuracyTesting = new LinkedHashMap<Integer, Double>(); // 汎化性能
        Map<Integer, Double> accuracyTraining = new LinkedHashMap<Integer, Double>(); // 学習時性能
        Map<Integer, Double> precisionTesting = new LinkedHashMap<Integer, Double>(); // 精度
        Map<Integer, Double> recallTesting = new LinkedHashMap<Integer, Double>(); // 再現性
        Map<Integer, Double> f_measureTesting = new LinkedHashMap<Integer, Double>(); // f値
        int[] ohVisCount = new int[col]; // ワンホットな部分の訪問回数収集のため
        Map<Integer, Map<Integer, Double>> weightTransition = new LinkedHashMap<Integer, Map<Integer, Double>>(); // 重みの推移，<重みの要素番号<回数，重み>>
        Map<Integer, Map<Integer, Double>> avgWeightTransition = new LinkedHashMap<Integer, Map<Integer, Double>>(); // 重みの推移，<重みの要素番号<回数，重み>>
        for(int cn = 0; cn < col; cn++){
            weightTransition.put(cn, new LinkedHashMap<Integer, Double>()); // LHMをもつLHM
            avgWeightTransition.put(cn, new LinkedHashMap<Integer, Double>());
        }
        
        Map<Integer, LearningResult> training_Weight = new LinkedHashMap<Integer, LearningResult>();
        Map<Integer, LearningResult> testing_Weight = new LinkedHashMap<Integer, LearningResult>();
        Map<Integer, LearningResult> testing_AvgWeight = new LinkedHashMap<Integer, LearningResult>();
        
        
        int datasize = dataset.size(); // 全データ数
        int testdatasize = (int)(dataset.size() * 0.1); // テストデータ数
        int learningdatasize = dataset.size() - testdatasize; // 学習データ数
        System.out.println("datasize:" + datasize);
        System.out.println("learningdatasize:" + learningdatasize);
        System.out.println("testdatasize:" + testdatasize);
        Random rnd = new Random(); // 学習データのとり方がランダムの時
        
        
        //<editor-fold defaultstate="collapsed" desc="学習データ順序のシャッフル">
        
        // 順序入れ替え
        for(int i = 0; i < dataset.size(); i++){
            int target = rnd.nextInt(datasize); // i番目との入れ替え先
            double[] tmpData = dataset.get(i); // i番目の元データを保持
            int tmpLabel = label.get(i); // i番目の元ラベルを保持
            // データの変更
            dataset.set(i, dataset.get(target)); // i番目を置きかえ
            dataset.set(target, tmpData); // targetを置き換え
            // ラベルの変更
            label.set(i, label.get(target));
            label.set(target, tmpLabel);
        }
        
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
        
        
        int posTestDataSize = 0; // 正例のテストデータ数
        int negTestDataSize = 0; // 負例のテストデータ数
        // テストデータの正例数と負例数のカウント
        for (int l = learningdatasize; l < dataset.size(); l++) {
            if (label.get(l) == 1)  posTestDataSize++;
            if (label.get(l) == -1) negTestDataSize++;
        }
        
        int k;
        //int n = 1; // 対数データ収集の判定に使用
        int learningCount = 0; // トータルの学習回数(avgpunit iteration?)
        for(k = 1; k <= trial; k++){ // 学習ステップ数
            // 注目する学習データをステップごとにランダムにするため，順序入れ替え配列用意
            int[] pickupIndex = new int[learningdatasize];
            for(int j = 0; j < learningdatasize; j++){
                pickupIndex[j] = j;
            }
            for(int j = 0; j < learningdatasize; j++){
                int target = rnd.nextInt(learningdatasize);
                int tmp = pickupIndex[j];
                pickupIndex[j] = pickupIndex[target];
                pickupIndex[target] = tmp;
            }
            
            for(int j = 0; j < learningdatasize; j++){
                boolean updateflag = avgP.learning(dataset.get(pickupIndex[j]), label.get(pickupIndex[j])); // true:更新あり,false:更新なし
                learningCount++; // 学習回数のカウント
                
                // 重みの訪問回数記録
                for(int wn = 0; wn < ohVisCount.length; wn++){
                    //ohVisCount[wn] += (int)(dataset.get(pickupIndex[j])[wn + ohVis_BIAS] + 0.5);
                    
                    // 0のとき->更新なし
                    // 負のとき->更新あり
                    // 正のとき->2パターン
                    double puVal = dataset.get(pickupIndex[j])[wn];
                    if(isDoubleValueEqual(puVal, 0d) == true){
                        // 0のとき
                    }
                    else if(puVal < 0d){
                        // 負のとき
                        ohVisCount[wn] += 1;
                    }
                    else if(puVal > 0d){
                        // 正のとき
                        if(puVal < 1d)  ohVisCount[wn] += (int)(puVal + 0.5);
                        else            ohVisCount[wn] += 1;
                    }
                }
            }
            //avgP.eta *= 0.9; // 学習率の更新
            //System.out.println("eta : " + avgP.eta);
            
            
            // LearningResult
            //if(k % (trial / 100) == 0){
            if(true){ // エポックごとに記録
                System.out.println("learning..." + (double)k * 100 / trial + "% ---------------------------------------------");
                LearningResult result;
                
                
                System.out.println("[train, weight]"); // 学習時性能，weight
                result = new LearningResult(learningdatasize, posTestDataSize, negTestDataSize);
                for (int l = 0; l < learningdatasize; l++) {
                    // 判定が正しいとき，カウント＆TFTN収集
                    if (isDoubleValueEqual(avgP.predict(dataset.get(l)), label.get(l)) == true) {
                        result.countCorrect();
                        if (label.get(l) == 1)  result.countTruePosNum();
                        else                    result.countTrueNegNum();
                    }
                }
                result.sysOutput();
                accuracyTraining.put(k, (double)result.correct / learningdatasize);
                training_Weight.put(k, result.getCopy());
                
                
                System.out.println("[test, weight]"); // 汎化性能の測定
                result = new LearningResult(testdatasize, posTestDataSize, negTestDataSize);
                for (int l = learningdatasize; l < dataset.size(); l++) {
                    // 判定が正しいとき，カウント＆TFTN収集
                    if (isDoubleValueEqual(avgP.predict(dataset.get(l)), label.get(l)) == true) {
                        result.countCorrect();
                        if (label.get(l) == 1)  result.countTruePosNum();
                        else                    result.countTrueNegNum();
                    }
                }
                result.sysOutput();
                
                accuracyTesting.put(k, result.getAccuracy());
                precisionTesting.put(k, result.getPrecision());
                recallTesting.put(k, result.getRecall());
                f_measureTesting.put(k, result.getFmeasure());
                
                testing_Weight.put(k, result.getCopy());
            
                
                System.out.println("[test, aveWeight]"); // 汎化性能，aveweight
                result = new LearningResult(testdatasize, posTestDataSize, negTestDataSize);
                for (int l = learningdatasize; l < dataset.size(); l++) {
                    // 判定が正しいとき，カウント＆TFTN収集
                    if (isDoubleValueEqual(avgP.predictAvgWeight(dataset.get(l)), label.get(l)) == true) {
                        result.countCorrect();
                        if (label.get(l) == 1)  result.countTruePosNum();
                        else                    result.countTrueNegNum();
                    }
                }
                result.sysOutput();
                
                testing_AvgWeight.put(k, result.getCopy());
                
                
                // 重みの推移の記録
                for (int wn = 0; wn < avgP.weight.length; wn++) {
                    weightTransition.get(wn).put(k, avgP.weight[wn]); // key:cnのLHMに追加
                    avgWeightTransition.get(wn).put(k, avgP.avgWeight[wn]);
                }
            }
        }
        System.out.println("Finish learning");
        
        
        
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
        strbuilder.append("Accuracy(train)," + accuracyTraining.get(k - 1) + System.getProperty("line.separator"));
        strbuilder.append("Accuracy(test)," + accuracyTesting.get(k - 1) + System.getProperty("line.separator"));
        for(int i = 0 ; i < weightSize; i++){
            strbuilder.append("average weight[" + Header[i] + "],"+avgP.avgWeight[i] + System.getProperty("line.separator"));
        }
        strbuilder.append(System.getProperty("line.separator"));
        for(int wn = 0; wn < ohVisCount.length; wn++){
            strbuilder.append("visitCount_weight[" + wn + "]," + ohVisCount[wn] + System.getProperty("line.separator"));
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
        
        
        // 平均重みの推移
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
        
        
        strbuilder = new StringBuilder();
        strbuilder.append(csvFile + System.getProperty("line.separator"));
        strbuilder.append("epoch,datasize,posdata,negdata,correct,truepos,trueneg,falsepos,falseneg,accuracy,precision,recall,fmeasure" + System.getProperty("line.separator")); // index
        for(Map.Entry<Integer, LearningResult> entry : training_Weight.entrySet()){
            LearningResult pickup = entry.getValue();
            strbuilder.append(entry.getKey()
                                + "," + pickup.getDataSize()
                                + "," + pickup.getPosDataSize() + "," + pickup.getNegDataSize()
                                + "," + pickup.getCorrect()
                                + "," + pickup.getTruePosNum() + "," + pickup.getTrueNegNum()
                                + "," + pickup.getFalsePosNum() + "," + pickup.getFalseNegNum()
                                + "," + pickup.getAccuracy()
                                + "," + pickup.getPrecision() + "," + pickup.getRecall() + "," + pickup.getFmeasure()
                                + System.getProperty("line.separator")
            );
        }
        strbuilder.append(System.getProperty("line.separator"));
        OutputFile(txtfilename + "_Training_Weight.csv", new String(strbuilder), true);
        
        
        strbuilder = new StringBuilder();
        strbuilder.append(csvFile + System.getProperty("line.separator"));
        strbuilder.append("epoch,datasize,posdata,negdata,correct,truepos,trueneg,falsepos,falseneg,accuracy,precision,recall,fmeasure" + System.getProperty("line.separator")); // index
        for(Map.Entry<Integer, LearningResult> entry : testing_Weight.entrySet()){
            LearningResult pickup = entry.getValue();
            strbuilder.append(entry.getKey()
                                + "," + pickup.getDataSize()
                                + "," + pickup.getPosDataSize() + "," + pickup.getNegDataSize()
                                + "," + pickup.getCorrect()
                                + "," + pickup.getTruePosNum() + "," + pickup.getTrueNegNum()
                                + "," + pickup.getFalsePosNum() + "," + pickup.getFalseNegNum()
                                + "," + pickup.getAccuracy()
                                + "," + pickup.getPrecision() + "," + pickup.getRecall() + "," + pickup.getFmeasure()
                                + System.getProperty("line.separator")
            );
        }
        strbuilder.append(System.getProperty("line.separator"));
        OutputFile(txtfilename + "_Testing_Weight.csv", new String(strbuilder), true);
        
        
        strbuilder = new StringBuilder();
        strbuilder.append(csvFile + System.getProperty("line.separator"));
        strbuilder.append("epoch,datasize,posdata,negdata,correct,truepos,trueneg,falsepos,falseneg,accuracy,precision,recall,fmeasure" + System.getProperty("line.separator")); // index
        for(Map.Entry<Integer, LearningResult> entry : testing_AvgWeight.entrySet()){
            LearningResult pickup = entry.getValue();
            strbuilder.append(entry.getKey()
                                + "," + pickup.getDataSize()
                                + "," + pickup.getPosDataSize() + "," + pickup.getNegDataSize()
                                + "," + pickup.getCorrect()
                                + "," + pickup.getTruePosNum() + "," + pickup.getTrueNegNum()
                                + "," + pickup.getFalsePosNum() + "," + pickup.getFalseNegNum()
                                + "," + pickup.getAccuracy()
                                + "," + pickup.getPrecision() + "," + pickup.getRecall() + "," + pickup.getFmeasure()
                                + System.getProperty("line.separator")
            );
        }
        strbuilder.append(System.getProperty("line.separator"));
        OutputFile(txtfilename + "_Testing_AvgWeight.csv", new String(strbuilder), true);
        
        
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
               createAndShowGui(accuracyTesting, imgfilename + "_Accuracy_Testing", 0, 0, graphOutputFlag);
               createAndShowGui(accuracyTraining, imgfilename + "_Accuracy_Training", 0, 0, graphOutputFlag);
               
               for(int wn = 0; wn < avgP.weight.length; wn++){
                   createAndShowGui(weightTransition.get(wn), imgfilename + "_Weight" + wn, 1, 0, graphOutputFlag);
               }
               for(int wn = 0; wn < avgP.avgWeight.length; wn++){
                   createAndShowGui(avgWeightTransition.get(wn), imgfilename + "_AvgWeight" + wn, 1, 0, graphOutputFlag);
               }
               
               createAndShowGui(precisionTesting, imgfilename + "_Precision_Testing", 0, 0, graphOutputFlag);
               createAndShowGui(recallTesting, imgfilename + "_Recall_Testing", 0, 0, graphOutputFlag);
               createAndShowGui(f_measureTesting, imgfilename + "_Fmeasure_Testing", 0, 0, graphOutputFlag);
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
    
    public void OutputFile(String name, String str, boolean tf) {
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

    public boolean checkBeforeWritefile(File file) {
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
    
    private void createAndShowGui(Map<Integer, Double> map, String str, int ymode, int xmode, boolean outputFlag) {
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
    
    private void saveImage(Graph panel , String a){
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
    public boolean isDoubleValueEqual(double dv1, double dv2){
        if(Math.abs(dv1 - dv2) <= 0.00001)  return true;
        else                                return false;
    }
    
    
    
    
    // インナークラス
    public class LearningResult{
        private int dataSize; // 学習データorテストデータ
        private int posDataSize; // 正例のテストデータ数
        private int negDataSize; // 負例のテストデータ数
        private int correct; // 正解数
        private double accuracy; // 正解率
        private int truePosNum; // 正例を正例と判定した正解数
        private int trueNegNum; // 負例を負例と判定した正解数 
        private int falsePosNum; // 負例を正例と判定した不正解数
        private int falseNegNum; // 正例を負例と判定した不正解数
        private double precision; // 精度，適合率
        private double recall; // 再現率
        private double f_measure; // f値
        private boolean calcFlag; // 諸々計算しているか
        
        private LearningResult(){ }
        
        public LearningResult(int dataSize, int posTestDataSize, int negTestDataSize){
            this.dataSize = dataSize;
            this.posDataSize = posTestDataSize;
            this.negDataSize = negTestDataSize;
            this.init();
        }
        
        public void init(){
            correct = 0;
            accuracy = 0;
            truePosNum = 0;
            trueNegNum = 0; 
            falsePosNum = 0;
            falseNegNum = 0;
            precision = 0;
            recall = 0;
            f_measure = 0;
            calcFlag = false;
        }
        
        public void countCorrect(){
            correct++;
        }
        public void countTruePosNum(){
            truePosNum++;
        }
        public void countTrueNegNum(){
            trueNegNum++;
        }
        
        public int getDataSize(){
            return dataSize;
        }
        public int getPosDataSize(){
            return posDataSize;
        }
        public int getNegDataSize(){
            return negDataSize;
        }
        
        public int getCorrect(){
            return correct;
        }
        public int getTruePosNum(){
            return truePosNum;
        }
        public int getTrueNegNum(){
            return trueNegNum;
        }
        
        public double getAccuracy(){
            if(calcFlag == false) calc();
            return accuracy;
        }
        public int getFalsePosNum(){
            if(calcFlag == false) calc();
            return falsePosNum;
        }
        public int getFalseNegNum(){
            if(calcFlag == false) calc();
            return falseNegNum;
        }
        public double getPrecision(){
            if(calcFlag == false) calc();
            return precision;
        }
        public double getRecall(){
            if(calcFlag == false) calc();
            return recall;
        }
        public double getFmeasure(){
            if(calcFlag == false) calc();
            return f_measure;
        }
        
        private void calc(){
            accuracy = (double)correct / dataSize;
            falsePosNum = dataSize - posDataSize - trueNegNum;
            falseNegNum = dataSize - negDataSize - truePosNum;
            precision = ((double) truePosNum / (truePosNum + falsePosNum)); // 精度，適合率
            recall = ((double) truePosNum / (truePosNum + falseNegNum)); // 再現率
            f_measure = 2 * recall * precision / (recall + precision); // f値
            calcFlag = true;
        }
        
        public LearningResult getCopy(){
            if(this.calcFlag == false) calc(); // 一応計算済みかチェック
            
            LearningResult copyLR = new LearningResult();
            copyLR.dataSize = this.dataSize;
            copyLR.posDataSize = this.posDataSize;
            copyLR.negDataSize = this.negDataSize;
            copyLR.correct = this.correct;
            copyLR.accuracy = this.accuracy;
            copyLR.truePosNum = this.truePosNum;
            copyLR.trueNegNum = this.trueNegNum; 
            copyLR.falsePosNum = this.falsePosNum;
            copyLR.falseNegNum = this.falseNegNum;
            copyLR.precision = this.precision;
            copyLR.recall = this.recall;
            copyLR.f_measure = this.f_measure;
            copyLR.calcFlag = this.calcFlag;
            return copyLR;
        }
        
        public void sysOutput(){
            if(this.calcFlag == false) calc(); // 一応計算済みかチェック
            
            System.out.println("    correct ratio : " + this.accuracy + "(" + this.correct + "/" + this.dataSize + ")");
            System.out.println("    precision : " + this.precision);
            System.out.println("    recall : " + this.recall);
            System.out.println("    f_measure : " + this.f_measure);
            System.out.println("    testData : " + this.dataSize);
            System.out.println("    posTestData : " + this.posDataSize);
            System.out.println("    negTestData : " + this.negDataSize);
            System.out.println("    truePos : " + this.truePosNum);
            System.out.println("    trueNeg : " + this.trueNegNum);
        }
    }
}
