using System;
using System.Collections.Generic;

[Serializable]
public class BattleState
{
    public bool isActive = false;
    public int battleMapWidth;
    public int battleMapHeight;
    public List<CellState> battleMapGrid = new List<CellState>(); // ����������
    public List<AnimalState> combatants = new List<AnimalState>(); // ����������
    public long currentActorId;
    public List<string> recentBattleEvents = new List<string>(); // ����������
    public string battleOriginCoordinates;

    public override string ToString()
    {
        return $"Battle Active: {isActive}, Combatants: {combatants?.Count ?? 0}, ActorID: {currentActorId}";
    }
}