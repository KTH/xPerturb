# Pipeline

## 0. Prerequisites
This part of the research utilizes the output derived from the previous section of the study found in `../correctness_attraction` which generates perturbed binaries from the different whiteboxes used in this study. 

When executing the pipeline bellow, the program expects to find perturbed binaries in a subfolder named `perturbations/`  under every whitebox folder like the filetree shows bellow. The perturbation binaries are named after the standard `wb_p_[PERTURBATION-PROBABILITY]_[PERTURBATION-POINT]`. For example the binary `wb_p_30_2200` is a perturbed whitebox with perturbation probability set to 30% at the perturbation point with index 2200 in the LLVM binary.

```
llvmperturb/
├── attacks/
│   ├── ...
│   ├── ...
│   └── attack.py
├── example_programs/
│   ├── wbs_aes_ches2016/
│   │   ├── ...
│   │   ├── src/
│   │   └── perturbations/
│   │       ├── wb_p_5_0
│   │       ├── wb_p_5_100
│   │       ├── wb_p_5_200
│   │       ├── ...
│   │       └── ...
│   ├── wbs_aes_kryptologik/
│   │   ├── ...
│   │   ├── src/
│   │   └── perturbations/
│   │       ├── wb_p_5_0
│   │       ├── wb_p_5_100
│   │       ├── wb_p_5_200
│   │       ├── ...
│   │       └── ...
│   └── wbs_aes_nsc2013_variants_generator/
│       ├── ...
│       ├── src/
│       └── perturbations/
│           ├── wb_p_5_0
│           ├── wb_p_5_100
│           ├── wb_p_5_200
│           ├── ...
│           └── ...
├── correctness_attraction/
└── ...
```

## 1. Run `python attack.py`

Carry out a complete attack with both deadpool, tracer and daredevil. Parsed
logs are saved into `logs/` and raw daredevil logs are saved into the folder `daredevilLogs/` just in case so the attacks does not have to be conducted once again if something messes up the parsed logs. Conducting the attacks is **very** time consuming!
If you want to running the kryptologik attack, the program need to have access to `DemoKey_table.bin` in the current working directory (because of how the kryptologik whitebox is implemented). Add this file to fit your setup: eg add it to the `attack` directory -> `cd` to `xPerturb/llvmPerturb/attacks` and `cp ../example_programs/wbs_aes_kryptologik/src/DemoKey_table.bin .` then `python attack.py`

(This step uses the module `targets.py`)

## 2. Run `python score.py`

In order to determine the success of an attack, a score is calculated. `score.py`
takes the parsed daredevil logs and prints the score of each attack to std-out.

The attack scores are printed in two sections separated by a blank line. The first section consists of the attack scores derived from attacking the original whitebox, two scores are calculated for every attack, hence two columns. The second part consists of an additional column telling us at which point the perturbations were inserted.

All attack scores derived from a specific whitebox type with a set perturbation probability are manually copied and passed from the output of the console to the `xPerturb/llvmPerturb/experiment_results/attack/` folder and placed in files named according to the following standard `[WHITEBOX-NAME]_attack_score_[PERTURBATION-PROBABILITY]`. eg: `chess_attack_score_10`

(Uses the module `attacks_statistics.py`)

## 3. Run `python atk_score_plt.py`

Plot the scores available in example the `experiment_results/attack/` folder

(Uses the module `populate.py`)
