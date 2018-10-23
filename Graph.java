/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 *
 * @author Ikeda-labPC7
 */
public class Graph extends JPanel {
    private int width = 800;
    private int heigth = 400;
    private int padding = 25;
    private int labelPadding = 25;
    private Color lineColor = new Color(44, 102, 230, 180);
    private Color lineColor1 = new Color(44, 102, 230, 180);
    private Color pointColor = new Color(100, 100, 100, 180);
    private Color pointColor1 = new Color(100, 100, 100, 180);
    private Color gridColor = new Color(200, 200, 200, 200);
    private static final Stroke GRAPH_STROKE = new BasicStroke(2f);
    private int pointWidth = 4;
    private int numberYDivisions = 10;
    private List<Double> scores; // 引数として与えられたデータリスト
    private List<Double> scores1;
    private int yAxisMode;
    private int xAxisMode;
    private List<Integer> loglist;
    

    public Graph(Map<Integer, Double> map, int yAxisMode, int xAxisMode) {
        //this.scores = scores;
        this.scores = new ArrayList<>(map.values());
        this.scores1 = new ArrayList<>();
        this.yAxisMode = yAxisMode; // 与えられたデータに応じてスケール変更
        this.xAxisMode = xAxisMode;
        
        // 対数を要素番号から呼べるようにしておく
        if(this.xAxisMode != 0) loglist = new ArrayList<Integer>(map.keySet());
    }
    
    public Graph(Map<Integer, Double> map, Map<Integer, Double> map2, int yAxisMode, int xAxisMode) {
        //this.scores = scores;
        this.scores = new ArrayList<>(map.values());
        this.scores1 = new ArrayList<>(map2.values());
        this.yAxisMode = yAxisMode; // 与えられたデータに応じてスケール変更
        this.xAxisMode = xAxisMode;
        
        // 対数を要素番号から呼べるようにしておく
        if(this.xAxisMode != 0) loglist = new ArrayList<Integer>(map.keySet());
    }

    protected void paintPointLine(Graphics2D g2, double xScale, double yScale, List<Double> scoreList, int colorflag){
        // 描画する点の座標の計算とリストへの格納
        List<Point> graphPoints = new ArrayList<>();
        for (int i = 0; i < scoreList.size(); i++) {
            int y1 = 0;
            int x1 = 0;
            
            x1 = (int)(i * xScale + padding + labelPadding);
            if(yAxisMode == 0)       y1 = (int)((1.0 - scoreList.get(i)) * yScale + padding);
            else if(yAxisMode == 1)  y1 = (int)((getMaxScore(false) - scoreList.get(i)) * yScale + padding);
            
            graphPoints.add(new Point(x1, y1));
        }

        // 点どうしをつなぐ線の描画
        Stroke oldStroke = g2.getStroke();
        if(colorflag == 0) g2.setColor(lineColor);
        else if(colorflag == 1) g2.setColor(lineColor1);
        g2.setStroke(GRAPH_STROKE);
        for (int i = 0; i < graphPoints.size() - 1; i++) {
            int x1 = graphPoints.get(i).x;
            int y1 = graphPoints.get(i).y;
            int x2 = graphPoints.get(i + 1).x;
            int y2 = graphPoints.get(i + 1).y;
            g2.drawLine(x1, y1, x2, y2);
        }

        // 点の描画
        g2.setStroke(oldStroke);
        if(colorflag == 0) g2.setColor(lineColor);
        else if(colorflag == 1) g2.setColor(lineColor1);
        for (int i = 0; i < graphPoints.size(); i++) {
            int x = graphPoints.get(i).x - pointWidth / 2;
            int y = graphPoints.get(i).y - pointWidth / 2;
            int ovalW = pointWidth;
            int ovalH = pointWidth;
            g2.fillOval(x, y, ovalW, ovalH);
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // xy座標のメモリの幅の計算
        double xScale = 0d;
        double yScale = 0d;
        xScale = ((double) getWidth() - (2 * padding) - labelPadding) / (scores.size() - 1);
        if(yAxisMode == 0)          yScale = ((double) getHeight() - (2 * padding) - labelPadding) / (1.0 - 0);
        else if(yAxisMode == 1)     yScale = ((double) getHeight() - 2 * padding - labelPadding) / (getMaxScore(false) - getMinScore(false));
        
        // draw white background
        g2.setColor(Color.WHITE);
        g2.fillRect(padding + labelPadding, padding, getWidth() - (2 * padding) - labelPadding, getHeight() - 2 * padding - labelPadding);
        g2.setColor(Color.BLACK);

        // create hatch marks and grid lines for y axis.
        for (int i = 0; i < numberYDivisions + 1; i++) {
            int x0 = padding + labelPadding;
            int x1 = pointWidth + padding + labelPadding;
            int y0 = getHeight() - ((i * (getHeight() - padding * 2 - labelPadding)) / numberYDivisions + padding + labelPadding);
            int y1 = y0;
            if (scores.size() > 0) {
                g2.setColor(gridColor);
                g2.drawLine(padding + labelPadding + 1 + pointWidth, y0, getWidth() - padding, y1);
                g2.setColor(Color.BLACK);
                
                String yLabel = new String();
                if(yAxisMode == 0)       yLabel = ((int) ((0 + (1.0 - 0) * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0 + "";
                else if(yAxisMode == 1)  yLabel = ((int) ((getMinScore(false) + (getMaxScore(false) - getMinScore(false)) * ((i * 1.0) / numberYDivisions)) * 100)) / 100.0 + "";
                
                FontMetrics metrics = g2.getFontMetrics();
                int labelWidth = metrics.stringWidth(yLabel);
                g2.drawString(yLabel, x0 - labelWidth - 5, y0 + (metrics.getHeight() / 2) - 3);
            }
            g2.drawLine(x0, y0, x1, y1);
        }

        // and for x axis
        for (int i = 0; i < scores.size(); i++) {
            if (scores.size() > 1) {
                int x0 = i * (getWidth() - padding * 2 - labelPadding) / (scores.size() - 1) + padding + labelPadding;
                int x1 = x0;
                int y0 = getHeight() - padding - labelPadding;
                int y1 = y0 - pointWidth;
                
                if (xAxisMode == 0 && i % ((int)((scores.size() / 10.0))) == 0) {
                    g2.setColor(gridColor);
                    g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
                    g2.setColor(Color.BLACK);
                    String xLabel =  i + "";
                    FontMetrics metrics = g2.getFontMetrics();
                    int labelWidth = metrics.stringWidth(xLabel);
                    g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
                }
                else if (xAxisMode == 10 && i % (xAxisMode - 1) == 0) {
                    g2.setColor(gridColor);
                    g2.drawLine(x0, getHeight() - padding - labelPadding - 1 - pointWidth, x1, padding);
                    g2.setColor(Color.BLACK);
                    String xLabel = loglist.get(i) + "";
                    FontMetrics metrics = g2.getFontMetrics();
                    int labelWidth = metrics.stringWidth(xLabel);
                    g2.drawString(xLabel, x0 - labelWidth / 2, y0 + metrics.getHeight() + 3);
                }
                g2.drawLine(x0, y0, x1, y1);
            }
        }

        // create x and y axes 
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, padding + labelPadding, padding);
        g2.drawLine(padding + labelPadding, getHeight() - padding - labelPadding, getWidth() - padding, getHeight() - padding - labelPadding);

        paintPointLine(g2, xScale, yScale, scores, 0); // グラフ内の点と線の描画
        if(scores1.isEmpty() == true) paintPointLine(g2, xScale, yScale, scores1, 1);
    }

//    @Override
//    public Dimension getPreferredSize() {
//        return new Dimension(width, heigth);
//    }

    private double getMinScore(boolean tf) {
        double minScore = Double.MAX_VALUE;
        for (Double score : scores) {
            minScore = Math.min(minScore, score);
        }
        
        if(tf == true)  return minScore;
        else            return (int)(minScore - 1d);
    }
    
    private double getMaxScore(boolean tf) {
        double maxScore = Double.MIN_VALUE;
        for (Double score : scores) {
            maxScore = Math.max(maxScore, score);
        }
        
        if(tf == true)  return maxScore;
        else            return (int)(maxScore + 1d);
    }

    public void setScores(List<Double> scores) {
        this.scores = scores;
        invalidate();
        this.repaint();
    }

    public List<Double> getScores() {
        return scores;
    }
}
