
using UnityEngine;

public class AnimalViewController : MonoBehaviour
{
    public AnimalState CurrentData { get; private set; } // Поточні дані цієї тварини

    // Цей метод викликається з SimulationLoader, коли тварина вперше створюється
    public void Initialize(AnimalState initialState)
    {
        CurrentData = initialState;
        // Встановлюємо ім'я GameObject'а для зручності в ієрархії
        gameObject.name = $"{CurrentData.speciesType}_{CurrentData.id}";
        UpdateVisuals();
        // Debug.Log($"AnimalViewController Initialized for: {gameObject.name}");
    }

    // Цей метод викликається з SimulationLoader, коли дані для цієї тварини оновлюються
    public void UpdateState(AnimalState newState)
    {
        CurrentData = newState;
        UpdateVisuals();
        // Debug.Log($"AnimalViewController Updated State for: {gameObject.name}");
    }

    // Оновлює візуальний стан GameObject'а на основі CurrentData
    private void UpdateVisuals()
    {
        if (CurrentData == null) return;

        // Головне - активність об'єкта залежно від того, чи мертва тварина
        gameObject.SetActive(!CurrentData.isDead);

        if (CurrentData.isDead)
        {
            // Якщо тварина мертва, можливо, ти захочеш тут додати
            // якусь візуальну реакцію (зміна кольору, партиклі тощо),
            // але ти казав, анімацій не буде. Поки що просто ховаємо.
            return;
        }

        // Тут можна додавати іншу логіку оновлення візуальних аспектів,
        // якщо вона не пов'язана з позицією (позиція оновлюється в SimulationLoader).
        // Наприклад, якщо в AnimalState є поле "scale" або "color",
        // ти міг би оновлювати transform.localScale або матеріал рендерера тут.
    }

    // Цей метод викликається автоматично Unity, коли користувач клікає мишкою
    // на GameObject, до якого прикріплений цей скрипт І на якому є Collider.
    void OnMouseDown()
    {
        if (CurrentData == null || CurrentData.isDead) return; // Не реагуємо на клік по мертвій або неініціалізованій тварині

        Debug.Log($"Clicked on: {gameObject.name} (ID: {CurrentData.id}, Species: {CurrentData.speciesType}, HP: {CurrentData.healthPoints})");

        // Тут буде логіка для відображення індивідуального логу/статів цієї тварини.
        // Наприклад, ти можеш викликати метод якогось UI менеджера:
        // UIManager.Instance.ShowAnimalDetails(CurrentData);
        // Або просто активувати якусь панель статів, прив'язану до цього AnimalViewController.
        // Це ми реалізуємо на Фазі 2 (Інтерфейс).
    }
}