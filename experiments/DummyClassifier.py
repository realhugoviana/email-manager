import pandas as pd
import numpy as np
from sklearn.metrics import confusion_matrix, ConfusionMatrixDisplay
import matplotlib.pyplot as plt

class DummyClassifier:
    def __init__(self):
        """Initialise the class and its attributes
        """
        self.y:np.ndarray[str] = None
        self.support:int = None
        self.labs:list[str] = None
        self.props:np.ndarray[float] = None
        
    def fit(self, y:np.ndarray[str]) -> None:
        """Fit the dummy classifier from the proportion of occurring label

        Args:
            y (np.ndarray[str]): The list of real labels.
        """
        # Store the labels and the support (length of label)
        self.y:np.ndarray[str] = y
        self.support:int = y.shape[0]
        
        # Get the list of unique label and create a proportion array that will make a probability distribution of the labels
        self.labs:list[str] = list(sorted(list(set(self.y))))
        self.props:np.ndarray[float] = np.zeros(len(self.labs), dtype=float)
        
        # Get the probability distribution of each occurring label 
        for i, lab in enumerate(self.labs):
            self.props[i] = sum(self.y==lab)/self.support
        
    def transform(self, repeat:int = 10000, base_seed:int = 0) -> np.ndarray[str]:
        """Function to sample ypred in the same shape of y a certain number of time following seed of base_seed + r for r in repeat

        Args:
            repeat (int, optional): Number of experiments with different seed. Defaults to 10000.
            base_seed (int, optional): Base seed. Defaults to 0.

        Returns:
            np.ndarray[str]: A matrix containing the results of all the experiments.
        """
        # Initialise the list that will contains the results
        res_list:list[str] = []
        
        # For each experience, sample randomly following the probability distribution to get ypred(r) the vector of dimension dim(ypred) for the experiment r 
        for r in range(repeat):
            rng:np.random.RandomState = np.random.RandomState(base_seed + r)
            res_list.append(rng.choice(self.labs, size=self.support, replace=True, p=self.props).tolist())
        
        # Return the matrix
        return np.array(res_list)
    
    def fit_transform(self, y:np.ndarray[str], repeat:int = 10000, base_seed:int = 0) -> np.ndarray[str]:
        """Apply fit and transform directly. See `fit` and `transform`

        Args:
            y (np.ndarray[str]): The real outcomes/
            repeat (int, optional): The number of experiments to make. Defaults to 10000.
            base_seed (int, optional): The base seed. Defaults to 0.

        Returns:
            np.ndarray[str]: The matrix containing the result of all the experiments.
        """
        self.fit(y)
        return self.transform(repeat, base_seed)

if __name__ == "__main__":
    # Get the fake-inbox data
    df:pd.DataFrame = pd.read_csv("fake-inbox/categories.csv")
    y:np.ndarray[str] = df[["agentType"]].to_numpy(dtype=str).T[0]
    
    # Initialise the dummy classifier
    dc:DummyClassifier = DummyClassifier()
    
    # Make 10 000 experiments
    repeat:int = 10_000
    res:np.ndarray[str] = dc.fit_transform(y, repeat=repeat)
    
    # Aggregate the results
    cf_matrices:np.ndarray[float] = np.array([confusion_matrix(y, res[r]).tolist() for r in range(repeat)]).sum(axis=0)/repeat
    
    # Plot the results 
    ConfusionMatrixDisplay(cf_matrices, display_labels=dc.labs).plot()
    plt.show()