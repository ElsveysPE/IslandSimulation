using System;

[Serializable]
public class CellState
{
    public int x;
    public int y;
    public string terrainType;

    public override string ToString()
    {
        return $"Cell({x},{y}) - {terrainType}";
    }
}
