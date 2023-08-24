import { Objective as objectiveSpecification } from "./objective.dto"

export class ObjectiveData {
    numberOfElementsPerModule: number
    isUseWeightedSumMethod: boolean
    objectiveSpecifications: objectiveSpecification[]
}