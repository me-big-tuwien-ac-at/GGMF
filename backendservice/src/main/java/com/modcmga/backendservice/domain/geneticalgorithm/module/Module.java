package com.modcmga.backendservice.domain.geneticalgorithm.module;
/**
 * @Package: com.modcmga.backendservice.domain.geneticalgorithm.domain
 * @Class: Module
 * @Author: Jan
 * @Date: 20.11.2021
 */

import com.modcmga.backendservice.model.modularisation.ModularisableElement;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * It contains the list and set of indices of the modularisable elements, which are assigned to this module.
 *
 * <p>
 *     Conceptually, the indices are in a linked list in ascended order. The last element of the linked list is the
 *     ending node. The ending node has the highest value in the module.
 * </p>
 *
 * <p>
 *     This module only operates on indices level.
 * </p>
 */
@Getter
public class Module {
    private final LinkedList<Integer> indices;

    /**
     * The set of indices, which is used to utilise the performance of sets e.g. check if an element contains an index.
     */
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private final Set<Integer> indicesMap;

    /**
     * Ctr.
     */
    public Module() {
        this.indices = new LinkedList<>();
        this.indicesMap = new HashSet<>();
    }

    /**
     * Adds the index to this module such that it is ordered.
     * @param index the index, which is added to this module
     */
    public void addIndex(int index) {
        this.indicesMap.add(index);
        
        if(this.indices.isEmpty()) {
            this.indices.add(index);
        } else if (this.indices.getLast() < index) {
            this.indices.addLast(index);
        } else if (this.indices.getFirst() > index) {
            this.indices.addFirst(index);
        } else {
            for (int i = 1; i < this.indices.size(); i++) {
                if (!this.indices.contains(index)) {
                    var predecessor = this.indices.get(i - 1);
                    var current = this.indices.get(i);

                    if (predecessor < index && index < current) {
                        this.indices.add(i, index);
                        return;
                    }
                }
            }
        }
    }

    /**
     * Adds a set of indices to this module while keeping the ascending order.
     * @param indices the set of indices to be added.
     */
    public void addIndices(final Set<Integer> indices) {
        indices.stream()
                .forEach(i -> addIndex(i));
    }

    /**
     * Removes the index from the module
     * @param index the index which will be removed from the module
     */
    public void removeIndex(int index) {
        this.indices.remove(Integer.valueOf(index));
        this.indicesMap.remove(index);
    }

    /**
     * Returns true if the index is in the module.
     * @param index The examined index
     * @return true if the index of the modularisable element is in the module.
     */
    public boolean isIndexInModule(int index) {
        return indicesMap.contains(index);
    }

    /**
     * Returns the allele value of the ending node of this module.
     * @return the allele value of the ending node of this module.
     */
    public int getAlleleOfEndingNode() {
        return indices.getLast();
    }

    @Override
    public String toString() {
        if (indices.isEmpty()) {
            return "";
        }

        var endingNode = this.indices.getLast();

        return String.format("%d: %s",
                endingNode,
                indices.stream()
                        .map(index -> index.toString())
                        .collect(Collectors.joining("->")));
    }

    @Override
    public int hashCode() {
        return getIndices()
                .stream()
                .mapToInt(i -> 31 * i.intValue())
                .sum();
    }

    @Override
    public boolean equals(Object o) {
        // Lombok's implementation does not work properly. Therefore, this is implemented in a custom fashion.
        if (!(o instanceof Module)) {
            return false;
        }

        Module otherModule = (Module) o;

        return this.indices.equals(otherModule.indices) && this.indicesMap.equals(otherModule.indicesMap);
    }
}
