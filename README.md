[![Travis Build Status](https://api.travis-ci.org/Spirals-Team/jPerturb.svg?branch=master)](https://travis-ci.org/Spirals-Team/jPerturb)

# jPerturb : a state perturbation tool for Java.

## Download & Install

To retrieve the project:
```
git clone https://github.com/Spirals-Team/jPerturb
```

To install and run test:
```
mvn test
```

## Usage

To process and inject perturbation points to the resources classes used for test.

```
java -jar target/jPerturb-0.0.1-SNAPSHOT-jar-with-dependencies.jar -type IntNum:boolean -i src/test/resources/ -o target/trash/
```

Process and inject perturbation to the resources classes used for test with rename.

```
java -jar target/jPerturb-0.0.1-SNAPSHOT-jar-with-dependencies.jar -r -type IntNum:boolean -i src/test/resources/ -o target/trash/
```

To perform the correctness attraction analysis:

```
mvn exec:java -Dexec.mainClass="experiment.Main2" -Dexec.args="-v -s quicksort.QuickSortManager -nb 10 -size 10 -exp call one"
```

## Experiments

You can find code of our experiments at [jPerturb-experiments](http://github.com/Spirals-Team/jPerturb-experiments.git).
