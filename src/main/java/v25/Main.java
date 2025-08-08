package v25;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {

    private static final String OLLAMA_URL = "http://127.0.0.1:11434/api/chat";
    private static final String MODEL = "llama3.2:latest";

    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        Scanner in = new Scanner(System.in, StandardCharsets.UTF_8);

        System.out.println("Ollama Chat (model: " + MODEL + ")");
        System.out.println("Type your question and press Enter. Type 'exit' to quit.");

        while (true) {
            System.out.print("\nYou> ");
            String question = in.nextLine().trim();
            if (question.equalsIgnoreCase("exit")) break;
            if (question.isEmpty()) continue;

            String json = """
            {
              "model": "%s",
              "messages": [
                {"role":"system","content":"You are a helpful assistant for telecom churn analysis."},
                {"role":"user","content":"%s"}
              ],
              "stream": false,
              "options": {"temperature": 0.2, "top_p": 0.9}
            }
            """.formatted(MODEL, escapeJson(question));


            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(OLLAMA_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            try {
                HttpResponse<String> res = client.send(req, HttpResponse.BodyHandlers.ofString());
                String body = res.body();
                String answer = extractAssistantMessage(body);      // <-- NEW
                if (answer == null) answer = extractResponseField(body); // fallback for /api/generate
                if (answer == null) {
                    System.out.println("Assistant> [Could not parse answer]\nRaw: " + body);
                } else {
                    System.out.println("Assistant> " + answer);
                }
            } catch (Exception e) {
                System.out.println("Error contacting Ollama: " + e.getMessage());
            }
        }

        System.out.println("Bye.");
    }

    private static String escapeJson(String s) {
        return s
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }

    // NEW: extract message.content from /api/chat response
    private static String extractAssistantMessage(String json) {
        // Find: "message":{"role":"assistant","content":"..."}
        int mIdx = json.indexOf("\"message\":{");
        if (mIdx < 0) return null;

        // Find content field after "message":{
        int cKey = json.indexOf("\"content\":\"", mIdx);
        if (cKey < 0) return null;

        int start = cKey + "\"content\":\"".length();
        StringBuilder out = new StringBuilder();
        boolean escape = false;
        for (int i = start; i < json.length(); i++) {
            char ch = json.charAt(i);
            if (escape) {
                switch (ch) {
                    case 'n': out.append('\n'); break;
                    case 'r': out.append('\r'); break;
                    case 't': out.append('\t'); break;
                    case '"': out.append('"'); break;
                    case '\\': out.append('\\'); break;
                    default: out.append(ch); break;
                }
                escape = false;
            } else if (ch == '\\') {
                escape = true;
            } else if (ch == '"') {
                break;
            } else {
                out.append(ch);
            }
        }
        return out.toString();
    }

    // Fallback for /api/generate responses
    private static String extractResponseField(String json) {
        int i = json.indexOf("\"response\":\"");
        if (i < 0) return null;
        int start = i + "\"response\":\"".length();
        StringBuilder out = new StringBuilder();
        boolean escape = false;
        for (int k = start; k < json.length(); k++) {
            char c = json.charAt(k);
            if (escape) {
                switch (c) {
                    case 'n': out.append('\n'); break;
                    case 'r': out.append('\r'); break;
                    case 't': out.append('\t'); break;
                    case '"': out.append('"'); break;
                    case '\\': out.append('\\'); break;
                    default: out.append(c); break;
                }
                escape = false;
            } else if (c == '\\') {
                escape = true;
            } else if (c == '"') {
                break;
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
