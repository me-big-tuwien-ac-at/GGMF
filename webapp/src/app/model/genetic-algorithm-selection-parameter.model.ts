export class GeneticAlgorithmSelectionParameter {
    constructor(
        public chromosomeEncoding: string,
        public offspringSelector: string,
        public survivorSelector: string,
        public crossoverType: string,
        public mutationType: string
     ) { }
}