package com.opendev.bolao.email;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.Test;

/**
 * Testes para Email.populateData() cobrindo casos com caracteres especiais
 * nos valores dos placeholders que causavam IllegalArgumentException com replaceAll().
 *
 * Caso real: valor contendo '$' (ex: OTP token, URL com query string) lançava
 * "Illegal group reference" porque replaceAll interpreta '$n' como grupo de captura.
 */
class EmailPopulateDataTest {

    /**
     * Subclasse de teste que permite injetar conteúdo diretamente,
     * sem depender de arquivos de template em disco.
     */
    static class TestEmail extends Email {
        TestEmail() {
            super("cabecalho.html", "assunto-teste");
        }

        void setConteudoDirectly(String conteudo) {
            setConteudo(conteudo);
        }

        void invocarPopulateData() {
            populateData();
        }
    }

    @Test
    void deveSubstituirPlaceholderSemCaracteresEspeciais() {
        TestEmail email = new TestEmail();
        email.setConteudoDirectly("Olá ${nome}, bem-vindo!");
        email.setPropriedade("nome", "João");

        assertThatNoException().isThrownBy(email::invocarPopulateData);
        assertThat(email.getConteudo()).isEqualTo("Olá João, bem-vindo!");
    }

    @Test
    void deveSubstituirPlaceholderComCifraoDolar() {
        // Caso que causava IllegalArgumentException: Illegal group reference
        TestEmail email = new TestEmail();
        email.setConteudoDirectly("Seu código é: ${otp}");
        email.setPropriedade("otp", "ABC$123");

        assertThatNoException().isThrownBy(email::invocarPopulateData);
        assertThat(email.getConteudo()).isEqualTo("Seu código é: ABC$123");
    }

    @Test
    void deveSubstituirPlaceholderComBarra() {
        TestEmail email = new TestEmail();
        email.setConteudoDirectly("Caminho: ${path}");
        email.setPropriedade("path", "C:\\Users\\test");

        assertThatNoException().isThrownBy(email::invocarPopulateData);
        assertThat(email.getConteudo()).isEqualTo("Caminho: C:\\Users\\test");
    }

    @Test
    void deveSubstituirPlaceholderComUrlComParametros() {
        TestEmail email = new TestEmail();
        email.setConteudoDirectly("Acesse: ${link}");
        email.setPropriedade("link", "https://app.com/verificar?token=$2a$12$abc&email=user@test.com");

        assertThatNoException().isThrownBy(email::invocarPopulateData);
        assertThat(email.getConteudo())
            .isEqualTo("Acesse: https://app.com/verificar?token=$2a$12$abc&email=user@test.com");
    }

    @Test
    void deveSubstituirMultiplosPlaceholders() {
        TestEmail email = new TestEmail();
        email.setConteudoDirectly("Olá ${nome}! Código: ${otp}. Acesse: ${link}");
        email.setPropriedade("nome", "Maria");
        email.setPropriedade("otp", "X$Y$Z");
        email.setPropriedade("link", "https://site.com?ref=$special");

        assertThatNoException().isThrownBy(email::invocarPopulateData);
        assertThat(email.getConteudo())
            .isEqualTo("Olá Maria! Código: X$Y$Z. Acesse: https://site.com?ref=$special");
    }
}
