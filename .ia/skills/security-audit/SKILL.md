---
version: 1.0.0
mode: audit
description: "Auditoria OWASP e e-MAG"

---

# Security Audit Skill

## Objetivo

Auditar código por vulnerabilidades antes do commit.

## Checklist

1. **Zero Trust:** Valide todos os inputs.
2. **SQL Injection:** Verifique concatenação em JPQL.
3. Verifique possibiliades de vulnerabilidades como XSS, bibliotecas com CVE conhecidas e sugira atualizações.
4. Sugira o uso do  OWASP Dependency-Check Maven plugin no pom.xml (se houver) 