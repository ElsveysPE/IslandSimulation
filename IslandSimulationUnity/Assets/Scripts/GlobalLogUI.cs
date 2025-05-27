using UnityEngine;
using UnityEngine.UI;
using TMPro;
using System.Collections.Generic;
using System.Text;
using System.Linq;
using System.Collections;

public class GlobalLogUI : MonoBehaviour
{
    [Header("UI Елементи (ОБОВ'ЯЗКОВО призначити в Інспекторі!)")]
    public TextMeshProUGUI logTextTMP;
    public ScrollRect logScrollView;
    public GameObject logDisplayPanel;
    public Button toggleButton;
    public TextMeshProUGUI toggleButtonText;
    public RectTransform mainLogPanelRect;

    [Header("Налаштування Логу")]
    public int maxLogMessages = 200;
    public bool scrollToBottomOnNewMessage = true;
    public bool logWorldAnimalActions = true; // Новий прапорець для ввімкнення/вимкнення логу дій тварин у світі

    [Header("Налаштування Згортання")]
    public float collapsedWidth = 40f;
    private float _expandedWidth;
    private bool _isLogVisible = true;

    public List<string> _logMessages = new List<string>();
    private StringBuilder _stringBuilder = new StringBuilder();
    private int _lastLoggedTick = -1;
    private ulong _messageCounter = 0; // Для унікальних ключів, якщо буде потрібно

    void Start()
    {
        if (mainLogPanelRect == null) mainLogPanelRect = GetComponent<RectTransform>();
        if (mainLogPanelRect != null) _expandedWidth = mainLogPanelRect.sizeDelta.x;
        else _expandedWidth = 200f;

        if (logDisplayPanel == null && logScrollView != null) logDisplayPanel = logScrollView.gameObject;
        else if (logScrollView == null && logDisplayPanel != null)
            logScrollView = logDisplayPanel.GetComponent<ScrollRect>() ?? logDisplayPanel.GetComponentInChildren<ScrollRect>();

        if (logTextTMP == null) Debug.LogError("GlobalLogUI: 'Log Text TMP' НЕ ПРИЗНАЧЕНО!", this);
        if (logScrollView == null) Debug.LogError("GlobalLogUI: 'Log Scroll View' НЕ ПРИЗНАЧЕНО!", this);
        if (mainLogPanelRect == null) Debug.LogError("GlobalLogUI: 'Main Log Panel Rect' НЕ ПРИЗНАЧЕНО!", this);


        if (toggleButton != null) toggleButton.onClick.AddListener(ToggleLogVisibility);
        UpdateToggleButtonText();
        UpdateLogVisibilityState();
    }

    public void ToggleLogVisibility()
    {
        _isLogVisible = !_isLogVisible;
        UpdateLogVisibilityState();
        UpdateToggleButtonText();
    }

    void UpdateLogVisibilityState()
    {
        if (mainLogPanelRect == null) return;
        if (_isLogVisible)
        {
            if (logDisplayPanel != null) logDisplayPanel.SetActive(true);
            mainLogPanelRect.SetSizeWithCurrentAnchors(RectTransform.Axis.Horizontal, _expandedWidth);
        }
        else
        {
            if (logDisplayPanel != null) logDisplayPanel.SetActive(false);
            mainLogPanelRect.SetSizeWithCurrentAnchors(RectTransform.Axis.Horizontal, collapsedWidth);
        }
    }

    void UpdateToggleButtonText()
    {
        if (toggleButtonText != null)
            toggleButtonText.text = _isLogVisible ? "<" : ">";
    }

    private void AddMessageInternal(string message) // Приватний метод для фактичного додавання
    {
        if (logTextTMP == null) return;
        if (string.IsNullOrEmpty(message)) return;

        if (_logMessages.Count >= maxLogMessages && maxLogMessages > 0)
        {
            _logMessages.RemoveAt(0);
        }
        _logMessages.Add(message); // Мітку часу тепер додає викликаючий метод, якщо потрібно

        UpdateLogDisplay();
        if (scrollToBottomOnNewMessage && _isLogVisible && logScrollView != null && logScrollView.gameObject.activeInHierarchy)
        {
            StartCoroutine(ScrollToBottomCoroutine());
        }
    }

    public void AddMessage(string message, bool addTimestamp = true)
    {
        string finalMessage = addTimestamp ? System.DateTime.Now.ToString("[HH:mm:ss] ") + message : message;
        AddMessageInternal(finalMessage);
    }

    public void AddMessageList(IEnumerable<string> messages, bool addTimestamp = false)
    {
        if (logTextTMP == null) return;
        if (messages == null || !messages.Any()) return;

        bool changed = false;
        foreach (string message in messages)
        {
            if (string.IsNullOrEmpty(message)) continue;
            // Додаємо кожне повідомлення через внутрішній метод, щоб не дублювати логіку ліміту
            string finalMessage = addTimestamp ? System.DateTime.Now.ToString("[HH:mm:ss] ") + message : message;
            // Напряму в _logMessages, щоб уникнути багаторазового UpdateLogDisplay
            if (_logMessages.Count >= maxLogMessages && maxLogMessages > 0) _logMessages.RemoveAt(0);
            _logMessages.Add(finalMessage);
            changed = true;
        }

        if (changed)
        {
            UpdateLogDisplay();
            if (scrollToBottomOnNewMessage && _isLogVisible && logScrollView != null && logScrollView.gameObject.activeInHierarchy)
            {
                StartCoroutine(ScrollToBottomCoroutine());
            }
        }
    }

    public void LogTickState(int tick, string dayTime, bool isBattleActive, int worldAnimalsCount)
    {
        string timestamp = System.DateTime.Now.ToString("[HH:mm:ss] ");
        if (tick != _lastLoggedTick || _logMessages.Count < 2) // Додаємо, якщо тік змінився або лог майже порожній
        {
            AddMessageInternal($"{timestamp}--- ТІК: {tick} ({dayTime}) ---");
            _lastLoggedTick = tick;
        }
        if (!isBattleActive)
        {
            AddMessageInternal($"{timestamp}Світ. Тварин на карті: {worldAnimalsCount}");
        }
    }

    public void LogBattleStatus(BattleState battleState, List<AnimalState> allCombatantDetails)
    {
        if (battleState == null || !battleState.isActive) return;
        string timestamp = System.DateTime.Now.ToString("[HH:mm:ss] ");

        AddMessageInternal($"{timestamp}БІЙ АКТИВНИЙ! Початок: {battleState.battleOriginCoordinates}. Комбатантів: {battleState.combatants?.Count ?? 0}");

        AnimalState currentActor = null;
        if (allCombatantDetails != null && battleState.currentActorId != 0)
        {
            currentActor = allCombatantDetails.Find(c => c != null && c.id == battleState.currentActorId);
        }

        if (currentActor != null)
        {
            AddMessageInternal($"{timestamp}ХІД: {currentActor.speciesType} ID:{currentActor.id} [{currentActor.x},{currentActor.y}] Дія: '{currentActor.currentBattleAction}'");
        }
        else if (battleState.currentActorId != 0)
        {
            AddMessageInternal($"{timestamp}ХІД: Тварина ID:{battleState.currentActorId} (деталі не знайдено).");
        }
    }

    public void LogWorldAnimalActions(List<AnimalState> animals)
    {
        if (!logWorldAnimalActions || animals == null || !animals.Any()) return;

        string timestamp = System.DateTime.Now.ToString("[HH:mm:ss] ");
        bool headerAdded = false;
        foreach (var animal in animals)
        {
            if (animal != null && !string.IsNullOrEmpty(animal.currentAction) && animal.currentAction.ToUpper() != "IDLE" && animal.currentAction.ToUpper() != "N/A" && animal.currentAction.ToUpper() != "PENDING")
            {
                if (!headerAdded)
                {
                    AddMessageInternal($"{timestamp}Дії тварин у світі:");
                    headerAdded = true;
                }
                AddMessageInternal($"  {animal.speciesType} ID:{animal.id} [{animal.x},{animal.y}] -> '{animal.currentAction}'");
            }
        }
    }

    public void DisplayBattleEvents(List<string> events)
    {
        if (events != null && events.Count > 0)
        {
            string timestamp = System.DateTime.Now.ToString("[HH:mm:ss] ");
            AddMessageInternal($"{timestamp}Події бою:");
            // Додаємо кожну подію з відступом і без додаткової мітки часу, якщо вона вже є в рядку події
            List<string> indentedEvents = new List<string>();
            foreach (var e in events) indentedEvents.Add("  " + e);
            AddMessageList(indentedEvents, false);
        }
    }

    void UpdateLogDisplay()
    {
        if (logTextTMP == null) return;
        _stringBuilder.Clear();
        foreach (string msg in _logMessages)
        {
            _stringBuilder.AppendLine(msg);
        }
        logTextTMP.text = _stringBuilder.ToString();
    }

    System.Collections.IEnumerator ScrollToBottomCoroutine()
    {
        yield return new WaitForEndOfFrame();
        if (logScrollView != null && logScrollView.gameObject.activeInHierarchy)
        {
            logScrollView.verticalNormalizedPosition = 0f;
        }
    }

    public void ClearLog()
    {
        _logMessages.Clear();
        _lastLoggedTick = -1;
        if (logTextTMP != null)
        {
            UpdateLogDisplay();
        }
    }
}