import { Component, OnInit, ElementRef, ViewChild } from '@angular/core';

import { GeneticAlgorithmParameter } from '../model/genetic-algorithm-parameter'
import { ParetoSetParameter } from '../model/pareto-set-parameter'
import { ConceptualModelParameter } from '../model/conceptual-model-parameter'
import { ModelParameter } from '../model/conceptual-model-parameter'
import { EdgeWeight } from '../interface/edge-weight.interface'
import { Objective } from '../interface/objective.interface';
import { ModulariseConceptualModelDTO } from '../dto/modularise-conceptual-model-dto'
import { ModularisationDataService } from "../modularisation/modularisation-data.service"

import { MatTableDataSource } from '@angular/material/table';

import { saveAs } from 'file-saver';
import { SpinnerService } from '../shared/spinner.service';
import { AppConfig } from '../app.config.service';
import { DropDownElement } from '../interface/drop-down-element.interface';
import { MutationWeight } from '../interface/mutation-weight.interface';
import { ObjectiveData } from '../model/objective-data.model';
import { ToolTips } from '../interface/tooltip.interface';
import { GeneticAlgorithmSelection } from '../interface/genetic-algorithm-selection.interface';
import { MetaModel } from '../interface/conceptual-model.interface';
import { GeneticAlgorithmSelectionParameter } from '../model/genetic-algorithm-selection-parameter.model';

@Component({
  selector: 'app-modularisation',
  templateUrl: './modularisation.component.html',
  styleUrls: ['./modularisation.component.css']
})
export class ModularisationComponent implements OnInit {
  @ViewChild('add-edge-weight-name') addEdgeWeightTextField: ElementRef;

  readonly spinnerService: SpinnerService

  selectedGeneticAlgorithmSelection: GeneticAlgorithmSelection;
  readonly geneticAlgorithmSelections: GeneticAlgorithmSelection[];
  private selectedSingleObjectiveOffspringSelectorIndex: number;
  private selectedMultiObjectiveOffspringSelectorIndex: number;
  private selectedSingleObjectiveSurvivorSelectorIndex: number;
  private selectedMultiObjectiveSurvivorSelectorIndex: number;
  private selectedCrossoverIndex: number;
  private selectedMutationIndex: number;

  geneticAlgorithmParameter: GeneticAlgorithmParameter;
  paretoSetParameter: ParetoSetParameter;

  readonly metaModels: MetaModel[];
  selectedMetaModel: MetaModel;
  selectedConceptualModel: String;
  isADOxxSelected: boolean;
  
  readonly displayedEdgeWeightColumns: string[];
  readonly edgeWeightDataSource: MatTableDataSource<EdgeWeight>;

  isUseWeightedSumMethod: boolean;
  readonly displayedObjectiveColumns: string[];
  readonly objectivesDataSource: Objective[];

  readonly mutationWeightParameter: MutationWeight;

  isUseCustomEdgeWeights: boolean;
  isShowEdgeWeightTable: boolean;

  numberOfElementsPerModule: number;
  isShowObjectivesTable: boolean;

  modularisationFileToUpload: File | null = null;
  dtdFileToUpload: File | null = null;

  tooltips: ToolTips;

  constructor(
    private modularisationDataService: ModularisationDataService,
    spinnerService: SpinnerService
  ) { 
    this.spinnerService = spinnerService

    this.geneticAlgorithmSelections = AppConfig.settings.geneticAlgorithmSelections;
    this.selectedGeneticAlgorithmSelection = this.geneticAlgorithmSelections[0]

    this.edgeWeightDataSource = new MatTableDataSource<EdgeWeight>(AppConfig.settings.edgeWeights); 
    this.displayedEdgeWeightColumns = AppConfig.settings.displayedEdgeWeightColumns;
    this.displayedObjectiveColumns = AppConfig.settings.displayedObjectiveColumns;

    this.isUseWeightedSumMethod = AppConfig.settings.isUseWeightedSumMethod;
    this.objectivesDataSource = AppConfig.settings.objectives;

    this.metaModels = AppConfig.settings.metaModels;
    this.selectedMetaModel = this.metaModels[AppConfig.settings.uiSettings.selectedConceptualModelIndex]
    this.selectedConceptualModel = this.selectedMetaModel.conceptualModels[0]
    this.isADOxxSelected = this.isADOxxFileSelected()

    this.geneticAlgorithmParameter = AppConfig.settings.initialGeneticAlgorithmParameter;
    this.paretoSetParameter = AppConfig.settings.initialParetoSetParameter;
    this.mutationWeightParameter = AppConfig.settings.initialMutationWeightParameter;

    this.isUseCustomEdgeWeights = AppConfig.settings.uiSettings.isUseCustomEdgeWeights;
    this.isShowEdgeWeightTable = AppConfig.settings.uiSettings.isShowEdgeWeightTable;

    this.numberOfElementsPerModule = AppConfig.settings.numberOfElementsPerModule
    this.isShowObjectivesTable = AppConfig.settings.uiSettings.isShowObjectivesTable;

    this.tooltips = AppConfig.settings.tooltips;

    this.selectedSingleObjectiveOffspringSelectorIndex = 0;
    this.selectedMultiObjectiveOffspringSelectorIndex = 0;
    this.selectedSingleObjectiveSurvivorSelectorIndex = 0;
    this.selectedMultiObjectiveSurvivorSelectorIndex = 0;
    this.selectedCrossoverIndex = 0;
    this.selectedMutationIndex = 0;
  }

  ngOnInit() {
  }

  public offspringSelection(): string[] {
    if (this.isUseWeightedSumMethod) {
      return this.selectedGeneticAlgorithmSelection.singleObjectiveOffspringSelector
    }
    return this.selectedGeneticAlgorithmSelection.multiObjectiveOffspringSelector
  }

  public survivorSelection(): string[] {
    if (this.isUseWeightedSumMethod) {
      return this.selectedGeneticAlgorithmSelection.singleObjectiveSurvivorSelector
    }
    return this.selectedGeneticAlgorithmSelection.multiObjectiveSurvivorSelector
  }

  public onChangeGeneticAlgorithmSelection(selectedIndex: number): void {
    this.selectedGeneticAlgorithmSelection = this.geneticAlgorithmSelections[selectedIndex]
    this.selectedCrossoverIndex = 0
    this.selectedMutationIndex = 0
  }
  
  public onChangeOffspringSelection(selectedIndex: number): void {
    if (this.isUseWeightedSumMethod) {
      this.selectedSingleObjectiveOffspringSelectorIndex = selectedIndex
    } else {
      this.selectedMultiObjectiveOffspringSelectorIndex = selectedIndex
    }
  }
  
  public onChangeSurvivorSelection(selectedIndex: number): void {
    if (this.isUseWeightedSumMethod) {
      this.selectedSingleObjectiveSurvivorSelectorIndex = selectedIndex
    } else {
      this.selectedMultiObjectiveSurvivorSelectorIndex = selectedIndex
    }
  }

  public onChangeCrossoverSelection(selectedIndex: number): void {
    this.selectedCrossoverIndex = selectedIndex
  }

  public onChangeMutationSelection(selectedIndex: number): void {
    this.selectedMutationIndex = selectedIndex
  }

  public onChangeMetaModel(selectedIndex: number): void {
    this.selectedMetaModel = this.metaModels[selectedIndex]
    this.onChangeConceptualModel(0)

    this.isADOxxSelected = this.isADOxxFileSelected()
  }

  public onChangeConceptualModel(selectedIndex: number): void {
    this.selectedConceptualModel = this.selectedMetaModel.conceptualModels[selectedIndex]
  }

  public hideEdgeWeightTable(showEdgeWeightButton) {
    showEdgeWeightButton.textContent = this.isShowEdgeWeightTable ? "Show" : "Hide"
    this.isShowEdgeWeightTable = !this.isShowEdgeWeightTable
  }

  public deleteEdgeWeight(button, index) {
    const edgeWeights = this.edgeWeightDataSource.data

    if (edgeWeights.length > 1) {
      edgeWeights.splice(index, 1)

      this.edgeWeightDataSource.data = edgeWeights
    }
  }

  public addEdgeWeight(event, edgeWeightName, edgeWeight) {
    if (edgeWeightName.length > 0 && edgeWeight.length > 0) {
      const edgeWeights = this.edgeWeightDataSource.data

      const newEdgeweight: EdgeWeight = { name: edgeWeightName, weight: parseInt(edgeWeight) }

      edgeWeights.push(newEdgeweight)

      this.edgeWeightDataSource.data = edgeWeights
    }
  }

  public hideObjectivesTable(showObjectivesButton) {
    showObjectivesButton.textContent = this.isShowObjectivesTable ? "Show" : "Hide"
    this.isShowObjectivesTable = !this.isShowObjectivesTable
  }

  public checkNumberOfObjectivesSelected(checkbox, index) {
    const selectedObjective = this.objectivesDataSource.filter(objective => objective.isSelected)
    if (selectedObjective.length == 0) {
      checkbox.checked = true
    }
  }

  public handleModularisationFileInput(files: FileList): void {
    this.modularisationFileToUpload = files.item(0)
  }

  public isModelTypeRequired(): boolean {
    if (this.modularisationFileToUpload == null) {
      return true
    }

    const fileExtension = this.getModularisationFileExtension()
    return fileExtension != "graphml" 
  }

  private getModularisationFileExtension(): string {
    const fileName = this.modularisationFileToUpload.name

    return fileName.split('.').pop();
  }

  public isADOxxFileSelected(): boolean {
    const metaModelType = this.getSelectedMetaModel()
    return metaModelType == ModelParameter.MetaModelType.ADOXX
  }  
  
  public handleDtdFileInput(files: FileList): void {
    this.dtdFileToUpload = files.item(0)
  }

  public modularise(): void {
    const chromosomeEncoding = this.selectedGeneticAlgorithmSelection.chromosomeEncoding
    var offspringSelector: string;
    var survivorSelector: string

    if (this.isUseWeightedSumMethod) {
      // Single objective
      offspringSelector = this.selectedGeneticAlgorithmSelection.singleObjectiveOffspringSelector[this.selectedSingleObjectiveOffspringSelectorIndex]
      survivorSelector = this.selectedGeneticAlgorithmSelection.singleObjectiveSurvivorSelector[this.selectedSingleObjectiveSurvivorSelectorIndex]
    } else {
      // Multi objective
      offspringSelector = this.selectedGeneticAlgorithmSelection.multiObjectiveOffspringSelector[this.selectedMultiObjectiveOffspringSelectorIndex]
      survivorSelector = this.selectedGeneticAlgorithmSelection.multiObjectiveSurvivorSelector[this.selectedMultiObjectiveSurvivorSelectorIndex]
    }

    const crossoverType = this.selectedGeneticAlgorithmSelection.crossovers[this.selectedCrossoverIndex]
    const mutationType = this.selectedGeneticAlgorithmSelection.mutations[this.selectedMutationIndex]

    const geneticAlgorithmSelectionParameter = new GeneticAlgorithmSelectionParameter(
      chromosomeEncoding,
      offspringSelector,
      survivorSelector,
      crossoverType,
      mutationType
    )

    const selectedObjectives = this.objectivesDataSource
    .filter(objective => objective.isSelected);
    const objectiveData = new ObjectiveData(this.numberOfElementsPerModule, this.isUseWeightedSumMethod, selectedObjectives);

    const isCustomEdgeWeightsSelected = this.isUseCustomEdgeWeights ? this.edgeWeightDataSource.data : null;
    const conceptualModelParameter = new ConceptualModelParameter(
      this.getSelectedMetaModel(), 
      this.getSelectedConceptualModel(), 
      isCustomEdgeWeightsSelected,
      objectiveData);
    
    const modulariseConceptualModelDTO = new ModulariseConceptualModelDTO(
      geneticAlgorithmSelectionParameter,
      this.geneticAlgorithmParameter, 
      this.paretoSetParameter, 
      this.mutationWeightParameter,
      conceptualModelParameter, 
      this.modularisationFileToUpload,
      this.dtdFileToUpload);

      this.modularisationDataService.sendGetModularisedConceptualRequest(modulariseConceptualModelDTO).subscribe({
        next: (response) => {
          this.promptSaveFileAsDialog(new Blob([response.body], { type: 'application/zip' }))
        },
        error: (error) => {          
          console.log(error)
        }
      });
  }

  private getSelectedMetaModel(): ModelParameter.MetaModelType {
    var metaModelType: ModelParameter.MetaModelType
    switch(this.selectedMetaModel.label) {
      case "Papyrus UML": {
        metaModelType = ModelParameter.MetaModelType.PAPYRUSUML
        break
      }
      case "Archi": {
        metaModelType = ModelParameter.MetaModelType.ARCHI
        break
      }
      case "ADOxx": {
        metaModelType = ModelParameter.MetaModelType.ADOXX
        break
      }
    }
    
    return metaModelType
  }

  private getSelectedConceptualModel(): ModelParameter.ConceptualModelType {
    var conceptualModelType: ModelParameter.ConceptualModelType
    switch(this.selectedConceptualModel) {
      case "UML": {
        conceptualModelType = ModelParameter.ConceptualModelType.UML
        break
      }
      case "Archimate": {
        conceptualModelType = ModelParameter.ConceptualModelType.ARCHIMATE
        break
      }
      case "EPC": {
        conceptualModelType = ModelParameter.ConceptualModelType.EPC
        break
      }
      case "ER": {
        conceptualModelType = ModelParameter.ConceptualModelType.ER
        break
      }
      case "OWL": {
        conceptualModelType = ModelParameter.ConceptualModelType.OWL
        break
      }
    }
    return conceptualModelType
  }

  private promptSaveFileAsDialog(modularisedConceptualModelResponseFileAsBlob: Blob) {
    const nowDateTime = new Date().toLocaleString("en-GB");
    saveAs(modularisedConceptualModelResponseFileAsBlob, `${nowDateTime}.zip`)
  }
}