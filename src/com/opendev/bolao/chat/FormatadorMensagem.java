package com.opendev.bolao.chat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;

public class FormatadorMensagem {
	
	private static final String CAMINHO_ICONES = "/bolao/img/emotionicons/";
	private Map icones;
	
	public void formatar(Mensagem msg) {
        String formatada = StringEscapeUtils.escapeHtml4(msg.getTexto());
        formatada = formatada.replaceAll("\\[b\\]", "<span style=\"font-weight: bold;\">");
        formatada = formatada.replaceAll("\\[/b\\]", "</span>");
        formatada = formatada.replaceAll("\\[i\\]", "<span style=\"font-style: italic;\">");
        formatada = formatada.replaceAll("\\[/i\\]", "</span>");
        formatada = formatada.replaceAll("\\[u\\]", "<span style=\"text-decoration: underline;\">");
        formatada = formatada.replaceAll("\\[/u\\]", "</span>");
        formatada = formatarIcones(formatada);
        msg.setTexto(formatada);
	}

	private String formatarIcones(String msg) {
        Map emotions = getIcones();
        String emotionCode = null;
        String imageName = null;
        String imgTag = null;
        String formattedMsg = new String(msg);
        for (Iterator i = emotions.keySet().iterator(); i.hasNext();) {
            emotionCode = (String) i.next();
            imageName = (String) emotions.get(emotionCode);
            imgTag = "<span><img src=\"" + CAMINHO_ICONES +
                imageName + "\" alt=\"" + emotionCode +
                "\" title=\"" + emotionCode + "\" style=\"border:none; " +
                "vertical-align: middle;\" /></span>";
            formattedMsg = formattedMsg.replaceAll(emotionCode, imgTag);
        }
        return formattedMsg;
	}

	public Map getIcones() {
        if (icones == null) {
            icones = new HashMap(28);
            icones.put("\\:\\)", "emoticon_smile.gif");
            icones.put("\\=\\)", "emoticon_smile.gif");
            icones.put("\\:\\(", "emoticon_sad.gif");
            icones.put("\\=\\(", "emoticon_sad.gif");
            icones.put("\\:P", "emoticon_tongue.gif");
            icones.put("\\:O", "emoticon_surprised.gif");
            icones.put("\\|\\)", "emoticon_sleep.gif");
            icones.put("\\:D", "emoticon_bigsmile.gif");
            icones.put("\\=D", "emoticon_bigsmile.gif");
            icones.put("\\:d", "emoticon_happy.gif");
            icones.put("\\:\\$", "emoticon_shy.gif");
            icones.put("\\;\\)", "emoticon_eyeblink.gif");
            icones.put("\\;P", "emoticon_eyeblinktongue.gif");
            icones.put("8\\)", "emoticon_cool.gif");
            icones.put("\\:´\\(", "emoticon_cry.gif");
            icones.put("\\:\\?", "emoticon_doubt.gif");
            icones.put("\\:\\/", "emoticon_indiferent.gif");
            icones.put("\\(666\\)", "emoticon_devil.gif");
            icones.put("\\(H\\)", "emoticon_heart.gif");
            icones.put("\\(BH\\)", "emoticon_heartbroken.gif");
            icones.put("\\(F\\)", "emoticon_flower.gif");
        }
        return icones;
	}

}
