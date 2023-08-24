import { GeneticAlgorithmSelection } from './genetic-algorithm-selection.interface'
import { EdgeWeight } from './edge-weight.interface'
import { Objective } from './objective.interface'
import { GeneticAlgorithmParameter } from '../model/genetic-algorithm-parameter'
import { ParetoSetParameter } from '../model/pareto-set-parameter'
import { UISettings } from './ui-settings.interface'
import { MutationWeight } from './mutation-weight.interface'
import { ToolTips } from './tooltip.interface'
import { MetaModel } from './conceptual-model.interface'

export interface AppConfigSetting {
    geneticAlgorithmSelections: GeneticAlgorithmSelection[],
    displayedEdgeWeightColumns: string[],
    edgeWeights: EdgeWeight[]
    displayedObjectiveColumns: string[],
    numberOfElementsPerModule: number,
    isUseWeightedSumMethod: boolean,
    objectives: Objective[],
    metaModels: MetaModel[],
    initialGeneticAlgorithmParameter: GeneticAlgorithmParameter,
    initialParetoSetParameter: ParetoSetParameter,
    initialMutationWeightParameter: MutationWeight,
    uiSettings: UISettings,
    tooltips: ToolTips
}