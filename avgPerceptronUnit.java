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
        //System.out.println(a); 
        //return a >= 0.0d ? 1.0d : -1.0d;
        //return Math.abs(Math.signum(a) - 0.0) <= 0.00001 ? 1 : Math.signum(a); 
        return (a > 0d || isDoubleValueEqual(a, 0d) == true) ? 1d : -1d;
    }
    
    public boolean learning(int[] data, int label)// assume data size = wSize include bias unit
    {
            double dot = threshold(dotProduct(data, this.weight)); 
            // Math.abs(dot - label) >= 0.00001
            if(isDoubleValueEqual(dot, label) == false){
                for(int i = 0 ; i < data.length ; i++){
                    double update = data[i] * label;
                    this.weight[i] += update;
                    this.allWeight[i] += this.weight[i];
                    this.avgWeight[i] = (this.allWeight[i]/ this.iteration); 
                }
                this.iteration ++;
                
                return true;
            }
            else{
                return false;
            }
    }
    
    public boolean learning(double[] data, double label)// assume data size = wSize include bias unit
    {
            double dot = threshold(dotProduct(data, this.weight)); 
            
//            System.out.println("dotp:" + dotProduct(data, this.weight));
//            System.out.println("dot:" + dot);
//            System.out.println("label:" + label);

            // Math.abs(dot - label) >= 0.00001 
            if(isDoubleValueEqual(dot, label) == false){
//                System.out.println("dot:" + dot);
//                for(int i = 0 ; i < data.length ; i++){
//                    System.out.println("weight[" + i + "]:" + this.weight[i] + " * " + data[i]);    
//                }
//                System.out.println("label:" + label);
//                System.out.println("|");

                for(int i = 0 ; i < data.length ; i++){
                    double update = data[i] * label;
                    this.weight[i] += update;
                    this.allWeight[i] += this.weight[i];
                    this.avgWeight[i] = (this.allWeight[i]/ this.iteration); 
                }
                this.iteration ++;
                
//                for(int i = 0 ; i < data.length ; i++){
//                    System.out.println("weight[" + i + "]:" + this.weight[i] + " * " + data[i]);    
//                }
                
                return true;
            }
            else{
                return false;
            }
    }
    
    public double predict(int[] data)
    {
        return threshold(dotProduct(data, this.avgWeight)); 
    }
    
    public double predict(double[] data)
    {    
        return threshold(dotProduct(data, this.avgWeight)); 
    }
            
    public double dotProduct(int[] a , double[] b)
    {
            int sum = 0;
            for(int i = 0 ; i < a.length ; i ++){
                sum +=  a[i] * b[i]; 
            }
            return sum;
    }
    
    public double dotProduct(double[] a , double[] b) //double 入力～
    {
            double sum = 0;
            for(int i = 0 ; i < a.length ; i++){
                sum +=  a[i] * b[i];
            }
            return sum;
    }
    
    // 引数として与えられた浮動小数点２値の比較
    // ほぼ同じ：true，ことなる:false
    public boolean isDoubleValueEqual(double dv1, double dv2){
        if(Math.abs(dv1 - dv2) <= 0.00001)  return true;
        else                                return false;
    }
}
