# GGMF
## Prerequisites
The following prerequisites are needed to run the application:
- Docker\
For building the container, hosting the backend application.
- Angular 7+\
For building and running the web application locally.

## Application
At first clone the repository using the *git clone* command.

The application can be found in the folder *02\. Application* folder. This folder consists of two subfolders *backendservice* and *webapp*. *backendservice* is the backend application, which is responsible for modularising conceptual models. *webapp* is the web application that serves as the client for the backend, which calls the backend services.\

To run the complete application, the following steps must be done:

1. Change directory to *02\. Application*
2. Start Docker. Run *docker-compose build* in the current directory *02\. Application*. This builds the docker container.
3. After building, run *docker-compose up* to run the backend application in the container.
3. Change directory to *webapp*
4. Execute ng build
5. Execute ng serve
6. In an internet browser, such as firefox, open [http://localhost:4200](http://localhost:4200) to access the web application.

## Parameters & Settings
The following table shows the possible parameters and settings that can be changed in the UI.

|Type | Name | Explanation |
| --- | :--- | :--- |
| Genetic Algorithm Parameter| Population Size| The size of solutions in the current generation. |
| Genetic Algorithm Parameter| Mutation Probability | The probability of mutating a chromosome and changing a modularisation<br/>solution of a knowledge graph. |
| Genetic Algorithm Parameter| Crossover Probability| The probability of starting the crossover process for 2 modularisation solutions.<br/>During this process modularisation informations are exchanged while preserving<br/>the linear linkage encoding constraints. |
| Genetic Algorithm Parameter| Tournament Size | The number of chromosomes who compete in a tournament and are selected for<br/>the alteration process. The winner is determined by the best fitness.|
| Termination| Number of Generations | The number of iterations until the genetic algorithms terminates. |
| Termination| Converged Gene Rate | TODO |
| Pareto Set Parameter| Minimum Pareto Set Size | The possible minimum size of Pareto optimal solutions set (The Pareto set size can<br/>be lower than the given minimum size if the Pareto set contains duplicate<br/> solutions. Duplicate solutions are removed). |
| Pareto Set Parameter| Maximum Pareto Set Size | The possible maximum size of Pareto optimal solutions set. |
| Mutation Parameter| Split Module Weight | The weight of splitting the random module during the mutation process (Higher<br/>weights increases the probability of splitting modules). |
| Mutation Parameter| Combine Modules Weight | The weight of combining the random module during the mutation process (Higher<br/>weights increases the probability of combining modules). |
| Mutation Parameter| Move Elements Between Modules Weight | The weight of moving a modularisable element from one module to another<br/>module during the mutation process (Higher weights increases the probability of<br/>moving modularisable elements). |
| Conceptual Model| Conceptual Model Type | The conceptual model type of the file which is uploaded. |
| Conceptual Model| Meta Model Type | The meta model type of the file which is uploaded. |
| Conceptual Model| Use custom edge weights | The flag to determine if the edge weights should customised. If this is true, table<br/>with edges and weights is displayed. |
| Conceptual Model| Edge Weights | The table of edges and their weights. The weights for the type of edges is used for<br/>the objectives that utilise information from edges. |
| Objectives| Use weighted sum method | The flag to determine if the weighted sum method is used for the list of objectives. |
| Objectives| Objectives | The table of objectives used to determine the fitness of a modularisation solution. |
| File| Concpetual Model File | The file containing the conceptual model. |