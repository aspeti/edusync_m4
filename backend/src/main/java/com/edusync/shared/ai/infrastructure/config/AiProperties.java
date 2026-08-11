package com.edusync.shared.ai.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracion del spike LLM ({@code edusync.ai.*}).
 *
 * <p>La API key de Open WebUI MUST venir solo de env {@code OPEN_WEBUI_API_KEY};
 * nunca hardcodearla ni commitearla.
 */
@ConfigurationProperties(prefix = "edusync.ai")
public class AiProperties {

  /** Si es {@code false}, {@code POST /api/v1/ai/chat} responde 503. */
  private boolean enabled = true;

  /** {@code ollama} (default) o {@code open-webui}. */
  private String provider = "ollama";

  private final Ollama ollama = new Ollama();
  private final OpenWebUi openWebui = new OpenWebUi();

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = provider;
  }

  public Ollama getOllama() {
    return ollama;
  }

  public OpenWebUi getOpenWebui() {
    return openWebui;
  }

  public static class Ollama {
    private String baseUrl = "http://localhost:11434";
    private String model = "llama3.1:latest";
    private int timeoutSeconds = 120;

    public String getBaseUrl() {
      return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
    }

    public String getModel() {
      return model;
    }

    public void setModel(String model) {
      this.model = model;
    }

    public int getTimeoutSeconds() {
      return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
      this.timeoutSeconds = timeoutSeconds;
    }
  }

  public static class OpenWebUi {
    private String baseUrl = "http://localhost:3000";
    /** NUNCA poner un valor real aqui; solo ${OPEN_WEBUI_API_KEY}. */
    private String apiKey = "";
    private String model = "llama3.1:latest";
    private int timeoutSeconds = 120;

    public String getBaseUrl() {
      return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
      this.baseUrl = baseUrl;
    }

    public String getApiKey() {
      return apiKey;
    }

    public void setApiKey(String apiKey) {
      this.apiKey = apiKey;
    }

    public String getModel() {
      return model;
    }

    public void setModel(String model) {
      this.model = model;
    }

    public int getTimeoutSeconds() {
      return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
      this.timeoutSeconds = timeoutSeconds;
    }
  }
}
