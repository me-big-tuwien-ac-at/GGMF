import { GeneticAlgorithmParameter } from "../model/genetic-algorithm-parameter"
import { ParetoSetParameter } from "../model/pareto-set-parameter"
import { ConceptualModelParameter, ModelParameter } from "../model/conceptual-model-parameter"
import { EdgeWeight } from '../dto/edge-weight.dto'
import { Objective } from "../dto/objective.dto"
import { MutationWeight as MutationWeightDTO } from "../dto/mutation-weight.dto"
import { MutationWeight } from "../interface/mutation-weight.interface"
import { ObjectiveData } from "./objective-data.dto"
import { GeneticAlgorithmSelectionParameter } from "../model/genetic-algorithm-selection-parameter.model"

export class ModulariseConceptualModelDTO {
    private _chromosomeEncoding: string
    private _offspringSelector: string
    private _survivorSelector: string
    private _crossoverType: string
    private _mutationType: string
    private _countGeneration: number
    private _countPopulation: number
    private _mutationProbability: number
    private _crossoverProbability: number
    private _tournamentSize: number
    private _convergenceRate: number
    private _convergedGeneRate: number
    private _mutationWeight: MutationWeightDTO
    private _minimumParetoSetSize: number
    private _maximumParetoSetSize: number
    private _conceptualModelType: string
    private _metaModelType: string
    private _conceptualModelFile: File
    private _dtdFile: File
    private _edgeWeights: EdgeWeight[]
    private _isUseWeightedSumMethod: boolean
    private _objectives: Objective[]
    private _objectiveData: ObjectiveData

    constructor(
        geneticAlgorithmSelectionParameter: GeneticAlgorithmSelectionParameter,
        geneticAlgorithmParameter: GeneticAlgorithmParameter,
        paretoSetParameter: ParetoSetParameter,
        mutationWeightParamter: MutationWeight,
        conceptualModelParameter: ConceptualModelParameter,
        conceptualModelFile: File,
        dtdFile: File) {
            this._chromosomeEncoding = geneticAlgorithmSelectionParameter.chromosomeEncoding
            this._offspringSelector = geneticAlgorithmSelectionParameter.offspringSelector
            this._survivorSelector = geneticAlgorithmSelectionParameter.survivorSelector
            this._crossoverType = geneticAlgorithmSelectionParameter.crossoverType
            this._mutationType = geneticAlgorithmSelectionParameter.mutationType

            this._countGeneration = geneticAlgorithmParameter.numberOfGenerations
            this._countPopulation = geneticAlgorithmParameter.countPopulation
            this._mutationProbability = geneticAlgorithmParameter.mutationProbability
            this._crossoverProbability = geneticAlgorithmParameter.crossoverProbability
            this._convergenceRate = geneticAlgorithmParameter.convergenceRate
            this._convergedGeneRate = geneticAlgorithmParameter.convergedGeneRate
            this._tournamentSize = geneticAlgorithmParameter.tournamentSize

            this._minimumParetoSetSize = paretoSetParameter.minimumParetoSetSize
            this._maximumParetoSetSize = paretoSetParameter.maximumParetoSetSize

            this._mutationWeight = mutationWeightParamter

            this._metaModelType = ModelParameter.MetaModelType[conceptualModelParameter.metaModelType]
            this._conceptualModelType = ModelParameter.ConceptualModelType[conceptualModelParameter.conceptualModelType]

            this._edgeWeights = conceptualModelParameter.edgeWeights

            this._isUseWeightedSumMethod = conceptualModelParameter.objectiveData.isUseWeightedSumMethod
            this._objectives = conceptualModelParameter.objectiveData.objectives
            .map(objectiveModel => {
                var objectiveDTO = new Objective();
                objectiveDTO.objectiveType = objectiveModel.objectiveType
                objectiveDTO.selected = objectiveModel.isSelected
                objectiveDTO.weight = objectiveModel.objectiveWeight
                return objectiveDTO
            })
            this._objectiveData = new ObjectiveData()
            this._objectiveData.numberOfElementsPerModule = conceptualModelParameter.objectiveData.numberOfElementsPerModule
            this._objectiveData.isUseWeightedSumMethod = conceptualModelParameter.objectiveData.isUseWeightedSumMethod
            this._objectiveData.objectiveSpecifications = conceptualModelParameter.objectiveData.objectives
            .map(objectiveModel => {
                var objectiveDTO = new Objective();
                objectiveDTO.objectiveType = objectiveModel.objectiveType
                objectiveDTO.selected = objectiveModel.isSelected
                objectiveDTO.weight = objectiveModel.objectiveWeight
                return objectiveDTO
            })

            this._conceptualModelFile = conceptualModelFile
            this._dtdFile = dtdFile
    }
    
    get chromosomeEncoding(): string {
        return this._chromosomeEncoding
    }
    
    get offspringSelector(): string {
        return this._offspringSelector
    }
    
    get survivorSelector(): string {
        return this._survivorSelector
    }
    
    get crossoverType(): string {
        return this._crossoverType
    }
    
    get mutationType(): string {
        return this._mutationType
    }

    get countGeneration(): number {
        return this._countGeneration
    }
    
    get countPopulation(): number {
        return this._countPopulation
    }
    
    get mutationProbability(): number {
        return this._mutationProbability
    }
    
    get crossoverProbability(): number {
        return this._crossoverProbability
    }
    
    get tournamentSize(): number {
        return this._tournamentSize
    }
    
    get convergenceRate(): number {
        return this._convergenceRate
    }
    
    get convergedGeneRate(): number {
        return this._convergedGeneRate
    }
    
    get minimumParetoSetSize(): number {
        return this._minimumParetoSetSize
    }
    
    get maximumParetoSetSize(): number {
        return this._maximumParetoSetSize
    }
    
    get mutationWeight(): MutationWeightDTO {
        return this._mutationWeight
    }

    get conceptualModelType(): string {
        return this._conceptualModelType
    }
    
    get metaModelType(): string {
        return this._metaModelType
    }

    get conceptualModelFile(): File {
        return this._conceptualModelFile
    }

    get dtdFile(): File {
        return this._dtdFile
    }

    get edgeWeights(): EdgeWeight[] {
        return this._edgeWeights
    }

    get isUseWeightedSumMethod(): boolean {
        return this._isUseWeightedSumMethod
    }

    get objectiveData(): ObjectiveData {
        return this._objectiveData
    }

    get objectives(): Objective[] {
        return this._objectives
    }
}