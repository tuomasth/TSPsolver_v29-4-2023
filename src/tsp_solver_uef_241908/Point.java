package tsp_solver_uef_241908;

/**
 * TSP Solver by Tuomas Hyvönen, Java file 8 of 11 
 * 
 * A class for creating a Cartesian x-y-point instance if needed. 
 * Can adjust priority (=weight, has nothing to do with distance). 
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
public class Point {
    private double x;
    private double y;
    private double weight; // nice even though never used 
    
    /**
     * The constructor.
     * 
     * @param x double
     * @param y double
     */
    public Point(double x, double y) {
        if(((x < 5000000) && (y < 5000000)) && 
           ((x > -1) && (y > -1))) {
            this.x = x;
            this.y = y;
            this.weight = 1.0;
        }
        else {
            System.out.println("Coordinates should be less than 5 000 000 and more than -1");
        }
    } 
    
    /**
     * The getter method for X.
     * 
     * @return double
     */
    public double getX() {
        //printPoint(p);
        return this.x;
    }
    
    /**
     * The getter method for Y.
     * 
     * @return double
     */
    public double getY() {
        //printPoint(p);
        return this.y;
    }
    
    /**
     * The setter method for point object.
     * 
     * @param x double
     * @param y double
     */
    public void setPoint(double x, double y) {
        if(((x < 5000000) && (y < 5000000)) && 
           ((x > -1) && (y > -1))) {
                this.x = x;
                this.y = y;
        }
        else {
            System.out.println("Coordinates should be less than 5 000 000 and more than -1");
        }
    }
    
    /**
     * Print a point's information.
     * 
     * @param p Point
     */
    public void printPoint(Point p) {
        System.out.println("POINT WEIGHT AND X&Y ARE " + p.weight + "; " 
                + p.x + ", " + p.y + "\n-- " + p.toString() + "\n");
    }
    
    /**
     * If a point needs a priority or a special price, this setter method will set a weight.
     * 
     * Currently unused.
     * 
     * @param weight double
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }
    
    /**
     * Returns the weight of a point, has nothing to do with coordinates.
     * 
     * Currently unused.
     * 
     * @return double
     */
    public double getWeight() {
        return this.weight;
    }
}
