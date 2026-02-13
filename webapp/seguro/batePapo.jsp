<%@include file="/template/menu.jspf" %>

<script type="text/javascript" src="${base}/dwr/interface/BatePapo.js"></script>
<script type="text/javascript">
  var idUltimaMsg = -1;
  var recMsgsTimeout = 0;
  var recParticipantesTimeout = 0;
  var depoisEnviarTimeout = 0;
  var indexMsgAnterior = 0;
  
  var iniciar = function() {
	var handleKeyPress = function(ev) {
		var code = -1;
		var enterCode = 13;
		var browserDetector = new BrowserDetector();
		if (browserDetector.isIE) {
			code = ev.keyCode;
		} else {
			code = ev.which;
		}
		if (code == enterCode) {
			if (browserDetector.isIE) {
				ev.returnValue = false;
			}			
			enviarMensagem();
			return false;
		}
		return true;
	};
	$("chat_msg_text").onkeypress = handleKeyPress;
    BatePapo.entrar();
    atualizarListaParticipantes();
    buscarMensagens();
  }
  
  var sair = function() {
  	window.clearTimeout(recMsgsTimeout);
  	window.clearTimeout(depoisEnviarTimeout);
    window.clearTimeout(recParticipantesTimeout);
  	BatePapo.sair();
  }
  
  function enviarMensagem() {
    var textoMsg = $("chat_msg_text").value;
    if (textoMsg != "") {
      BatePapo.enviarMensagem(textoMsg);
      DWRUtil.setValue("chat_msg_text", "");
      if (recMsgsTimeout != 0) {
        window.clearTimeout(recMsgsTimeout);
      }
      var buscarMsgsFunc = function() {
        buscarMensagens();
        window.clearTimeout(depoisEnviarTimeout);
      };
      depoisEnviarTimeout = window.setTimeout(buscarMsgsFunc, 500);
    }
  }
  
  function buscarMensagens() {
    var callBackFunc = function(msgs) {
      var msg = null;
      var msgsList = $("batepapo_mensagens");
      var msgLi = null;
      var msgCabSpan = null;
      var msgCabSpanTxt = null;
      var msgCabSpanImg = null;
      var msgContentDiv = null;
      for (var i = 0; i < msgs.length; i++) {
        msg = msgs[i];
        if (msg == null || msg == "undefined") {
        	break;
        }
        if (msg.id == indexMsgAnterior) {
            continue;
        }
        msgLi = document.createElement("li");
        if (msg.apelidoParticipante == "auto") {
            msgLi.className = "auto";
        } else {
            if (indexMsgAnterior % 2 == 0) {
                msgLi.className = "par";
            } else {
                msgLi.className = "impar";
            }
        }
        msgCabSpan = document.createElement("span");
       	msgCabSpanImg = "<img alt=\"\" src=\"${base}/img/message.png\" />";
        if (msg.apelidoParticipante == "auto") {
            msgCabSpanTxt = msgCabSpanImg + " (" + msg.dataHoraEnvio + ") Mensagem automática:";
        } else {
            msgCabSpanTxt = msgCabSpanImg + " (" + msg.dataHoraEnvio + ") <span style=\"font-style: italic;\">" + msg.apelidoParticipante + "</span> fala:";
        }
        msgCabSpan.innerHTML = msgCabSpanTxt;
        msgLi.appendChild(msgCabSpan);
        
        msgContentDiv = document.createElement("div");
        msgContentDiv.innerHTML = msg.texto;
		msgLi.appendChild(msgContentDiv);
					
		msgsList.insertBefore(msgLi, msgsList.firstChild);
			
		idUltimaMsg = msg.id;
        indexMsgAnterior++;
      }
      recMsgsTimeout = window.setTimeout("buscarMensagens();", 5000);
    }
    if (idUltimaMsg == -1) {
    	var receiveIdCallback = function(id) {
	    	idUltimaMsg = id;
	    	recMsgsTimeout = window.setTimeout("buscarMensagens();", 5000);
    	}
    	BatePapo.buscarIdUltimaMensagem({callback:receiveIdCallback});
    } else {
    	BatePapo.buscarMensagens(idUltimaMsg, {callback:callBackFunc});
    }
  }
  
  function atualizarListaParticipantes() {
  	var callBackFunc = function(participantes) {
  		DWRUtil.setValue("totalParticipantesSpan", participantes.length);
        var divPessoas = $("batepapo_lista_pessoas");
        var divPai = $("batepapo_lista_pessoas_conteudo");
        var novaDivPai = document.createElement("div");
        var divPar = null;
        var divParText = null;
        for (var i = 0; i < participantes.length; i++) {
            divPar = document.createElement("div");
            divPar.className = "participanteBatePapo";
            divParText = document.createTextNode(participantes[i]);
            divPar.appendChild(divParText);
            novaDivPai.appendChild(divPar);
        }
        novaDivPai.id = "batepapo_lista_pessoas_conteudo";
        divPessoas.replaceChild(novaDivPai, divPai);
        recParticipantesTimeout = window.setTimeout("atualizarListaParticipantes();", 10000);
  	}
    BatePapo.buscarTodosParticipantes({callback: callBackFunc});
  }
  
  function alterarApelido() {
    BatePapo.alterarApelido(DWRUtil.getValue("apelido_tf"));
    window.clearTimeout(recParticipantesTimeout);
    atualizarListaParticipantes();
  }
  
  window.onload = iniciar;
  window.onunload = sair;
</script>

<div id="sala_batepapo" style="float: right; width: 605px;">
<div id="esquerda" style="position: relative; float: left;">
<div id="batepapo">
<ul id="batepapo_mensagens">
</ul>
</div>
<div style="vertical-align: middle;">
	<div style="height: 20px;"></div>
	<div><label for="apelido_tf"><fmt:message key="chat.apelido" /></label>  <input id="apelido_tf" type="text" class="text" onblur="alterarApelido();" /></div>
	<div style="height: 6px;"></div>
	<div>
	<textarea cols="30" rows="2" id="chat_msg_text"></textarea>
	</div>
	<div style="height: 6px;"></div>
	<div style="padding-left: 80px;">
	<fmt:message var="sendMsgLabel" key="chat.send" />
	<input type="button" name="msgSubmit" onclick="enviarMensagem();" class="button" value="${sendMsgLabel}" />
	</div>
	<div style="height: 20px;"></div>
</div>
</div>
<div id="direita" style="float: right;">
	<div id="batepapo_lista_pessoas">
		<div style="background-image: url('${base}/img/fundo_cinza.png'); height: 22px; border-bottom: 1px solid #4E4E4E; text-align: center;">
			<div style="height: 3px;"></div>
			<fmt:message key="chat.total" />
			(<span id="totalParticipantesSpan">0</span>)
		</div>
        <div id="batepapo_lista_pessoas_conteudo">
        </div>
	</div>
</div>
</div>
