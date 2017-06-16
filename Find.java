import jsat.SimpleDataSet;
import jsat.classifiers.DataPoint;

import java.util.List;



public class Find {

  public Find() { System.out.println("Hello, I'm a finder"); }

  public static int[] theMatchingWavFile(SimpleDataSet source, SimpleDataSet projectedData) {

    //gonna need to do basic error checking here before too long...
    //if(source.getSampleSize() != projectedData.getSampleSize()) { return null; }
    double[] ranksForProjection1 = new double[2];
    double[] ranksForProjection2 = new double[2];

    ranksForProjection1[0] = sumOfSquaredError(source.getDataPoint(0), projectedData.getDataPoint(0));
    ranksForProjection1[1] = sumOfSquaredError(source.getDataPoint(0), projectedData.getDataPoint(1));
    ranksForProjection2[0] = sumOfSquaredError(source.getDataPoint(1), projectedData.getDataPoint(0));
    ranksForProjection2[1] = sumOfSquaredError(source.getDataPoint(1), projectedData.getDataPoint(1));

    return assignedRanks(ranksForProjection1, ranksForProjection2);

  }


  private static int[] assignedRanks(double[] a, double[] b) {

    if(a[0] < b[0]) { return new int[] {0,1}; }
    else if(a[0] == b[0]) { return new int[] {0,1};} 
    else { return new int[] {1,0}; }

  }

  public static double[] theMatchingWavFile() { 
    System.out.println("finding matching wav file"); 
    return new double[0];
  }

  private static double crossCorrelation(DataPoint source, DataPoint projectedData) {

    return 0.0;

  }

  private static double sumOfSquaredError(DataPoint source, DataPoint projectedData) {

    double[] sourceArray = source.getNumericalValues().arrayCopy();
    double[] projectedArray = projectedData.getNumericalValues().arrayCopy();
    double sumOfSquaredError = 0.0;
    for(int i = 0; i < sourceArray.length; i++) {
      double sourceValue = sourceArray[i];
      double projectedValue = projectedArray[i];
      sumOfSquaredError += Math.pow((projectedValue - sourceValue), 2);
    }
    return sumOfSquaredError;
 
  }

}
