using System;
using System.Collections.Generic;

[Serializable]
public class SimState
{
    public int tick;
    public string dayTime;
    public int worldMapWidth;
    public int worldMapHeight;
    public List<CellState> worldMapGrid = new List<CellState>(); // ����������
    public List<AnimalState> animalsOnWorldMap = new List<AnimalState>(); // ����������
    public BattleState currentBattle = new BattleState(); // ���������� (���������� ���)

    public override string ToString()
    {
        return $"Tick: {tick}, Day: {dayTime}, Animals: {animalsOnWorldMap?.Count ?? 0}, Battle: {currentBattle?.isActive ?? false}";
    }
}