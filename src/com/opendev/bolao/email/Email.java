package com.opendev.bolao.email;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Email {

	private static final Logger LOGGER = LoggerFactory.getLogger(Email.class);
	
    private static final String TEMPLATE_DIR = "/com/opendev/bolao/email/templates/";
	private static final String TEMPLATE_CABECALHO = "cabecalho.html";
	private static final String TEMPLATE_RODAPE = "rodape.html";
    private static final String TITULO_PADRAO = "Bolão de Placa - TV Cipó na Copa 2006";
    private static volatile EmailConfiguration CONFIG = EmailConfiguration.load();
	
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
        setPropriedade("sistema", CONFIG.getProperty("mail.property.systemurl"));
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

		try {
            EmailConfiguration configuration = CONFIG;
            Properties settings = configuration.asProperties();
            String smtpHost = settings.getProperty("mail.smtp.host");
            if (smtpHost == null || smtpHost.trim().isEmpty()) {
                throw new EmailException("Servidor SMTP não configurado (mail.smtp.host).");
            }

            MailContext mailContext = createMailContext(settings);
			Session session = Session.getInstance(mailContext.properties(), mailContext.authenticator());
			Message msg = new MimeMessage(session);
			
			// Definindo os destinatários
			String destino = converterParaValoresSeparadosPorVirgula(getEnderecosDestino());
			if (destino != null) {
				msg.addRecipients(Message.RecipientType.TO, InternetAddress.parse(destino, false));
			} else {
				throw new EmailException("Pelo menos um destinatário deve ser informado!");
			}
			String copias = converterParaValoresSeparadosPorVirgula(getEnderecosCopia());
			if (copias != null) {
				msg.addRecipients(Message.RecipientType.CC, InternetAddress.parse(copias, false));
			}			
			String copiasOcultas = converterParaValoresSeparadosPorVirgula(getEnderecosCopiaOculta());
			if (copiasOcultas != null) {
				msg.addRecipients(Message.RecipientType.BCC, InternetAddress.parse(copiasOcultas, false));
			}
			
			// Propriedades da mensagem
			if (this.de != null && !this.de.trim().isEmpty()) {
				msg.setFrom(new InternetAddress(this.de));
			} else {
                String fromAddress = settings.getProperty("mail.from.address");
                if (fromAddress != null && !fromAddress.trim().isEmpty()) {
                    String fromName = settings.getProperty("mail.from.name");
                    if (fromName != null && !fromName.trim().isEmpty()) {
                        msg.setFrom(new InternetAddress(fromAddress, fromName, StandardCharsets.UTF_8.name()));
                    } else {
                        msg.setFrom(new InternetAddress(fromAddress));
                    }
                }
            }
			msg.setSubject(getAssunto());
			msg.setHeader("X-Mailer", "BOLAO DE PLACA");
			msg.setSentDate(new Date());
			msg.setContent(getConteudo(), "text/html; charset=UTF-8");

			LOGGER.info("[EMAIL] Tentando enviar email via host={}:{} (SSL={}, TLS={}) para={}", 
					smtpHost, 
					mailContext.properties().getProperty("mail.smtp.port"),
					mailContext.properties().getProperty("mail.smtp.ssl.enable", "false"),
					mailContext.properties().getProperty("mail.smtp.starttls.enable", "false"),
					destino);

			Transport.send(msg);
			LOGGER.info("[EMAIL] Email enviado com sucesso para={}", destino);
		} catch (MessagingException | UnsupportedEncodingException e) {
			LOGGER.error("[EMAIL] Falha crítica ao enviar email para={} via host={}. Erro: {}", 
					getEnderecosDestino(), 
					CONFIG.getProperty("mail.smtp.host"),
					e.getMessage());
			throw new EmailException("Erro ao enviar email! (" + getNomeTemplate() + ")", e);
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
			conteudo.append(new String(buffer).trim());
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
	 * 
	 *
	 */
	protected void populateData() {
        Iterator iter = this.property.keySet().iterator();
        String chave = null;
        String valor = null;
        while (iter.hasNext()) {
			chave = (String) iter.next();
			valor = this.property.getProperty(chave);
			this.conteudo = this.conteudo.replaceAll("\\$\\{" + chave + "\\}", valor);
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

    private static MailContext createMailContext(Properties settings) throws EmailException {
        Properties propriedadesDeEnvio = new Properties();
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.host");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.port");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.starttls.enable");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.starttls.required");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.ssl.enable");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.ssl.trust");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.connectiontimeout");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.timeout");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.writetimeout");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.auth.mechanisms");
        copyIfPresent(settings, propriedadesDeEnvio, "mail.smtp.sasl.enable");

        boolean usarAutenticacao = Boolean.parseBoolean(settings.getProperty("mail.smtp.auth", "false"));
        Authenticator auth = null;
        if (usarAutenticacao) {
            String usuario = settings.getProperty("mail.smtp.auth.user");
            String senha = settings.getProperty("mail.smtp.auth.password");
            if (usuario == null || usuario.trim().isEmpty()) {
                throw new EmailException("Usuário SMTP não informado (mail.smtp.auth.user).");
            }
            propriedadesDeEnvio.setProperty("mail.smtp.auth", "true");
            String senhaNormalizada = senha == null ? "" : senha;
            auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(usuario, senhaNormalizada);
                }
            };
        } else {
            propriedadesDeEnvio.setProperty("mail.smtp.auth", "false");
        }

        return new MailContext(propriedadesDeEnvio, auth);
    }

    private static void copyIfPresent(Properties source, Properties target, String key) {
        String value = source.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            target.setProperty(key, value.trim());
        }
    }

    static void reloadConfiguration() {
        CONFIG = EmailConfiguration.load();
    }

    static EmailConfiguration configuration() {
        return CONFIG;
    }

    static MailContext mailContextForTests(Properties settings) throws EmailException {
        return createMailContext(settings);
    }

    static final class MailContext {
        private final Properties properties;
        private final Authenticator authenticator;

        private MailContext(Properties properties, Authenticator authenticator) {
            this.properties = properties;
            this.authenticator = authenticator;
        }

        Properties properties() {
            return properties;
        }

        Authenticator authenticator() {
            return authenticator;
        }
    }
}
