using UnityEngine;
using System.Collections.Generic;

public class MapGenerator : MonoBehaviour
{
    [Header("Map Visuals (Assign from Project Window)")]
    public GameObject mapTilePrefab;
    public Material plainMaterial;
    public Material hillMaterial;
    public Material mountainMaterial;
    public Material waterMaterial;
    public Material defaultMaterial;

    [Header("Map Configuration")]
    public float worldMapTileYOffset = 0.0f;
    public float battleMapTileYOffset = 0.05f;
    public float mapTileScale = 1.0f;

    private GameObject worldMapContainer;
    private GameObject battleMapContainer;
    private bool worldMapGenerated = false; // ��������, ��� ���������� ������ ����� ���� ���� ���
    private string _currentBattleMapSignature = null; // ��� ������������ ����� ���, ���� ���� ��������

    private HashSet<string> loggedUnknownTerrains = new HashSet<string>();
    // private CameraController _cameraController; // ��������� �� CameraController ����� �� ������� ���, ���� FitCameraToMap �� �����������

    void Awake()
    {
        Debug.Log("--- MapGenerator Awake: ����������� ���������� ---");
        worldMapContainer = new GameObject("WorldMapVisuals_Container_MG");
        worldMapContainer.transform.parent = this.transform;

        battleMapContainer = new GameObject("BattleMapVisuals_Container_MG");
        battleMapContainer.transform.parent = this.transform;

        worldMapContainer.SetActive(false);
        battleMapContainer.SetActive(false);

        // ����������� CameraController ��� ����� �� � ���������, ���� �� �� ��������� ���� ������
        // if (Camera.main != null)
        // {
        //     _cameraController = Camera.main.GetComponent<CameraController>();
        // }
    }

    public void ShowWorldMap(SimState state)
    {
        Debug.Log($"MapGenerator: ������ ShowWorldMap ��� ҳ��: {state?.tick ?? -1}");
        if (state == null || state.worldMapGrid == null)
        {
            Debug.LogError("ShowWorldMap: �������� null SimulationState ��� worldMapGrid.");
            return;
        }

        battleMapContainer.SetActive(false);
        worldMapContainer.SetActive(true);
        // _currentBattleMapSignatureForCameraFit = null; // �� ���� ���� ��� ����� FitCameraToMap, ����� �� �������

        bool needsMapGeneration = !worldMapGenerated && state.worldMapGrid.Count > 0 && state.worldMapWidth > 0 && state.worldMapHeight > 0;

        if (needsMapGeneration)
        {
            Debug.Log($"MapGenerator: ��������� ������ ����� ({state.worldMapWidth}x{state.worldMapHeight}).");
            GenerateMapVisual(state.worldMapWidth, state.worldMapHeight, state.worldMapGrid, worldMapContainer, worldMapTileYOffset, "WorldMap");
            worldMapGenerated = true;
        }

        // �������� ������ FitCameraToMap
        // worldMapGeneratedAndCameraFitted ����� �� ��������������� � ��� �����
    }

    public void ShowBattleMap(BattleState battleState)
    {
        Debug.Log($"MapGenerator: ������ ShowBattleMap. ��� ��������: {battleState?.isActive}. ����������: {battleState?.combatants?.Count ?? 0}");
        if (battleState == null || !battleState.isActive || battleState.battleMapGrid == null)
        {
            Debug.LogError("ShowBattleMap: �������� null BattleStateData, ��� �� �� ��������, ��� battleMapGrid �������.");
            HideBattleMapAndTryShowWorld();
            return;
        }

        worldMapContainer.SetActive(false);
        battleMapContainer.SetActive(true);
        // worldMapGeneratedAndCameraFitted = false; // �� ���� ���� ��� ����� FitCameraToMap, ����� �� �������

        string newBattleMapSignature = $"{battleState.battleOriginCoordinates}_{battleState.battleMapWidth}x{battleState.battleMapHeight}";
        bool mapNeedsRedraw = (battleMapContainer.transform.childCount == 0 || _currentBattleMapSignature != newBattleMapSignature);

        if (mapNeedsRedraw && battleState.battleMapGrid.Count > 0 && battleState.battleMapWidth > 0 && battleState.battleMapHeight > 0)
        {
            Debug.Log($"MapGenerator: ���������/������������ ����� ��� ({battleState.battleMapWidth}x{battleState.battleMapHeight}) ��� {battleState.battleOriginCoordinates}.");
            GenerateMapVisual(battleState.battleMapWidth, battleState.battleMapHeight, battleState.battleMapGrid, battleMapContainer, battleMapTileYOffset, "BattleMap");
            _currentBattleMapSignature = newBattleMapSignature;
        }

        // �������� ������ FitCameraToMap
        // _currentBattleMapSignatureForCameraFit ����� �� ���������������
    }

    private void HideBattleMapAndTryShowWorld()
    {
        battleMapContainer.SetActive(false);
        _currentBattleMapSignature = null;

        if (SimulationLoader.CurrentSimState != null)
        {
            worldMapContainer.SetActive(true);
            // �� ��������� ShowWorldMap �����, ��� �������� ���������� ������,
            // SimulationLoader ��� �������, �� ���������� �� ����� CurrentSimState ��� ���������� ��������.
            // ���� ������� ������� �������� ������ �����, SimulationLoader �� ��������� ShowWorldMap.
        }
        else
        {
            worldMapContainer.SetActive(false);
        }
    }

    void GenerateMapVisual(int width, int height, List<CellState> gridData, GameObject parentContainer, float yLevel, string mapTypeForLog)
    {
        Debug.Log($"--- MapGenerator GenerateMapVisual ({mapTypeForLog}) ���������. �������: {gridData?.Count ?? 0}, ������: {width}, ������: {height} ---");
        if (mapTilePrefab == null) { Debug.LogError("Map Tile Prefab �� ����������! ����� �� ���� ��������."); return; }
        if (gridData == null || width <= 0 || height <= 0) { Debug.LogWarning($"GenerateMapVisual: ��������� ��� ��� ����� '{parentContainer.name}'."); return; }

        ClearContainer(parentContainer);

        float offsetX = (width - 1) / 2.0f * mapTileScale;
        float offsetZ = (height - 1) / 2.0f * mapTileScale;
        int createdTilesCount = 0;

        foreach (CellState cellData in gridData)
        {
            if (cellData == null) { continue; }
            float centeredX = cellData.x * mapTileScale - offsetX;
            float centeredZ = cellData.y * mapTileScale - offsetZ; // Y � ����� �������� �� Z � Unity
            Vector3 position = new Vector3(centeredX, yLevel, centeredZ);

            GameObject tile = Instantiate(mapTilePrefab, position, Quaternion.Euler(90, 0, 0), parentContainer.transform);
            tile.name = $"Tile_{mapTypeForLog}_{cellData.x}_{cellData.y}_{cellData.terrainType}";
            tile.transform.localScale = new Vector3(mapTileScale, mapTileScale, 1f);
            createdTilesCount++;

            Renderer tileRenderer = tile.GetComponent<Renderer>();
            if (tileRenderer != null)
            {
                tileRenderer.material = GetMaterialForTerrain(cellData.terrainType);
            }
        }
        Debug.Log($"��������� ����� ({mapTypeForLog}) ��� '{parentContainer.name}' ���������. �������� {createdTilesCount} ������.");
    }

    Material GetMaterialForTerrain(string terrainType)
    {
        string processedTerrainType = terrainType?.ToUpper() ?? "NULL_OR_EMPTY";
        Material chosenMaterial = defaultMaterial;

        switch (processedTerrainType)
        {
            case "PLAIN": chosenMaterial = plainMaterial; break;
            case "HILL": chosenMaterial = hillMaterial; break;
            case "MOUNTAIN": chosenMaterial = mountainMaterial; break;
            case "WATER": chosenMaterial = waterMaterial; break;
            default:
                if (!loggedUnknownTerrains.Contains(processedTerrainType) && !string.IsNullOrEmpty(terrainType))
                {
                    Debug.LogWarning($"MapGenerator: �������� ��� �������� '{terrainType}'. ���� ����������� Default Material.");
                    loggedUnknownTerrains.Add(processedTerrainType);
                }
                chosenMaterial = defaultMaterial;
                break;
        }

        if (chosenMaterial == null)
        {
            Debug.LogError($"����в�� �� ���������� (NULL) ��� terrainType='{terrainType}' (����������='{processedTerrainType}'). ������ ����������� �������� (��������� Default) � ��������� MapGenerator!");
        }
        return chosenMaterial;
    }

    void ClearContainer(GameObject container)
    {
        if (container == null) return;
        for (int i = container.transform.childCount - 1; i >= 0; i--)
        {
            Destroy(container.transform.GetChild(i).gameObject);
        }
    }
}