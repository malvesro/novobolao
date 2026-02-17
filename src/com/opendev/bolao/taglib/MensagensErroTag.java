package com.opendev.bolao.taglib;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.JspException;
import jakarta.servlet.jsp.JspWriter;
import jakarta.servlet.jsp.tagext.TagSupport;

import com.opendev.bolao.util.MensagemErro;


public class MensagensErroTag extends TagSupport {
    
    private String nomeAtributo;
    
    public int doEndTag() throws JspException {
        List erros = (List) pageContext.findAttribute(getNomeAtributo());
        if (erros != null && !erros.isEmpty()) {
            StringBuffer buffer = new StringBuffer();
            buffer.append("<div style=\"height: 10px;\"></div>");
            buffer.append("<div class=\"legenda\">");
            String base = ((HttpServletRequest) pageContext.getRequest()).getContextPath();
            for (Iterator iter = erros.iterator(); iter.hasNext();) {
                MensagemErro msg = (MensagemErro) iter.next();
                buffer.append("<p>");
                if (msg.getSeveridade() == MensagemErro.SEVERIDADE_AVISO) {
                    buffer.append("<img alt=\"\" src=\"" + base + "/img/warning.gif\" style=\"vertical-align: top;\" />");
                } else if (msg.getSeveridade() == MensagemErro.SEVERIDADE_ERRO) {
                    buffer.append("<img alt=\"\" src=\"" + base + "/img/error.gif\" style=\"vertical-align: top;\" />");
                }
                buffer.append(msg.getNomeDoCampo());
                buffer.append(": ");
                buffer.append(msg.getMensagem());
                buffer.append("</p>");
            }
            buffer.append("</div>");
            buffer.append("<div style=\"height: 10px;\"></div>");
            try {
                JspWriter writer = pageContext.getOut();
                writer.write(buffer.toString());
            } catch (IOException e) {
                throw new JspException(e.getMessage(), e);
            }
        }
        return EVAL_PAGE;
    }

    public int doStartTag() throws JspException {
        return SKIP_BODY;
    }

    public String getNomeAtributo() {
        return this.nomeAtributo;
    }
    
    public void setNomeAtributo(String nomeAtributo) {
        this.nomeAtributo = nomeAtributo;
    }

}
