export interface GeneticAlgorithmSelection {
    chromosomeEncoding: string
    singleObjectiveOffspringSelector: string[]
    multiObjectiveOffspringSelector: string[]
    singleObjectiveSurvivorSelector: string[]
    multiObjectiveSurvivorSelector: string[]
    survivorSelection: string[]
    crossovers: string[]
    mutations: string[]
}