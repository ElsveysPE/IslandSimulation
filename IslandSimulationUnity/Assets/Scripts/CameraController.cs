using UnityEngine;

public class CameraController : MonoBehaviour
{
    [Header("Налаштування Камери")]
    [Tooltip("Швидкість наближення/віддалення камери коліщатком мишки.")]
    public float zoomSpeed = 10f;
    [Tooltip("Мінімальний ортографічний розмір (для Orthographic камери).")]
    public float minOrthoSize = 5f;
    [Tooltip("Максимальний ортографічний розмір (для Orthographic камери).")]
    public float maxOrthoSize = 50f;
    [Tooltip("Мінімальний кут огляду (для Perspective камери).")]
    public float perspectiveMinFOV = 15f;
    [Tooltip("Максимальний кут огляду (для Perspective камери).")]
    public float perspectiveMaxFOV = 90f;

    [Header("Налаштування Руху (Стрілками)")]
    [Tooltip("Швидкість руху камери стрілками.")]
    public float panSpeed = 20f;

    private Camera _camera;

    void Awake()
    {
        _camera = GetComponent<Camera>();
        if (_camera == null)
        {
            Debug.LogError("CameraController: На цьому GameObject ВІДСУТНІЙ компонент Camera!", this);
            this.enabled = false;
            return;
        }
        // Початкове обмеження розміру/FOV, якщо камера вже має якісь значення
        ApplyZoomLimits();
    }

    void Update()
    {
        if (_camera == null) return;

        HandleZoom();
        HandleKeyboardPan();
    }

    void HandleZoom()
    {
        float scrollInput = Input.GetAxis("Mouse ScrollWheel");

        if (Mathf.Abs(scrollInput) > 0.01f) // Якщо є прокрутка
        {
            if (_camera.orthographic)
            {
                _camera.orthographicSize -= scrollInput * zoomSpeed;
            }
            else // Для перспективної камери змінюємо Field of View
            {
                _camera.fieldOfView -= scrollInput * zoomSpeed;
            }
            ApplyZoomLimits(); // Застосовуємо обмеження після зміни
        }
    }

    void ApplyZoomLimits()
    {
        if (_camera.orthographic)
        {
            _camera.orthographicSize = Mathf.Clamp(_camera.orthographicSize, minOrthoSize, maxOrthoSize);
        }
        else
        {
            _camera.fieldOfView = Mathf.Clamp(_camera.fieldOfView, perspectiveMinFOV, perspectiveMaxFOV);
        }
    }

    void HandleKeyboardPan()
    {
        float panX = 0f;
        float panZ = 0f;

        // Рух стрілками
        if (Input.GetKey(KeyCode.UpArrow)) panZ += 1;
        if (Input.GetKey(KeyCode.DownArrow)) panZ -= 1;
        if (Input.GetKey(KeyCode.LeftArrow)) panX -= 1;
        if (Input.GetKey(KeyCode.RightArrow)) panX += 1;

        // Опціонально: додати WASD для панорамування
        if (Input.GetKey(KeyCode.W)) panZ += 1;
        if (Input.GetKey(KeyCode.S)) panZ -= 1;
        if (Input.GetKey(KeyCode.A)) panX -= 1;
        if (Input.GetKey(KeyCode.D)) panX += 1;

        Vector3 moveDirection = new Vector3(panX, 0, panZ);

        // Нормалізуємо вектор руху, якщо він діагональний, щоб швидкість була однаковою
        if (moveDirection.sqrMagnitude > 1)
        {
            moveDirection.Normalize();
        }

        // Рухаємо камеру у світових координатах
        // Time.deltaTime робить рух незалежним від частоти кадрів
        transform.Translate(moveDirection * panSpeed * Time.deltaTime, Space.World);
    }

    // Цей метод налаштовує ортографічний розмір камери, щоб вмістити карту.
    // Він не обмежує подальше панорамування.
    public void FitCameraToMap(int mapPixelWidth, int mapPixelHeight, float tileScale = 1.0f)
    {
        if (_camera == null)
        {
            Debug.LogError("FitCameraToMap: Камера не знайдена на цьому GameObject!");
            return;
        }
        if (!_camera.orthographic)
        {
            Debug.LogWarning("FitCameraToMap: Метод призначений для ортографічної камери. Для перспективної камери потрібна інша логіка налаштування огляду (наприклад, зміна відстані або FOV).");
            return;
        }
        if (mapPixelHeight <= 0 || mapPixelWidth <= 0)
        {
            Debug.LogWarning("FitCameraToMap: Некоректні розміри карти (ширина або висота <= 0).");
            return;
        }

        // Розраховуємо загальні світові розміри карти
        float totalMapWorldWidth = mapPixelWidth * tileScale;
        float totalMapWorldHeight = mapPixelHeight * tileScale;

        // Розраховуємо ортографічний розмір, щоб вмістити карту
        float screenAspect = (float)Screen.width / Screen.height;
        float mapAspect = totalMapWorldWidth / totalMapWorldHeight;

        if (screenAspect >= mapAspect)
        {
            // Екран ширший або такий самий, як карта -> вміщуємо по висоті
            _camera.orthographicSize = totalMapWorldHeight / 2.0f;
        }
        else
        {
            // Екран вищий, ніж карта -> вміщуємо по ширині
            _camera.orthographicSize = (totalMapWorldWidth / screenAspect) / 2.0f;
        }

        // Можна додати невеликий відступ, щоб карта не була впритул до країв екрану
         _camera.orthographicSize += tileScale * 0.5f; // Наприклад, відступ в половину плитки з кожного боку

        ApplyZoomLimits(); // Переконуємося, що розрахований розмір в межах допустимого
        Debug.Log($"CameraController: Камеру налаштовано (зум) під карту. Ортографічний розмір: {_camera.orthographicSize}");
    }
}