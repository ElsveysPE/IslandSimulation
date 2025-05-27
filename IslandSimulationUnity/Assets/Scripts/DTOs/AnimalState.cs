using System;
using System.Collections.Generic;

[Serializable]
public class AnimalState
{
    // Identification & Type
    public long id;
    public string speciesType;
    public bool isFemale;

    // Position
    public int x;
    public int y;
    // verticalPosition було видалено з Java DTO

    // Core Stats & Status
    public int healthPoints;
    public int maxHealth;
    public string healthStatus; // e.g., "HEALTHY", "INJURED"
    public bool isDead;

    // Action & Movement Points (World)
    public int actionPoints; // Current world AP
    public float currentMovementPoints;
    public float maxMovementPoints;

    // Battle-Specific Stats
    public string currentBattleAction;
    public string currentAction;

    // Conditions & States
    // У Java параметр конструктора був "Conditions", але поле називається "conditions".
    // У JSON, ймовірно, буде "conditions" (з маленької).
    public List<string> conditions = new List<string>();
    public List<string> battleConditions = new List<string>();
    public bool hasEatenThisTick;
    public int deepRestPoints;

    // Key Attributes for Display
    public int size;
    public int agility;
    public int constitution;
    public int strength;
    public int speed;
    public int perception; // Base perception
    public int stealth;    // Base stealth
    public int currentStealthValue; // Effective stealth if different from base
    public float physicalResistance;

    // Grouping/Social
    public List<long> group = new List<long>(); // List<Long> в Java -> List<long> в C#

    public override string ToString()
    {
        return $"{speciesType} ID:{id} ({x},{y}) HP:{healthPoints}/{maxHealth}";
    }
}
