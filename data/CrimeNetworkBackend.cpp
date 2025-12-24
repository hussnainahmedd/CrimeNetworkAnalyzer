#include <iostream>
#include <fstream>
#include <sstream>
#include <string>
#include <vector>
#include <map>
#include <queue>
#include <stack>
#include <set>
#include <algorithm>
#include <chrono>
#include <thread>
#include <ctime>

#ifdef _WIN32
    #include <windows.h>
    #include <direct.h>
    #define mkdir(path, mode) _mkdir(path)
#else
    #include <sys/stat.h>
    #include <sys/types.h>
    #include <unistd.h>
#endif

using namespace std;

// File paths
const string DATA_DIR =  "C:\\Users\\husss\\OneDrive\\Documents\\NetBeansProjects\\DSA PROEJCT\\CrimeNetworkAnalyzer\\data\\";
const string REQUEST_FILE = DATA_DIR + "request.json";
const string RESPONSE_FILE = DATA_DIR + "response.json";
const string GRAPH_DATA_FILE = DATA_DIR + "graph_data.txt";
const string CASE_DATA_FILE = DATA_DIR + "case_data.txt";
const string USERS_FILE = DATA_DIR + "users.txt";
const string ASSIGNMENTS_FILE = DATA_DIR + "assignments.txt";
const string ACTIVITY_LOG_FILE = DATA_DIR + "activity_log.txt";
const string BACKEND_STATUS_FILE = DATA_DIR + ".backend_running";

// Graph Node Structure
struct Node {
    string id;
    string name;
    string type;
    map<string, string> attributes;
};

// Graph Edge Structure
struct Edge {
    string from;
    string to;
    string relationType;
};

// Tree Node for Case Hierarchy
struct CaseNode {
    string type;
    string description;
    vector<CaseNode*> children;
    
    CaseNode(string t, string d) : type(t), description(d) {}
};

// Case Assignment Structure
struct CaseAssignment {
    string officerId;
    string caseId;
    string priority;
    string assignedDate;
    string status;
};

// User Management Class
class UserManager {
private:
    map<string, pair<string, string>> users;
    
public:
    void loadUsers() {
        ifstream file(USERS_FILE);
        if (!file.is_open()) {
            createDefaultUsers();
            return;
        }
        
        string line;
        while (getline(file, line)) {
            if (line.empty()) continue;
            
            size_t pos1 = line.find('|');
            size_t pos2 = line.find('|', pos1 + 1);
            
            if (pos1 != string::npos && pos2 != string::npos) {
                string username = line.substr(0, pos1);
                string password = line.substr(pos1 + 1, pos2 - pos1 - 1);
                string role = line.substr(pos2 + 1);
                users[username] = {password, role};
            }
        }
        file.close();
    }
    
    void createDefaultUsers() {
        ofstream file(USERS_FILE);
        file << "admin|admin123|admin" << endl;
        file << "officer1|pass123|officer" << endl;
        file << "officer2|pass456|officer" << endl;
        file.close();
        
        users["admin"] = {"admin123", "admin"};
        users["officer1"] = {"pass123", "officer"};
        users["officer2"] = {"pass456", "officer"};
    }
    
    bool addUser(string username, string password, string role) {
        if (users.find(username) != users.end()) {
            return false;
        }
        
        users[username] = {password, role};
        
        ofstream file(USERS_FILE, ios::app);
        file << username << "|" << password << "|" << role << endl;
        file.close();
        
        return true;
    }
    
    string listOfficers() {
        stringstream result;
        result << "=== REGISTERED OFFICERS ===" << endl << endl;
        
        int count = 0;
        for (auto& user : users) {
            if (user.second.second == "officer") {
                count++;
                result << count << ". Officer ID: " << user.first << endl;
                result << "   Status: Active" << endl;
                result << "   Role: Field Officer" << endl << endl;
            }
        }
        
        if (count == 0) {
            result << "No officers registered yet." << endl;
        } else {
            result << "Total Officers: " << count << endl;
        }
        
        return result.str();
    }
};

// Case Assignment Manager
class AssignmentManager {
private:
    vector<CaseAssignment> assignments;
    
public:
    void loadAssignments() {
        ifstream file(ASSIGNMENTS_FILE);
        if (!file.is_open()) return;
        
        string line;
        while (getline(file, line)) {
            if (line.empty()) continue;
            
            stringstream ss(line);
            CaseAssignment ca;
            getline(ss, ca.officerId, '|');
            getline(ss, ca.caseId, '|');
            getline(ss, ca.priority, '|');
            getline(ss, ca.assignedDate, '|');
            getline(ss, ca.status, '|');
            
            assignments.push_back(ca);
        }
        file.close();
    }
    
    void assignCase(string officerId, string caseId, string priority) {
        CaseAssignment ca;
        ca.officerId = officerId;
        ca.caseId = caseId;
        ca.priority = priority;
        
        time_t now = time(0);
        char buffer[80];
        strftime(buffer, 80, "%Y-%m-%d", localtime(&now));
        ca.assignedDate = string(buffer);
        ca.status = "Open";
        
        assignments.push_back(ca);
        
        ofstream file(ASSIGNMENTS_FILE, ios::app);
        file << ca.officerId << "|" << ca.caseId << "|" << ca.priority << "|" 
             << ca.assignedDate << "|" << ca.status << endl;
        file.close();
    }
    
    string getOfficerCases(string officerId) {
        stringstream result;
        result << "=== CASES ASSIGNED TO " << officerId << " ===" << endl << endl;
        
        int count = 0;
        for (auto& ca : assignments) {
            if (ca.officerId == officerId) {
                count++;
                result << "Case #" << count << ":" << endl;
                result << "  Case ID: " << ca.caseId << endl;
                result << "  Priority: " << ca.priority << endl;
                result << "  Assigned Date: " << ca.assignedDate << endl;
                result << "  Status: " << ca.status << endl;
                result << "  ----------------------------------------" << endl;
            }
        }
        
        if (count == 0) {
            result << "No cases assigned to this officer yet." << endl;
        } else {
            result << "\nTotal Assigned Cases: " << count << endl;
        }
        
        return result.str();
    }
    
    string getAllAssignments() {
        stringstream result;
        result << "=== ALL CASE ASSIGNMENTS ===" << endl << endl;
        
        if (assignments.empty()) {
            result << "No case assignments yet." << endl;
            return result.str();
        }
        
        for (size_t i = 0; i < assignments.size(); i++) {
            auto& ca = assignments[i];
            result << (i + 1) << ". Case " << ca.caseId << " → Officer " << ca.officerId << endl;
            result << "   Priority: " << ca.priority << " | Status: " << ca.status << endl;
            result << "   Assigned: " << ca.assignedDate << endl;
            result << "   ----------------------------------------" << endl;
        }
        
        result << "\nTotal Assignments: " << assignments.size() << endl;
        
        return result.str();
    }
    
    void updateCaseStatus(string officerId, string caseId, string newStatus) {
        for (auto& ca : assignments) {
            if (ca.officerId == officerId && ca.caseId == caseId) {
                ca.status = newStatus;
                saveAssignments();
                return;
            }
        }
    }
    
    void saveAssignments() {
        ofstream file(ASSIGNMENTS_FILE);
        for (auto& ca : assignments) {
            file << ca.officerId << "|" << ca.caseId << "|" << ca.priority << "|" 
                 << ca.assignedDate << "|" << ca.status << endl;
        }
        file.close();
    }
};

// Activity Logger
class ActivityLogger {
public:
    static void log(string username, string action, string details) {
        ofstream file(ACTIVITY_LOG_FILE, ios::app);
        
        time_t now = time(0);
        char buffer[80];
        strftime(buffer, 80, "%Y-%m-%d %H:%M:%S", localtime(&now));
        
        file << buffer << "|" << username << "|" << action << "|" << details << endl;
        file.close();
    }
    
    static string getRecentActivity(int limit = 20) {
        ifstream file(ACTIVITY_LOG_FILE);
        if (!file.is_open()) {
            return "No activity logs found.";
        }
        
        vector<string> logs;
        string line;
        while (getline(file, line)) {
            logs.push_back(line);
        }
        file.close();
        
        stringstream result;
        result << "=== RECENT ACTIVITY LOG ===" << endl << endl;
        
        int start = max(0, (int)logs.size() - limit);
        for (int i = start; i < (int)logs.size(); i++) {
            stringstream ss(logs[i]);
            string timestamp, username, action, details;
            getline(ss, timestamp, '|');
            getline(ss, username, '|');
            getline(ss, action, '|');
            getline(ss, details, '|');
            
            result << "[" << timestamp << "]" << endl;
            result << "User: " << username << " | Action: " << action << endl;
            result << "Details: " << details << endl;
            result << "----------------------------------------" << endl;
        }
        
        return result.str();
    }
};

// Main Graph Class
class CrimeGraph {
private:
    map<string, Node> nodes;
    map<string, vector<pair<string, string>>> adjList;
    
public:
    void addNode(string id, string name, string type, map<string, string> attrs) {
        Node node;
        node.id = id;
        node.name = name;
        node.type = type;
        node.attributes = attrs;
        nodes[id] = node;
        
        if (adjList.find(id) == adjList.end()) {
            adjList[id] = vector<pair<string, string>>();
        }
    }
    
    void addEdge(string from, string to, string relationType) {
        if (nodes.find(from) == nodes.end() || nodes.find(to) == nodes.end()) {
            return;
        }
        adjList[from].push_back({to, relationType});
        adjList[to].push_back({from, relationType});
    }
    
    string bfs(string start, string end) {
        if (nodes.find(start) == nodes.end() || nodes.find(end) == nodes.end()) {
            return "❌ Error: One or both nodes not found in graph!\n\nAvailable nodes:\n" + listAllNodes();
        }
        
        queue<string> q;
        map<string, string> parent;
        map<string, bool> visited;
        
        q.push(start);
        visited[start] = true;
        parent[start] = "";
        
        bool found = false;
        
        while (!q.empty() && !found) {
            string current = q.front();
            q.pop();
            
            if (current == end) {
                found = true;
                break;
            }
            
            for (auto& neighbor : adjList[current]) {
                string nextId = neighbor.first;
                if (!visited[nextId]) {
                    visited[nextId] = true;
                    parent[nextId] = current;
                    q.push(nextId);
                }
            }
        }
        
        if (!found) {
            return "⚠️ No path found between " + start + " and " + end;
        }
        
        vector<string> path;
        string current = end;
        while (current != "") {
            path.push_back(current);
            current = parent[current];
        }
        reverse(path.begin(), path.end());
        
        stringstream result;
        result << "╔════════════════════════════════════════╗" << endl;
        result << "║     BFS SHORTEST PATH ANALYSIS         ║" << endl;
        result << "╚════════════════════════════════════════╝" << endl << endl;
        result << "📍 From: " << nodes[start].name << " (" << start << ")" << endl;
        result << "📍 To: " << nodes[end].name << " (" << end << ")" << endl;
        result << "📏 Path length: " << path.size() - 1 << " connections" << endl << endl;
        result << "🔗 Connection Path:" << endl;
        result << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << endl;
        
        for (size_t i = 0; i < path.size(); i++) {
            result << (i + 1) << ". " << nodes[path[i]].name << " (" << path[i] << ")";
            if (i < path.size() - 1) {
                string relType = "";
                for (auto& neighbor : adjList[path[i]]) {
                    if (neighbor.first == path[i + 1]) {
                        relType = neighbor.second;
                        break;
                    }
                }
                result << endl << "   ⬇️ [" << relType << "]" << endl;
            }
            result << endl;
        }
        result << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << endl;
        
        return result.str();
    }
    
    string dfs(string start) {
        if (nodes.find(start) == nodes.end()) {
            return "❌ Error: Start node not found in graph!\n\nAvailable nodes:\n" + listAllNodes();
        }
        
        stack<string> s;
        map<string, bool> visited;
        vector<string> traversal;
        
        s.push(start);
        
        while (!s.empty()) {
            string current = s.top();
            s.pop();
            
            if (!visited[current]) {
                visited[current] = true;
                traversal.push_back(current);
                
                for (auto& neighbor : adjList[current]) {
                    if (!visited[neighbor.first]) {
                        s.push(neighbor.first);
                    }
                }
            }
        }
        
        stringstream result;
        result << "╔════════════════════════════════════════╗" << endl;
        result << "║    DFS NETWORK EXPLORATION REPORT      ║" << endl;
        result << "╚════════════════════════════════════════╝" << endl << endl;
        result << "🎯 Starting Point: " << nodes[start].name << " (" << start << ")" << endl;
        result << "📊 Nodes Reached: " << traversal.size() << endl << endl;
        result << "🔍 Traversal Order:" << endl;
        result << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << endl;
        
        for (size_t i = 0; i < traversal.size(); i++) {
            result << (i + 1) << ". " << nodes[traversal[i]].name 
                   << " (" << traversal[i] << ") - " 
                   << nodes[traversal[i]].type << endl;
        }
        result << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << endl;
        
        return result.str();
    }
    
    string listAllNodes() {
        if (nodes.empty()) {
            return "No nodes in graph yet. Add suspects or locations first!";
        }
        
        stringstream result;
        for (auto& pair : nodes) {
            Node& node = pair.second;
            result << "  • " << node.id << " (" << node.name << ") - " << node.type << endl;
        }
        return result.str();
    }
    
    string displayGraph() {
        if (nodes.empty()) {
            return "╔════════════════════════════════════════╗\n"
                   "║      CRIME NETWORK GRAPH STATUS        ║\n"
                   "╚════════════════════════════════════════╝\n\n"
                   "⚠️ Graph is empty! Add suspects, locations, and connections to get started.";
        }
        
        stringstream result;
        result << "╔════════════════════════════════════════╗" << endl;
        result << "║       CRIME NETWORK GRAPH REPORT       ║" << endl;
        result << "╚════════════════════════════════════════╝" << endl << endl;
        
        int totalEdges = 0;
        for (auto& pair : adjList) {
            totalEdges += pair.second.size();
        }
        
        result << "📊 NETWORK STATISTICS:" << endl;
        result << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << endl;
        result << "Total Nodes: " << nodes.size() << endl;
        result << "Total Connections: " << totalEdges / 2 << endl;
        result << "Network Density: " << (nodes.size() > 1 ? (totalEdges / 2) * 100 / (nodes.size() * (nodes.size() - 1) / 2) : 0) << "%" << endl;
        result << endl;
        
        result << "👤 NODES IN NETWORK:" << endl;
        result << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << endl;
        for (auto& pair : nodes) {
            Node& node = pair.second;
            result << "🔹 ID: " << node.id << " | Name: " << node.name 
                   << " | Type: " << node.type;
            
            if (!node.attributes.empty()) {
                result << endl << "   Attributes: ";
                for (auto& attr : node.attributes) {
                    result << attr.first << "=" << attr.second << " ";
                }
            }
            result << endl;
        }
        
        result << endl << "🔗 CONNECTIONS:" << endl;
        result << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << endl;
        
        if (totalEdges == 0) {
            result << "⚠️ No connections yet. Add connections between suspects and locations!" << endl;
        } else {
            set<pair<string, string>> printedEdges;
            
            for (auto& pair : adjList) {
                string from = pair.first;
                for (auto& edge : pair.second) {
                    string to = edge.first;
                    string type = edge.second;
                    
                    if (printedEdges.find({from, to}) == printedEdges.end() && 
                        printedEdges.find({to, from}) == printedEdges.end()) {
                        
                        result << "• " << nodes[from].name << " (" << from << ") <--[" 
                               << type << "]--> " 
                               << nodes[to].name << " (" << to << ")" << endl;
                        
                        printedEdges.insert({from, to});
                    }
                }
            }
        }
        result << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << endl;
        
        return result.str();
    }
    
    void saveToFile() {
        ofstream file(GRAPH_DATA_FILE);
        if (!file.is_open()) return;
        
        file << "NODES" << endl;
        for (auto& pair : nodes) {
            Node& node = pair.second;
            file << node.id << "|" << node.name << "|" << node.type;
            for (auto& attr : node.attributes) {
                file << "|" << attr.first << ":" << attr.second;
            }
            file << endl;
        }
        
        file << "EDGES" << endl;
        set<pair<string, string>> savedEdges;
        for (auto& pair : adjList) {
            string from = pair.first;
            for (auto& edge : pair.second) {
                string to = edge.first;
                if (savedEdges.find({from, to}) == savedEdges.end() && 
                    savedEdges.find({to, from}) == savedEdges.end()) {
                    file << from << "|" << to << "|" << edge.second << endl;
                    savedEdges.insert({from, to});
                }
            }
        }
        
        file.close();
    }
    
    void loadFromFile() {
        ifstream file(GRAPH_DATA_FILE);
        if (!file.is_open()) return;
        
        string line, section;
        while (getline(file, line)) {
            if (line == "NODES") {
                section = "NODES";
                continue;
            } else if (line == "EDGES") {
                section = "EDGES";
                continue;
            }
            
            if (section == "NODES" && !line.empty()) {
                stringstream ss(line);
                string id, name, type, attr;
                getline(ss, id, '|');
                getline(ss, name, '|');
                getline(ss, type, '|');
                
                map<string, string> attrs;
                while (getline(ss, attr, '|')) {
                    size_t pos = attr.find(':');
                    if (pos != string::npos) {
                        attrs[attr.substr(0, pos)] = attr.substr(pos + 1);
                    }
                }
                
                addNode(id, name, type, attrs);
            } else if (section == "EDGES" && !line.empty()) {
                stringstream ss(line);
                string from, to, relType;
                getline(ss, from, '|');
                getline(ss, to, '|');
                getline(ss, relType, '|');
                
                addEdge(from, to, relType);
            }
        }
        
        file.close();
    }
};

// Case Tree Class
class CaseTree {
private:
    map<string, CaseNode*> cases;
    
public:
    void addCaseItem(string caseId, string type, string description) {
        if (cases.find(caseId) == cases.end()) {
            cases[caseId] = new CaseNode("case", "Case " + caseId);
        }
        
        CaseNode* newItem = new CaseNode(type, description);
        cases[caseId]->children.push_back(newItem);
    }
    
    string viewCase(string caseId) {
        if (cases.find(caseId) == cases.end()) {
            return "❌ Error: Case " + caseId + " not found!";
        }
        
        stringstream result;
        result << "╔════════════════════════════════════════╗" << endl;
        result << "║         CASE HIERARCHY REPORT          ║" << endl;
        result << "╚════════════════════════════════════════╝" << endl << endl;
        result << "📁 Case ID: " << caseId << endl << endl;
        
        CaseNode* root = cases[caseId];
        
        if (root->children.empty()) {
            result << "⚠️ No items added to this case yet." << endl;
            return result.str();
        }
        
        map<string, vector<CaseNode*>> categorized;
        for (auto child : root->children) {
            categorized[child->type].push_back(child);
        }
        
        for (auto& pair : categorized) {
            result << "━━━ " << pair.first << " (" << pair.second.size() << ") ━━━" << endl;
            for (size_t i = 0; i < pair.second.size(); i++) {
                result << "  " << (i + 1) << ". " << pair.second[i]->description << endl;
            }
            result << endl;
        }
        
        return result.str();
    }
    
    void saveToFile() {
        ofstream file(CASE_DATA_FILE);
        if (!file.is_open()) return;
        
        for (auto& pair : cases) {
            file << "CASE|" << pair.first << endl;
            for (auto child : pair.second->children) {
                file << child->type << "|" << child->description << endl;
            }
            file << "END" << endl;
        }
        
        file.close();
    }
    
    void loadFromFile() {
        ifstream file(CASE_DATA_FILE);
        if (!file.is_open()) return;
        
        string line, currentCase;
        while (getline(file, line)) {
            if (line.substr(0, 4) == "CASE") {
                currentCase = line.substr(5);
            } else if (line == "END") {
                currentCase = "";
            } else if (!currentCase.empty() && !line.empty()) {
                size_t pos = line.find('|');
                if (pos != string::npos) {
                    string type = line.substr(0, pos);
                    string desc = line.substr(pos + 1);
                    addCaseItem(currentCase, type, desc);
                }
            }
        }
        
        file.close();
    }
};

// JSON Parser
map<string, string> parseJSON(const string& json) {
    map<string, string> data;
    size_t pos = 0;
    
    while (pos < json.length()) {
        size_t keyStart = json.find('"', pos);
        if (keyStart == string::npos) break;
        
        size_t keyEnd = json.find('"', keyStart + 1);
        if (keyEnd == string::npos) break;
        
        string key = json.substr(keyStart + 1, keyEnd - keyStart - 1);
        
        size_t colonPos = json.find(':', keyEnd);
        if (colonPos == string::npos) break;
        
        size_t valStart = json.find('"', colonPos);
        if (valStart == string::npos) break;
        
        size_t valEnd = json.find('"', valStart + 1);
        if (valEnd == string::npos) break;
        
        string value = json.substr(valStart + 1, valEnd - valStart - 1);
        
        data[key] = value;
        pos = valEnd + 1;
    }
    
    return data;
}

// Check if file exists
bool fileExists(const string& filename) {
    ifstream file(filename);
    return file.good();
}

// Create directory
void createDirectory(const string& path) {
    mkdir(path.c_str(), 0777);
}

// Main Processing Function
void processRequest(CrimeGraph& graph, CaseTree& caseTree, UserManager& userMgr, AssignmentManager& assignMgr) {
    ifstream requestFile(REQUEST_FILE);
    if (!requestFile.is_open()) {
        return;
    }
    
    string json;
    getline(requestFile, json);
    requestFile.close();
    
    // Delete request file immediately after reading
    remove(REQUEST_FILE.c_str());
    
    map<string, string> request = parseJSON(json);
    string action = request["action"];
    string response;
    
    if (action == "add_suspect") {
        map<string, string> attrs;
        attrs["age"] = request["age"];
        attrs["address"] = request["address"];
        graph.addNode(request["id"], request["name"], "suspect", attrs);
        graph.saveToFile();
        
        ActivityLogger::log("admin", "add_suspect", "Added suspect: " + request["id"] + " - " + request["name"]);
        
        response = "✅ SUSPECT ADDED SUCCESSFULLY\n━━━━━━━━━━━━━━━━━━━━━━━━━━\n";
        response += "ID: " + request["id"] + "\n";
        response += "Name: " + request["name"] + "\n";
        response += "Age: " + request["age"] + "\n";
        response += "Address: " + request["address"] + "\n";
        response += "Status: ✓ Active in network database";
        
    } else if (action == "add_location") {
        map<string, string> attrs;
        attrs["crime_type"] = request["type"];
        graph.addNode(request["id"], request["name"], "location", attrs);
        graph.saveToFile();
        
        ActivityLogger::log("admin", "add_location", "Added location: " + request["id"] + " - " + request["name"]);
        
        response = "✅ CRIME LOCATION ADDED\n━━━━━━━━━━━━━━━━━━━━━━━━━━\n";
        response += "ID: " + request["id"] + "\n";
        response += "Location: " + request["name"] + "\n";
        response += "Crime Type: " + request["type"] + "\n";
        response += "Status: ✓ Added to network map";
        
    } else if (action == "add_connection") {
        graph.addEdge(request["from"], request["to"], request["type"]);
        graph.saveToFile();
        
        ActivityLogger::log("admin", "add_connection", request["from"] + " → " + request["to"] + " [" + request["type"] + "]");
        
        response = "✅ CONNECTION ESTABLISHED\n━━━━━━━━━━━━━━━━━━━━━━━━━━\n";
        response += "From: " + request["from"] + "\n";
        response += "To: " + request["to"] + "\n";
        response += "Type: " + request["type"] + "\n";
        response += "Status: ✓ Link added to network graph";
        
    } else if (action == "bfs") {
        ActivityLogger::log("user", "bfs_search", "Path: " + request["start"] + " → " + request["end"]);
        response = graph.bfs(request["start"], request["end"]);
        
    } else if (action == "dfs") {
        ActivityLogger::log("user", "dfs_search", "Starting from: " + request["start"]);
        response = graph.dfs(request["start"]);
        
    } else if (action == "display_graph") {
        ActivityLogger::log("user", "view_graph", "Full network display");
        response = graph.displayGraph();
        
    } else if (action == "add_case_item") {
        caseTree.addCaseItem(request["case_id"], request["type"], request["description"]);
        caseTree.saveToFile();
        
        ActivityLogger::log("admin", "add_case_item", "Case " + request["case_id"] + ": " + request["type"]);
        
        response = "✅ CASE ITEM ADDED\n━━━━━━━━━━━━━━━━━━━━━━━━━━\n";
        response += "Case: " + request["case_id"] + "\n";
        response += "Type: " + request["type"] + "\n";
        response += "Description: " + request["description"] + "\n";
        response += "Status: ✓ Item logged in case file";
        
    } else if (action == "view_case") {
        ActivityLogger::log("user", "view_case", "Viewing case: " + request["case_id"]);
        response = caseTree.viewCase(request["case_id"]);
        
    } else if (action == "add_officer") {
        bool success = userMgr.addUser(request["officer_id"], request["password"], "officer");
        if (success) {
            ActivityLogger::log("admin", "add_officer", "New officer: " + request["officer_id"]);
            response = "✅ OFFICER ADDED SUCCESSFULLY\n━━━━━━━━━━━━━━━━━━━━━━━━━━\n";
            response += "Officer ID: " + request["officer_id"] + "\n";
            response += "Status: ✓ Active\n";
            response += "Credentials created and stored securely.";
        } else {
            response = "❌ ERROR: Officer ID already exists!";
        }
        
    } else if (action == "list_officers") {
        ActivityLogger::log("admin", "list_officers", "Viewing officer list");
        response = userMgr.listOfficers();
        
    } else if (action == "assign_case") {
        assignMgr.assignCase(request["officer_id"], request["case_id"], request["priority"]);
        
        ActivityLogger::log("admin", "assign_case", "Case " + request["case_id"] + " → Officer " + request["officer_id"]);
        
        response = "✅ CASE ASSIGNED SUCCESSFULLY\n━━━━━━━━━━━━━━━━━━━━━━━━━━\n";
        response += "Officer: " + request["officer_id"] + "\n";
        response += "Case: " + request["case_id"] + "\n";
        response += "Priority: " + request["priority"] + "\n";
        response += "Status: ✓ Officer notified";
        
    } else if (action == "get_officer_cases") {
        response = assignMgr.getOfficerCases(request["officer_id"]);
        
    } else if (action == "get_all_assignments") {
        ActivityLogger::log("admin", "view_assignments", "Viewing all case assignments");
        response = assignMgr.getAllAssignments();
        
    } else if (action == "update_case_status") {
        assignMgr.updateCaseStatus(request["officer_id"], request["case_id"], request["status"]);
        
        ActivityLogger::log(request["officer_id"], "update_status", "Case " + request["case_id"] + " → " + request["status"]);
        
        response = "✅ CASE STATUS UPDATED\n━━━━━━━━━━━━━━━━━━━━━━━━━━\n";
        response += "Case: " + request["case_id"] + "\n";
        response += "New Status: " + request["status"] + "\n";
        response += "Updated by: " + request["officer_id"];
        
    } else if (action == "get_activity_log") {
        response = ActivityLogger::getRecentActivity(20);
        
    } else {
        response = "❌ Unknown action: " + action;
    }
    
    ofstream responseFile(RESPONSE_FILE);
    if (responseFile.is_open()) {
        responseFile << response;
        responseFile.close();
    }
}

// Create status file to indicate backend is running
void createStatusFile() {
    ofstream file(BACKEND_STATUS_FILE);
    if (file.is_open()) {
        time_t now = time(0);
        file << "Backend started at: " << ctime(&now);
        file.close();
    }
}

// Remove status file
void removeStatusFile() {
    remove(BACKEND_STATUS_FILE.c_str());
}

int main() {
    // Create data directory if it doesn't exist
    createDirectory(DATA_DIR);
    
    CrimeGraph graph;
    CaseTree caseTree;
    UserManager userMgr;
    AssignmentManager assignMgr;
    
    cout << "╔═══════════════════════════════════════════════╗" << endl;
    cout << "║  CRIME NETWORK ANALYZER - BACKEND SERVER     ║" << endl;
    cout << "║  Version 2.0 - Auto-Running Service          ║" << endl;
    cout << "╚═══════════════════════════════════════════════╝" << endl;
    cout << endl;
    
    cout << "📂 Initializing system components..." << endl;
    cout << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << endl;
    
    graph.loadFromFile();
    caseTree.loadFromFile();
    userMgr.loadUsers();
    assignMgr.loadAssignments();
    
    // Create status file
    createStatusFile();
    
    cout << endl;
    cout << "✅ System initialization complete!" << endl;
    cout << "🟢 Backend server is now running..." << endl;
    cout << "📡 Monitoring for requests from Java frontend..." << endl;
    cout << "⚠️  Press Ctrl+C to stop the server." << endl;
    cout << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << endl;
    cout << endl;
    
    int requestCount = 0;
    auto lastCheckTime = chrono::steady_clock::now();
    
    while (true) {
        if (fileExists(REQUEST_FILE)) {
            requestCount++;
            auto currentTime = chrono::system_clock::now();
            time_t timeNow = chrono::system_clock::to_time_t(currentTime);
            char timeBuffer[80];
            strftime(timeBuffer, 80, "%H:%M:%S", localtime(&timeNow));
            
            cout << "[" << timeBuffer << "] Request #" << requestCount << " 📨 Processing..." << endl;
            processRequest(graph, caseTree, userMgr, assignMgr);
            cout << "[" << timeBuffer << "] Request #" << requestCount << " ✅ Completed!" << endl;
            cout << "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━" << endl;
        }
        
        // Display "still running" message every 30 seconds
        auto now = chrono::steady_clock::now();
        auto elapsed = chrono::duration_cast<chrono::seconds>(now - lastCheckTime).count();
        if (elapsed >= 30) {
            cout << "⏰ Backend still running... (Requests processed: " << requestCount << ")" << endl;
            lastCheckTime = now;
        }
        
        // Sleep for 500ms to avoid high CPU usage
        this_thread::sleep_for(chrono::milliseconds(500));
    }
    
    // Cleanup (though this won't be reached in normal operation)
    removeStatusFile();
    
    return 0;
}