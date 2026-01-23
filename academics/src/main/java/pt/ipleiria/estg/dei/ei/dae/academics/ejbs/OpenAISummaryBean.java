package pt.ipleiria.estg.dei.ei.dae.academics.ejbs;

import jakarta.ejb.Stateless;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

@Stateless
public class OpenAISummaryBean {
    private static final Logger logger = Logger.getLogger(OpenAISummaryBean.class.getName());
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent";
    private static final int MAX_TEXT_LENGTH = 30000; // Gemini suporta até 1M tokens

    // A API key será configurada via variável de ambiente
    // Podes definir no docker-compose.yaml: GEMINI_API_KEY=sua_chave_aqui
    private String getApiKey() {
        String apiKey = System.getenv("GEMINI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            logger.warning("⚠️ GEMINI_API_KEY não está definida! Configure a variável de ambiente.");
            return null;
        }
        return apiKey;
    }

    /**
     * Extrai texto de um ficheiro PDF
     */
    public String extractTextFromPDF(File pdfFile) throws IOException {
        logger.info("📄 Extraindo texto do PDF: " + pdfFile.getName());

        try (PDDocument document = PDDocument.load(pdfFile)) {
            PDFTextStripper stripper = new PDFTextStripper();
            // Processar até 30 páginas (Gemini consegue lidar com muito texto)
            stripper.setEndPage(Math.min(30, document.getNumberOfPages()));
            String text = stripper.getText(document);

            // Limitar tamanho do texto
            if (text.length() > MAX_TEXT_LENGTH) {
                text = text.substring(0, MAX_TEXT_LENGTH);
            }

            logger.info("✅ Texto extraído. Tamanho: " + text.length() + " caracteres");
            return text;
        }
    }

    /**
     * Gera resumo usando Google Gemini API
     */
    public String generateSummary(String text) {
        String apiKey = getApiKey();

        if (apiKey == null) {
            logger.warning("⚠️ GEMINI_API_KEY não configurada!");
            return "⚠️ Resumo não disponível. Configure a variável GEMINI_API_KEY no docker-compose.yaml";
        }

        if (text == null || text.trim().isEmpty()) {
            logger.warning("Texto vazio");
            return "Resumo não disponível (texto vazio)";
        }

        logger.info("🤖 Gerando resumo com Google Gemini...");

        // Tentar até 3 vezes em caso de erro
        int maxRetries = 3;
        int retryDelayMs = 2000; // 2 segundos

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                // URL com API key como query parameter
                String url = GEMINI_API_URL + "?key=" + apiKey;
                HttpPost request = new HttpPost(url);

                // Prompt otimizado para resumos académicos
                String prompt = "Analisa o seguinte texto de um artigo científico e gera um resumo conciso em português " +
                              "com no máximo 250 palavras. Destaca os pontos principais, objetivos, metodologia e conclusões:\n\n" + text;

                // JSON para a API do Gemini
                String json = String.format(
                    "{\"contents\":[{\"parts\":[{\"text\":\"%s\"}]}]}",
                    escapeJson(prompt)
                );

                request.setEntity(new StringEntity(json, StandardCharsets.UTF_8));
                request.setHeader("Content-Type", "application/json");

                logger.info("📤 Tentativa " + attempt + "/" + maxRetries + " - Enviando requisição para Google Gemini...");

                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                    if (statusCode == 200) {
                        String summary = extractSummaryFromGeminiResponse(responseBody);
                        logger.info("✅ Resumo gerado com sucesso! Tamanho: " + summary.length() + " caracteres");
                        return summary;

                    } else if (statusCode == 429) {
                        // Rate limit - tentar novamente
                        logger.warning("⚠️ Rate limit atingido (429). Tentativa " + attempt + "/" + maxRetries);
                        logger.warning("Resposta: " + responseBody.substring(0, Math.min(500, responseBody.length())));

                        if (attempt < maxRetries) {
                            logger.info("⏳ Aguardando " + (retryDelayMs / 1000) + " segundos antes de tentar novamente...");
                            Thread.sleep(retryDelayMs);
                            retryDelayMs *= 2; // Exponential backoff
                            continue;
                        } else {
                            logger.severe("❌ Limite de requisições excedido após " + maxRetries + " tentativas");
                            return "⚠️ Resumo não disponível. A API Gemini atingiu o limite de requisições.\n\n" +
                                   "💡 Aguarde alguns minutos e tente novamente.";
                        }

                    } else if (statusCode == 400) {
                        logger.severe("❌ Erro 400 - Requisição inválida");
                        logger.warning("Resposta: " + responseBody.substring(0, Math.min(1000, responseBody.length())));
                        return "⚠️ Resumo não disponível. Erro na requisição à API Gemini.\n\n" +
                               "O texto pode ser muito longo ou conter caracteres inválidos.";

                    } else if (statusCode == 403) {
                        logger.severe("❌ Erro 403 - Chave API inválida ou sem permissão");
                        logger.warning("Resposta: " + responseBody.substring(0, Math.min(500, responseBody.length())));
                        return "⚠️ Resumo não disponível. A chave API do Gemini é inválida.\n\n" +
                               "Por favor, verifique a variável GEMINI_API_KEY no docker-compose.yaml";

                    } else {
                        logger.warning("❌ Erro na API Gemini. Status: " + statusCode);
                        logger.warning("Resposta: " + responseBody.substring(0, Math.min(500, responseBody.length())));

                        if (attempt < maxRetries) {
                            logger.info("⏳ Tentando novamente em " + (retryDelayMs / 1000) + " segundos...");
                            Thread.sleep(retryDelayMs);
                            retryDelayMs *= 2;
                            continue;
                        } else {
                            return "⚠️ Resumo não disponível (erro " + statusCode + " na API Gemini).\n\n" +
                                   "Tente novamente mais tarde.";
                        }
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.severe("❌ Thread interrompida durante retry: " + e.getMessage());
                return "⚠️ Resumo não disponível (operação interrompida).";

            } catch (IOException e) {
                logger.severe("❌ Erro de I/O ao comunicar com Gemini (tentativa " + attempt + "): " + e.getMessage());
                e.printStackTrace();

                if (attempt < maxRetries) {
                    try {
                        logger.info("⏳ Tentando reconectar em " + (retryDelayMs / 1000) + " segundos...");
                        Thread.sleep(retryDelayMs);
                        retryDelayMs *= 2;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                    continue;
                } else {
                    return "⚠️ Resumo não disponível. Erro de conexão com a API Gemini.\n\n" +
                           "Verifique sua conexão de internet e tente novamente.";
                }
            }
        }

        return "⚠️ Resumo não disponível após " + maxRetries + " tentativas.";
    }

    /**
     * Gera resumo a partir de um ficheiro PDF
     */
    public String generateSummaryFromPDF(File pdfFile) {
        try {
            String text = extractTextFromPDF(pdfFile);
            return generateSummary(text);
        } catch (IOException e) {
            logger.severe("❌ Erro ao processar PDF: " + e.getMessage());
            e.printStackTrace();
            return "Resumo não disponível (erro ao processar PDF).";
        }
    }

    /**
     * Escapa caracteres especiais para JSON
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");
    }

    /**
     * Extrai o resumo da resposta JSON do Gemini
     */
    private String extractSummaryFromGeminiResponse(String json) {
        try {
            // Nova abordagem: procurar por "text" dentro de "parts"
            // A estrutura é: {"candidates":[{"content":{"parts":[{"text":"..."}]}}]}

            // Encontrar o primeiro "text" dentro de "parts"
            int partsIndex = json.indexOf("\"parts\"");
            if (partsIndex == -1) {
                logger.warning("Não encontrou 'parts' na resposta: " + json.substring(0, Math.min(500, json.length())));
                return "Erro ao processar resposta da API Gemini.";
            }

            // Procurar por "text" após "parts"
            int textKeyIndex = json.indexOf("\"text\"", partsIndex);
            if (textKeyIndex == -1) {
                logger.warning("Não encontrou 'text' após 'parts' na resposta");
                return "Erro ao processar resposta da API Gemini.";
            }

            // Encontrar o início do valor (após "text": ")
            int valueStartIndex = json.indexOf("\"", textKeyIndex + 6); // 6 = length of "text"
            if (valueStartIndex == -1) {
                return "Erro ao processar resposta da API Gemini.";
            }
            valueStartIndex++; // Pular a aspa inicial

            // Encontrar o fim do valor (próxima aspa não escapada)
            int valueEndIndex = valueStartIndex;
            while (valueEndIndex < json.length()) {
                valueEndIndex = json.indexOf("\"", valueEndIndex);
                if (valueEndIndex == -1) {
                    return "Erro ao processar resposta da API Gemini.";
                }

                // Verificar se a aspa está escapada
                int backslashCount = 0;
                int checkIndex = valueEndIndex - 1;
                while (checkIndex >= valueStartIndex && json.charAt(checkIndex) == '\\') {
                    backslashCount++;
                    checkIndex--;
                }

                // Se número par de backslashes (ou zero), a aspa não está escapada
                if (backslashCount % 2 == 0) {
                    break;
                }

                valueEndIndex++; // Continuar procurando
            }

            if (valueEndIndex == -1 || valueEndIndex <= valueStartIndex) {
                return "Erro ao processar resposta da API Gemini.";
            }

            String summary = json.substring(valueStartIndex, valueEndIndex);

            // Decodificar escape sequences
            summary = summary
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\")
                .trim();

            logger.info("📝 Resumo extraído com sucesso: " + summary.length() + " caracteres");
            return summary;

        } catch (Exception e) {
            logger.severe("Erro ao fazer parse da resposta Gemini: " + e.getMessage());
            e.printStackTrace();
            return "Erro ao processar resposta da API Gemini.";
        }
    }
}
