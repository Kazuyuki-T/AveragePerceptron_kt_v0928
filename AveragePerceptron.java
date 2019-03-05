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
        //String name = "gameclear_Databalancing_pickup_onehot_ar"; // 2-3
        //String name = "gameclear_Databalancing_pickup_onehot_ptstar"; // 2-4
        //String name = "iris"; // test
        //String name = "gameclear_Databalancing_pickup_onehot_ar_dataadd4";
        //String name = "gameclear_Databalancing_pickup_onehot_st";
        //String name = "gameclear_Databalancing_pickup";
        //String name = "gameclear_newData_onehot_ar_hpDiv04p1"; int col = 23; // 特徴量+ラベルの数
        //String name = "gameclear_newData_onehot_hpDiv10"; int col = 19;
        //String name = "gameclear_newData_onehot_hpDiv10_exceptHp0"; int col = 19;
        //String name = "gameclear_newData_onehot_hpDiv04p1"; int col = 14;
        //String name = "gameclear_newData_onehot_ar_hpDiv10_exceptHp0"; int col = 19;
        //String name = "gameclear_newData_onehot_allitem_hp";
        //String name = "gameclear_newData_onehot_allitem_hpdiv04p1"; int col = 26;
        //String name = "gameclear_newData_onehot_allitem_hpdiv04p1_onlyhpresetdata"; int col = 26;
        String name = "gameclear_newData_onehot_allitem_hpdiv04p1_mixdata"; int col = 26;
        //String name = "gameclear_newData_onehot_allitem_hpdiv04p1_mixdata_spDiv05"; int col = 31;
        //String name = "gameclear_newData_onehot_allitem_hpdiv04p1_mixdata_spDiv04"; int col = 30;
        //String name = "gameclear_newData_onehot_allitem_hpdiv04p1_mixdata_spDiv04p0"; int col = 30;
        //String name = "gameclear_newData_onehot_allitem_hpdiv04p1_mixdata_spDiv04p1"; int col = 31;
        //String name = "stairDownTiming"; int col = 7;
        //String name = "stairDownTimingIG0"; int col = 7;
        //String name = "stairDownTiming_col9"; int col = 9;
        //String name = "btendTiming"; int col = 9;
        //String name = "btendTiming_col7"; int col = 7;
        //String name = "btendTiming_onehot_ar"; int col = 13;
        //String name = "gameclear_newData_onehot_ar"; int col = 13;
        
        
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
        
        
        avgPerceptronManager avgpm = new avgPerceptronManager();
        

        // 階層分計算し，
        String csvfilenameWithDir;
        String txtfilenameWithDir;
        String imgfilenameWithDir;
        int mode = 0; // 交差検証ありなし
        int trial = 1000;
        int check = 0; // テスト性能などの確認タイミング
        
//        for(int flr = 0; flr < 4; flr++){
//            csvfilenameWithDir = dir + "data_" + flr + "f_" + name;
//            txtfilenameWithDir = folderName + "/" + txtfilename;
//            imgfilenameWithDir = folderName + "/" + txtfilename + "_" + flr + "f";
//            // 学習回数，学習方法，確認タイミング，重みの数（特長量＋ラベル），ファイルネーム×３，グラフ表示の有り無し
//            avgpm.run(trial, mode, check, col, csvfilenameWithDir, txtfilenameWithDir, imgfilenameWithDir, false);
//        }
        
        // テスト用
        //run(trial, mode, check, col, dir + "iris2", folderName + "/" + txtfilename, folderName + "/" + txtfilename, false);
        //avgpm.run(trial, mode, check, 13, dir + "data_2f_" + name, folderName + "/" + txtfilename, folderName + "/" + txtfilename + "_2f", false);
        avgpm.run(trial, mode, check, col, dir + "data_2f_" + name, folderName + "/" + txtfilename, folderName + "/" + txtfilename + "_2f", false);
    }
}
