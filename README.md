# Local AI Console Chat — Telco Churn Domain (Ollama + Llama 3.2)

This project is a **Java console application** that communicates with a **local AI model** via the **Ollama REST API** and answers questions about **customer churn and segmentation** (based on Part 1 of the exam).

## 1. Requirements

- **Windows** with [Ollama](https://ollama.com) installed
- **Model**: `llama3.2:latest` pulled locally
- **Java 17+** (project runs in IntelliJ IDEA or any Java IDE)

## 2. Local Model Setup

## Local model setup

1. Start the Ollama server (leave this window open):
   ```powershell
   ollama serve
   ```
2. Pull the model (if you don’t already have it):
    ```powershell
   ollama pull llama3.2:latest
    ```

## How the app works

- The app sends a `POST` request to **Ollama** at: `http://127.0.0.1:11434/api/chat`
- Request JSON includes:
  - `"model": "llama3.2:latest"`
  - `"messages"`: a system instruction + your question
  - `"stream": false` (read full reply at once)
  - `"options": {"temperature": 0.2, "top_p": 0.9}`

It reads `message.content` from the JSON response and prints it in the console.

**Relevant settings (in `Main.java`):**
```java
private static final String MODEL = "llama3.2:latest"; // model name
private static final double TEMPERATURE = 0.2;         // randomness
private static final double TOP_P = 0.9;               // sampling
```

## Running the app

1. Open the project in IntelliJ IDEA.
2. Ensure Ollama is running (`ollama serve`) and the model is pulled.
3. Run the `Main` class (green play button).
4. Ask questions or exit with exit command
