using UnityEngine;
using UnityEngine.UI; // ������ ��� ������ � UI ����������, ������� Button
using UnityEngine.SceneManagement;
using System.Diagnostics;
using System.IO;

public class StartButtonController : MonoBehaviour
{
    [Header("��������� �� UI ��������")]
    [Tooltip("������, ��� ���� ��������� ������ ���������. ��������� � ��������� Unity.")]
    public Button simulationStartButton; // ���� ������� ����������� ���� UI ������

    [Header("������������ ����")]
    [Tooltip("��'� ����� � �������� ����������.")]
    public string simulationSceneName = "IslandMap";

    [Header("������������ ������� Java (Ҳ���� ��� ���������/Standalone!)")]
    [Tooltip("�������� ������ ������� ���������� .jar �����?")]
    public bool tryStartLocalJava = true;
    [Tooltip("���� �� .jar ����� Java-��������� ²������ ������ UNITY ������� (���������, '../target/YourApp.jar'). ��� ������ ���������� ����.")]
    public string javaJarPathRelativeToUnityRoot = "../target/IslandSimulation.jar"; // ��̲�� �� ����� ����� �� ���� �� ����� ������������ JAR
    [Tooltip("��������� ��� ������� Java .jar (���� ������).")]
    public string javaJarArguments = "";

    [Header("������������ JSON ��� SimulationLoader �� �������� ����")]
    [Tooltip("��'� ����� JSON � StreamingAssets, ���� ���� ������������� SimulationLoader.")]
    public string targetJsonFileNameInStreamingAssets = "sim_state.json";

    void Start()
    {
        // ����������, �� ������ ���� ���������� � ���������
        if (simulationStartButton != null)
        {
            // ������ ������� �� ��䳿 onClick ������.
            // ����� ��� ��������� �� �� ������ ���� ����������� ����� OnStartSimulationButtonPressed()
            simulationStartButton.onClick.AddListener(OnStartSimulationButtonPressed);
        }
        else
        {
            UnityEngine.Debug.LogError("StartButtonController: ������ 'simulationStartButton' �� ���������� � ���������! ���������� ������� �� ���� ��������� ����� �� ������.");
        }
    }

    public void OnStartSimulationButtonPressed()
    {
        UnityEngine.Debug.Log("StartButtonController: ��������� ������ '����� ���������' (������ � OnStartSimulationButtonPressed)");
        bool proceedToLoadScene = true;

#if UNITY_EDITOR || UNITY_STANDALONE
        if (tryStartLocalJava)
        {
            if (!string.IsNullOrEmpty(javaJarPathRelativeToUnityRoot))
            {
                if (TryStartJavaProcess())
                {
                    UnityEngine.Debug.Log("StartButtonController: Java-������ ������ ���������� (��� �������� ������, � �� ��� ������� ���� ���� �����).");
                }
                else
                {
                    UnityEngine.Debug.LogError("StartButtonController: ������� �� ��� �������/������ Java-�������. ����� ��������� ���� ����������� ���� ��� ��� �� ������ ����.");
                    // proceedToLoadScene = false; // �����������, ���� �� ����� ������������� ����� ��� ������� Java
                }
            }
            else
            {
                UnityEngine.Debug.LogWarning("StartButtonController: tryStartLocalJava ��������, ��� 'Java Jar Path' �� �������. Java �� ���� ��������.");
            }
        }
        else
        {
            UnityEngine.Debug.Log("StartButtonController: tryStartLocalJava ��������. ������ Java ���������.");
        }
#else
        UnityEngine.Debug.Log("StartButtonController: ��������� �� � ���������� (���������, WebGL). ������������ ������ Java ����������. ��������������� JSON � StreamingAssets.");
#endif

        if (proceedToLoadScene)
        {
            PlayerPrefs.SetString("SelectedJsonFileName_IslandSim", targetJsonFileNameInStreamingAssets);
            PlayerPrefs.SetInt("TriggerSimulationLoad_IslandSim", 1);
            PlayerPrefs.Save();
            UnityEngine.Debug.Log($"StartButtonController: ����������� JSON ��� ������������: '{targetJsonFileNameInStreamingAssets}' �� ������ �������.");

            if (!string.IsNullOrEmpty(simulationSceneName))
            {
                SceneManager.LoadScene(simulationSceneName);
            }
            else
            {
                UnityEngine.Debug.LogError("StartButtonController: ��'� ����� ��������� �� �������!");
            }
        }
        // else
        // {
        //     UnityEngine.Debug.LogError("StartButtonController: ������������ ����� ���������.");
        // }
    }

#if UNITY_EDITOR || UNITY_STANDALONE
    private bool TryStartJavaProcess()
    {
        string unityProjectRootPath;
        if (Application.isEditor)
        {
            unityProjectRootPath = Path.GetFullPath(Path.Combine(Application.dataPath, ".."));
        }
        else
        {
            // ��� ���� ���� ���� �� ����� ���� �� ����� <AppName>_Data
            unityProjectRootPath = Path.GetFullPath(Path.Combine(Application.dataPath, ".."));
        }

        string absoluteJarPath = Path.GetFullPath(Path.Combine(unityProjectRootPath, javaJarPathRelativeToUnityRoot));
        string jarDirectory = Path.GetDirectoryName(absoluteJarPath);

        if (!File.Exists(absoluteJarPath))
        {
            UnityEngine.Debug.LogError($"StartButtonController: Java .jar ���� �� �������� �� ������: '{absoluteJarPath}'. ������ 'Java Jar Path Relative To Unity Root'. ����� Unity ������� (��������): '{unityProjectRootPath}'");
            return false;
        }

        string streamingAssetsFullPath = Application.streamingAssetsPath;
        if (!Directory.Exists(streamingAssetsFullPath))
        {
            try
            {
                Directory.CreateDirectory(streamingAssetsFullPath);
                UnityEngine.Debug.Log($"StartButtonController: �������� ����� StreamingAssets (���� �� �� ����): {streamingAssetsFullPath}");
            }
            catch (System.Exception ex)
            {
                UnityEngine.Debug.LogError($"StartButtonController: �� ������� �������� ����� StreamingAssets: {ex.Message}");
                return false;
            }
        }

        try
        {
            ProcessStartInfo startInfo = new ProcessStartInfo();
            startInfo.FileName = "java";
            startInfo.Arguments = $"-jar \"{absoluteJarPath}\" {javaJarArguments}";

            // ���������� ������ �������� ��� Java
            // �������� ���������� �� ����� ������� (�� ������ ����� Unity �� ����� Java)
            // ����������, �� javaJarPathRelativeToUnityRoot ���������� � "../<java_project_folder>/..."
            // � �� ����� Unity ���������� "IslandSimulationUnity" (��� ������) �� ���� � ���, �� � <java_project_folder>
            string repoRootGuess = Path.GetFullPath(Path.Combine(unityProjectRootPath, "..")); // ����� �� ����� ���� �� ����� Unity �������

            // ������ ������ ����� Unity (�������� Application.productName ��� ��������� ��'�) �� �����, �� ������ jar
            bool isUnityProjectSubfolderPresent = Directory.Exists(Path.Combine(repoRootGuess, Path.GetFileName(unityProjectRootPath)));
            string[] jarPathSegments = javaJarPathRelativeToUnityRoot.Replace("\\", "/").Split('/');
            string javaProjectSubfolderGuess = (jarPathSegments.Length > 1 && jarPathSegments[0] == "..") ? jarPathSegments[1] : "";

            if (isUnityProjectSubfolderPresent && !string.IsNullOrEmpty(javaProjectSubfolderGuess) && Directory.Exists(Path.Combine(repoRootGuess, javaProjectSubfolderGuess)))
            {
                startInfo.WorkingDirectory = repoRootGuess;
                UnityEngine.Debug.Log($"StartButtonController: ����������� ������ ��������� ��� Java: {repoRootGuess}");
            }
            else
            {
                startInfo.WorkingDirectory = jarDirectory; // �������� ������ - ����� � JAR
                UnityEngine.Debug.LogWarning($"StartButtonController: �� ������� ����� ��������� ����� ���������� ��� WorkingDirectory Java. ����������� �� ����� � JAR: {jarDirectory}. �� ���� ���� ����������, ���� Java ����� ������ ������� ������ ����������.");
            }

            startInfo.UseShellExecute = false;
            startInfo.CreateNoWindow = true;

            UnityEngine.Debug.Log($"StartButtonController: ������ Java: \"{startInfo.FileName}\" {startInfo.Arguments} � ������� �������� \"{startInfo.WorkingDirectory}\"");
            Process process = new Process { StartInfo = startInfo };
            process.Start();

            UnityEngine.Debug.Log("StartButtonController: Java-������ ��������. ���������� ���������� (���� 30 ������)...");
            bool exited = process.WaitForExit(30000);

            if (exited && process.ExitCode == 0)
            {
                UnityEngine.Debug.Log("StartButtonController: Java-������ ������ ���������� (ExitCode 0). �������������, �� sim_state.json �������� � StreamingAssets.");
                string expectedJsonPathInStreamingAssets = Path.Combine(Application.streamingAssetsPath, targetJsonFileNameInStreamingAssets);
                if (File.Exists(expectedJsonPathInStreamingAssets))
                {
                    UnityEngine.Debug.Log($"StartButtonController: ���� '{expectedJsonPathInStreamingAssets}' �������� � StreamingAssets.");
                    return true;
                }
                else
                {
                    UnityEngine.Debug.LogError($"StartButtonController: Java-������ ����������, ��� ���� '{expectedJsonPathInStreamingAssets}' �� �������� � StreamingAssets. ������ ����� ������ ����� � Java �� �����!");
                    return false;
                }
            }
            else if (!exited)
            {
                UnityEngine.Debug.LogError("StartButtonController: Java-������ �� ���������� �� 30 ������ (�������).");
                try { if (!process.HasExited) process.Kill(); } catch { }
                return false;
            }
            else
            {
                UnityEngine.Debug.LogError($"StartButtonController: Java-������ ���������� � ����� �������: {process.ExitCode}.");
                return false;
            }
        }
        catch (System.ComponentModel.Win32Exception ex)
        {
            UnityEngine.Debug.LogError($"StartButtonController: ������� Win32 ��� ������� Java: {ex.Message}. �����������, �� 'java' �������� � ���������� PATH.");
            return false;
        }
        catch (System.Exception ex)
        {
            UnityEngine.Debug.LogError($"StartButtonController: �������� ������� ������� Java: {ex.GetType().Name} - {ex.Message}");
            return false;
        }
    }
#endif
}