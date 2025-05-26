package Organisms.Animals.Behaviours.Miscellaneous;

import Map.MapStructure;
import Organisms.Animals.Animal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface Social {
    // Logger for static methods within this interface
    static final Logger SOCIAL_LOGGER = Logger.getLogger(Social.class.getName());

    public static void expandPack(Animal groupHolder, Animal newMember) {
        if (groupHolder == null || newMember == null) {
            SOCIAL_LOGGER.log(Level.WARNING, "Cannot expand pack: groupHolder or newMember is null.");
            return;
        }
        if (groupHolder.equals(newMember)) {
            SOCIAL_LOGGER.log(Level.INFO, "Cannot expand pack: groupHolder ("+ groupHolder.getId() +") and newMember ("+ newMember.getId() +") are the same animal.");
            return;
        }

        Set<Animal> holderGroup = groupHolder.getGroup();
        Set<Animal> newMemberGroup = newMember.getGroup();

        if (holderGroup == null) {
            SOCIAL_LOGGER.log(Level.WARNING, "groupHolder (" + groupHolder.getId() + ")'s group is null. Cannot expand pack. Consider initializing it first.");
            return;
        }
        if (newMemberGroup == null) {
            SOCIAL_LOGGER.log(Level.WARNING, "newMember (" + newMember.getId() + ")'s group is null. Cannot expand pack. Consider initializing it first.");
            return;
        }

        SOCIAL_LOGGER.log(Level.INFO, "Expanding pack: Adding " + newMember.getId() + " ("+newMember.getClass().getSimpleName()+") to group context of " + groupHolder.getId() + " ("+groupHolder.getClass().getSimpleName()+").");

        // Snapshot of groupHolder's original members BEFORE adding the new member.
        Set<Animal> originalHolderMembers = new HashSet<>(holderGroup);

        // 1. groupHolder adds newMember
        if (holderGroup.add(newMember)) {
            SOCIAL_LOGGER.log(Level.FINER, "{0} ({1}) added to {2} ({3})'s group list.", new Object[]{newMember.getId(), newMember.getClass().getSimpleName(), groupHolder.getId(), groupHolder.getClass().getSimpleName()});
        }

        // 2. newMember adds groupHolder
        if (newMemberGroup.add(groupHolder)) {
            SOCIAL_LOGGER.log(Level.FINER, "{0} ({1}) added to {2} ({3})'s group list.", new Object[]{groupHolder.getId(), groupHolder.getClass().getSimpleName(), newMember.getId(), newMember.getClass().getSimpleName()});
        }

        // 3. newMember adds all original members of groupHolder's group (excluding groupHolder itself, already added)
        for (Animal originalMember : originalHolderMembers) {
            if (!originalMember.equals(groupHolder) && !originalMember.equals(newMember)) {
                if (newMemberGroup.add(originalMember)) {
                    SOCIAL_LOGGER.log(Level.FINER, "Original member {0} ({1}) added to {2} ({3})'s group list.", new Object[]{originalMember.getId(), originalMember.getClass().getSimpleName(), newMember.getId(), newMember.getClass().getSimpleName()});
                }
            }
        }

        // 4. All original members of groupHolder's group add newMember to their groups.
        for (Animal originalMember : originalHolderMembers) {
            Set<Animal> individualOriginalMemberGroup = originalMember.getGroup();
            if (individualOriginalMemberGroup != null) {
                if (!originalMember.equals(newMember)) {
                    if (individualOriginalMemberGroup.add(newMember)) {
                        SOCIAL_LOGGER.log(Level.FINER, "{0} ({1}) added to original member {2} ({3})'s group list.", new Object[]{newMember.getId(), newMember.getClass().getSimpleName(), originalMember.getId(), originalMember.getClass().getSimpleName()});
                    }
                }
            } else {
                SOCIAL_LOGGER.log(Level.WARNING, "Original member {0} ({1}) has a null group. Cannot add new member {2} to it.", new Object[]{originalMember.getId(), originalMember.getClass().getSimpleName(), newMember.getId()});
            }
        }
        SOCIAL_LOGGER.log(Level.INFO, "Pack expansion involving {0} ({1}) and {2} ({3}) completed.", new Object[]{groupHolder.getId(), groupHolder.getClass().getSimpleName(), newMember.getId(), newMember.getClass().getSimpleName()});
    }

    public static void shrinkPack(Animal groupHolder, Animal memberToRemove) {
        if (groupHolder == null || memberToRemove == null) {
            SOCIAL_LOGGER.log(Level.WARNING, "Cannot shrink pack: groupHolder or memberToRemove is null.");
            return;
        }
        if (groupHolder.equals(memberToRemove)) {
            SOCIAL_LOGGER.log(Level.INFO, "Cannot shrink pack: groupHolder ("+groupHolder.getId()+") and memberToRemove ("+memberToRemove.getId()+") are the same animal. If self-removal, handle directly on the animal's group.");
            return;
        }

        Set<Animal> holderGroup = groupHolder.getGroup();
        Set<Animal> memberToRemoveGroup = memberToRemove.getGroup();

        if (holderGroup == null) {
            SOCIAL_LOGGER.log(Level.WARNING, "groupHolder (" + groupHolder.getId() + ")'s group is null. Cannot shrink pack effectively.");
        }

        SOCIAL_LOGGER.log(Level.INFO, "Shrinking pack: Removing {0} ({1}) from affiliations related to {2} ({3})'s group context.", new Object[]{memberToRemove.getId(), memberToRemove.getClass().getSimpleName(), groupHolder.getId(), groupHolder.getClass().getSimpleName()});

        // 1. groupHolder removes memberToRemove (if its group exists and contains the member)
        if (holderGroup != null && holderGroup.remove(memberToRemove)) {
            SOCIAL_LOGGER.log(Level.FINER, "{0} ({1}) removed from {2} ({3})'s group list.", new Object[]{memberToRemove.getId(), memberToRemove.getClass().getSimpleName(), groupHolder.getId(), groupHolder.getClass().getSimpleName()});
        }

        // 2. memberToRemove removes groupHolder (if its group exists and contains the holder)
        if (memberToRemoveGroup != null && memberToRemoveGroup.remove(groupHolder)) {
            SOCIAL_LOGGER.log(Level.FINER, "{0} ({1}) removed from {2} ({3})'s group list.", new Object[]{groupHolder.getId(), groupHolder.getClass().getSimpleName(), memberToRemove.getId(), memberToRemove.getClass().getSimpleName()});
        }

        // 3. All other members currently in groupHolder's (snapshot) group also remove memberToRemove from their groups.
        if (holderGroup != null) {
            Set<Animal> currentHolderGroupMembersSnapshot = new HashSet<>(holderGroup);
            for (Animal remainingMemberInHolderGroup : currentHolderGroupMembersSnapshot) {
                if (remainingMemberInHolderGroup.equals(memberToRemove) || remainingMemberInHolderGroup.equals(groupHolder)) continue;

                Set<Animal> individualRemainingMemberGroup = remainingMemberInHolderGroup.getGroup();
                if (individualRemainingMemberGroup != null && individualRemainingMemberGroup.remove(memberToRemove)) {
                    SOCIAL_LOGGER.log(Level.FINER, "{0} ({1}) removed from {2} ({3})'s group list.", new Object[]{memberToRemove.getId(), memberToRemove.getClass().getSimpleName(), remainingMemberInHolderGroup.getId(), remainingMemberInHolderGroup.getClass().getSimpleName()});
                }
                if (memberToRemoveGroup != null && memberToRemoveGroup.remove(remainingMemberInHolderGroup)) {
                    SOCIAL_LOGGER.log(Level.FINER, "{0} ({1}) removed from {2} ({3})'s group list.", new Object[]{remainingMemberInHolderGroup.getId(), remainingMemberInHolderGroup.getClass().getSimpleName(), memberToRemove.getId(), memberToRemove.getClass().getSimpleName()});
                }
            }
        }
        SOCIAL_LOGGER.log(Level.INFO, "Pack shrinking involving {0} ({1}) and {2} ({3}) completed.", new Object[]{groupHolder.getId(), groupHolder.getClass().getSimpleName(), memberToRemove.getId(), memberToRemove.getClass().getSimpleName()});
    }



    public static int countOtherGroupMembersInCell(Animal subjectAnimal, MapStructure.Cell cell) {
        if (subjectAnimal == null || cell == null || subjectAnimal.getGroup() == null || subjectAnimal.getGroup().isEmpty()) {
            return 0;
        }

        Set<Animal> animalsInSameCell = cell.getAnimals();

        if (animalsInSameCell == null) {
            SOCIAL_LOGGER.log(Level.WARNING, "Cell [{0},{1}] returned null for getAnimals().", new Object[]{cell.getX(), cell.getY()});
            return 0;
        }

        int alliesCount = 0;
        for (Animal otherAnimalInCell : animalsInSameCell) {
            if (otherAnimalInCell.equals(subjectAnimal)) {
                continue;
            }
            if (subjectAnimal.getGroup().contains(otherAnimalInCell)) {
                alliesCount++;
            }
        }
        return alliesCount;
    }

    default void packTactics(Animal animalToBuff) {
        if (animalToBuff == null) {
            SOCIAL_LOGGER.log(Level.WARNING, "Cannot apply pack tactics: animalToBuff is null.");
            return;
        }
        if (animalToBuff.getCell() == null) {
            SOCIAL_LOGGER.log(Level.WARNING, "Animal {0} ({1}) cannot apply pack tactics: cell is null.", new Object[]{animalToBuff.getId(), animalToBuff.getClass().getSimpleName()});
            return;
        }

        int alliesInCellCount = countOtherGroupMembersInCell(animalToBuff, animalToBuff.getCell());

        if (alliesInCellCount > 0) {
            int currentAttackAdv = animalToBuff.getAttackAdv();
            animalToBuff.setAttackAdv(currentAttackAdv + alliesInCellCount);
            SOCIAL_LOGGER.log(Level.FINER, "Animal {0} ({1}) gained {2} attack advantage from pack tactics. Old AtkAdv: {3}, New AtkAdv: {4}",
                    new Object[]{animalToBuff.getId(), animalToBuff.getClass().getSimpleName(), alliesInCellCount, currentAttackAdv, animalToBuff.getAttackAdv()});
        }
    }
}