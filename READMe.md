# kNearest_Neighbour_Java

Here is some code for k-Nearest Neighbour algorithm in Java and works with datasets containing numeric input attributes. For this task I used the breast cancer dataset provided. The dataset contains real data related to patients which had a breast tumour which has been classified as malign (cancerous) or benign (non-cancerous). The dataset has the following attributes:

PID is the patient id. 
CLUMP_THICKNESS, CELL_SIZE, CELL_SHAPE, MARG_ADESION, EPIT_CELL_SIZE, BARE_NUCLEI, BLAND_CHROM, N_NUCLEOLI, MITOSES are input attributes whose values are numeric and indicate various measures related to the tumour's cells. 
CLASS is the output attribute with the values benign and malign.

The dataset was randomly split in a training dataset containing 70% of the data, and a test dataset containing 30% of the data, and then the rows containing missing values were removed to simplify this task. 
The code reads from the keyboard the number of nearest neighbours k. The code also reads the training and the test dataset files, and rescale's the numeric input attributes to make sure there is no bias in their contribution to the distances computed in the k-Nearest Neighbour algorithm. 

The code then classifies any patients from the test dataset using the k-nearest neighbour algorithm. The code then saves the test dataset and the computed classes for these patients in a comma separated values file called predictions.csv. Moreover, the code computes and displays on the screen the value of k, and the confusion matrix and the following performance indicators: accuracy, sensitivity, precision and specificity with respect to the class 'malign'.
