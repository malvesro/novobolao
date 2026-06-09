package com.opendev.bolao.service.impl;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import com.opendev.bolao.email.Email;
import com.opendev.bolao.model.Participante;
import com.opendev.bolao.repository.ParticipanteRepository;
import com.opendev.bolao.service.RecuperacaoSenhaService;
import com.opendev.bolao.service.dto.ResultadoTrocaSenha;
import com.opendev.bolao.service.dto.SolicitacaoOtp;
import com.opendev.bolao.service.dto.ValidacaoOtp;
import com.opendev.bolao.util.SanitizationUtils;
import com.opendev.bolao.util.ValidacaoUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;

public class RecuperacaoSenhaServiceImpl implements RecuperacaoSenhaService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecuperacaoSenhaServiceImpl.class);
    private static final Duration EXPIRACAO_OTP = Duration.ofMinutes(30);
    private static final int MAX_TENTATIVAS = 5;
    private static final String DEFAULT_NEUTRAL_MESSAGE = "Se o e-mail estiver cadastrado, enviaremos as instruções.";

    private ParticipanteRepository participanteRepository;
    private PasswordEncoder passwordEncoder;
    private OtpStore otpStore = new OtpStore();

    public void setParticipanteRepository(ParticipanteRepository participanteRepository) {
        this.participanteRepository = participanteRepository;
    }

    public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public void setOtpStore(OtpStore otpStore) {
        this.otpStore = otpStore == null ? new OtpStore() : otpStore;
    }

    @Override
    public SolicitacaoOtp solicitarOtp(String email, String ip, String userAgent) {
        String emailNormalizado = normalizarEmail(email);
        if (!SanitizationUtils.isValidEmail(emailNormalizado) || !ValidacaoUtils.isEmailValido(emailNormalizado)) {
            LOGGER.warn("[RECUPERACAO][OTP] Email invalido informado ip={}", ip);
            return new SolicitacaoOtp(false, DEFAULT_NEUTRAL_MESSAGE);
        }

        Optional<Participante> participanteOpt = participanteRepository.findByEmail(emailNormalizado);
        if (participanteOpt.isEmpty()) {
            LOGGER.info("[RECUPERACAO][OTP] Email nao encontrado (mensagem neutra) ip={}", ip);
            return new SolicitacaoOtp(true, DEFAULT_NEUTRAL_MESSAGE);
        }

        String otp = gerarOtpNumerico();
        otpStore.store(emailNormalizado, otp, ip);

        Participante participante = participanteOpt.get();
        try {
            Email emailSender = new Email("recuperacao-senha-otp.html", "🜲 O Oráculo da Memória: Redefinição de Vossa Chave");
            emailSender.setPropriedade("nome", participante.getNome());
            emailSender.setPropriedade("otp", otp);
            emailSender.setPropriedade("expiracaoMinutos", Long.toString(EXPIRACAO_OTP.toMinutes()));
            emailSender.adicionarEnderecoDestino(participante.getEmail());
            emailSender.enviar();
        } catch (Exception ex) {
            LOGGER.error("[RECUPERACAO][OTP] Falha ao enviar email ip={} email={}", ip, emailNormalizado, ex);
            return new SolicitacaoOtp(false, "Sistema temporariamente indisponível para envio de e-mails.");
        }

        LOGGER.info("[RECUPERACAO][OTP] OTP enviado ip={} email={}", ip, emailNormalizado);
        return new SolicitacaoOtp(true, DEFAULT_NEUTRAL_MESSAGE);
    }

    @Override
    public ValidacaoOtp validarOtp(String email, String otp, String ip) {
        String emailNormalizado = normalizarEmail(email);
        if (SanitizationUtils.isValidEmail(emailNormalizado) && otpStore.validate(emailNormalizado, otp)) {
            return new ValidacaoOtp(true, DEFAULT_NEUTRAL_MESSAGE);
        }
        LOGGER.info("[RECUPERACAO][OTP] OTP invalido ip={} email={}", ip, emailNormalizado);
        return new ValidacaoOtp(false, DEFAULT_NEUTRAL_MESSAGE);
    }

    @Override
    public ResultadoTrocaSenha redefinirSenha(String email, String otp, String novaSenha, String ip) {
        String emailNormalizado = normalizarEmail(email);
        if (!SanitizationUtils.isValidEmail(emailNormalizado) || !ValidacaoUtils.isEmailValido(emailNormalizado)) {
            LOGGER.warn("[RECUPERACAO][OTP] Email invalido na redefinicao ip={}", ip);
            return new ResultadoTrocaSenha(false, "Dados inválidos.");
        }

        if (!ValidacaoUtils.isSenhaValida(novaSenha)) {
            LOGGER.warn("[RECUPERACAO][OTP] Senha invalida ip={} email={}", ip, emailNormalizado);
            return new ResultadoTrocaSenha(false, "Senha inválida.");
        }

        if (!otpStore.consume(emailNormalizado, otp)) {
            LOGGER.warn("[RECUPERACAO][OTP] OTP invalido na redefinicao ip={} email={}", ip, emailNormalizado);
            return new ResultadoTrocaSenha(false, "Código inválido ou expirado.");
        }

        Optional<Participante> participanteOpt = participanteRepository.findByEmail(emailNormalizado);
        if (participanteOpt.isEmpty()) {
            LOGGER.warn("[RECUPERACAO][OTP] Email nao encontrado na redefinicao ip={} email={}", ip, emailNormalizado);
            return new ResultadoTrocaSenha(false, "Dados inválidos.");
        }

        Participante participante = participanteOpt.get();
        participante.setSenha(passwordEncoder.encode(novaSenha));
        participante.setDataHoraUltimaTrocaSenha(new Timestamp(System.currentTimeMillis()));
        participanteRepository.save(participante);

        LOGGER.info("[RECUPERACAO][OTP] Senha atualizada ip={} email={}", ip, emailNormalizado);
        return new ResultadoTrocaSenha(true, "Senha atualizada com sucesso.");
    }

    private String normalizarEmail(String email) {
        String sanitized = SanitizationUtils.cleanText(email, 254);
        return sanitized == null ? null : sanitized.trim().toLowerCase();
    }

    private String gerarOtpNumerico() {
        SecureRandom random = new SecureRandom();
        int otp = 100000 + random.nextInt(900000);
        return Integer.toString(otp);
    }

    static class OtpStore {
        private final SecureRandom random = new SecureRandom();
        private final java.util.Map<String, OtpEntry> entries = new java.util.concurrent.ConcurrentHashMap<>();

        void store(String emailNormalizado, String otp, String ipSolicitacao) {
            String salt = gerarSalt();
            String hash = hashOtp(otp, salt);
            Instant agora = Instant.now();
            entries.put(emailNormalizado, new OtpEntry(hash, salt, agora.plus(EXPIRACAO_OTP), 0, MAX_TENTATIVAS, agora, ipSolicitacao));
        }

        boolean validate(String emailNormalizado, String otp) {
            OtpEntry entry = entries.get(emailNormalizado);
            if (entry == null || entry.expirado()) {
                entries.remove(emailNormalizado);
                return false;
            }
            if (entry.tentativas >= entry.maxTentativas) {
                entries.remove(emailNormalizado);
                return false;
            }
            if (entry.matches(otp)) {
                return true;
            }
            entry.tentativas++;
            if (entry.tentativas >= entry.maxTentativas) {
                entries.remove(emailNormalizado);
            }
            return false;
        }

        boolean consume(String emailNormalizado, String otp) {
            if (!validate(emailNormalizado, otp)) {
                return false;
            }
            entries.remove(emailNormalizado);
            return true;
        }

        private String gerarSalt() {
            byte[] bytes = new byte[16];
            random.nextBytes(bytes);
            return Base64.getEncoder().encodeToString(bytes);
        }

        private String hashOtp(String otp, String salt) {
            try {
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                digest.update(salt.getBytes(StandardCharsets.UTF_8));
                byte[] hashed = digest.digest(otp.getBytes(StandardCharsets.UTF_8));
                return Base64.getEncoder().encodeToString(hashed);
            } catch (NoSuchAlgorithmException ex) {
                throw new IllegalStateException("Algoritmo SHA-256 não disponível", ex);
            }
        }

        static class OtpEntry {
            private final String otpHash;
            private final String salt;
            private final Instant expiracao;
            private final int maxTentativas;
            private final Instant criadoEm;
            private final String ipSolicitacao;
            private int tentativas;

            OtpEntry(String otpHash, String salt, Instant expiracao, int tentativas, int maxTentativas, Instant criadoEm, String ipSolicitacao) {
                this.otpHash = otpHash;
                this.salt = salt;
                this.expiracao = expiracao;
                this.tentativas = tentativas;
                this.maxTentativas = maxTentativas;
                this.criadoEm = criadoEm;
                this.ipSolicitacao = ipSolicitacao;
            }

            boolean expirado() {
                return Instant.now().isAfter(expiracao);
            }

            boolean matches(String otp) {
                try {
                    MessageDigest digest = MessageDigest.getInstance("SHA-256");
                    digest.update(salt.getBytes(StandardCharsets.UTF_8));
                    byte[] hashed = digest.digest(otp.getBytes(StandardCharsets.UTF_8));
                    String computed = Base64.getEncoder().encodeToString(hashed);
                    return otpHash.equals(computed);
                } catch (NoSuchAlgorithmException ex) {
                    throw new IllegalStateException("Algoritmo SHA-256 não disponível", ex);
                }
            }
        }
    }
}
