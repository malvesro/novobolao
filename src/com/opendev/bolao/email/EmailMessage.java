package com.opendev.bolao.email;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa os dados básicos de um e-mail a ser enviado.
 */
public class EmailMessage {
    private final String de;
    private final String deNome;
    private final String assunto;
    private final String conteudo;
    private final List<String> para;
    private final List<String> cc;
    private final List<String> bcc;

    public EmailMessage(String de, String deNome, String assunto, String conteudo, List<String> para) {
        this(de, deNome, assunto, conteudo, para, Collections.emptyList(), Collections.emptyList());
    }

    public EmailMessage(String de, String deNome, String assunto, String conteudo, 
                        List<String> para, List<String> cc, List<String> bcc) {
        this.de = de;
        this.deNome = deNome;
        this.assunto = assunto;
        this.conteudo = conteudo;
        this.para = para != null ? new ArrayList<>(para) : Collections.emptyList();
        this.cc = cc != null ? new ArrayList<>(cc) : Collections.emptyList();
        this.bcc = bcc != null ? new ArrayList<>(bcc) : Collections.emptyList();
    }

    public String getDe() { return de; }
    public String getDeNome() { return deNome; }
    public String getAssunto() { return assunto; }
    public String getConteudo() { return conteudo; }
    public List<String> getPara() { return Collections.unmodifiableList(para); }
    public List<String> getCc() { return Collections.unmodifiableList(cc); }
    public List<String> getBcc() { return Collections.unmodifiableList(bcc); }
}
