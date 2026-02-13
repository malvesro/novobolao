package com.opendev.bolao.email;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;


public class Email {
	
    private static final String TEMPLATE_DIR = "/com/opendev/bolao/email/templates/";
	private static final String TEMPLATE_CABECALHO = "cabecalho.html";
	private static final String TEMPLATE_RODAPE = "rodape.html";
	private static final ResourceBundle CONFIG = ResourceBundle.getBundle("com.opendev.bolao.email.email");
	private static final String TITULO_PADRAO = "Bolão de Placa - TV Cipó na Copa 2006";
	
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
        setPropriedade("sistema", CONFIG.getString("mail.property.systemurl"));
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
        this(nomeTemplate, null, assunto, CONFIG.getString("mail.from.address"));
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
            boolean usarAutenticação = new Boolean(CONFIG.getString("mail.smtp.auth")).booleanValue();
            Authenticator auth = null;
            if (usarAutenticação) {
                auth = new Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        PasswordAuthentication pwdAuth = new PasswordAuthentication(
                                CONFIG.getString("mail.smtp.auth.user"), CONFIG.getString("mail.smtp.auth.password"));
                        return pwdAuth;
                    }
                };
            }
            
			Properties propriedadesDeEnvio = new Properties(System.getProperties());
			propriedadesDeEnvio.put("mail.smtp.host", CONFIG.getString("mail.smtp.host"));

			Session session = Session.getDefaultInstance(propriedadesDeEnvio, auth);
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
//			msg.setReplyTo(InternetAddress.parse("dine5.deinf@bcb.gov.br", false));
			if (this.de != null) {
				msg.setFrom(new InternetAddress(this.de));
			}
			msg.setSubject(getAssunto());
			msg.setHeader("X-Mailer", "BANCO CENTRAL DO BRASIL");
			msg.setSentDate(new Date());
			msg.setContent(getConteudo(), "text/html");

			Transport.send(msg);
		} catch (MessagingException e) {
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
}	
