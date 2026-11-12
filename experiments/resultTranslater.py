import pandas as pd
import os

## Parameters
ROOT_PATH:str = "fake-inbox"
EML_PATH:str = "eml"
EXP_PATH:str = "results_exp_no_context.csv" # results_exp1.csv, results_exp2.csv or results_no_context.csv
STOP:str = False
NUMBER_OF_MAIL:int = 156

if __name__ == "__main__":
    # Import dataframe and get message_id/agent_name couple
    df:pd.DataFrame = pd.read_csv(f"{ROOT_PATH}/{EXP_PATH}")
    mails:list[str] = df[["message-id"]].to_numpy().ravel().tolist()
    classifications:list[str] = df[["agent-name"]].to_numpy().ravel().tolist()

    # Extract each eml files
    emls:dict[int,str] = {}
    for i in list(os.walk(f"{ROOT_PATH}/{EML_PATH}"))[0][2]:
        with open(f"{ROOT_PATH}/{EML_PATH}/{i}", encoding="utf-8") as file:
            emls[int(i.split("_")[0])] = file.read()

    # For each eml file, find its classification
    real_classification:dict[str,str] = {}
    checkunique:list[int] = []
    for i, mail in enumerate(mails):
        for eml_id, eml in emls.items():
            # If it is the correct eml, get the classification
            if f"Message-ID: {mail}" in eml:
                real_classification[f"{eml_id}"] = classifications[i]
                print(f"{eml_id}: {classifications[i]}")
                checkunique.append(eml_id)
                if STOP : input()

    # Check whether the classification was unique, otherwise, there is a problem
    if len(set(checkunique)) != len(checkunique): 
        print("Error")
    else:
        print("ok")
        # If no error, check all mail has a classification
        print(set(range(1,NUMBER_OF_MAIL+1))-set(map(int,real_classification.keys())))