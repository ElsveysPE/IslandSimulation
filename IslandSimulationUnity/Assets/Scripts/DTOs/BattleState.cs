using System;
using System.Collections.Generic;

[Serializable]
public class BattleState
{
    public bool isActive = false;
    public int battleMapWidth;
    public int battleMapHeight;
    public List<CellState> battleMapGrid = new List<CellState>(); // Ініціалізуємо
    public List<AnimalState> combatants = new List<AnimalState>(); // Ініціалізуємо
    public long currentActorId;
    public List<string> recentBattleEvents = new List<string>(); // Ініціалізуємо
    public string battleOriginCoordinates;

    public override string ToString()
    {
        return $"Battle Active: {isActive}, Combatants: {combatants?.Count ?? 0}, ActorID: {currentActorId}";
    }
}