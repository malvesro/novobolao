package com.opendev.bolao.email;

import java.io.IOException;
import java.io.InputStream;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Email {

	private static final Logger LOGGER = LoggerFactory.getLogger(Email.class);
	
    private static final String TEMPLATE_DIR = "/com/opendev/bolao/email/templates/";
	private static final String TEMPLATE_CABECALHO = "cabecalho.html";
	private static final String TEMPLATE_RODAPE = "rodape.html";
    private static final String EMAIL_BACKGROUND_IMAGE_PATH = "/img/brasao-fundo-email.jpg";
    private static final String VERSION_PROPERTIES_RESOURCE = "/version.properties";
    private static final String EMAIL_BACKGROUND_CACHE_BUSTER_PROPERTY = "mail.property.emailbg.cachebuster";
    private static final String TITULO_PADRAO = "Bolão de Placa - TV Cipó na Copa 2006";
    private static volatile EmailConfiguration CONFIG = EmailConfiguration.load();
    private static final String EMAIL_BACKGROUND_VERSION = resolveEmailBackgroundVersion();
	
	private Properties property;
	private String conteudo;
	private String nomeTemplate;
	private String assunto;
	private String de;
	private List enderecosDestino;
	private List enderecosCopia;
	private List enderecosCopiaOculta;
	
	/**
	 * 
	 * @param nomeTemplate
	 * @param titulo
	 * @param assunto
	 */
	public Email(String nomeTemplate, String titulo, String assunto, String de) {
		if (nomeTemplate == null) {
			throw new IllegalArgumentException("O nome do template de email não pode ser 'null'");
		}
		this.property = new  Properties();
		this.nomeTemplate = nomeTemplate;
		this.assunto = assunto;
		this.de = de;
		if (titulo == null) {
			titulo = TITULO_PADRAO;
		}
		setPropriedade("titulo", titulo);
        String systemUrl = CONFIG.getProperty("mail.property.systemurl");
        setPropriedade("sistema", systemUrl);
        setPropriedade("emailBgUrl", buildEmailBackgroundUrl(systemUrl));
	}

    private String buildEmailBackgroundUrl(String systemUrl) {
        if (systemUrl == null || systemUrl.trim().isEmpty()) {
            return "";
        }
        String base = systemUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        String imageUrl = base + EMAIL_BACKGROUND_IMAGE_PATH;
        if (EMAIL_BACKGROUND_VERSION == null || EMAIL_BACKGROUND_VERSION.isEmpty()) {
            return imageUrl;
        }
        return imageUrl + "?v=" + EMAIL_BACKGROUND_VERSION;
    }

    private static String resolveEmailBackgroundVersion() {
        String configuredVersion = sanitizeCacheBuster(CONFIG.getProperty(EMAIL_BACKGROUND_CACHE_BUSTER_PROPERTY));
        if (configuredVersion != null) {
            return configuredVersion;
        }

        try (InputStream stream = Email.class.getResourceAsStream(VERSION_PROPERTIES_RESOURCE)) {
            if (stream == null) {
                return null;
            }
            Properties versionProperties = new Properties();
            versionProperties.load(stream);

            String buildTimestamp = sanitizeCacheBuster(versionProperties.getProperty("build.timestamp"));
            if (buildTimestamp != null) {
                return buildTimestamp;
            }

            return sanitizeCacheBuster(versionProperties.getProperty("app.version"));
        } catch (IOException ex) {
            LOGGER.debug("[EMAIL] Não foi possível carregar version.properties para cache-buster do fundo: {}", ex.getMessage());
            return null;
        }
    }

    private static String sanitizeCacheBuster(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.replaceAll("[^A-Za-z0-9._-]", "_");
    }
	
	/**
	 * 
	 * @param nomeTemplate
	 * @param assunto
	 */
    public Email(String nomeTemplate, String assunto, String de) {
        this(nomeTemplate, null, assunto, de);
    }
    
    public Email(String nomeTemplate, String assunto) {
        this(nomeTemplate, null, assunto, CONFIG.getProperty("mail.from.address"));
    }
	
    /**
     * Retorna os endereços de e-mail dos administradores configurados via
     * variável de ambiente SMTP_ADMIN_EMAILS (separados por vírgula).
     */
    public static String[] getAdminEmails() {
        return CONFIG.getAdminEmails();
    }

	/**
	 * 
	 * @param chave
	 * @param valor
	 */
	public void setPropriedade(String chave, Object valor) {
		this.property.put(chave, valor);
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	public void enviar() throws Exception {
		generateData();
		populateData();

		String fromAddress = getDe();
		String fromName = CONFIG.getProperty("mail.from.name");

		if (fromAddress == null || fromAddress.trim().isEmpty()) {
			fromAddress = CONFIG.getProperty("mail.from.address");
		}

		EmailMessage message = new EmailMessage(
			fromAddress,
			fromName,
			getAssunto(),
			getConteudo(),
			getEnderecosDestino(),
			getEnderecosCopia(),
			getEnderecosCopiaOculta()
		);

		try {
			EmailSender sender = EmailSenderFactory.getSender();
			sender.enviar(message);
		} catch (Exception e) {
			LOGGER.error("[EMAIL] Erro ao enviar e-mail para {}: {}", getEnderecosDestino(), e.getMessage());
			throw new EmailException("Erro ao enviar e-mail! (" + getNomeTemplate() + ")", e);
		}
	}
	
	/**
	 * 
	 * @throws Exception
	 */
	protected void generateData() throws Exception {
		StringBuffer conteudoGerado = new StringBuffer();
		conteudoGerado.append(lerTemplate(TEMPLATE_CABECALHO));
		conteudoGerado.append(lerTemplate(getNomeTemplate()));
		conteudoGerado.append(lerTemplate(TEMPLATE_RODAPE));
		this.conteudo = conteudoGerado.toString();
	}
	
	/**
	 * 
	 * @param template
	 * @return
	 * @throws EmailException
	 */
	protected String lerTemplate(String template) throws EmailException {
		InputStream stream = getClass().getResourceAsStream(TEMPLATE_DIR + template);
        if (stream == null) {
            throw new EmailException("O template com o nome '" + template + "' não foi encontrado!");
        }
		StringBuffer conteudo = new StringBuffer();
		try {
			byte[] buffer = new byte[stream.available()];
			stream.read(buffer);
			conteudo.append(new String(buffer, StandardCharsets.UTF_8).trim());
		} catch (IOException e) {
			throw new EmailException("Erro ao ler template de email. Nome do template: " + getNomeTemplate(), e);
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					throw new EmailException("Erro ao fechar 'stream' com arquivo. Nome do template: " + getNomeTemplate(), e);
				}
			}
		}
		return conteudo.toString();
	}

	/**
	 * Substitui os placeholders ${chave} no conteúdo do template pelos valores
	 * definidos via setPropriedade(). Usa replace() literal em vez de replaceAll()
	 * para evitar IllegalArgumentException quando o valor contém '$' ou '\',
	 * caracteres com significado especial no segundo argumento de replaceAll/Matcher.
	 */
	protected void populateData() {
        Iterator iter = this.property.keySet().iterator();
        String chave = null;
        String valor = null;
        while (iter.hasNext()) {
			chave = (String) iter.next();
			valor = this.property.getProperty(chave);
			this.conteudo = this.conteudo.replace("${" + chave + "}", valor);
		}
	}
	
	public String getConteudo() {
		return this.conteudo;
	}
	
	public void setConteudo(String conteudo) {
		this.conteudo = conteudo;
	}
	
	public String getNomeTemplate() {
		return this.nomeTemplate;
	}
	
	public void setNomeTemplate(String nomeTemplate) {
		this.nomeTemplate = nomeTemplate;
	}
	
	public Properties getProperty() {
		return this.property;
	}
	
	public void setProperty(Properties propriedades) {
		this.property = propriedades;
	}
	
	public String getAssunto() {
		return this.assunto;
	}
	
	public void setAssunto(String assunto) {
		this.assunto = assunto;
	}
	
	public List getEnderecosCopia() {
		return this.enderecosCopia;
	}
	
	public void setEnderecosCopia(List copia) {
		this.enderecosCopia = copia;
	}
	
	public List getEnderecosCopiaOculta() {
		return this.enderecosCopiaOculta;
	}
	
	public void setEnderecosCopiaOculta(List copiaOculta) {
		this.enderecosCopiaOculta = copiaOculta;
	}
	
	public List getEnderecosDestino() {
		return this.enderecosDestino;
	}
	
	public void setEnderecosDestino(List para) {
		this.enderecosDestino = para;
	}
	
	public String getDe() {
		return this.de;
	}
	
	public void setDe(String de) {
		this.de = de;
	}

	public void adicionarEnderecoDestino(String endereco) {
		if (this.enderecosDestino == null) {
			this.enderecosDestino = new ArrayList();
		}
		this.enderecosDestino.add(endereco);
	}
	
	public void adicionarEnderecoCopia(String endereco) {
		if (this.enderecosCopia == null) {
			this.enderecosCopia = new ArrayList();
		}
		this.enderecosCopia.add(endereco);
	}
	
	public void adicionarEnderecoCopiaOculta(String endereco) {
		if (this.enderecosCopiaOculta == null) {
			this.enderecosCopiaOculta = new ArrayList();
		}
		this.enderecosCopiaOculta.add(endereco);
	}
	
	/**
	 * Transforma uma lista 
	 * 
	 * @param lista
	 * @return
	 */
	private String converterParaValoresSeparadosPorVirgula(List lista) {
		if (lista == null || lista.isEmpty()) {
			return null;
		}
		StringBuffer buffer = new StringBuffer();
		Iterator i = lista.iterator();
		while (i.hasNext()) {
			Object element = i.next();
			if (element != null) {
				buffer.append(element.toString());
				if (i.hasNext()) {
					buffer.append(",");
				}
			}
		}
		return buffer.toString();
	}

}
