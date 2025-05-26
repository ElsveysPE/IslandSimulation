package Organisms.Animals.Management;

import Organisms.Animals.Animal;

import java.util.logging.Level;
import java.util.logging.Logger;


public class RecountCR {

    private static final Logger logger = Logger.getLogger(RecountCR.class.getName());

    // --- Tunable Weights & Constants (Adjust these to balance your game) ---
    private static final float W_HEALTH = 0.5f;            // Weight for each point of Effective Max Health
    private static final float W_AGILITY_DEF = 2.0f;       // Weight for Agility's defensive contribution
    private static final float W_CON_RESILIENCE = 1.0f;    // Weight for Constitution's resilience contribution
    private static final float W_ATTACK_STAT = 1.5f;       // Weight for the primary attack stat (STR or AGI)
    private static final float W_SPEED_UTIL = 0.3f;        // Weight for Speed's utility/minor offensive contribution

    private static final float POWER_HP_RATIO = 1.5f;  // Power for health ratio (e.g., 1.0 for linear, 1.5-2.0 for steeper drop)
    private static final float CR_SCALING_DIVISOR = 30.0f; // Overall scaler to bring CR to desired range (adjusted due to new CON factor)
    private static final float MIN_CR_ALIVE = 0.05f;       // Minimum CR for any creature with HP > 0
    private static final float MIN_PHYS_RES_CLAMP = 0.01f; // Minimum value for physRes to prevent division by zero/extreme EHP


    public static void recountCR(Animal animal) {
        if (animal == null) {
            logger.log(Level.WARNING, "Attempted to recount CR for a null animal.");
            return;
        }
        float mHP = animal.getMaxHealth();
        float cHP = animal.getHealthPoints();
        float pRES = animal.getPhysRes();
        float AGI = animal.getAgility();
        float CON = animal.getConstitution();
        float STR = animal.getStrength();
        float SPD = animal.getSpeed();


        // 1. Effective Max Health (effHP_m)
        // Lower physRes is better (it's a damage multiplier).
        // Clamp physRes to a minimum value.
        float clampedPhysRes = Math.max(MIN_PHYS_RES_CLAMP, pRES);
        float effHP_m = mHP / clampedPhysRes;
        effHP_m = Math.max(1.0f, effHP_m);

        if (mHP <= 0) {
            animal.setBasicCR(0.0f);
            animal.setDynamicCR(0.0f);
            logger.log(Level.FINE, "CR for animal " + animal.hashCode() + " set to 0 (max health is <= 0).");
            return;
        }
        // 2. Defensive Value component (dv_base) - Now includes Constitution
        float dv_base = (effHP_m * W_HEALTH) + (AGI * W_AGILITY_DEF) + (CON * W_CON_RESILIENCE);

        // 3. Determine Primary Attack Stat value (atk_stat_val)
        //this is simplified btw, usually animal uses the one that is bigger, but not always
        //remake it after alpha release
        float atk_stat_val = Math.max(STR, AGI);

        // 4. Offensive Value component (ov_base)
        float ov_base = (atk_stat_val * W_ATTACK_STAT) + (SPD * W_SPEED_UTIL);

        // 5. Calculate Raw Base Score (raw_base_score)
        float raw_base_score = dv_base + ov_base;

        // 6. Calculate baseCR
        float baseCR = raw_base_score;
        // baseCR should reflect potential, so it can be positive even if cHP is 0.
        // Apply MIN_CR_ALIVE floor if the calculated stats are very low but positive.
        if (baseCR < MIN_CR_ALIVE && baseCR > 0) { // Avoid applying MIN_CR_ALIVE if raw_base_score leads to 0 or negative
            baseCR = MIN_CR_ALIVE;
        } else if (baseCR <= 0) { // If calculation results in 0 or negative base, set to a very small positive or actual min.
            baseCR = MIN_CR_ALIVE; // Or some other sensible floor like 0.01f if all stats were 0.
            // For simplicity, using MIN_CR_ALIVE as the absolute floor for any potential.
        }


        animal.setBasicCR(baseCR);

        // --- Calculate DynamicCR ---

        // 1. Health Ratio (hp_ratio)
        float hp_ratio = cHP / mHP;
        hp_ratio = Math.max(0.0f, Math.min(1.0f, hp_ratio)); // Clamp between 0 and 1

        // 2. Health Modifier (hp_mod)
        float hp_mod = (float) Math.pow(hp_ratio, POWER_HP_RATIO); // If cHP is 0, hp_mod will be 0

        // 3. Calculate dynamicCR
        float dynamicCR = baseCR * hp_mod;

        // 4. Apply Floor for dynamicCR if alive, otherwise it's 0
        if (cHP > 0) {
            dynamicCR = Math.max(MIN_CR_ALIVE, dynamicCR);
        } else {
            dynamicCR = 0.0f; // Explicitly 0 if health is not positive
        }

        animal.setDynamicCR(dynamicCR);

        logger.log(Level.FINE, "CR recounted successfully for animal ID: " + animal.hashCode() +
                " | BaseCR: " + String.format("%.2f", baseCR) +
                " | DynamicCR: " + String.format("%.2f", dynamicCR) +
                " | (mHP:" + mHP + ", cHP:" + cHP + ", pRES:" + pRES +
                ", AGI:" + AGI + ", CON:" + CON + ", STR:" + STR + ", SPD:" + SPD + ")");
    }
}

