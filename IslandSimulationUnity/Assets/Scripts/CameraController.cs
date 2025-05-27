using UnityEngine;

public class CameraController : MonoBehaviour
{
    [Header("������������ ������")]
    [Tooltip("�������� ����������/��������� ������ ��������� �����.")]
    public float zoomSpeed = 10f;
    [Tooltip("̳�������� ������������� ����� (��� Orthographic ������).")]
    public float minOrthoSize = 5f;
    [Tooltip("������������ ������������� ����� (��� Orthographic ������).")]
    public float maxOrthoSize = 50f;
    [Tooltip("̳�������� ��� ������ (��� Perspective ������).")]
    public float perspectiveMinFOV = 15f;
    [Tooltip("������������ ��� ������ (��� Perspective ������).")]
    public float perspectiveMaxFOV = 90f;

    [Header("������������ ���� (��������)")]
    [Tooltip("�������� ���� ������ ��������.")]
    public float panSpeed = 20f;

    private Camera _camera;

    void Awake()
    {
        _camera = GetComponent<Camera>();
        if (_camera == null)
        {
            Debug.LogError("CameraController: �� ����� GameObject ²����Ͳ� ��������� Camera!", this);
            this.enabled = false;
            return;
        }
        // ��������� ��������� ������/FOV, ���� ������ ��� �� ���� ��������
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

        if (Mathf.Abs(scrollInput) > 0.01f) // ���� � ���������
        {
            if (_camera.orthographic)
            {
                _camera.orthographicSize -= scrollInput * zoomSpeed;
            }
            else // ��� ������������ ������ ������� Field of View
            {
                _camera.fieldOfView -= scrollInput * zoomSpeed;
            }
            ApplyZoomLimits(); // ����������� ��������� ���� ����
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

        // ��� ��������
        if (Input.GetKey(KeyCode.UpArrow)) panZ += 1;
        if (Input.GetKey(KeyCode.DownArrow)) panZ -= 1;
        if (Input.GetKey(KeyCode.LeftArrow)) panX -= 1;
        if (Input.GetKey(KeyCode.RightArrow)) panX += 1;

        // �����������: ������ WASD ��� �������������
        if (Input.GetKey(KeyCode.W)) panZ += 1;
        if (Input.GetKey(KeyCode.S)) panZ -= 1;
        if (Input.GetKey(KeyCode.A)) panX -= 1;
        if (Input.GetKey(KeyCode.D)) panX += 1;

        Vector3 moveDirection = new Vector3(panX, 0, panZ);

        // ���������� ������ ����, ���� �� �����������, ��� �������� ���� ���������
        if (moveDirection.sqrMagnitude > 1)
        {
            moveDirection.Normalize();
        }

        // ������ ������ � ������� �����������
        // Time.deltaTime ������ ��� ���������� �� ������� �����
        transform.Translate(moveDirection * panSpeed * Time.deltaTime, Space.World);
    }

    // ��� ����� ��������� ������������� ����� ������, ��� ������� �����.
    // ³� �� ������ �������� �������������.
    public void FitCameraToMap(int mapPixelWidth, int mapPixelHeight, float tileScale = 1.0f)
    {
        if (_camera == null)
        {
            Debug.LogError("FitCameraToMap: ������ �� �������� �� ����� GameObject!");
            return;
        }
        if (!_camera.orthographic)
        {
            Debug.LogWarning("FitCameraToMap: ����� ����������� ��� ������������ ������. ��� ������������ ������ ������� ���� ����� ������������ ������ (���������, ���� ������ ��� FOV).");
            return;
        }
        if (mapPixelHeight <= 0 || mapPixelWidth <= 0)
        {
            Debug.LogWarning("FitCameraToMap: ��������� ������ ����� (������ ��� ������ <= 0).");
            return;
        }

        // ����������� ������� ����� ������ �����
        float totalMapWorldWidth = mapPixelWidth * tileScale;
        float totalMapWorldHeight = mapPixelHeight * tileScale;

        // ����������� ������������� �����, ��� ������� �����
        float screenAspect = (float)Screen.width / Screen.height;
        float mapAspect = totalMapWorldWidth / totalMapWorldHeight;

        if (screenAspect >= mapAspect)
        {
            // ����� ������ ��� ����� �����, �� ����� -> ������ �� �����
            _camera.orthographicSize = totalMapWorldHeight / 2.0f;
        }
        else
        {
            // ����� �����, �� ����� -> ������ �� �����
            _camera.orthographicSize = (totalMapWorldWidth / screenAspect) / 2.0f;
        }

        // ����� ������ ��������� ������, ��� ����� �� ���� ������� �� ���� ������
         _camera.orthographicSize += tileScale * 0.5f; // ���������, ������ � �������� ������ � ������� ����

        ApplyZoomLimits(); // ������������, �� ������������ ����� � ����� �����������
        Debug.Log($"CameraController: ������ ����������� (���) �� �����. ������������� �����: {_camera.orthographicSize}");
    }
}