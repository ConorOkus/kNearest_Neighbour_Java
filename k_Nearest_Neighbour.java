import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

public class k_Nearest_Neighbour {
	private static final String trainingdatasetfile="breast_cancer_train.csv";
	private static final String testdatasetfile="breast_cancer_test.csv";
	private static final String[] headers = {"PID","CLUMP_THICKNESS","CELL_SIZE","CELL_SHAPE","MARG_ADESION",
			"EPIT_CELL_SIZE","BARE_NUCLEI","BLAND_CHROM","N_NUCLEOLI","MITOSES","CLASS","PREDICTIONS"};
	public static boolean ASC = true;
	
	
	public static void main(String[] args) throws IOException {
		
		CSVReader trainingSet = new CSVReader(new FileReader(trainingdatasetfile), ',', '\'', 1); // This is used to read from CSV file
		CSVReader testSet = new CSVReader(new FileReader(testdatasetfile), ',', '\'', 1); // This is used to read from CSV file
		
		List<String[]> trainingData = trainingSet.readAll(); // Read contents from file and store in a list 
		List<String[]> testData = testSet.readAll(); // Read contents from file and store in a list 
		ArrayList<String[]> kNearestNeighbours = new ArrayList<String[]>(); // Store a list of all of the nearest neighbors as string []
		int k; // Value of K
		String diagnoses = ""; // The class attribute
		String [] predictions = new String[testData.size()]; // Predictions array
		String [] actual = new String[testData.size()]; // Actual array
		int [][] matrix = {{0,0}, // Create the confusion matrix
				           {0,0}};
		
		/* Ask the user to input a value for K and store the input in variable k */
		
		System.out.println("Please enter in a value for K: ");
		Scanner in = new Scanner(System.in);
		k = Integer.parseInt(in.nextLine());
		System.out.println("Value for K = " + k);
		System.out.println();
		in.close();
		
		/* Store the actual classifier in an array named actual */
		
		getActual(testData, actual);
		
		
		/* Normalize/standardize the data sets */ 
		
		ArrayList<Double> means = columnMeans(trainingData);
		ArrayList<Double> stDevs = columnsStdevs(trainingData, means);
		ArrayList<Double> meansTest = columnMeans(testData);
		ArrayList<Double> stDevsTest = columnsStdevs(testData, meansTest);
		
		
		standardizeDataset(trainingData, means, stDevs);
		standardizeDataset(testData, meansTest, stDevsTest);
		
		
		/* Loop through test data, find nearest neighbors for each instance
		 * Get the diagnoses / predicted class attribute for each instance
		 * Add the diagnoses to an array of predictions */
		
		for (int i = 0; i < testData.size(); i++) {
			kNearestNeighbours = getNeighbors(trainingData, testData.get(i), k);
			
			diagnoses = getResponse(kNearestNeighbours);
					
			predictions[i] = diagnoses;
		}
		
		/* Create a new CVS writer and write to "k" file. Add the headers to the file first,
		 * then create an array that will have an extra column for the predictions. Take all the
		 * values from the test data set and add them to the new array "entries". Set the final
		 * column to the predicted elements and write to the file */
		
		CSVWriter writer = new CSVWriter(new FileWriter("predictions" + k + ".csv"));
		
		writer.writeNext(headers);
		
		String[] entries = new String[12];
		int z = 0;
		for(String[] row : testData) {
			for (int i = 0; i < row.length; i++) {
				 entries[i] = row[i].replace("\"", "");
			}
			entries[11] = predictions[z];
			writer.writeNext(entries);
			z++;
		}
		
		writer.close();
		
		
		/* Create and show the confusion matrix*/
		
		confusionMatrix(actual, predictions, matrix);
		
		showMatrix(matrix);
		
		System.out.println();
		
		/* Get the performance indicators */
		
		getAccuracy(matrix);
		getSensitivity(matrix);
		getPrecision(matrix);
		getSpecificity(matrix);
		
}
	
	/* Using the euclidean algorithm to find the distance between two instances */
	
	public static double euclideanDistance(String[] a, String[] b){
		double distance = 0;
		
		for (int i = 1; i < a.length-1; i++) {
			distance += (Double.parseDouble(a[i]) - Double.parseDouble(b[i])) * (Double.parseDouble(a[i]) - Double.parseDouble(b[i]));
		}
		return Math.sqrt(distance);

	}
	
	/* This function returns an ArrayList of the K nearest neighbors 
	 * by looping through the training data set and compares 
	 * the distance between the new instance using the euclidean function.
	 * Then create a sorted map based on the distance with the key 
	 * being the instance of the patient and the value being the distance.
	 * Then add each instance to the ArrayList and return it
	 * */

	public static ArrayList<String[]> getNeighbors(List<String[]> trainingData, String[] testInstance, int k) {
		HashMap<String[],Double> rowDistance = new HashMap<String[],Double>();
		ArrayList<String[]> getNeighbours = new ArrayList<String[]>();
		ArrayList<String[]> kNearestNeighbours = new ArrayList<String[]>();
		double dist = 0;
		
		for (String[] row : trainingData) {
		    dist = euclideanDistance(row, testInstance);
		    rowDistance.put(row, dist);
		}
		
		Map<String[],Double> sortedMap = sortByComparator(rowDistance, ASC);
		
		getNeighbours = addNeighbours(sortedMap);
		
		for (int i = 0; i < k; i++) {
			kNearestNeighbours.add(i,(String[]) getNeighbours.get(i));
		}
		
		return kNearestNeighbours;
	 }

	/* Sort the map of patient instances and distances so that k nearest neighbors can be
	 * added to an ArrayList with the nearest neighbor being added first */
	
	public static Map<String[], Double> sortByComparator(Map<String[], Double> unsortMap, final boolean order){

        List<Entry<String[], Double>> list = new LinkedList<Entry<String[], Double>>(unsortMap.entrySet());

        // Sorting the list based on values
        Collections.sort(list, new Comparator<Entry<String[], Double>>()
        {
            public int compare(Entry<String[], Double> o1,
                    Entry<String[], Double> o2)
            {
                if (order)
                {
                    return o1.getValue().compareTo(o2.getValue());
                }
                else
                {
                    return o2.getValue().compareTo(o1.getValue());

                }
            }
        });

        // Maintaining insertion order with the help of LinkedList
        Map<String[], Double> sortedMap = new LinkedHashMap<String[], Double>();
        for (Entry<String[], Double> entry : list)
        {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
	
	/* Takes in a sorted map as parameter and adds the keys which are the 
	 * instances of the nearest k neighbors to an ArrayList and returns the ArrayList
	 * Now we have an ArrayList of instances with the k nearest neighbors */
	
	public static ArrayList<String[]> addNeighbours(Map<String[], Double> sortedMap) {
		 ArrayList<String[]> neighbours = new ArrayList<String[]>(10);
		 int counter = 0;
		 
			Iterator it = sortedMap.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry pair = (Map.Entry)it.next();
		        neighbours.add(counter,(String[]) pair.getKey());
		        counter++;
		    }
		    
		return neighbours;
		 
	 }
	
	/* Checks most common diagnosis of an instance by looping through the k
	 * nearest neighbor set and adding the class attribute to an array
	 * Then counts the occurrences of each attribute and return the most common */
	
	public static String getResponse(ArrayList<String[]> nearestNeighbours){
		 ArrayList<String> a = new ArrayList<>();
		 String diagnoses = "";
		
		 for (int i = 0; i < nearestNeighbours.size(); i++) {
			String [] z = nearestNeighbours.get(i);
			a.add(z[10]);
			
		}
		 
		 Map<String, Long> occurrences = a.stream().collect(Collectors.groupingBy(w -> w, Collectors.counting()));
		 		 
		 Map.Entry<String, Long> maxEntry = null;

		 for (Map.Entry<String, Long> entry : occurrences.entrySet())
		 {
		     if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) > 0)
		     {
		         maxEntry = entry;
		     }
		 }
		 		 
		 diagnoses = maxEntry.getKey();
		 
		 
		return diagnoses;
		 
	 }

	/* Accuracy of predictions for class malign */
	
	public static void getAccuracy(int[][] matrix) {
		double accuracy;
		double total = 207;
				
		accuracy = (matrix[0][0] + matrix[1][1])  / total * 100; 
		
		System.out.println("Accuracy: " + accuracy + "%");
		
	}
	
	/* Sensitivity = TA/(TA+FR) */
	
	public static void getSensitivity(int[][] matrix) {
		double sensitivity;
		double sum = matrix[0][0] + matrix[0][1];
		
		sensitivity = matrix[0][0] / sum; 
		
		System.out.println("Sensitivity: " + sensitivity);
		
	}
	
	/* Precision = TA/(TA+FA) */
	
	public static void getPrecision(int[][] matrix) {
		double precision;
		double sum = matrix[0][0] + matrix[1][1];
		
		precision = matrix[0][0] / sum; 
		
		System.out.println("Precision: " + precision);
		
	}
	
	/* Specificity = TR/(FA+TR) */
	
	public static void getSpecificity(int[][] matrix) {
		double specificity;
		double sum = matrix[1][0] + matrix[1][1];
		
		specificity = matrix[1][1] / sum; 
		
		System.out.println("Specificity: " + specificity);
		
	}
	
	/* Calculate the mean for each column */
	
	public static ArrayList<Double> columnMeans(List<String[]> dataset) {
		ArrayList<Double> means = new ArrayList<Double>();
		ArrayList<Double> colValues = new ArrayList<Double>();
		double meanValue;
		
		for (int i = 1; i < dataset.get(0).length-1; i++) {
			for(String [] rows : dataset) {
				colValues.add(Double.parseDouble(rows[i]));
				
			}
			meanValue = sumDouble(colValues) / ((double)dataset.size());
			means.add(meanValue);
			colValues.removeAll(colValues);
		}
		
		
		return means;
		
	}
	
	/* Calculate standard deviation for each column */
	
	public static ArrayList<Double> columnsStdevs(List<String[]> dataset, ArrayList<Double> means) {
		ArrayList<Double> stDevs = new ArrayList<Double>();
		ArrayList<Double> colValues = new ArrayList<Double>();
		double variance = 0;
		
		for (int i = 1; i < dataset.get(0).length-1; i++) {
			for(String [] rows : dataset) {
				variance += Math.pow(Double.parseDouble(rows[i]) - means.get(i-1),2); 
			}
			variance = variance / dataset.size();
			stDevs.add(Math.sqrt(variance));
			colValues.removeAll(colValues);
		}
		
		return stDevs;
		
	}
	
	/* Standardize data set */
	
	public static void standardizeDataset(List<String[]> dataset, ArrayList<Double> means, ArrayList<Double> stDevs) {
		for (String[] row : dataset) {
			for (int i = 1; i < row.length-1; i++) {
				row[i] = Double.toString((Double.parseDouble(row[i]) - means.get(i-1)) / stDevs.get(i-1));
			}
			
		}
	}
	
	/* Addition function */
	
	public static double sumDouble (List<Double> list) {
	    double sum = 0;
	    for (double i: list) {
	        sum += i;
	    }
	    return sum;
	}
	
	/* Fills the confusion matrix based on actual and predicted responses */
	
	public static void confusionMatrix(String[] actual, String[] predictions, int[][] matrix) {
		
		for (int i = 0; i < actual.length; i++) {
			if (actual[i].equals("malign")) {
				if (predictions[i].equals("malign")) {
					matrix[0][0] = matrix[0][0] + 1;
				} else {
					matrix[0][1] = matrix[0][1] + 1;
				}
				
			} else if (actual[i].equals("benign")) {
				if (predictions[i].equals("benign")) {
					matrix[1][1] = matrix[1][1] + 1;
				} else {
					matrix[1][0] = matrix[1][0] + 1;
				}
			} 
		}
	}
	
	/* Print the matrix (no labels) */
	
	public static void showMatrix(int[][] matrix) {
		System.out.println("Confusion Matrix");
		System.out.println();
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {
				System.out.print(" " + matrix[i][j] + "\t");
			}
			System.out.println();
		}
		
		
	}
	
	/* Add the actual results to an array for later comparison */
	
	public static void getActual(List<String[]> dataset, String[] actual) {
		int b = 0;
		for (String[] row : dataset) {
			actual[b] = row[row.length-1].replace("\"", "");
			b++;
		}
	}
	
}
