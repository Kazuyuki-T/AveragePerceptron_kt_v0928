/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Ikeda-labPC7
 */
public class avgPerceptronUnit {
    double[] weight;
    double[] allWeight;
    double[] avgWeight;
    int iteration;
    
    public avgPerceptronUnit(int wSize){
        this.weight = new double[wSize];
        this.allWeight = new double[wSize];
        this.avgWeight = new double[wSize];
        this.weight[wSize-1] = 1;
        this.allWeight[wSize-1] = 1;
        this.avgWeight[wSize-1] = 1;
        this.iteration = 1;
    }
    
    public double threshold(double a){
        return a >= 0 ? 1: -1;
    }
    
    public void learning(int[] data,int label)// assume data size = wSize include bias unit
    {
            double dot = threshold(dotProduct(data, this.weight)); 
            if(dot != label){
                for(int i = 0 ; i < data.length ; i++){
                    double update = data[i] * label;
                    this.weight[i] += update;
                    this.allWeight[i] += this.weight[i];
                    this.avgWeight[i] = (this.allWeight[i]/ this.iteration); 
                }
                this.iteration ++;
            }
    }
    
    public void learning(double[] data, double label)// assume data size = wSize include bias unit
    {
            double dot = threshold(dotProduct(data, this.weight)); 
            if(dot != label){
                for(int i = 0 ; i < data.length ; i++){
                    double update = data[i] * label;
                    this.weight[i] += update;
                    this.allWeight[i] += this.weight[i];
                    this.avgWeight[i] = (this.allWeight[i]/ this.iteration); 
                }
                this.iteration ++;
            }
    }
    
    public double predict(int[] data)
    {
        return threshold(dotProduct(data,this.avgWeight)); 
    }
    
    public double predict(double[] data)
    {    
        return threshold(dotProduct(data,this.avgWeight)); 
    }
            
    public double dotProduct(int[] a , double[] b)
    {
//        if(a.length == b.length)
//        {
            int sum = 0;
            for(int i = 0 ; i < a.length ; i ++){
                sum +=  a[i] * b[i]; 
            }
            return sum;
//        }
//        else 
//            return Integer.MIN_VALUE;
    }
    
    public double dotProduct(double[] a , double[] b) //double 入力～
    {
//        if(a.length == b.length)
//        {
            int sum = 0;
            for(int i = 0 ; i < a.length ; i ++){
                sum +=  a[i] * b[i]; 
            }
            return sum;
//        }
//        else 
//            return Integer.MIN_VALUE;
    }
}
