package tsp_solver_uef_241908;
import java.util.*;

/**
 * TSP Solver by Tuomas Hyvönen, Java file 4 of 11 (unnecessary tester class) 
 * 
 * Extra Java main method for testing purposes. Does not create a new User Interface window. 
 * If used, disable the main method in "TSP_Solver_UEF_241908.java" first. That creates 
 * a new UI every single time unlike this Java file here. 
 * 
 * Open source Java code, feel free to edit and try your own improvements. 
 * Tested with Windows 11 
 * Apache NetBeans 17 
 * Java JRE 8u371 64bit 
 * Java JDK 18.0.2 64bit 
 * 
 * @author Tuomas Hyvönen 
 * @version 2.0 
 */
public class Just_a_Tester_Class {
    
    /**
     * Possible main method when testing single details, rename to "main", 
     * call whatever methods you want and test them.
     * 
     * @param args String
     */
    public static void previousmain(String args[]) {
        // change the name to "main" if used to test single methods without the user interface 
        // the other one can be "previousmain" for example 
        
        Point p1 = new Point(1.0, 1.1);  // 
        Point p2 = new Point(1.2, 9.1);  // 
        Point p3 = new Point(8.0, 9.1);  // 
        Point p4 = new Point(8.2, 1.1);  // 
        Point p5 = new Point(2.0, 3.1);  // 
        Point p6 = new Point(3.2, 6.1);  // 
        Point p7 = new Point(2.0, 4.1);  // 
        Point p8 = new Point(5.2, 5.1);  // 
        Point p9 = new Point(6.0, 3.1);  // 
        Point p10 = new Point(6.2, 7.1); // 
        
        ArrayList<Double> inputXs = new ArrayList<>(Arrays.asList(
                p1.getX(), p2.getX(), p3.getX(), p4.getX()));
        ArrayList<Double> inputYs = new ArrayList<>(Arrays.asList(
                p1.getY(), p2.getY(), p3.getY(), p4.getY()));
        ArrayList<Double> neuronXs = new ArrayList<>(Arrays.asList(
                p5.getX(), p6.getX(), p7.getX(), p8.getX(), p9.getX(), p10.getX()));
        ArrayList<Double> neuronYs = new ArrayList<>(Arrays.asList(
                p5.getY(), p6.getY(), p7.getY(), p8.getY(), p9.getY(), p10.getY()));
        
        // 10.0 is the (computed / guessed) max distance in hull 
        KohonenSOM.performSOM(20, 10.0, inputXs, inputYs, neuronXs, neuronYs, false, null); 
        // false & null means "no logic stacks & no chromosomes" 
        // 20 is the max iterations 
        
        ArrayList answer = new ArrayList();
        /*
        ArrayList Xs = new ArrayList();
        ArrayList Ys = new ArrayList();
        for(int i = 0; i < Xs.size(); i++) {
            for(int j = 0; j < 2; j++) {
                if(j==0) {
                    answer.add(Xs.get(i));
                }
                else {
                    answer.add(Ys.get(i));
                }
            }
        }
        */
        ArrayList answer2 = Sub_algorithms.twoOpt(answer, Double.MAX_VALUE);
        ArrayList answer3 = Sub_algorithms.twoOpt(answer2, Double.MAX_VALUE);
        System.out.println(answer3.toString());
        
        //int max = answer3.size();
        //int min = 1;
        //int range = (max - min) + 1;     
        //int random = (int)(Math.random() * range) + min;
        System.out.println(answer3.get(0));
        System.out.println("");
    }
} 