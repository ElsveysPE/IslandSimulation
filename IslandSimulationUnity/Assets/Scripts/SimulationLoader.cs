using UnityEngine;
using UnityEngine.Networking; // Для UnityWebRequest
using Newtonsoft.Json;
using Newtonsoft.Json.Linq; // Для JObject, JArray, JToken
using System.IO;
using System.Collections;
using System.Collections.Generic;
using System.Linq; // Для .Find() у списку

// Допоміжний клас для мапінгу виду тварини на префаб в інспекторі
[System.Serializable]
public class AnimalPrefabMapping
{
    public string speciesType;
    public GameObject prefab;
}

// Переконайся, що твої C# DTO класи називаються саме так:
// SimState, CellData, AnimalStateData, BattleStateData
// І що AnimalViewController.cs очікує AnimalStateData

public class SimulationLoader : MonoBehaviour
{
    [Header("Налаштування файлу даних")]
    public string jsonFileName = "sim_state.json";
    [Tooltip("Як часто (в секундах) перевіряти та перезавантажувати sim_state.json. Встановіть 0 або менше, щоб завантажити лише один раз.")]
    public float reloadIntervalSeconds = 1.0f;

    [Header("Мапінг Префабів Тварин")]
    public List<AnimalPrefabMapping> animalPrefabMappings = new List<AnimalPrefabMapping>();
    private Dictionary<string, GameObject> _prefabsDict;

    [Header("Посилання на UI та Генератори")]
    public MapGenerator mapGenerator;
    public GlobalLogUI globalLogDisplay;

    public static SimState CurrentSimState { get; private set; }
    private Dictionary<long, GameObject> _animalGameObjects = new Dictionary<long, GameObject>();
    private bool _isLoading = false;

    private const int CELLS_PER_YIELD_DESERIALIZE = 150;
    private const int ANIMALS_PER_YIELD_DESERIALIZE = 20;
    private const int BATTLE_EVENTS_PER_YIELD_DESERIALIZE = 20;
    private const int VISUAL_UPDATES_PER_FRAME_ANIMALS = 15;

    private Coroutine _updateVisualsCoroutine;
    private Coroutine _loadAndProcessStateLoopCoroutine;

    void Awake()
    {
        Debug.Log("--- SimulationLoader Awake: Ініціалізація ---");
        _prefabsDict = new Dictionary<string, GameObject>();
        if (animalPrefabMappings != null && animalPrefabMappings.Count > 0)
        {
            // Debug.Log($"Знайдено {animalPrefabMappings.Count} записів у 'Animal Prefab Mappings'.");
            for (int i = 0; i < animalPrefabMappings.Count; i++)
            {
                var mapping = animalPrefabMappings[i];
                if (mapping == null || mapping.prefab == null || string.IsNullOrEmpty(mapping.speciesType))
                {
                    Debug.LogError($"Запис #{i} у animalPrefabMappings некоректний (null, або префаб не призначено, або speciesType порожній). Пропускаю.");
                    continue;
                }
                if (!_prefabsDict.ContainsKey(mapping.speciesType))
                {
                    _prefabsDict.Add(mapping.speciesType, mapping.prefab);
                    // Debug.Log($"Додано в словник: Ключ='{mapping.speciesType}', Префаб='{mapping.prefab.name}'");
                }
                else
                {
                    Debug.LogWarning($"Дублікат виду '{mapping.speciesType}' у animalPrefabMappings. Використовується перший знайдений: {_prefabsDict[mapping.speciesType].name}");
                }
            }
        }
        else { Debug.LogWarning("Список 'Animal Prefab Mappings' порожній або не призначений! Тварини не будуть створені, якщо для них немає префабів."); }
        Debug.Log($"--- Словник префабів тварин ініціалізовано. Записів: {_prefabsDict.Count} ---");

        if (mapGenerator == null) Debug.LogError("Посилання на MapGenerator НЕ ПРИЗНАЧЕНО в інспекторі SimulationLoader! Карта не буде ініціалізована/оновлена.");
        if (globalLogDisplay == null) Debug.LogWarning("Посилання на GlobalLogUI не призначено в інспекторі SimulationLoader. Логи UI не працюватимуть.");
    }

    void Start()
    {
        Debug.Log("--- SimulationLoader Start: Запуск основного циклу ---");
        if (_loadAndProcessStateLoopCoroutine == null)
        {
            _loadAndProcessStateLoopCoroutine = StartCoroutine(LoadAndProcessStateLoopCoroutine());
        }
    }

    IEnumerator LoadAndProcessStateLoopCoroutine()
    {
        yield return StartCoroutine(LoadAndProcessSingleStateCoroutine());
        if (reloadIntervalSeconds > 0)
        {
            while (true)
            {
                float waitStartTime = Time.time;
                while (_isLoading)
                {
                    if (Time.time - waitStartTime > 30.0f) { Debug.LogError("Таймаут очікування _isLoading в LoadAndProcessStateLoopCoroutine!"); _isLoading = false; break; }
                    yield return null;
                }
                // Debug.Log($"({Time.time:F2}s) Очікую {reloadIntervalSeconds}с перед наступним завантаженням.");
                yield return new WaitForSeconds(reloadIntervalSeconds);
                if (this == null || !gameObject.activeInHierarchy) { Debug.LogWarning("SimulationLoader знищено/вимкнено. Зупиняю цикл."); yield break; }
                yield return StartCoroutine(LoadAndProcessSingleStateCoroutine());
            }
        }
        else { Debug.Log("Одноразове завантаження JSON (reloadIntervalSeconds <= 0)."); }
    }

    IEnumerator LoadAndProcessSingleStateCoroutine()
    {
        if (_isLoading)
        {
            yield break;
        }
        _isLoading = true;
        Debug.Log($"({Time.time:F2}s) LoadAndProcessSingleState: Початок завантаження JSON. Останній відомий тік JSON: {CurrentSimState?.tick ?? -1}");

        string jsonString = null;
        string path = Path.Combine(Application.streamingAssetsPath, jsonFileName);

#if UNITY_WEBGL && !UNITY_EDITOR
        using (UnityEngine.Networking.UnityWebRequest www = UnityEngine.Networking.UnityWebRequest.Get(path)) {
            www.timeout = 10; 
            yield return www.SendWebRequest();
            if (www.result != UnityEngine.Networking.UnityWebRequest.Result.Success) { Debug.LogError($"Помилка завантаження JSON (WebGL): {www.error} для {path}"); _isLoading = false; yield break; }
            jsonString = www.downloadHandler.text;
        }
#else
        if (!File.Exists(path)) { Debug.LogError($"JSON файл не знайдено: {path}"); _isLoading = false; yield break; }
        try { jsonString = File.ReadAllText(path); }
        catch (System.Exception ex) { Debug.LogError($"Помилка читання JSON: {ex.Message}"); _isLoading = false; yield break; }
#endif

        if (string.IsNullOrEmpty(jsonString)) { Debug.LogError("JSON рядок порожній після завантаження."); _isLoading = false; yield break; }

        yield return StartCoroutine(PopulateSimStateManuallyCoroutine(jsonString));
    }

    IEnumerator PopulateSimStateManuallyCoroutine(string jsonString)
    {
        // Debug.Log($"({Time.time:F2}s) PopulateSimState: Початок десеріалізації JSON.");
        JObject rootJObject = null;
        try
        {
            rootJObject = JObject.Parse(jsonString);
        }
        catch (System.Exception ex) { Debug.LogError($"Помилка JObject.Parse: {ex.Message}"); _isLoading = false; yield break; }
        yield return null;

        SimState newSimState = new SimState();
        newSimState.tick = rootJObject["tick"]?.Value<int>() ?? (CurrentSimState?.tick ?? 0);
        newSimState.dayTime = rootJObject["dayTime"]?.Value<string>();
        newSimState.worldMapWidth = rootJObject["worldMapWidth"]?.Value<int>() ?? 0;
        newSimState.worldMapHeight = rootJObject["worldMapHeight"]?.Value<int>() ?? 0;
        // Debug.Log($"SimState базові поля (НОВИЙ Тік JSON: {newSimState.tick}): DayTime='{newSimState.dayTime}'");
        yield return null;

        JArray mapGridJArray = rootJObject["worldMapGrid"] as JArray;
        newSimState.worldMapGrid = new List<CellState>();
        if (mapGridJArray != null)
        {
            for (int i = 0; i < mapGridJArray.Count; i++)
            {
                JObject cellJObject = mapGridJArray[i] as JObject;
                if (cellJObject != null) try { newSimState.worldMapGrid.Add(cellJObject.ToObject<CellState>()); } catch (System.Exception ex) { Debug.LogWarning($"Помилка розбору CellData #{i}: {ex.Message}"); }
                if ((i + 1) % CELLS_PER_YIELD_DESERIALIZE == 0) yield return null;
            }
            // Debug.Log($"Розбір worldMapGrid завершено. Клітинки: {newSimState.worldMapGrid.Count}");
        }
        else { Debug.LogWarning("worldMapGrid порожній у JSON."); }
        yield return null;

        JArray animalsJArray = rootJObject["animalsOnWorldMap"] as JArray;
        newSimState.animalsOnWorldMap = new List<AnimalState>();
        if (animalsJArray != null)
        {
            for (int i = 0; i < animalsJArray.Count; i++)
            {
                JObject animalJObject = animalsJArray[i] as JObject;
                if (animalJObject != null) try { newSimState.animalsOnWorldMap.Add(animalJObject.ToObject<AnimalState>()); } catch (System.Exception ex) { Debug.LogWarning($"Помилка розбору AnimalStateData (world) #{i}: {ex.Message}"); }
                if ((i + 1) % ANIMALS_PER_YIELD_DESERIALIZE == 0) yield return null;
            }
            // Debug.Log($"Розбір animalsOnWorldMap завершено. Тварини: {newSimState.animalsOnWorldMap.Count}");
        }
        else { Debug.LogWarning("animalsOnWorldMap порожній у JSON."); }
        yield return null;

        JObject battleJObject = rootJObject["currentBattle"] as JObject;
        newSimState.currentBattle = new BattleState();
        if (battleJObject != null)
        {
            try
            {
                newSimState.currentBattle = battleJObject.ToObject<BattleState>();
                if (newSimState.currentBattle.battleMapGrid == null) newSimState.currentBattle.battleMapGrid = new List<CellState>();
                if (newSimState.currentBattle.combatants == null) newSimState.currentBattle.combatants = new List<AnimalState>();
                if (newSimState.currentBattle.recentBattleEvents == null) newSimState.currentBattle.recentBattleEvents = new List<string>();
            }
            catch (System.Exception ex)
            {
                Debug.LogError($"Помилка розбору CurrentBattle: {ex.Message}");
                newSimState.currentBattle.isActive = false;
            }
        }
        else { Debug.LogWarning("currentBattle порожній у JSON."); newSimState.currentBattle.isActive = false; }
        // Debug.Log($"Розбір currentBattle завершено. Бій активний: {newSimState.currentBattle.isActive}. Комбатанти: {newSimState.currentBattle.combatants?.Count ?? 0}");
        yield return null;

        CurrentSimState = newSimState;

        Debug.Log($"({Time.time:F2}s) Покрокова десеріалізація JSON для Тіку {CurrentSimState.tick} ЗАВЕРШЕНА.");
        _isLoading = false;
        ProcessSimStateVisuals();
    }

    void ProcessSimStateVisuals()
    {
        if (CurrentSimState == null)
        {
            if (globalLogDisplay != null) globalLogDisplay.AddMessage("ПОМИЛКА: CurrentSimState is null в SimulationLoader!");
            return;
        }

        // Очищаємо лог, якщо тік змінився (або якщо це перший лог для цього тіку)
        // Це робиться всередині LogTickState
        if (globalLogDisplay != null)
        {
            globalLogDisplay.LogTickState(CurrentSimState.tick, CurrentSimState.dayTime,
                                          CurrentSimState.currentBattle?.isActive ?? false,
                                          CurrentSimState.animalsOnWorldMap?.Count ?? 0);
        }

        if (_updateVisualsCoroutine != null)
        {
            StopCoroutine(_updateVisualsCoroutine);
            _updateVisualsCoroutine = null;
        }

        if (mapGenerator == null)
        {
            Debug.LogError("MapGenerator НЕ ПРИЗНАЧЕНО в SimulationLoader!");
            if (globalLogDisplay != null) globalLogDisplay.AddMessage("ПОМИЛКА: MapGenerator не призначено!");
            return;
        }

        if (CurrentSimState.currentBattle != null && CurrentSimState.currentBattle.isActive)
        {
            if (globalLogDisplay != null)
            {
                globalLogDisplay.LogBattleStatus(CurrentSimState.currentBattle, CurrentSimState.currentBattle.combatants);
                globalLogDisplay.DisplayBattleEvents(CurrentSimState.currentBattle.recentBattleEvents);
            }
            mapGenerator.ShowBattleMap(CurrentSimState.currentBattle);
            _updateVisualsCoroutine = StartCoroutine(UpdateAnimalVisualsCoroutine(CurrentSimState.currentBattle.combatants, true));
        }
        else // Світовий режим
        {
            if (globalLogDisplay != null)
            {
                // Виводимо дії тварин у світі
                globalLogDisplay.LogWorldAnimalActions(CurrentSimState.animalsOnWorldMap);
            }
            mapGenerator.ShowWorldMap(CurrentSimState);
            _updateVisualsCoroutine = StartCoroutine(UpdateAnimalVisualsCoroutine(CurrentSimState.animalsOnWorldMap, false));
        }
    }

    IEnumerator UpdateAnimalVisualsCoroutine(List<AnimalState> animalStatesToDisplay, bool isBattleMode)
    {
        // Debug.Log($"--- ({Time.time:F2}s) UpdateAnimalVisualsCoroutine РОЗПОЧАТО. Тік JSON: {CurrentSimState?.tick}. Режим бою: {isBattleMode}. Очікується тварин з даних: {animalStatesToDisplay?.Count ?? 0} ---");
        if (_prefabsDict == null) { Debug.LogError("КРИТИЧНО: _prefabsDict є null!"); yield break; }

        List<AnimalState> statesToProcess = animalStatesToDisplay ?? new List<AnimalState>();
        HashSet<long> idsInCurrentDisplayList = new HashSet<long>();
        foreach (AnimalState state in statesToProcess) { if (state != null) idsInCurrentDisplayList.Add(state.id); }

        List<long> existingGameObjectIds = new List<long>(_animalGameObjects.Keys);
        int processedThisFrame = 0;

        float currentMapTileScale = 1.0f;
        float tileYOffset = 0.0f;
        if (mapGenerator != null)
        {
            currentMapTileScale = mapGenerator.mapTileScale;
            tileYOffset = isBattleMode ? mapGenerator.battleMapTileYOffset : mapGenerator.worldMapTileYOffset;
        }
        float animalYPosAboveTile = tileYOffset + 0.05f;
        int mapWidth = 0;
        int mapHeight = 0;
        if (CurrentSimState != null)
        {
            mapWidth = isBattleMode ? (CurrentSimState.currentBattle?.battleMapWidth ?? 0) : CurrentSimState.worldMapWidth;
            mapHeight = isBattleMode ? (CurrentSimState.currentBattle?.battleMapHeight ?? 0) : CurrentSimState.worldMapHeight;
        }
        float offsetX_map = (mapWidth > 0) ? (mapWidth - 1) / 2.0f * currentMapTileScale : 0f;
        float offsetZ_map = (mapHeight > 0) ? (mapHeight - 1) / 2.0f * currentMapTileScale : 0f;

        foreach (long animalId in existingGameObjectIds)
        {
            if (!_animalGameObjects.TryGetValue(animalId, out GameObject animalGO) || animalGO == null) continue;
            if (idsInCurrentDisplayList.Contains(animalId))
            {
                AnimalState state = statesToProcess.Find(s => s != null && s.id == animalId);
                if (state != null && !state.isDead)
                {
                    animalGO.SetActive(true);
                    float animalCenteredX = state.x * currentMapTileScale - offsetX_map;
                    float animalCenteredZ = state.y * currentMapTileScale - offsetZ_map;
                    animalGO.transform.position = new Vector3(animalCenteredX, animalYPosAboveTile, animalCenteredZ);
                    AnimalViewController avc = animalGO.GetComponent<AnimalViewController>();
                    if (avc != null) avc.UpdateState(state);
                }
                else { animalGO.SetActive(false); }
            }
            else { animalGO.SetActive(false); }
            if (++processedThisFrame >= VISUAL_UPDATES_PER_FRAME_ANIMALS) { processedThisFrame = 0; yield return null; }
        }

        foreach (AnimalState state in statesToProcess)
        {
            if (state == null || state.isDead) continue;
            if (!_animalGameObjects.ContainsKey(state.id))
            {
                // Debug.Log($"UpdateAnimalVisuals: Спроба створити тварину ID: {state.id}, SpeciesType з JSON: '[{state.speciesType}]'");
                if (string.IsNullOrEmpty(state.speciesType)) { Debug.LogError($"speciesType порожній для ID {state.id}"); continue; }

                if (_prefabsDict.TryGetValue(state.speciesType, out GameObject prefabToInstantiate))
                {
                    if (prefabToInstantiate != null)
                    {
                        float animalCenteredX = state.x * currentMapTileScale - offsetX_map;
                        float animalCenteredZ = state.y * currentMapTileScale - offsetZ_map;
                        Vector3 initialPosition = new Vector3(animalCenteredX, animalYPosAboveTile, animalCenteredZ);
                        GameObject animalGO = Instantiate(prefabToInstantiate, initialPosition, Quaternion.identity);
                        animalGO.transform.SetParent(this.transform, true);
                        _animalGameObjects[state.id] = animalGO;
                        AnimalViewController avc = animalGO.GetComponent<AnimalViewController>();
                        if (avc == null) avc = animalGO.AddComponent<AnimalViewController>();
                        avc.Initialize(state);
                        // Debug.Log($"СТВОРЕНО: {animalGO.name}");
                    }
                }
                else { Debug.LogError($"НЕ ЗНАЙДЕНО префаб для виду '[{state.speciesType}]'."); }
                if (++processedThisFrame >= VISUAL_UPDATES_PER_FRAME_ANIMALS) { processedThisFrame = 0; yield return null; }
            }
        }
        // Debug.Log($"--- ({Time.time:F2}s) UpdateAnimalVisualsCoroutine ЗАВЕРШЕНО для Тіку JSON: {CurrentSimState?.tick}. Тварин у словнику: {_animalGameObjects.Count} ---");
        _updateVisualsCoroutine = null;
    }
}