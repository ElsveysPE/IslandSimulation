
using UnityEngine;

public class AnimalViewController : MonoBehaviour
{
    public AnimalState CurrentData { get; private set; } // ������ ��� ���� �������

    // ��� ����� ����������� � SimulationLoader, ���� ������� ������ �����������
    public void Initialize(AnimalState initialState)
    {
        CurrentData = initialState;
        // ������������ ��'� GameObject'� ��� �������� � ��������
        gameObject.name = $"{CurrentData.speciesType}_{CurrentData.id}";
        UpdateVisuals();
        // Debug.Log($"AnimalViewController Initialized for: {gameObject.name}");
    }

    // ��� ����� ����������� � SimulationLoader, ���� ��� ��� ���� ������� �����������
    public void UpdateState(AnimalState newState)
    {
        CurrentData = newState;
        UpdateVisuals();
        // Debug.Log($"AnimalViewController Updated State for: {gameObject.name}");
    }

    // ������� ��������� ���� GameObject'� �� ����� CurrentData
    private void UpdateVisuals()
    {
        if (CurrentData == null) return;

        // ������� - ��������� ��'���� ������� �� ����, �� ������ �������
        gameObject.SetActive(!CurrentData.isDead);

        if (CurrentData.isDead)
        {
            // ���� ������� ������, �������, �� ������� ��� ������
            // ����� �������� ������� (���� �������, ������� ����),
            // ��� �� �����, ������� �� ����. ���� �� ������ ������.
            return;
        }

        // ��� ����� �������� ���� ����� ��������� ��������� �������,
        // ���� ���� �� ���'����� � �������� (������� ����������� � SimulationLoader).
        // ���������, ���� � AnimalState � ���� "scale" ��� "color",
        // �� �� �� ���������� transform.localScale ��� ������� ��������� ���.
    }

    // ��� ����� ����������� ����������� Unity, ���� ���������� ���� ������
    // �� GameObject, �� ����� ����������� ��� ������ � �� ����� � Collider.
    void OnMouseDown()
    {
        if (CurrentData == null || CurrentData.isDead) return; // �� ������� �� ��� �� ������ ��� ������������� ������

        Debug.Log($"Clicked on: {gameObject.name} (ID: {CurrentData.id}, Species: {CurrentData.speciesType}, HP: {CurrentData.healthPoints})");

        // ��� ���� ����� ��� ����������� �������������� ����/����� ���� �������.
        // ���������, �� ����� ��������� ����� ������� UI ���������:
        // UIManager.Instance.ShowAnimalDetails(CurrentData);
        // ��� ������ ���������� ����� ������ �����, ����'����� �� ����� AnimalViewController.
        // �� �� �������� �� ��� 2 (���������).
    }
}