export class GeneticAlgorithmParameter {
    constructor(
        public numberOfGenerations: number,
        public countPopulation: number,
        public mutationProbability: number,
        public crossoverProbability: number,
        public convergenceRate: number,
        public convergedGeneRate: number,
        public tournamentSize: number)
        { }    
}