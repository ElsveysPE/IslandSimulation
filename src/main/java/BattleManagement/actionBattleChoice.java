package BattleManagement;

import Organisms.Animals.Animal;
import Organisms.Animals.Behaviours.BattleConditions.BattleConditions;
import Organisms.Animals.Conditions.Conditions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static BattleManagement.BattleCell.getBattleNeighbours;
import static BattleManagement.BattleCell.isBorder;
import static Organisms.Animals.Behaviours.BattleConditions.BattleConditionEffects.*;

public class actionBattleChoice {
    private static final Logger logger = Logger.getLogger(actionBattleChoice.class.getName());

    public static List<Animal> initiativeOrder = new ArrayList<>();

    private static void disableRetreating(Animal animal) {
        if (animal != null && animal.battleConditions != null) {
            animal.battleConditions.remove(BattleConditions.RETREATING);
            logger.log(Level.FINE, "[Battle] Disabled RETREATING for Animal: {0}", animal);
        }
    }

    public static void chooseBattleAction(Animal actor) {
        // ... (initial null checks, initiativeOrder check, logger setup, pack setup, enemiesOnField calculation as before) ...
        String actorLogName = actor.toString(); // Assuming Animal.toString() is safe and doesn't use forbidden methods

        // Calculate adjacentEnemyCR
        // Pass the actual BattleMap that your battle instance is using
        HashSet<BattleCell> neighbouringCells = getBattleNeighbours(actor.getBattleCell());
        float adjacentEnemyCR = 0;
        List<String> adjacentEnemyNames = new ArrayList<>(); // For logging
        HashSet<Animal> enemiesOnField = new HashSet<>(); // <<< DECLARATION WAS MISSING HERE IN MY PREVIOUS SNIPPET
        if (initiativeOrder != null) { // Check initiativeOrder before iterating
            for (Animal combatant : initiativeOrder) {
                if (combatant == null || combatant.isDead() || combatant.getBattleCell() == null) continue;
                if (combatant.equals(actor)) continue;
                if (!combatant.getGroup().contains(combatant)) {
                    enemiesOnField.add(combatant);
                }
            }
        }
        for (Animal enemy : enemiesOnField) { // enemiesOnField calculated earlier
            if (enemy.getBattleCell() != null && neighbouringCells.contains(enemy.getBattleCell())) {
                adjacentEnemyCR += enemy.getDynamicCR();
                adjacentEnemyNames.add(enemy.toString());
            }
        }
        logger.log(Level.INFO, "[BattleChoice] Actor {0} has {1} adjacent enemies ({2}) with total CR: {3}",
                new Object[]{actorLogName, adjacentEnemyNames.size(), adjacentEnemyNames, adjacentEnemyCR});

        if (actor.battleConditions == null) {
            logger.log(Level.SEVERE, "[BattleChoice] Actor {0} has null battleConditions! Initializing.", actorLogName);
            actor.battleConditions = new HashSet<>();
        }

        // --- Main Decision Logic ---
        if (!actor.battleConditions.contains(BattleConditions.RETREATING)) {
            logger.log(Level.FINER, "[BattleChoice] Actor {0} is NOT currently RETREATING.", actorLogName);

            // Condition 1: Significantly Stronger
            if (adjacentEnemyCR < actor.getDynamicCR() * 0.6f && actor.getHealthPoints() > actor.getMaxHealth() / 5) {
                logger.log(Level.FINE, "[BattleChoice] Actor {0} evaluating 'Significantly Stronger'. AdjCR: {1}, ActorCR: {2}, Health: {3}%", new Object[]{actorLogName, adjacentEnemyCR, actor.getDynamicCR(), (int)((float)actor.getHealthPoints()/actor.getMaxHealth()*100)});
                if (adjacentEnemyCR >= actor.getDynamicCR()*0.6 && !enemiesOnField.isEmpty()) {
                    actor.setBattleAction(possibleBattleActions.MOVE_TO_ENEMY);
                } else if (adjacentEnemyCR < actor.getDynamicCR()*0.6) {
                    applyReckless(actor); // Your helper
                    actor.setBattleAction(possibleBattleActions.ATTACK);
                } else {
                    actor.setBattleAction(possibleBattleActions.STAND_GROUND);
                }
            }
            // Condition 2: Moderately Stronger
            else if (adjacentEnemyCR < actor.getDynamicCR() * 0.75f && actor.getHealthPoints() > actor.getMaxHealth() / 2) {
                logger.log(Level.FINE, "[BattleChoice] Actor {0} evaluating 'Moderately Stronger'. AdjCR: {1}, ActorCR: {2}, Health: {3}%", new Object[]{actorLogName, adjacentEnemyCR, actor.getDynamicCR(), (int)((float)actor.getHealthPoints()/actor.getMaxHealth()*100)});
                if (adjacentEnemyCR >= actor.getDynamicCR()*0.6 && !enemiesOnField.isEmpty()) {
                    actor.setBattleAction(possibleBattleActions.MOVE_TO_ENEMY);
                } else if (adjacentEnemyCR < actor.getDynamicCR()*0.6) {
                    actor.setBattleAction(possibleBattleActions.ATTACK);
                } else {
                    actor.setBattleAction(possibleBattleActions.STAND_GROUND);
                }
            }
            // Condition 3: Disadvantaged (Retreat logic)
            else if (actor.getHealthPoints() < actor.getMaxHealth() / 2 || adjacentEnemyCR > actor.getDynamicCR() * 0.75f) {
                logger.log(Level.FINE, "[BattleChoice] Actor {0} evaluating 'Disadvantaged/Low Health'. AdjCR: {1}, ActorCR: {2}, Health: {3}%", new Object[]{actorLogName, adjacentEnemyCR, actor.getDynamicCR(), (int)((float)actor.getHealthPoints()/actor.getMaxHealth()*100)});
                if (actor.battleConditions.contains(BattleConditions.RECKLESS)) {
                    disableReckless(actor); // Your helper
                }
                // --- CORRECTED USAGE OF isBorder ---
                if (isBorder(actor.getBattleCell())) { // Pass Battle.BattleMap
                    actor.setBattleAction(possibleBattleActions.RETREAT);
                    logger.log(Level.INFO, "[BattleChoice] Actor {0} chose: RETREAT (disadvantaged, at border).", actorLogName);
                } else {
                    applyRetreating(actor); // Your helper
                    actor.setBattleAction(possibleBattleActions.MOVE_OUT);
                    logger.log(Level.INFO, "[BattleChoice] Actor {0} chose: MOVE_OUT (disadvantaged, to retreat).", actorLogName);
                }
                // --- END CORRECTION ---
            }
            // Condition 4: Default/Balanced (Final else for non-retreating path)
            else {
                logger.log(Level.FINE, "[BattleChoice] Actor {0} evaluating 'Default/Balanced'. AdjCR: {1}, ActorCR: {2}, Health: {3}%", new Object[]{actorLogName, adjacentEnemyCR, actor.getDynamicCR(), (int)((float)actor.getHealthPoints()/actor.getMaxHealth()*100)});
                if (adjacentEnemyCR > 0) {
                    actor.setBattleAction(possibleBattleActions.ATTACK);
                } else if (!enemiesOnField.isEmpty()) {
                    actor.setBattleAction(possibleBattleActions.MOVE_TO_ENEMY);
                } else {
                    actor.setBattleAction(possibleBattleActions.STAND_GROUND);
                }
            }
        }
        // Logic if ALREADY RETREATING
        else {
            logger.log(Level.FINE, "[BattleChoice] Actor {0} IS ALREADY RETREATING.", actorLogName);
            if (!actor.conditions.contains(Conditions.PRONE) && !actor.conditions.contains(Conditions.INCAPACITATED)
                    && !actor.conditions.contains(Conditions.GRAPPLED)) {
                // --- CORRECTED USAGE OF isBorder ---
                if (isBorder(actor.getBattleCell())) { // Pass Battle.BattleMap
                    actor.setBattleAction(possibleBattleActions.RETREAT);
                    logger.log(Level.INFO, "[BattleChoice] Actor {0} chose: RETREAT (continuing retreat from border).", actorLogName);
                } else {
                    actor.setBattleAction(possibleBattleActions.MOVE_OUT);
                    logger.log(Level.INFO, "[BattleChoice] Actor {0} chose: MOVE_OUT (continuing retreat towards border).", actorLogName);
                }
                // --- END CORRECTION ---
            } else { // Is prone, incapacitated, or grappled WHILE retreating
                if (adjacentEnemyCR > 0) {
                    actor.setBattleAction(possibleBattleActions.ATTACK);
                    logger.log(Level.INFO, "[BattleChoice] Actor {0} chose: ATTACK (retreating but incapacitated & enemy adjacent).", actorLogName);
                } else {
                    actor.setBattleAction(possibleBattleActions.STAND_GROUND);
                    logger.log(Level.INFO, "[BattleChoice] Actor {0} chose: STAND_GROUND (retreating, incapacitated, no adjacent enemy).", actorLogName);
                }
            }
        }

        // Final log to see what was chosen
        if (actor.getBattleAction() == null) { // Should not happen if all paths set an action
            logger.log(Level.WARNING, "[BattleChoice] Actor {0} had NO action set by logic! Defaulting to STAND_GROUND.", actorLogName);
            actor.setBattleAction(possibleBattleActions.STAND_GROUND);
        }
        logger.log(Level.INFO, "[BattleChoice END] Actor {0} final action: {1}", new Object[]{actorLogName, actor.getBattleAction()});
    }
}

