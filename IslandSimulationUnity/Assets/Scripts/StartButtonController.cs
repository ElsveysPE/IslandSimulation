using UnityEngine;
using UnityEngine.UI; // Додано для роботи з UI елементами, зокрема Button
using UnityEngine.SceneManagement;
using System.Diagnostics;
using System.IO;

public class StartButtonController : MonoBehaviour
{
    [Header("Посилання на UI Елементи")]
    [Tooltip("Кнопка, яка буде ініціювати запуск симуляції. Призначте з редактора Unity.")]
    public Button simulationStartButton; // Сюди потрібно перетягнути вашу UI Кнопку

    [Header("Налаштування Сцен")]
    [Tooltip("Ім'я сцени з основною симуляцією.")]
    public string simulationSceneName = "IslandMap";

    [Header("Налаштування Запуску Java (ТІЛЬКИ для Редактора/Standalone!)")]
    [Tooltip("Ввімкнути спробу запуску локального .jar файлу?")]
    public bool tryStartLocalJava = true;
    [Tooltip("Шлях до .jar файлу Java-симуляції ВІДНОСНО КОРЕНЯ UNITY ПРОЕКТУ (наприклад, '../target/YourApp.jar'). Або повний абсолютний шлях.")]
    public string javaJarPathRelativeToUnityRoot = "../target/IslandSimulation.jar"; // ЗАМІНИ НА ТОЧНУ НАЗВУ ТА ШЛЯХ ДО ТВОГО ВИКОНУВАНОГО JAR
    [Tooltip("Аргументи для запуску Java .jar (якщо потрібні).")]
    public string javaJarArguments = "";

    [Header("Налаштування JSON для SimulationLoader на Наступній Сцені")]
    [Tooltip("Ім'я файлу JSON в StreamingAssets, який буде завантажувати SimulationLoader.")]
    public string targetJsonFileNameInStreamingAssets = "sim_state.json";

    void Start()
    {
        // Перевіряємо, чи кнопка була призначена в інспекторі
        if (simulationStartButton != null)
        {
            // Додаємо слухача до події onClick кнопки.
            // Тепер при натисканні на цю кнопку буде викликатися метод OnStartSimulationButtonPressed()
            simulationStartButton.onClick.AddListener(OnStartSimulationButtonPressed);
        }
        else
        {
            UnityEngine.Debug.LogError("StartButtonController: Кнопка 'simulationStartButton' не призначена в інспекторі! Функціонал запуску не буде працювати через цю кнопку.");
        }
    }

    public void OnStartSimulationButtonPressed()
    {
        UnityEngine.Debug.Log("StartButtonController: Натиснуто кнопку 'Старт Симуляції' (виклик з OnStartSimulationButtonPressed)");
        bool proceedToLoadScene = true;

#if UNITY_EDITOR || UNITY_STANDALONE
        if (tryStartLocalJava)
        {
            if (!string.IsNullOrEmpty(javaJarPathRelativeToUnityRoot))
            {
                if (TryStartJavaProcess())
                {
                    UnityEngine.Debug.Log("StartButtonController: Java-процес успішно відпрацював (або зроблено спробу, і він сам записав файл куди треба).");
                }
                else
                {
                    UnityEngine.Debug.LogError("StartButtonController: Помилка під час запуску/роботи Java-процесу. Сцена симуляції може завантажити старі дані або не знайти файл.");
                    // proceedToLoadScene = false; // Розкоментуй, якщо не хочеш завантажувати сцену при помилці Java
                }
            }
            else
            {
                UnityEngine.Debug.LogWarning("StartButtonController: tryStartLocalJava увімкнено, але 'Java Jar Path' не вказано. Java не буде запущена.");
            }
        }
        else
        {
            UnityEngine.Debug.Log("StartButtonController: tryStartLocalJava вимкнено. Запуск Java пропущено.");
        }
#else
        UnityEngine.Debug.Log("StartButtonController: Платформа не є десктопною (наприклад, WebGL). Автоматичний запуск Java неможливий. Використовується JSON з StreamingAssets.");
#endif

        if (proceedToLoadScene)
        {
            PlayerPrefs.SetString("SelectedJsonFileName_IslandSim", targetJsonFileNameInStreamingAssets);
            PlayerPrefs.SetInt("TriggerSimulationLoad_IslandSim", 1);
            PlayerPrefs.Save();
            UnityEngine.Debug.Log($"StartButtonController: Встановлено JSON для завантаження: '{targetJsonFileNameInStreamingAssets}' та тригер запуску.");

            if (!string.IsNullOrEmpty(simulationSceneName))
            {
                SceneManager.LoadScene(simulationSceneName);
            }
            else
            {
                UnityEngine.Debug.LogError("StartButtonController: Ім'я сцени симуляції не вказано!");
            }
        }
        // else
        // {
        //     UnityEngine.Debug.LogError("StartButtonController: Завантаження сцени скасовано.");
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
            // Для білда шлях буде на рівень вище від папки <AppName>_Data
            unityProjectRootPath = Path.GetFullPath(Path.Combine(Application.dataPath, ".."));
        }

        string absoluteJarPath = Path.GetFullPath(Path.Combine(unityProjectRootPath, javaJarPathRelativeToUnityRoot));
        string jarDirectory = Path.GetDirectoryName(absoluteJarPath);

        if (!File.Exists(absoluteJarPath))
        {
            UnityEngine.Debug.LogError($"StartButtonController: Java .jar файл НЕ ЗНАЙДЕНО за шляхом: '{absoluteJarPath}'. Перевір 'Java Jar Path Relative To Unity Root'. Корінь Unity проекту (орієнтовно): '{unityProjectRootPath}'");
            return false;
        }

        string streamingAssetsFullPath = Application.streamingAssetsPath;
        if (!Directory.Exists(streamingAssetsFullPath))
        {
            try
            {
                Directory.CreateDirectory(streamingAssetsFullPath);
                UnityEngine.Debug.Log($"StartButtonController: Створено папку StreamingAssets (якщо її не було): {streamingAssetsFullPath}");
            }
            catch (System.Exception ex)
            {
                UnityEngine.Debug.LogError($"StartButtonController: Не вдалося створити папку StreamingAssets: {ex.Message}");
                return false;
            }
        }

        try
        {
            ProcessStartInfo startInfo = new ProcessStartInfo();
            startInfo.FileName = "java";
            startInfo.Arguments = $"-jar \"{absoluteJarPath}\" {javaJarArguments}";

            // Визначення робочої директорії для Java
            // Спробуємо встановити на корінь проекту (де лежить папка Unity та папка Java)
            // Припускаємо, що javaJarPathRelativeToUnityRoot починається з "../<java_project_folder>/..."
            // і що папка Unity називається "IslandSimulationUnity" (або подібне) на тому ж рівні, що і <java_project_folder>
            string repoRootGuess = Path.GetFullPath(Path.Combine(unityProjectRootPath, "..")); // Йдемо на рівень вище від папки Unity проекту

            // Спроба знайти папку Unity (зазвичай Application.productName або фіксоване ім'я) та папку, де лежить jar
            bool isUnityProjectSubfolderPresent = Directory.Exists(Path.Combine(repoRootGuess, Path.GetFileName(unityProjectRootPath)));
            string[] jarPathSegments = javaJarPathRelativeToUnityRoot.Replace("\\", "/").Split('/');
            string javaProjectSubfolderGuess = (jarPathSegments.Length > 1 && jarPathSegments[0] == "..") ? jarPathSegments[1] : "";

            if (isUnityProjectSubfolderPresent && !string.IsNullOrEmpty(javaProjectSubfolderGuess) && Directory.Exists(Path.Combine(repoRootGuess, javaProjectSubfolderGuess)))
            {
                startInfo.WorkingDirectory = repoRootGuess;
                UnityEngine.Debug.Log($"StartButtonController: Встановлено робочу директорію для Java: {repoRootGuess}");
            }
            else
            {
                startInfo.WorkingDirectory = jarDirectory; // Запасний варіант - папка з JAR
                UnityEngine.Debug.LogWarning($"StartButtonController: Не вдалося точно визначити корінь репозиторію для WorkingDirectory Java. Встановлено на папку з JAR: {jarDirectory}. Це може бути некоректно, якщо Java очікує писати відносно кореня репозиторію.");
            }

            startInfo.UseShellExecute = false;
            startInfo.CreateNoWindow = true;

            UnityEngine.Debug.Log($"StartButtonController: Запуск Java: \"{startInfo.FileName}\" {startInfo.Arguments} в робочій директорії \"{startInfo.WorkingDirectory}\"");
            Process process = new Process { StartInfo = startInfo };
            process.Start();

            UnityEngine.Debug.Log("StartButtonController: Java-процес запущено. Очікування завершення (макс 30 секунд)...");
            bool exited = process.WaitForExit(30000);

            if (exited && process.ExitCode == 0)
            {
                UnityEngine.Debug.Log("StartButtonController: Java-процес успішно завершився (ExitCode 0). Передбачається, що sim_state.json оновлено в StreamingAssets.");
                string expectedJsonPathInStreamingAssets = Path.Combine(Application.streamingAssetsPath, targetJsonFileNameInStreamingAssets);
                if (File.Exists(expectedJsonPathInStreamingAssets))
                {
                    UnityEngine.Debug.Log($"StartButtonController: Файл '{expectedJsonPathInStreamingAssets}' знайдено в StreamingAssets.");
                    return true;
                }
                else
                {
                    UnityEngine.Debug.LogError($"StartButtonController: Java-процес завершився, але файл '{expectedJsonPathInStreamingAssets}' НЕ ЗНАЙДЕНО в StreamingAssets. Перевір логіку запису файлу в Java та шляхи!");
                    return false;
                }
            }
            else if (!exited)
            {
                UnityEngine.Debug.LogError("StartButtonController: Java-процес не завершився за 30 секунд (таймаут).");
                try { if (!process.HasExited) process.Kill(); } catch { }
                return false;
            }
            else
            {
                UnityEngine.Debug.LogError($"StartButtonController: Java-процес завершився з кодом помилки: {process.ExitCode}.");
                return false;
            }
        }
        catch (System.ComponentModel.Win32Exception ex)
        {
            UnityEngine.Debug.LogError($"StartButtonController: Помилка Win32 при запуску Java: {ex.Message}. Переконайся, що 'java' доступна в системному PATH.");
            return false;
        }
        catch (System.Exception ex)
        {
            UnityEngine.Debug.LogError($"StartButtonController: Загальна помилка запуску Java: {ex.GetType().Name} - {ex.Message}");
            return false;
        }
    }
#endif
}