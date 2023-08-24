import { Objective } from "../interface/objective.interface";

export class ObjectiveData
 {
    constructor(
        public numberOfElementsPerModule: number,
        public isUseWeightedSumMethod: boolean,
        public objectives: Objective[])
        { }    
}