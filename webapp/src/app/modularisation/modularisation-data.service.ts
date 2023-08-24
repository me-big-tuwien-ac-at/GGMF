import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { Observable } from 'rxjs';

import { ModulariseConceptualModelDTO } from "../dto/modularise-conceptual-model-dto"
import { Environment } from 'src/assets/environment';

@Injectable({
  providedIn: 'root'
})
export class ModularisationDataService {

  constructor(private httpClient: HttpClient) { }

      public sendGetModularisedConceptualRequest(modulariseConceptualModelDTO: ModulariseConceptualModelDTO): Observable<any> {
        const formData = this.createFormData(modulariseConceptualModelDTO)

        var url = Environment.backendUrl;
        if (this.isGraphMLType(modulariseConceptualModelDTO)) {
          url += "apply"
        } else if (this.isMetaModelType(modulariseConceptualModelDTO)) {
          url += "modularise"
        }

        return this.httpClient.post(
          url, 
          formData,
          { observe: 'response', responseType: 'blob' })
      }

      private createFormData(modulariseConceptualModelDTO: ModulariseConceptualModelDTO): FormData {
        var formData = new FormData()

        formData.append('chromosomeEncoding', String(modulariseConceptualModelDTO.chromosomeEncoding))
        formData.append('offspringSelector', String(modulariseConceptualModelDTO.offspringSelector))
        formData.append('survivorSelector', String(modulariseConceptualModelDTO.survivorSelector))
        formData.append('crossoverType', String(modulariseConceptualModelDTO.crossoverType))
        formData.append('mutationType', String(modulariseConceptualModelDTO.mutationType))

        formData.append('countPopulation', String(modulariseConceptualModelDTO.countPopulation))
        formData.append('mutationProbability', String(modulariseConceptualModelDTO.mutationProbability))
        formData.append('crossoverProbability', String(modulariseConceptualModelDTO.crossoverProbability))
        formData.append('tournamentSize', String(modulariseConceptualModelDTO.tournamentSize))

        formData.append('countGeneration', String(modulariseConceptualModelDTO.countGeneration))
        formData.append('convergenceRate', String(modulariseConceptualModelDTO.convergenceRate))
        formData.append('convergedGeneRate', String(modulariseConceptualModelDTO.convergedGeneRate))

        formData.append('minimumParetoSetSize', String(modulariseConceptualModelDTO.minimumParetoSetSize))
        formData.append('maximumParetoSetSize', String(modulariseConceptualModelDTO.maximumParetoSetSize))

        formData.append('mutationWeight', JSON.stringify(modulariseConceptualModelDTO.mutationWeight))

        formData.append('metaModelFile', modulariseConceptualModelDTO.conceptualModelFile)
        formData.append('conceptualModelType', modulariseConceptualModelDTO.conceptualModelType)
        formData.append('metaModelType', String(modulariseConceptualModelDTO.metaModelType))

        if (this.isAdoxxMetaModelType(modulariseConceptualModelDTO)) {
          formData.append('dtdFile', modulariseConceptualModelDTO.dtdFile)
        } else if (this.isGraphMLType(modulariseConceptualModelDTO)) {
          formData.append('graphmlFile', modulariseConceptualModelDTO.conceptualModelFile)
        } 

        if (modulariseConceptualModelDTO.edgeWeights != null) {
          formData.append('edgeWeights', JSON.stringify(modulariseConceptualModelDTO.edgeWeights))
        }

        formData.append('objectiveData', JSON.stringify(modulariseConceptualModelDTO.objectiveData))

        return formData
      }

      private isMetaModelType(modulariseConceptualModelDTO: ModulariseConceptualModelDTO): boolean {
        var fileExtension = this.getModularisationFileExtension(modulariseConceptualModelDTO)
        return fileExtension === "xml" || fileExtension === "archimate" || fileExtension === "uml"
      }

      private isGraphMLType(modulariseConceptualModelDTO: ModulariseConceptualModelDTO): boolean {
        var fileExtension = this.getModularisationFileExtension(modulariseConceptualModelDTO)
        return fileExtension === "graphml"
      }

      private isAdoxxMetaModelType(modulariseConceptualModelDTO: ModulariseConceptualModelDTO): boolean {
        var fileExtension = this.getModularisationFileExtension(modulariseConceptualModelDTO)
        return fileExtension === "xml"
      }

      private getModularisationFileExtension(modulariseConceptualModelDTO: ModulariseConceptualModelDTO): string {
        var fileName = modulariseConceptualModelDTO.conceptualModelFile.name

        return fileName.split('.').pop()
      }
}
