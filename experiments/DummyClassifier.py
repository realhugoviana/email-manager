from typing import Callable
import pandas as pd
import numpy as np
from sklearn.metrics import confusion_matrix, ConfusionMatrixDisplay, accuracy_score, precision_score, recall_score, precision_recall_fscore_support
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
        
    def transform(self, repeat:int = 10000, base_seed:int = 0, uniform:bool=False) -> np.ndarray[str]:
        """Function to sample ypred in the same shape of y a certain number of time following seed of base_seed + r for r in repeat

        Args:
            repeat (int, optional): Number of experiments with different seed. Defaults to 10000.
            base_seed (int, optional): Base seed. Defaults to 0.
            uniform (bool, optional): If uniform, then take a class at uniform random instead of weighted random. Default to False.

        Returns:
            np.ndarray[str]: A matrix containing the results of all the experiments.
        """
        # Initialise the list that will contains the results
        res_list:list[str] = []
        
        # For each experience, sample randomly following the probability distribution to get ypred(r) the vector of dimension dim(ypred) for the experiment r 
        for r in range(repeat):
            rng:np.random.RandomState = np.random.RandomState(base_seed + r)
            if not uniform:
                res_list.append(rng.choice(self.labs, size=self.support, replace=True, p=self.props).tolist())
            elif uniform:
                res_list.append(rng.choice(self.labs, size=self.support, replace=True).tolist())
            
        # Return the matrix
        return np.array(res_list)
    
    def fit_transform(self, y:np.ndarray[str], repeat:int = 10000, base_seed:int = 0, uniform:bool=False) -> np.ndarray[str]:
        """Apply fit and transform directly. See `fit` and `transform`

        Args:
            y (np.ndarray[str]): The real outcomes/
            repeat (int, optional): The number of experiments to make. Defaults to 10000.
            base_seed (int, optional): The base seed. Defaults to 0.
            uniform (bool, optional): If uniform, then take a class at uniform random instead of weighted random. Default to False.

        Returns:
            np.ndarray[str]: The matrix containing the result of all the experiments.
        """
        self.fit(y)
        return self.transform(repeat, base_seed, uniform=uniform)

USE_GROUPS:bool = False
GROUPS:dict[str,str] = {
    "NOA":"NOA",
    "EXP":"WORK",
    "EXT":"WORK",
    "PRO":"WORK",
    "RES":"WORK",
    "REV":"WORK",
    "STU":"WORK",
    "CRI":"CRITICAL",
    "ADM":"CRITICAL",
    "CON":"CRITICAL",
    "INS":"CRITICAL",
    "PER":"OTHER",
    "WRE":"OTHER"
}



if __name__ == "__main__":
    # Get the fake-inbox data
    df:pd.DataFrame = pd.read_csv("fake-inbox/categories.csv")
    y:np.ndarray[str] = df[["agentType"]].to_numpy(dtype=str).T[0]
    ypred_e1:np.ndarray[str] = df[["exp1"]].to_numpy(dtype=str).T[0]
    ypred_e2:np.ndarray[str] = df[["exp2"]].to_numpy(dtype=str).T[0]
    ypred_eNC:np.ndarray[str] = df[["expNC"]].to_numpy(dtype=str).T[0]
    
    # Regroup data if use group
    if USE_GROUPS:
        group_fnc:Callable = np.vectorize(GROUPS.get)
        y:np.ndarray[str] = group_fnc(y)
        ypred_e1:np.ndarray[str] = group_fnc(ypred_e1)
        ypred_e2:np.ndarray[str] = group_fnc(ypred_e2)
        ypred_eNC:np.ndarray[str] = group_fnc(ypred_eNC)
    
    # Initialise the dummy classifier
    dc:DummyClassifier = DummyClassifier()
    dcu:DummyClassifier = DummyClassifier()
    
    # Make 10 000 experiments
    repeat:int = 10_000
    res:np.ndarray[str] = dc.fit_transform(y, repeat=repeat)
    res_dcu:np.ndarray[str] = dcu.fit_transform(y, repeat=repeat, uniform=True)
    
    # Aggregate the results
    cf_matrices:np.ndarray[float] = np.array([confusion_matrix(y, res[r]).tolist() for r in range(repeat)]).sum(axis=0)/repeat
    cf_dcu_matrices:np.ndarray[float] = np.array([confusion_matrix(y, res_dcu[r]).tolist() for r in range(repeat)]).sum(axis=0)/repeat
    
    # Print the classification report
    precision, recall, _, support = np.array([np.array(precision_recall_fscore_support(y, res[r], labels=dc.labs, zero_division=0)).tolist() for r in range(repeat)]).sum(axis=0)/repeat
    print(
        "Accuracy :", round(sum(accuracy_score(y, res[r]) for r in range(repeat))/repeat, 4), 
        "\nLabels   :", *dc.labs,
        "\nPrecision:", *precision.round(4),
        "\nRecall   :", *recall.round(4),
        "\nSupport :", *support.round(4)
    )
        
    # Plot the results for dummy uniform
    ConfusionMatrixDisplay(cf_matrices, display_labels=dcu.labs).plot()
    plt.show()
    
    # Print the classification report
    precision, recall, _, support = np.array([np.array(precision_recall_fscore_support(y, res_dcu[r], labels=dcu.labs, zero_division=0)).tolist() for r in range(repeat)]).sum(axis=0)/repeat
    print(
        "Accuracy :", round(sum(accuracy_score(y, res_dcu[r]) for r in range(repeat))/repeat, 4), 
        "\nLabels   :", *dcu.labs,
        "\nPrecision:", *precision.round(4),
        "\nRecall   :", *recall.round(4),
        "\nSupport :", *support.round(4)
    )
            
    # Plot the results for dummy
    ConfusionMatrixDisplay(cf_dcu_matrices, display_labels=dcu.labs).plot()
    plt.show()
    
    # Plot Confusion for e1
    ConfusionMatrixDisplay(confusion_matrix(y,ypred_e1, labels=dc.labs), display_labels=dc.labs).plot()
    plt.show()
    precision, recall, _, support = precision_recall_fscore_support(y, ypred_e1, labels=dc.labs, zero_division=0)
    print(
        "Accuracy :", round(accuracy_score(y, ypred_e1), 4), 
        "\nLabels   :", *dc.labs,
        "\nPrecision:", *precision.round(4),
        "\nRecall   :", *recall.round(4),
        "\nSupport :", *support.round(4)
    )
    
    # Plot confusion for e2
    ConfusionMatrixDisplay(confusion_matrix(y,ypred_e2, labels=dc.labs), display_labels=dc.labs).plot()
    plt.show()
    precision, recall, _, support = precision_recall_fscore_support(y, ypred_e2, labels=dc.labs, zero_division=0)
    print(
        "Accuracy :", round(accuracy_score(y, ypred_e2), 4), 
        "\nLabels   :", *dc.labs,
        "\nPrecision:", *precision.round(4),
        "\nRecall   :", *recall.round(4),
        "\nSupport :", *support.round(4)
    )
       
    # Plot confusion for e3
    ConfusionMatrixDisplay(confusion_matrix(y,ypred_eNC, labels=dc.labs), display_labels=dc.labs).plot()
    plt.show()
    precision, recall, _, support = precision_recall_fscore_support(y, ypred_eNC, labels=dc.labs, zero_division=0)
    print(
        "Accuracy :", round(accuracy_score(y, ypred_eNC), 4), 
        "\nLabels   :", *dc.labs,
        "\nPrecision:", *precision.round(4),
        "\nRecall   :", *recall.round(4),
        "\nSupport :", *support.round(4)
    )