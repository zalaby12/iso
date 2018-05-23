package util.linalg;

/**
 * An implementation of a vector 
 * @author Zachary Halaby zhalaby3@gatech.edu
 * @version 1.0
 */
public class DoubleVector extends Vector {
    
    private double[] data;
    
    public DoubleVector(double[] data) {
        this.data = data;
    }
    
    public DoubleVector(int size) {
        data = new double[size];
    }

    public int size() {
        return data.length;
    }

    public double get(int i) {
        return data[i];
    }
    
    public void set(int i, double value) {
        data[i] = value;
    }
}
