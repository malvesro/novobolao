<%@ page contentType="text/html;charset=UTF-8" language="java" isErrorPage="true" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Erro | Bolão 2026</title>
    <link rel="stylesheet" href="<s:url value='/css/estilo.css'/>">
    <style>
        .error-container {
            max-width: 600px;
            margin: 100px auto;
            text-align: center;
            padding: 40px;
            background: #fff;
            border-radius: 8px;
            box-shadow: 0 4px 6px rgba(0,0,0,0.1);
        }
        .error-icon {
            font-size: 48px;
            margin-bottom: 20px;
        }
        h1 { color: #d32f2f; }
        .btn-home {
            display: inline-block;
            margin-top: 20px;
            padding: 10px 20px;
            background-color: #00796b;
            color: white;
            text-decoration: none;
            border-radius: 4px;
        }
    </style>
</head>
<body>
    <div class="error-container">
        <div class="error-icon">⚠️</div>
        <h1>Ops! Algo deu errado.</h1>
        <p>Pedimos desculpas pelo transtorno. Nossa equipe foi notificada e estamos trabalhando para resolver.</p>
        <p>Por favor, tente novamente mais tarde ou volte para a página inicial.</p>
        <a href="<s:url value='/index.action'/>" class="btn-home">Voltar ao Início</a>
    </div>
</body>
</html>
