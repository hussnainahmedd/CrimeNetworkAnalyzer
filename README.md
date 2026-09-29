<div align="center">

# Crime Network Analyzer

### A desktop app for mapping and analyzing criminal networks

[![Java](https://img.shields.io/badge/Java-24_ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![Java Swing](https://img.shields.io/badge/GUI-Java_Swing-ED8B00?style=for-the-badge)](https://docs.oracle.com/javase/tutorial/uiswing/)
[![C++](https://img.shields.io/badge/Backend-C++-00599C?style=for-the-badge&logo=c%2B%2B&logoColor=white)](https://isocpp.org/)
[![Maven](https://img.shields.io/badge/Build-Maven-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![JSON IPC](https://img.shields.io/badge/IPC-JSON_Files-000000?style=for-the-badge&logo=json&logoColor=white)](#how-it-works)

<br/>

![Crime Network Analyzer preview](assets/hero.webp)

<br/>

A hybrid system built for a university DSA project: a **Java Swing desktop GUI** backed by a **C++ analysis engine**. The two processes talk to each other through JSON request/response files, so the UI stays responsive while the backend crunches graph algorithms.

</div>

## ✨ Features

- **Role-based login** — admins get the full toolkit; officers get a limited view of their assigned cases
- **Suspect registry** — add suspects with name, age, address, and evidence notes
- **Crime locations** — log locations tied to cases
- **Connections graph** — link suspects as accomplices, family, friends, or via call history
- **Graph analysis** — run BFS, DFS, and shortest-path queries across the network from the "Analyze Network" tab
- **Case hierarchy** — organize cases and drill into case details
- **Officer management** — admins can add officers and assign cases to them
- **Activity log** — every action is recorded for audit

## 🛠 Tech Stack

| Layer | Tech |
|---|---|
| Frontend | Java Swing (single-window tabbed UI, `src/main/java/CrimeNetworkAnalyzer.java`) |
| Backend | C++ (`data/CrimeNetworkBackend.cpp`) — graph algorithms, file-based JSON IPC |
| Data | Flat text files in `data/` (`users.txt`, `graph_data.txt`, `case_data.txt`, `assignments.txt`, `activity_log.txt`) |
| Build | Maven (`pom.xml`, Java 24) |

### How it works

1. The C++ backend runs in the background and watches `data/request.json`.
2. The Swing UI writes analysis/data requests as JSON and polls `data/response.json`.
3. `data/.backend_running` is the status flag the UI checks on startup.

## 🚀 Build & Run

**Prerequisites:** JDK 24 and Maven installed.

```bash
# 1. Build the Java frontend
mvn compile

# 2. Start the C++ backend (Windows build is included)
./data/CrimeNetworkBackend.exe
# — or compile it yourself on Linux/macOS:
#   g++ -o backend data/CrimeNetworkBackend.cpp && ./backend

# 3. Run the app (default credentials live in data/users.txt)
java -cp target/classes CrimeNetworkAnalyzer
```

> ⚠️ Note: the backend talks to the frontend through a hardcoded `DATA_DIR` path at the top of `CrimeNetworkAnalyzer.java` (currently a Windows path). Point it at your local `data/` folder before running on another machine.

## 📁 Project Structure

```
CrimeNetworkAnalyzer/
├── src/main/java/CrimeNetworkAnalyzer.java  # entire Swing frontend (login + tabs)
├── data/
│   ├── CrimeNetworkBackend.cpp               # C++ analysis engine
│   ├── CrimeNetworkBackend.exe               # prebuilt Windows backend
│   └── *.txt                                 # users, graph, cases, assignments, logs
└── pom.xml                                   # Maven build (Java 24)
```

## 📝 What I learned

Building this taught me how to design a small client–server-style system with plain files as the transport, how graph algorithms like BFS/DFS/shortest path map to real data, and why separating UI from compute logic keeps an app maintainable. Built as a data structures & algorithms course project.

---

<div align="center">

Built by **Hussnain Ahmad** — [github.com/hussnainahmedd](https://github.com/hussnainahmedd)

</div>
