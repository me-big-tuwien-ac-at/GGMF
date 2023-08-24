import { EdgeWeight } from '../interface/edge-weight.interface'
import { ObjectiveData } from "../model/objective-data.model";

export class ConceptualModelParameter {
    constructor(
        public metaModelType: ModelParameter.MetaModelType,
        public conceptualModelType: ModelParameter.ConceptualModelType,
        public edgeWeights: EdgeWeight[],
        public objectiveData: ObjectiveData
    ) { }
}

export namespace ModelParameter {
    export enum MetaModelType {
        PAPYRUSUML,
        ARCHI,
        ADOXX
    }
    export enum ConceptualModelType {
        UML,
        ARCHIMATE,
        EPC,
        ER,
        OWL
    }
}